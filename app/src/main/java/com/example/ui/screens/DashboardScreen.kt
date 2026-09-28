package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ItemEntity
import com.example.data.model.StockMovementEntity
import com.example.ui.WarehouseTab
import com.example.ui.components.AddItemDialog
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberOnContainer
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.DangerContainer
import com.example.ui.theme.DangerOnContainer
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.MinimalistBlueContainer
import com.example.ui.theme.MinimalistBluePrimary
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.warehouseTextFieldColors
import com.example.viewmodel.WarehouseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.ui.components.AiVoucherScannerDialog
import androidx.compose.material.icons.filled.AutoAwesome

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: WarehouseViewModel,
    onNavigateToTab: (WarehouseTab) -> Unit
) {
    var showAiScannerDialog by remember { mutableStateOf(false) }
    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    val lowStockItems by viewModel.lowStockItems.collectAsStateWithLifecycle()
    val allMovements by viewModel.allMovements.collectAsStateWithLifecycle()
    val allRepresentatives by viewModel.allRepresentatives.collectAsStateWithLifecycle()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var quickSearchQuery by remember { mutableStateOf("") }

    val totalSkus = allItems.size
    val totalPiecesInStock = allItems.sumOf { it.totalPieces.toLong() }
    val lowStockCount = lowStockItems.size
    val outOfStockCount = allItems.count { it.totalPieces <= 0 }

    // Health score calculation
    val healthPercent = if (totalSkus > 0) {
        (((totalSkus - lowStockCount).toFloat() / totalSkus) * 100).toInt().coerceIn(0, 100)
    } else 100

    // Filtered lists if search query is active
    val displayLowStock = if (quickSearchQuery.isBlank()) lowStockItems else lowStockItems.filter {
        it.name.contains(quickSearchQuery, ignoreCase = true) || it.code.contains(quickSearchQuery, ignoreCase = true)
    }

    val displayMovements = if (quickSearchQuery.isBlank()) allMovements.take(6) else allMovements.filter {
        it.itemName.contains(quickSearchQuery, ignoreCase = true) || it.itemCode.contains(quickSearchQuery, ignoreCase = true)
    }.take(10)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
            .testTag("main_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- 1. Welcome & Hero Status Card ---
        item {
            DashboardHeroBanner(
                totalSkus = totalSkus,
                healthPercent = healthPercent,
                lowStockCount = lowStockCount,
                onAddNewItemClick = { showAddItemDialog = true },
                onViewLowStockClick = { onNavigateToTab(WarehouseTab.ITEMS) }
            )
        }

        // --- 2. Quick Search & Filter Bar ---
        item {
            OutlinedTextField(
                value = quickSearchQuery,
                onValueChange = { quickSearchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_search_input"),
                placeholder = { Text("بحث سريع في الأصناف والحركات...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث", tint = NavyPrimary) },
                trailingIcon = {
                    if (quickSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { quickSearchQuery = "" }) {
                            Icon(Icons.Default.Add, contentDescription = "مسح", modifier = Modifier.clip(CircleShape))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = warehouseTextFieldColors()
            )
        }

        // --- 3. Key Stock Metrics Grid ---
        item {
            Text(
                text = "ملخص حالة المخزون الحالي",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 2
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "إجمالي الأصناف",
                    value = "$totalSkus صنف",
                    subtitle = "إجمالي الوحدات: ${totalPiecesInStock} حبة",
                    icon = Icons.Default.Inventory2,
                    containerColor = Color.White,
                    accentColor = MinimalistBluePrimary
                )

                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "مستوى سلامة المخزون",
                    value = "$healthPercent%",
                    subtitle = if (healthPercent > 80) "مستوى آمن وممتاز" else "يتطلب إعادة طلب",
                    icon = Icons.Default.CheckCircle,
                    containerColor = Color.White,
                    accentColor = if (healthPercent > 80) EmeraldTertiary else AmberSecondary
                )

                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "تنبيهات النقص",
                    value = "$lowStockCount صنف",
                    subtitle = "تحت حد الأمان",
                    icon = Icons.Default.Warning,
                    containerColor = if (lowStockCount > 0) DangerContainer else Color.White,
                    accentColor = if (lowStockCount > 0) DangerRed else Color(0xFF64748B),
                    badgeCount = lowStockCount
                )

                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "المناديب والعملاء",
                    value = "${allRepresentatives.size} مندوب",
                    subtitle = "إجمالي السجلات: ${allMovements.size}",
                    icon = Icons.Default.LocalShipping,
                    containerColor = Color.White,
                    accentColor = NavyPrimary
                )
            }
        }

        // --- 4. Quick Movement Links Grid ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_actions_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MinimalistBlueContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = MinimalistBluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "إجراءات سريعة / حركات جديدة",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "روابط مباشرة لإضافة الخصم والزيادة وتسجيل العينات والتالف",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    // High impact AI Scanner Quick Card
                    Surface(
                        onClick = { showAiScannerDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFEFF6FF),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF93C5FD)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_link_ai_scanner")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MinimalistBluePrimary,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "الماكينة الذكية لخصم وتفريغ المخزون (AI OCR)",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E3A8A)
                                    )
                                    Text(
                                        text = "التقاط أو رفع صورة أمر التحميل/التفريغ لخصم أو زيادة الرصيد تلقائياً",
                                        fontSize = 11.sp,
                                        color = Color(0xFF3B82F6)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Grid of quick links
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        maxItemsInEachRow = 2
                    ) {
                        QuickActionTile(
                            modifier = Modifier.weight(1f),
                            title = "أمر تحميل / صرف",
                            subtitle = "خصم كميات من المخزون",
                            icon = Icons.Default.TrendingDown,
                            accentColor = DangerRed,
                            backgroundColor = DangerContainer.copy(alpha = 0.5f),
                            testTag = "quick_link_issue_order",
                            onClick = { onNavigateToTab(WarehouseTab.ISSUE) }
                        )

                        QuickActionTile(
                            modifier = Modifier.weight(1f),
                            title = "أمر تفريغ / مرتجع",
                            subtitle = "إضافة بضاعة وزيادة الرصيد",
                            icon = Icons.Default.TrendingUp,
                            accentColor = EmeraldTertiary,
                            backgroundColor = EmeraldContainer.copy(alpha = 0.5f),
                            testTag = "quick_link_return_order",
                            onClick = { onNavigateToTab(WarehouseTab.RETURN) }
                        )

                        QuickActionTile(
                            modifier = Modifier.weight(1f),
                            title = "صرف عينات للمندوب",
                            subtitle = "سند عينات ترويج وتذوق",
                            icon = Icons.Default.CardGiftcard,
                            accentColor = AmberSecondary,
                            backgroundColor = AmberContainer.copy(alpha = 0.5f),
                            testTag = "quick_link_sample_order",
                            onClick = { onNavigateToTab(WarehouseTab.SAMPLES) }
                        )

                        QuickActionTile(
                            modifier = Modifier.weight(1f),
                            title = "محضر بضاعة تالفة",
                            subtitle = "تسجيل التوالف وإرفاق صورة",
                            icon = Icons.Default.DeleteSweep,
                            accentColor = Color(0xFFDC2626),
                            backgroundColor = Color(0xFFFEE2E2),
                            testTag = "quick_link_damage_record",
                            onClick = { onNavigateToTab(WarehouseTab.DAMAGE) }
                        )

                        QuickActionTile(
                            modifier = Modifier.weight(1f),
                            title = "إضافة صنف جديد",
                            subtitle = "تعريف صنف وحجم وحد أمان",
                            icon = Icons.Default.AddBox,
                            accentColor = MinimalistBluePrimary,
                            backgroundColor = MinimalistBlueContainer.copy(alpha = 0.5f),
                            testTag = "quick_link_add_item",
                            onClick = { showAddItemDialog = true }
                        )

                        QuickActionTile(
                            modifier = Modifier.weight(1f),
                            title = "سجل الحركات",
                            subtitle = "استعراض السجل التفصيلي",
                            icon = Icons.Default.History,
                            accentColor = Color(0xFF475569),
                            backgroundColor = Color(0xFFF1F5F9),
                            testTag = "quick_link_ledger",
                            onClick = { onNavigateToTab(WarehouseTab.LEDGER) }
                        )
                    }
                }
            }
        }

        // --- 5. Low Stock Alert List Section ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("low_stock_summary_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (lowStockCount > 0) DangerContainer else EmeraldContainer,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (lowStockCount > 0) Icons.Default.NotificationsActive else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (lowStockCount > 0) DangerRed else EmeraldTertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "تنبيهات الأصناف الحرجة (نقص المخزون)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = if (lowStockCount > 0) "يوجد $lowStockCount صنف تحت حد الأمان المطلوب" else "جميع الأصناف متوفرة بنسب كافية وآمنة",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        if (lowStockCount > 0) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.toggleLowStockSlice(true)
                                    onNavigateToTab(WarehouseTab.ITEMS)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("dashboard_view_all_low_stock")
                            ) {
                                Text("عرض الكل", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))

                    if (displayLowStock.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldTertiary,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "لا توجد أصناف تحت حد الأمان حالياً",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF334155)
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            displayLowStock.take(4).forEach { item ->
                                LowStockDashboardRow(
                                    item = item,
                                    onIssueClick = {
                                        onNavigateToTab(WarehouseTab.ISSUE)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 6. Stock Distribution by Category ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = MinimalistBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "توزيع المخزون حسب التصنيف",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))

                    val categoriesGroup = allItems.groupBy { it.category }
                    if (categoriesGroup.isEmpty()) {
                        Text("لا توجد بيانات تصنيف حتى الآن", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            categoriesGroup.forEach { (catName, itemsInCat) ->
                                val catPieces = itemsInCat.sumOf { it.totalPieces.toLong() }
                                val progress = if (totalPiecesInStock > 0) (catPieces.toFloat() / totalPiecesInStock) else 0f

                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = catName.ifBlank { "عام" },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = "${itemsInCat.size} صنف | $catPieces حبة",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = MinimalistBluePrimary,
                                        trackColor = MinimalistBlueContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 7. Recent Movements Activity Log ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recent_movements_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "أحدث حركات المخزون",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        IconButton(onClick = { onNavigateToTab(WarehouseTab.LEDGER) }) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "الانتقال للسجل",
                                tint = MinimalistBluePrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))

                    if (displayMovements.isEmpty()) {
                        Text(
                            text = "لم يتم تسجيل أية حركات مخزنية بعد",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            displayMovements.forEach { mov ->
                                DashboardMovementRow(movement = mov)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Add Item Dialog if opened from Dashboard Quick Actions
    if (showAddItemDialog) {
        AddItemDialog(
            itemToEdit = null,
            onDismiss = { showAddItemDialog = false },
            onSave = { code, name, category, factor, cartons, pieces, minStock, supplier, price, pUnit, bUnit ->
                viewModel.addNewItem(
                    code = code,
                    name = name,
                    category = category,
                    conversionFactor = factor,
                    cartons = cartons,
                    pieces = pieces,
                    minStock = minStock,
                    supplier = supplier,
                    price = price,
                    packagingUnit = pUnit,
                    baseUnit = bUnit
                )
                showAddItemDialog = false
            }
        )
    }

    if (showAiScannerDialog) {
        AiVoucherScannerDialog(
            viewModel = viewModel,
            onDismiss = { showAiScannerDialog = false }
        )
    }
}

@Composable
private fun DashboardHeroBanner(
    totalSkus: Int,
    healthPercent: Int,
    lowStockCount: Int,
    onAddNewItemClick: () -> Unit,
    onViewLowStockClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_hero_banner"),
        shape = RoundedCornerShape(20.dp),
        color = MinimalistBluePrimary,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MinimalistBluePrimary,
                            Color(0xFF003F78)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "لوحة التحكم وإدارة حركة المخزن",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "متابعة فورية للتحميل والتفريغ ومراقبة مستويات الأمان",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onAddNewItemClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = MinimalistBluePrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_hero_add_item_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة صنف جديد", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    if (lowStockCount > 0) {
                        Button(
                            onClick = onViewLowStockClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFEF2F2),
                                contentColor = DangerRed
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$lowStockCount أصناف تحت الأمان", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    accentColor: Color,
    badgeCount: Int = 0
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp),
        border = if (badgeCount > 0) androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(accentColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .background(DangerRed, CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "تنبيه",
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    backgroundColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp)),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = Color(0xFF475569),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LowStockDashboardRow(
    item: ItemEntity,
    onIssueClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFFFF1F2),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(DangerRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = item.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF881337)
                    )
                    Text(
                        text = "كود: ${item.code} | المتبقي: ${item.formatStockText()}",
                        fontSize = 11.sp,
                        color = Color(0xFF9F1239)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .background(DangerRed.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "حد الأمان: ${item.minStockCartons} كرتون",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DangerRed
                )
            }
        }
    }
}

