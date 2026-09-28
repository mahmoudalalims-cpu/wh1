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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import com.example.ui.theme.warehouseTextFieldColors
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.security.SecurityManager
import com.example.ui.theme.MinimalistBluePrimary
import com.example.viewmodel.WarehouseViewModel

@Composable
fun SecuritySettingsDialog(
    viewModel: WarehouseViewModel,
    onDismiss: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val securityManager = viewModel.securityManager
    val currentStorekeeper by viewModel.storekeeperName.collectAsStateWithLifecycle()
    val currentDefaultRep by viewModel.defaultRepresentativeName.collectAsStateWithLifecycle()

    var storekeeperInput by remember(currentStorekeeper) { mutableStateOf(currentStorekeeper) }
    var defaultRepInput by remember(currentDefaultRep) { mutableStateOf(currentDefaultRep) }

    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var pinSuccess by remember { mutableStateOf<String?>(null) }

    var isProtected by remember { mutableStateOf(securityManager.isProtectionEnabled()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("security_settings_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "إعدادات الأمان وحماية المخزون",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "مؤسسة آصرة العرب - ARAB BOND EST",
                        fontSize = 11.sp,
                        color = Color(0xFFD97706),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Security status badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isProtected) Color(0xFFEFF6FF) else Color(0xFFFEF2F2))
                        .border(
                            1.dp,
                            if (isProtected) Color(0xFFBFDBFE) else Color(0xFFFECACA),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isProtected) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isProtected) MinimalistBluePrimary else Color(0xFFDC2626),
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = if (isProtected) "نظام الحماية ضد التلاعب: نشط" else "نظام الحماية: معطل",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isProtected) Color(0xFF1E3A8A) else Color(0xFF991B1B)
                                )
                                Text(
                                    text = if (isProtected) "يمنع حذف أو إلغاء أي أمر نهائي دون PIN" else "تنبيه: العمليات غير محمية",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                        Switch(
                            checked = isProtected,
                            onCheckedChange = {
                                isProtected = it
                                securityManager.setProtectionEnabled(it)
                                onShowMessage(if (it) "تم تفعيل حماية المخزون" else "تم تعطيل حماية المخزون")
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MinimalistBluePrimary
                            )
                        )
                    }
                }

                // Storekeeper & Representatives Persistence Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MinimalistBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "تثبيت اسم أمين المستودع والمناديب",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "حفظ الأسماء الافتراضية في الضبط لعدم الحاجة لإعادة إدخالها",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = storekeeperInput,
                            onValueChange = { storekeeperInput = it },
                            label = { Text("اسم أمين المستودع الثابت") },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = MinimalistBluePrimary, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("storekeeper_name_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = warehouseTextFieldColors()
                        )

                        OutlinedTextField(
                            value = defaultRepInput,
                            onValueChange = { defaultRepInput = it },
                            label = { Text("اسم المندوب الافتراضي") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MinimalistBluePrimary, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("default_rep_name_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = warehouseTextFieldColors()
                        )

                        Button(
                            onClick = {
                                viewModel.updateStorekeeperName(storekeeperInput)
                                viewModel.updateDefaultRepresentativeName(defaultRepInput)
                                onShowMessage("تم تثبيت وحفظ أسماء أمين المستودع والمناديب بنجاح")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تثبيت الأسماء في الضبط", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Change Master PIN Section
                Text(
                    text = "تغيير رمز المشرف (Admin PIN)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = oldPin,
                    onValueChange = {
                        oldPin = it
                        pinError = null
                        pinSuccess = null
                    },
                    label = { Text("كلمة المرور الحالية (الافتراضية: 1234)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = warehouseTextFieldColors()
                )

                OutlinedTextField(
                    value = newPin,
                    onValueChange = {
                        newPin = it
                        pinError = null
                        pinSuccess = null
                    },
                    label = { Text("كلمة المرور الجديدة") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = warehouseTextFieldColors()
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = {
                        confirmPin = it
                        pinError = null
                        pinSuccess = null
                    },
                    label = { Text("تأكيد كلمة المرور الجديدة") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = warehouseTextFieldColors()
                )

                if (pinError != null) {
                    Text(
                        text = pinError ?: "",
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (pinSuccess != null) {
                    Text(
                        text = pinSuccess ?: "",
                        color = Color(0xFF16A34A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        if (newPin != confirmPin) {
                            pinError = "كلمة المرور وتأكيدها غير متطابقين"
                            return@Button
                        }
                        val result = securityManager.changePin(oldPin, newPin)
                        result.onSuccess {
                            pinSuccess = "تم تحديث كلمة المرور بنجاح!"
                            pinError = null
                            oldPin = ""
                            newPin = ""
                            confirmPin = ""
                            onShowMessage("تم تحديث كلمة المرور الإدارية بنجاح")
                        }.onFailure { err ->
                            pinError = err.message
                            pinSuccess = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MinimalistBluePrimary)
                ) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حفظ كلمة المرور الجديدة", fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Firebase Cloud info card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEF3C7))
                        .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "الربط السحابي مع Google Firebase",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                        Text(
                            text = "نعم، النظام جاهز معمارياً للربط مع Google Firebase (Cloud Firestore & Realtime Database). يتيح ذلك مزامنة فورية للمخزون بين أجهزة المستودع والمشرفين ونسخ احتياطي فوري على خوادم قوقل.",
                            fontSize = 11.sp,
                            color = Color(0xFF78350F),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MinimalistBluePrimary)
            ) {
                Text("إغلاق")
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
