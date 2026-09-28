package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import com.example.ui.theme.MinimalistBlueContainer
import com.example.ui.theme.MinimalistBluePrimary
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AiVoucherScannerDialog
import com.example.ui.components.AppThemeAndDriveDialog
import com.example.ui.components.FirebaseSyncDialog
import com.example.ui.components.IssueVoucherDialog
import com.example.ui.components.ReturnVoucherDialog
import com.example.ui.components.SampleVoucherDialog
import com.example.ui.components.SecuritySettingsDialog
import com.example.ui.components.WarehouseHeader
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DamageRecordScreen
import com.example.ui.screens.GoogleAuthScreen
import com.example.ui.screens.IssueOrderScreen
import com.example.ui.screens.ItemsInventoryScreen
import com.example.ui.screens.MovementsLedgerScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.RepresentativesScreen
import com.example.ui.screens.ReturnOrderScreen
import com.example.ui.screens.SamplesScreen
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.NavyPrimary
import com.example.viewmodel.WarehouseViewModel
import kotlinx.coroutines.launch

enum class WarehouseTab(val title: String) {
    DASHBOARD("الرئيسية"),
    ITEMS("الأصناف"),
    ISSUE("أمر صرف"),
    RETURN("استرجاع"),
    SAMPLES("العينات"),
    DAMAGE("تالف"),
    REPRESENTATIVES("المناديب"),
    LEDGER("السجل"),
    REPORTS("التقارير")
}