@Composable
private fun DashboardMovementRow(movement: StockMovementEntity) {
    val (typeLabel, typeColor, containerColor) = when (movement.movementType) {
        "صرف" -> Triple("خصم / أمر تحميل", DangerRed, DangerContainer.copy(alpha = 0.5f))
        "استرجاع" -> Triple("إضافة / أمر تفريغ", EmeraldTertiary, EmeraldContainer.copy(alpha = 0.5f))
        "عينة" -> Triple("صرف عينة", AmberSecondary, AmberContainer.copy(alpha = 0.5f))
        "تالف" -> Triple("تسجيل تالف", Color(0xFFDC2626), Color(0xFFFEE2E2))
        else -> Triple(movement.movementType, MinimalistBluePrimary, MinimalistBlueContainer)
    }

    val formattedDate = try {
        val sdf = SimpleDateFormat("HH:mm - yyyy/MM/dd", Locale.getDefault())
        sdf.format(Date(movement.timestamp))
    } catch (e: Exception) {
        ""
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(containerColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeColor
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = movement.itemName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "رقم الحركة: ${movement.movementNumber} | $formattedDate",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val sign = if (movement.movementType == "استرجاع") "+" else "-"
                Text(
                    text = "$sign${movement.cartons} كرتون / ${movement.pieces} حبة",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = typeColor
                )
                if (movement.relatedOrderNumber.isNotBlank()) {
                    Text(
                        text = "مستند: ${movement.relatedOrderNumber}",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}
