package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persists and manages Storekeeper and Representative preferences across the app.
 * Allows fixing and locking the Storekeeper name and Default Sales Representative
 * so they remain stored persistently in SharedPreferences and don't reset.
 */
class WarehouseSettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("arab_bond_warehouse_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_STOREKEEPER_NAME = "storekeeper_name"
        private const val KEY_DEFAULT_REP_NAME = "default_representative_name"
        const val DEFAULT_STOREKEEPER_NAME = "محمد الحارثي - أمين المستودع"
        const val DEFAULT_REP_NAME = "أحمد عبد الله الشمري"
    }

    private val _storekeeperName = MutableStateFlow(getSavedStorekeeperName())
    val storekeeperName: StateFlow<String> = _storekeeperName.asStateFlow()

    private val _defaultRepresentativeName = MutableStateFlow(getSavedDefaultRepresentativeName())
    val defaultRepresentativeName: StateFlow<String> = _defaultRepresentativeName.asStateFlow()

    private fun getSavedStorekeeperName(): String {
        return prefs.getString(KEY_STOREKEEPER_NAME, DEFAULT_STOREKEEPER_NAME)
            ?.ifBlank { DEFAULT_STOREKEEPER_NAME } ?: DEFAULT_STOREKEEPER_NAME
    }

    private fun getSavedDefaultRepresentativeName(): String {
        return prefs.getString(KEY_DEFAULT_REP_NAME, DEFAULT_REP_NAME)
            ?.ifBlank { DEFAULT_REP_NAME } ?: DEFAULT_REP_NAME
    }

    fun updateStorekeeperName(newName: String) {
        val trimmed = newName.trim().ifBlank { DEFAULT_STOREKEEPER_NAME }
        prefs.edit().putString(KEY_STOREKEEPER_NAME, trimmed).apply()
        _storekeeperName.value = trimmed
    }

    fun updateDefaultRepresentativeName(newName: String) {
        val trimmed = newName.trim().ifBlank { DEFAULT_REP_NAME }
        prefs.edit().putString(KEY_DEFAULT_REP_NAME, trimmed).apply()
        _defaultRepresentativeName.value = trimmed
    }
}
