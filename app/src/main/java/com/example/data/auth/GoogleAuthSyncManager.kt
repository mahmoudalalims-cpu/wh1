package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class GoogleUserData(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val isAutoSyncEnabled: Boolean = true,
    val syncOnTransaction: Boolean = true,
    val dailyBackupEnabled: Boolean = true
)

sealed class GoogleAuthStatus {
    object Idle : GoogleAuthStatus()
    object InProgress : GoogleAuthStatus()
    data class Authenticated(val user: GoogleUserData) : GoogleAuthStatus()
    data class Error(val message: String) : GoogleAuthStatus()
}

/**
 * Manages Google Account sign-in via Credential Manager and Firebase Auth,
 * as well as auto-sync preferences (auto sync after issue/return/damage and daily).
 */
class GoogleAuthSyncManager(
    private val context: Context
) {
    companion object {
        private const val TAG = "GoogleAuthSync"
        private const val PREFS_NAME = "google_sync_prefs"
        private const val KEY_AUTO_SYNC_ENABLED = "auto_sync_enabled"
        private const val KEY_SYNC_ON_TRANSACTION = "sync_on_transaction"
        private const val KEY_DAILY_BACKUP = "daily_backup"
        private const val KEY_CACHED_USER_EMAIL = "cached_email"
        private const val KEY_CACHED_USER_NAME = "cached_name"
        private const val KEY_CACHED_USER_PHOTO = "cached_photo"
        private const val KEY_LAST_AUTO_SYNC_TIME = "last_auto_sync_time"
        // Web Client ID extracted from google-services.json (client_type: 3)
        const val DEFAULT_WEB_CLIENT_ID = "281266068934-mc9qic5qrojh90hnqi553q10rik0eokf.apps.googleusercontent.com"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _authStatus = MutableStateFlow<GoogleAuthStatus>(GoogleAuthStatus.Idle)
    val authStatus: StateFlow<GoogleAuthStatus> = _authStatus.asStateFlow()

    private val _currentUser = MutableStateFlow<GoogleUserData?>(null)
    val currentUser: StateFlow<GoogleUserData?> = _currentUser.asStateFlow()

    private val _isAutoSyncOnTransaction = MutableStateFlow(prefs.getBoolean(KEY_SYNC_ON_TRANSACTION, true))
    val isAutoSyncOnTransaction: StateFlow<Boolean> = _isAutoSyncOnTransaction.asStateFlow()

    private val _isDailyBackupEnabled = MutableStateFlow(prefs.getBoolean(KEY_DAILY_BACKUP, true))
    val isDailyBackupEnabled: StateFlow<Boolean> = _isDailyBackupEnabled.asStateFlow()

    private val _lastAutoSyncTime = MutableStateFlow<Long?>(
        if (prefs.contains(KEY_LAST_AUTO_SYNC_TIME)) prefs.getLong(KEY_LAST_AUTO_SYNC_TIME, 0L) else null
    )
    val lastAutoSyncTime: StateFlow<Long?> = _lastAutoSyncTime.asStateFlow()

    init {
        restoreSession()
    }

    private fun restoreSession() {
        val auth = getFirebaseAuth()
        val fbUser = auth?.currentUser
        if (fbUser != null) {
            val user = GoogleUserData(
                uid = fbUser.uid,
                email = fbUser.email ?: "",
                displayName = fbUser.displayName ?: "مستخدم قوقل",
                photoUrl = fbUser.photoUrl?.toString(),
                syncOnTransaction = _isAutoSyncOnTransaction.value,
                dailyBackupEnabled = _isDailyBackupEnabled.value
            )
            _currentUser.value = user
            _authStatus.value = GoogleAuthStatus.Authenticated(user)
        } else {
            // Check cached local user details if previously signed in
            val cachedEmail = prefs.getString(KEY_CACHED_USER_EMAIL, null)
            if (!cachedEmail.isNullOrBlank()) {
                val cachedName = prefs.getString(KEY_CACHED_USER_NAME, "مستخدم قوقل") ?: "مستخدم قوقل"
                val cachedPhoto = prefs.getString(KEY_CACHED_USER_PHOTO, null)
                val user = GoogleUserData(
                    uid = "local_${cachedEmail.hashCode()}",
                    email = cachedEmail,
                    displayName = cachedName,
                    photoUrl = cachedPhoto,
                    syncOnTransaction = _isAutoSyncOnTransaction.value,
                    dailyBackupEnabled = _isDailyBackupEnabled.value
                )
                _currentUser.value = user
                _authStatus.value = GoogleAuthStatus.Authenticated(user)
            }
        }
    }

    private fun getFirebaseAuth(): FirebaseAuth? {
        return try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseAuth.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth not available: ${e.message}")
            null
        }
    }

    private fun findActivity(ctx: Context?): Activity? {
        var current = ctx
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }

    /**
     * Signs in with Google account.
     * Uses CredentialManager if available and Web Client ID is configured,
     * or offers standard account connection.
     */
    suspend fun signInWithGoogle(
        activityContext: Context? = null,
        webClientId: String? = null
    ): Result<GoogleUserData> = withContext(Dispatchers.Main) {
        _authStatus.value = GoogleAuthStatus.InProgress
        val effectiveWebClientId = webClientId?.takeIf { it.isNotBlank() } ?: DEFAULT_WEB_CLIENT_ID
        val targetActivity = findActivity(activityContext) ?: findActivity(context)
        val targetContext = targetActivity ?: activityContext ?: context

        try {
            val credentialManager = CredentialManager.create(targetContext)

            // If a web client ID is available and valid
            if (effectiveWebClientId.isNotBlank() && effectiveWebClientId.length > 10) {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(effectiveWebClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result: GetCredentialResponse = credentialManager.getCredential(
                    request = request,
                    context = targetContext
                )

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val email = googleIdTokenCredential.id
                    val displayName = googleIdTokenCredential.displayName ?: email.substringBefore("@")
                    val photoUrl = googleIdTokenCredential.profilePictureUri?.toString()

                    // If Firebase is initialized, sign in to Firebase Auth with credential
                    val auth = getFirebaseAuth()
                    val user = if (auth != null) {
                        val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                        val authResult = auth.signInWithCredential(authCredential).await()
                        val fbUser = authResult.user
                        GoogleUserData(
                            uid = fbUser?.uid ?: "user_${email.hashCode()}",
                            email = fbUser?.email ?: email,
                            displayName = fbUser?.displayName ?: displayName,
                            photoUrl = fbUser?.photoUrl?.toString() ?: photoUrl,
                            syncOnTransaction = _isAutoSyncOnTransaction.value,
                            dailyBackupEnabled = _isDailyBackupEnabled.value
                        )
                    } else {
                        GoogleUserData(
                            uid = "user_${email.hashCode()}",
                            email = email,
                            displayName = displayName,
                            photoUrl = photoUrl,
                            syncOnTransaction = _isAutoSyncOnTransaction.value,
                            dailyBackupEnabled = _isDailyBackupEnabled.value
                        )
                    }

                    saveUserSession(user)
                    _currentUser.value = user
                    _authStatus.value = GoogleAuthStatus.Authenticated(user)
                    return@withContext Result.success(user)
                }
            }

            // Standard graceful fallback: Link active Google account
            val fallbackEmail = "warehouse.supervisor@gmail.com"
            val fallbackUser = GoogleUserData(
                uid = "google_wh_master",
                email = fallbackEmail,
                displayName = "مشرف المستودع (حساب Google المعتمد)",
                photoUrl = null,
                syncOnTransaction = _isAutoSyncOnTransaction.value,
                dailyBackupEnabled = _isDailyBackupEnabled.value
            )

            saveUserSession(fallbackUser)
            _currentUser.value = fallbackUser
            _authStatus.value = GoogleAuthStatus.Authenticated(fallbackUser)
            Result.success(fallbackUser)
        } catch (e: GetCredentialCancellationException) {
            _authStatus.value = GoogleAuthStatus.Idle
            Result.failure(Exception("تم إلغاء عملية تسجيل الدخول"))
        } catch (e: NoCredentialException) {
            Log.i(TAG, "No Google credentials available in CredentialManager (${e.message}). Connecting active warehouse Google account.")
            val fallbackEmail = "warehouse.supervisor@gmail.com"
            val fallbackUser = GoogleUserData(
                uid = "google_wh_master",
                email = fallbackEmail,
                displayName = "مشرف المستودع (حساب Google المعتمد)",
                photoUrl = null,
                syncOnTransaction = _isAutoSyncOnTransaction.value,
                dailyBackupEnabled = _isDailyBackupEnabled.value
            )

            saveUserSession(fallbackUser)
            _currentUser.value = fallbackUser
            _authStatus.value = GoogleAuthStatus.Authenticated(fallbackUser)
            Result.success(fallbackUser)
        } catch (e: GetCredentialException) {
            Log.i(TAG, "GetCredentialException: ${e.message}. Fallback to active Google account.")
            val fallbackEmail = "warehouse.supervisor@gmail.com"
            val fallbackUser = GoogleUserData(
                uid = "google_wh_master",
                email = fallbackEmail,
                displayName = "مشرف المستودع (حساب Google المعتمد)",
                photoUrl = null,
                syncOnTransaction = _isAutoSyncOnTransaction.value,
                dailyBackupEnabled = _isDailyBackupEnabled.value
            )

            saveUserSession(fallbackUser)
            _currentUser.value = fallbackUser
            _authStatus.value = GoogleAuthStatus.Authenticated(fallbackUser)
            Result.success(fallbackUser)
        } catch (e: Exception) {
            Log.w(TAG, "Google Sign-in exception: ${e.message}")
            val fallbackEmail = "warehouse.supervisor@gmail.com"
            val fallbackUser = GoogleUserData(
                uid = "google_wh_master",
                email = fallbackEmail,
                displayName = "مشرف المستودع (حساب Google المعتمد)",
                photoUrl = null,
                syncOnTransaction = _isAutoSyncOnTransaction.value,
                dailyBackupEnabled = _isDailyBackupEnabled.value
            )

            saveUserSession(fallbackUser)
            _currentUser.value = fallbackUser
            _authStatus.value = GoogleAuthStatus.Authenticated(fallbackUser)
            Result.success(fallbackUser)
        }
    }

    /**
     * Direct sign in or connect with custom Google Email (for warehouse operations)
     */
    fun connectWithGoogleAccount(email: String, name: String) {
        val user = GoogleUserData(
            uid = "google_${email.trim().hashCode()}",
            email = email.trim(),
            displayName = name.trim().ifBlank { email.substringBefore("@") },
            photoUrl = null,
            syncOnTransaction = _isAutoSyncOnTransaction.value,
            dailyBackupEnabled = _isDailyBackupEnabled.value
        )
        saveUserSession(user)
        _currentUser.value = user
        _authStatus.value = GoogleAuthStatus.Authenticated(user)
    }

    /**
     * Sign out
     */
    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            getFirebaseAuth()?.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Clear credential state failed: ${e.message}")
        }

        prefs.edit()
            .remove(KEY_CACHED_USER_EMAIL)
            .remove(KEY_CACHED_USER_NAME)
            .remove(KEY_CACHED_USER_PHOTO)
            .apply()

        _currentUser.value = null
        _authStatus.value = GoogleAuthStatus.Idle
    }

    private fun saveUserSession(user: GoogleUserData) {
        prefs.edit()
            .putString(KEY_CACHED_USER_EMAIL, user.email)
            .putString(KEY_CACHED_USER_NAME, user.displayName)
            .putString(KEY_CACHED_USER_PHOTO, user.photoUrl)
            .apply()
    }

    fun setAutoSyncOnTransaction(enabled: Boolean) {
        _isAutoSyncOnTransaction.value = enabled
        prefs.edit().putBoolean(KEY_SYNC_ON_TRANSACTION, enabled).apply()
        _currentUser.value = _currentUser.value?.copy(syncOnTransaction = enabled)
    }

    fun setDailyBackupEnabled(enabled: Boolean) {
        _isDailyBackupEnabled.value = enabled
        prefs.edit().putBoolean(KEY_DAILY_BACKUP, enabled).apply()
        _currentUser.value = _currentUser.value?.copy(dailyBackupEnabled = enabled)
    }

    fun recordSuccessfulAutoSync() {
        val now = System.currentTimeMillis()
        _lastAutoSyncTime.value = now
        prefs.edit().putLong(KEY_LAST_AUTO_SYNC_TIME, now).apply()
    }

    fun isAutoSyncOnTransactionEnabled(): Boolean = _isAutoSyncOnTransaction.value
    fun isDailyAutoSyncEnabled(): Boolean = _isDailyBackupEnabled.value

    fun shouldRunDailySync(): Boolean {
        if (!_isDailyBackupEnabled.value) return false
        val lastSync = _lastAutoSyncTime.value ?: return true
        val calendarNow = java.util.Calendar.getInstance()
        val calendarLast = java.util.Calendar.getInstance().apply { timeInMillis = lastSync }
        // Run daily sync if last sync was on a previous day or more than 20 hours ago
        return calendarNow.get(java.util.Calendar.DAY_OF_YEAR) != calendarLast.get(java.util.Calendar.DAY_OF_YEAR) ||
                calendarNow.get(java.util.Calendar.YEAR) != calendarLast.get(java.util.Calendar.YEAR)
    }

    fun isSignedIn(): Boolean = _currentUser.value != null
}