@Composable
fun WarehouseApp(
    viewModel: WarehouseViewModel = viewModel(),
    initialSharedImageUri: android.net.Uri? = null
) {
    // Set RTL layout for authentic Arabic warehouse enterprise system
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        var currentTab by remember { mutableStateOf(WarehouseTab.DASHBOARD) }
        var showSecurityDialog by remember { mutableStateOf(false) }
        var showCloudDialog by remember { mutableStateOf(false) }
        var showThemeDialog by remember { mutableStateOf(false) }
        var showAiScannerDialog by remember { mutableStateOf(initialSharedImageUri != null) }
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        val lowStockItems by viewModel.lowStockItems.collectAsStateWithLifecycle()
        val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()
        val viewedIssueVoucher by viewModel.viewedIssueVoucher.collectAsStateWithLifecycle()
        val viewedReturnVoucher by viewModel.viewedReturnVoucher.collectAsStateWithLifecycle()
        val viewedSampleVoucher by viewModel.viewedSampleVoucher.collectAsStateWithLifecycle()
        val googleUser by viewModel.googleAuthSyncManager.currentUser.collectAsStateWithLifecycle()
        var continueAsGuest by remember { mutableStateOf(false) }
        var showGoogleAuthScreen by remember { mutableStateOf(false) }

        // Listen for snackbar notifications
        LaunchedEffect(uiMessage) {
            uiMessage?.let { msg ->
                scope.launch {
                    snackbarHostState.showSnackbar(msg)
                    viewModel.clearUiMessage()
                }
            }
        }

        val isAuthRequired = (googleUser == null && !continueAsGuest) || showGoogleAuthScreen

        if (isAuthRequired) {
            GoogleAuthScreen(
                viewModel = viewModel,
                onLoginSuccess = { user ->
                    showGoogleAuthScreen = false
                    continueAsGuest = false
                    scope.launch {
                        snackbarHostState.showSnackbar("مرحباً بك: ${user.displayName} - تم تسجيل الدخول بنجاح")
                    }
                },
                onContinueAsGuest = {
                    continueAsGuest = true
                    showGoogleAuthScreen = false
                }
            )
        } else {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("warehouse_app_scaffold"),
                topBar = {
                    WarehouseHeader(
                        lowStockCount = lowStockItems.size,
                        onLowStockClick = {
                            currentTab = WarehouseTab.ITEMS
                            viewModel.toggleLowStockSlice(true)
                        },
                        onSecurityClick = {
                            showSecurityDialog = true
                        },
                        onCloudClick = {
                            showCloudDialog = true
                        },
                        onThemeClick = {
                            showThemeDialog = true
                        },
                        onAiScannerClick = {
                            showAiScannerDialog = true
                        },
                        onGoogleAuthClick = {
                            if (googleUser != null) {
                                showCloudDialog = true
                            } else {
                                showGoogleAuthScreen = true
                            }
                        },
                        googleUser = googleUser,
                        isCloudConnected = viewModel.firebaseSyncManager.isFirebaseInitialized()
                    )
                },
            bottomBar = {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFF1F5F9))
                    )
                    ScrollableTabRow(
                        selectedTabIndex = currentTab.ordinal,
                        containerColor = Color.White,
                        contentColor = MinimalistBluePrimary,
                        edgePadding = 8.dp,
                        divider = {},
                        modifier = Modifier.testTag("main_bottom_nav")
                    ) {
                        WarehouseTab.values().forEach { tab ->
                            val isSelected = currentTab == tab
                            Tab(
                                selected = isSelected,
                                onClick = { currentTab = tab },
                                text = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                icon = {
                                    when (tab) {
                                        WarehouseTab.DASHBOARD -> Icon(Icons.Default.Dashboard, contentDescription = tab.title)
                                        WarehouseTab.ITEMS -> Icon(Icons.Default.Inventory, contentDescription = tab.title)
                                        WarehouseTab.ISSUE -> Icon(Icons.Default.ReceiptLong, contentDescription = tab.title)
                                        WarehouseTab.RETURN -> Icon(Icons.Default.AssignmentReturn, contentDescription = tab.title)
                                        WarehouseTab.SAMPLES -> Icon(Icons.Default.CardGiftcard, contentDescription = tab.title)
                                        WarehouseTab.DAMAGE -> Icon(Icons.Default.DeleteSweep, contentDescription = tab.title)
                                        WarehouseTab.REPRESENTATIVES -> Icon(Icons.Default.LocalShipping, contentDescription = tab.title)
                                        WarehouseTab.LEDGER -> Icon(Icons.Default.History, contentDescription = tab.title)
                                        WarehouseTab.REPORTS -> Icon(Icons.Default.Assessment, contentDescription = tab.title)
                                    }
                                },
                                selectedContentColor = MinimalistBluePrimary,
                                unselectedContentColor = Color(0xFF64748B)
                            )
                        }
                    }
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        (fadeIn() + slideInHorizontally { width -> width / 8 })
                            .togetherWith(fadeOut() + slideOutHorizontally { width -> -width / 8 })
                    },
                    label = "tab_transition"
                ) { targetTab ->
                    when (targetTab) {
                        WarehouseTab.DASHBOARD -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToTab = { tab -> currentTab = tab }
                        )
                        WarehouseTab.ITEMS -> ItemsInventoryScreen(
                            viewModel = viewModel,
                            onNavigateToIssueWithItem = {
                                currentTab = WarehouseTab.ISSUE
                            },
                            onNavigateToReturn = {
                                currentTab = WarehouseTab.RETURN
                            },
                            onNavigateToDamage = {
                                currentTab = WarehouseTab.DAMAGE
                            }
                        )
                        WarehouseTab.ISSUE -> IssueOrderScreen(viewModel = viewModel)
                        WarehouseTab.REPRESENTATIVES -> RepresentativesScreen(viewModel = viewModel)
                        WarehouseTab.SAMPLES -> SamplesScreen(viewModel = viewModel)
                        WarehouseTab.RETURN -> ReturnOrderScreen(viewModel = viewModel)
                        WarehouseTab.DAMAGE -> DamageRecordScreen(viewModel = viewModel)
                        WarehouseTab.LEDGER -> MovementsLedgerScreen(viewModel = viewModel)
                        WarehouseTab.REPORTS -> ReportsScreen(viewModel = viewModel)
                    }
                }

                // Active issue voucher preview & print dialog
                viewedIssueVoucher?.let { voucher ->
                    IssueVoucherDialog(
                        order = voucher,
                        onDismiss = { viewModel.dismissIssueVoucher() }
                    )
                }

                // Active return voucher preview & print dialog
                viewedReturnVoucher?.let { voucher ->
                    ReturnVoucherDialog(
                        order = voucher,
                        onDismiss = { viewModel.dismissReturnVoucher() }
                    )
                }

                // Active sample voucher preview & print dialog
                viewedSampleVoucher?.let { voucher ->
                    SampleVoucherDialog(
                        order = voucher,
                        onDismiss = { viewModel.dismissSampleVoucher() }
                    )
                }

                // Security and PIN settings dialog
                if (showSecurityDialog) {
                    SecuritySettingsDialog(
                        viewModel = viewModel,
                        onDismiss = { showSecurityDialog = false },
                        onShowMessage = { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                }

                // Firebase Cloud Sync Dialog
                if (showCloudDialog) {
                    FirebaseSyncDialog(
                        viewModel = viewModel,
                        onDismiss = { showCloudDialog = false },
                        onShowMessage = { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        },
                        onSignOut = {
                            continueAsGuest = false
                            showGoogleAuthScreen = true
                        },
                        onOpenGoogleLogin = {
                            showGoogleAuthScreen = true
                        }
                    )
                }

                // Theme Mode, Custom Palettes & Google Drive Backup Dialog
                if (showThemeDialog) {
                    AppThemeAndDriveDialog(
                        viewModel = viewModel,
                        onDismiss = { showThemeDialog = false }
                    )
                }

                // AI Voucher OCR Scanner Dialog
                if (showAiScannerDialog) {
                    AiVoucherScannerDialog(
                        viewModel = viewModel,
                        initialImageUri = initialSharedImageUri,
                        onDismiss = { showAiScannerDialog = false }
                    )
                }
            }
        }
    }
}
}
