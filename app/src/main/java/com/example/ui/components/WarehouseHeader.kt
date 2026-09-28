package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.MinimalistAlertBadge
import com.example.ui.theme.MinimalistAlertContainer
import com.example.ui.theme.MinimalistAlertText
import com.example.ui.theme.MinimalistBluePrimary
import com.example.ui.theme.MinimalistOnBlueContainer

import androidx.compose.material.icons.filled.AutoAwesome

@Composable
fun WarehouseHeader(
    lowStockCount: Int = 0,
    onLowStockClick: (() -> Unit)? = null,
    onSecurityClick: (() -> Unit)? = null,
    onCloudClick: (() -> Unit)? = null,
    onThemeClick: (() -> Unit)? = null,
    onGoogleAuthClick: (() -> Unit)? = null,
    onAiScannerClick: (() -> Unit)? = null,
    googleUser: com.example.data.auth.GoogleUserData? = null,
    isCloudConnected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("warehouse_header_card"),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Official Arab Bond Logo & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFFBEB))
                            .border(1.5.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_arab_bond_logo),
                            contentDescription = "شعار مؤسسة آصرة العرب",
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "مؤسسة آصرة العرب",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MinimalistOnBlueContainer,
                                lineHeight = 20.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ARAB BOND",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                        Text(
                            text = "إدارة المستودعات الغذائية والرقابة المخزنية",
                            fontSize = 10.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Trailing actions: Google Account, Theme, Cloud Sync, Security & Low Stock Alert
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // AI Document Scanner Button
                    if (onAiScannerClick != null) {
                        Surface(
                            onClick = onAiScannerClick,
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFEFF6FF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD)),
                            modifier = Modifier.testTag("header_ai_scanner_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "ماسح الذكاء الاصطناعي",
                                    tint = MinimalistBluePrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "ماسح AI",
                                    color = MinimalistBluePrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    // Google Account Login & Sync Button
                    if (onGoogleAuthClick != null) {
                        Surface(
                            onClick = onGoogleAuthClick,
                            shape = RoundedCornerShape(16.dp),
                            color = if (googleUser != null) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (googleUser != null) Color(0xFF93C5FD) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.testTag("header_google_auth_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "حساب قوقل للمزامنة",
                                    tint = if (googleUser != null) Color(0xFF1D4ED8) else Color(0xFF64748B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (googleUser != null) (googleUser.displayName.split(" ").firstOrNull() ?: "Google") else "ربط Google",
                                    color = if (googleUser != null) Color(0xFF1D4ED8) else Color(0xFF475569),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Theme & Drive Backup Button
                    if (onThemeClick != null) {
                        Surface(
                            onClick = onThemeClick,
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.testTag("header_theme_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "المظهر والمزامنة",
                                    tint = Color(0xFF334155),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "المظهر",
                                    color = Color(0xFF334155),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Firebase Cloud Sync Button
                    if (onCloudClick != null) {
                        Surface(
                            onClick = onCloudClick,
                            shape = RoundedCornerShape(16.dp),
                            color = if (isCloudConnected) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCloudConnected) Color(0xFFBBF7D0) else Color(0xFFFDE68A)
                            ),
                            modifier = Modifier.testTag("header_cloud_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = "المزامنة السحابية",
                                    tint = if (isCloudConnected) Color(0xFF15803D) else Color(0xFFB45309),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isCloudConnected) "سحابي" else "Firebase",
                                    color = if (isCloudConnected) Color(0xFF15803D) else Color(0xFFB45309),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Security PIN Settings Button
                    if (onSecurityClick != null) {
                        Surface(
                            onClick = onSecurityClick,
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFEFF6FF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier.testTag("header_security_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "إعدادات الأمان",
                                    tint = MinimalistBluePrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "الأمان",
                                    color = MinimalistBluePrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Low Stock Alert Badge
                    if (lowStockCount > 0 && onLowStockClick != null) {
                        Surface(
                            onClick = onLowStockClick,
                            shape = RoundedCornerShape(20.dp),
                            color = MinimalistAlertContainer,
                            modifier = Modifier.testTag("low_stock_header_badge")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MinimalistAlertBadge
                                ) {
                                    Text(
                                        text = "$lowStockCount",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "نواقص",
                                    color = MinimalistAlertText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFF1F5F9))
            )
        }
    }
}
