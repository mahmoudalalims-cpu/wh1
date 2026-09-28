package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.sync.FirebaseSyncManager
import com.example.data.sync.FirebaseSyncStatus
import com.example.ui.theme.MinimalistBluePrimary
import com.example.ui.theme.NavyPrimary
import com.example.viewmodel.WarehouseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FirebaseSyncDialog(
    viewModel: WarehouseViewModel,
    onDismiss: () -> Unit,
    onShowMessage: (String) -> Unit,
    onSignOut: (() -> Unit)? = null,
    onOpenGoogleLogin: (() -> Unit)? = null
) {
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val isInitialized = viewModel.firebaseSyncManager.isFirebaseInitialized()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var copiedToClipboard by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Google Auth and Automatic Sync states
    val googleUser by viewModel.googleAuthSyncManager.currentUser.collectAsStateWithLifecycle()
    val isAutoSyncOnTransaction by viewModel.googleAuthSyncManager.isAutoSyncOnTransaction.collectAsStateWithLifecycle()
    val isDailyAutoSyncEnabled by viewModel.googleAuthSyncManager.isDailyBackupEnabled.collectAsStateWithLifecycle()

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar")) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .testTag("firebase_sync_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "الربط السحابي (Google Firebase)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "مزامنة بيانات المستودع وحمايتها سحابياً",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    // Status Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isInitialized) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isInitialized) Color(0xFF16A34A) else Color(0xFFD97706))
                            )
                            Text(
                                text = if (isInitialized) "متصل بالسحابة" else "محلي (Room)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isInitialized) Color(0xFF15803D) else Color(0xFFB45309)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(14.dp))

                // Firebase Status Banner
                if (isInitialized) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "قاعدة بيانات Cloud Firestore مهيأة وجاهزة",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF14532D)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (lastSyncTime != null) {
                                    "آخر مزامنة ناجحة: ${dateFormat.format(Date(lastSyncTime!!))}"
                                } else {
                                    "جاهز للمزامنة الفورية مع السحابة."
                                },
                                fontSize = 11.5.sp,
                                color = Color(0xFF166534)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sync action buttons
                    Text(
                        text = "عمليات المزامنة السحابية:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Primary Action: Smart Bidirectional Sync (Two-Way Merge & Update)
                    Button(
                        onClick = {
                            viewModel.syncWithFirebase { success, msg ->
                                onShowMessage(msg)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("smart_sync_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MinimalistBluePrimary)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text("مزامنة ذكية شاملة (دمج وتحديث فوري)", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                            Text("حفظ حركاتك الجديدة وتحديث المخزون بأمان دون مسح بياناتك", fontSize = 10.5.sp, color = Color.White.copy(alpha = 0.85f))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.uploadAllToFirebase { success, msg ->
                                    onShowMessage(msg)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E293B))
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("رفع كامل للسحابة", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.downloadAllFromFirebase { success, msg ->
                                    onShowMessage(msg)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A))
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("استعادة من السحابة", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Google Account & Auto-Sync Settings
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Account Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = if (googleUser != null) Color(0xFF1D4ED8) else Color(0xFF64748B),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = if (googleUser != null) googleUser!!.displayName else "حساب Google للمزامنة",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = if (googleUser != null) googleUser!!.email else "غير مسجل الدخول",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                if (googleUser != null) {
                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                viewModel.googleAuthSyncManager.signOut()
                                                onShowMessage("تم تسجيل الخروج من حساب Google بنجاح")
                                                onSignOut?.invoke()
                                                onDismiss()
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("خروج", fontSize = 10.sp, color = Color(0xFFDC2626))
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            if (onOpenGoogleLogin != null) {
                                                onDismiss()
                                                onOpenGoogleLogin()
                                            } else {
                                                scope.launch {
                                                    val res = viewModel.googleAuthSyncManager.signInWithGoogle(activityContext = context)
                                                    res.onSuccess { user ->
                                                        onShowMessage("تم تسجيل الدخول بنجاح بحساب: ${user.displayName}")
                                                    }.onFailure { err ->
                                                        onShowMessage("تعذر تسجيل الدخول: ${err.message}")
                                                    }
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("دخول Google", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Switch 1: Auto sync after every order (issue / return / damage)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "المزامنة التلقائية الفورية",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "مزامنة سحابية بعد كل أمر صرف أو استرجاع أو تالف",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Switch(
                                    checked = isAutoSyncOnTransaction,
                                    onCheckedChange = { isChecked ->
                                        viewModel.googleAuthSyncManager.setAutoSyncOnTransaction(isChecked)
                                        onShowMessage(if (isChecked) "تم تفعيل المزامنة التلقائية بعد كل أمر" else "تم إيقاف المزامنة التلقائية")
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MinimalistBluePrimary
                                    ),
                                    modifier = Modifier.testTag("toggle_auto_sync_transaction")
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Switch 2: Daily End-of-Day Backup
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "المزامنة اليومية التلقائية",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "إجراء نسخ احتياطي تلقائي في نهاية كل يوم تجنباً لفقد البيانات",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Switch(
                                    checked = isDailyAutoSyncEnabled,
                                    onCheckedChange = { isChecked ->
                                        viewModel.googleAuthSyncManager.setDailyBackupEnabled(isChecked)
                                        onShowMessage(if (isChecked) "تم تفعيل النسخ الاحتياطي اليومي التلقائي" else "تم إيقاف النسخ الاحتياطي اليومي")
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MinimalistBluePrimary
                                    ),
                                    modifier = Modifier.testTag("toggle_daily_auto_sync")
                                )
                            }
                        }
                    }
                } else {
                    // Pending google-services.json card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "بانتظار ملف الإعداد السحابي (google-services.json)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "التطبيق يعمل حالياً بأمان تام وموثوقية عبر قاعدة بيانات Room المحلية. تم تجهيز بنية Firebase و Firestore بالكامل في الكود البرمجي بانتظار ربط حساب مشروعك.",
                                fontSize = 11.5.sp,
                                color = Color(0xFF78350F),
                                lineHeight = 17.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Step by step guide
                    Text(
                        text = "خطوات إتمام الربط السحابي بـ Firebase:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Step 1: Application ID with copy button
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "١. معرّف الحزمة الخاص بالتطبيق (Package Name):",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = FirebaseSyncManager.APPLICATION_ID,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MinimalistBluePrimary
                                )
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(FirebaseSyncManager.APPLICATION_ID))
                                        copiedToClipboard = true
                                        onShowMessage("تم نسخ معرّف التطبيق بنجاح")
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (copiedToClipboard) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                                        contentDescription = "نسخ",
                                        tint = if (copiedToClipboard) Color(0xFF16A34A) else Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Step 2 & 3 Instructions
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StepItem(number = "٢", text = "ادخل إلى منصة Google Firebase (console.firebase.google.com) وأنشئ مشروعاً أو افتح مشروعك القائم.")
                        StepItem(number = "٣", text = "اضغط على إضافة تطبيق Android، وألصق معرّف الحزمة أعلاه.")
                        StepItem(number = "٤", text = "فعّل قاعدة بيانات Cloud Firestore من القائمة الجانبية في Firebase.")
                        StepItem(number = "٥", text = "حمّل ملف google-services.json وضعه داخل مجلد app في المشروع.")
                    }
                }

                // Sync status indicator (if syncing)
                if (syncStatus is FirebaseSyncStatus.Syncing) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEFF6FF))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MinimalistBluePrimary,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "جاري تنفيذ المزامنة السحابية...",
                            fontSize = 12.sp,
                            color = MinimalistBluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("إغلاق", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StepItem(number: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFEFF6FF),
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = number, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MinimalistBluePrimary)
            }
        }
        Text(
            text = text,
            fontSize = 11.5.sp,
            color = Color(0xFF475569),
            lineHeight = 16.sp
        )
    }
}
