package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ItemEntity
import com.example.ui.components.AddItemDialog
import com.example.ui.components.AdminPinDialog
import com.example.ui.components.InventorySettingsDialog
import com.example.ui.theme.MinimalistAlertBadge
import com.example.ui.theme.MinimalistAlertContainer
import com.example.ui.theme.MinimalistAlertText
import com.example.ui.theme.MinimalistBlueContainer
import com.example.ui.theme.MinimalistBluePrimary
import com.example.ui.theme.warehouseTextFieldColors
import com.example.ui.theme.MinimalistOnBlueContainer
import com.example.viewmodel.WarehouseViewModel

@Composable
fun ItemsInventoryScreen(
    viewModel: WarehouseViewModel,
    onNavigateToIssueWithItem: ((ItemEntity) -> Unit)? = null,
    onNavigateToReturn: (() -> Unit)? = null,
    onNavigateToDamage: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val filteredItems by viewModel.filteredItems.collectAsStateWithLifecycle()
    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    val lowStockItems by viewModel.lowStockItems.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val showOnlyLowStock by viewModel.showOnlyLowStock.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showInventorySettings by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<ItemEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<ItemEntity?>(null) }
    var pendingItemUpdate by remember { mutableStateOf<ItemEntity?>(null) }

    val categories = listOf("الكل", "شبس وتسالي", "فشار وبفك", "بسكويت وحلويات")

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Quick Stats Summary (Clean Minimalism 2-Column rounded-3xl Cards)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Total Items
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MinimalistBlueContainer,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.toggleLowStockSlice(false) }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "إجمالي الأصناف",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MinimalistOnBlueContainer
                        )
                        Text(
                            text = "${allItems.size}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalistOnBlueContainer
                        )
                    }
                }

                // Card 2: Low Stock Alerts
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MinimalistAlertContainer,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.toggleLowStockSlice(!showOnlyLowStock) }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "تنبيهات النقص",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MinimalistAlertText
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${lowStockItems.size}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalistAlertText
                            )
                            if (lowStockItems.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MinimalistAlertBadge
                                ) {
                                    Text(
                                        text = "عاجل",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Actions Row (Clean Minimalism Action Pills)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Issue Order Quick Action
                Surface(
                    onClick = { onNavigateToIssueWithItem?.invoke(allItems.firstOrNull() ?: return@Surface) },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = MinimalistBluePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("أمر صرف", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
                    }
                }

                // Return Order Quick Action
                Surface(
                    onClick = { onNavigateToReturn?.invoke() },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF0FDF4),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AssignmentReturn,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("استرجاع", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
                    }
                }

                // Damage Record Quick Action
                Surface(
                    onClick = { onNavigateToDamage?.invoke() },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFEF2F2),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تسجيل تالف", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("بحث باسم الصنف، الكود (مثل 216، 244)...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "بحث", tint = MinimalistBluePrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = warehouseTextFieldColors(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_items_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips & Low-Stock Slice Filter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Slice Toggle Button (تنبيه الحد الأدنى)
                Surface(
                    onClick = { viewModel.toggleLowStockSlice(!showOnlyLowStock) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (showOnlyLowStock) MinimalistAlertBadge else MinimalistAlertContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MinimalistAlertBadge),
                    modifier = Modifier.testTag("low_stock_slice_toggle")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (showOnlyLowStock) Color.White else MinimalistAlertBadge,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "النواقص (${lowStockItems.size})",
                            color = if (showOnlyLowStock) Color.White else MinimalistAlertText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat && !showOnlyLowStock
                    Surface(
                        onClick = {
                            viewModel.toggleLowStockSlice(false)
                            viewModel.setSelectedCategory(cat)
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MinimalistBluePrimary else Color.White,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MinimalistBluePrimary else Color(0xFFE2E8F0)
                        )
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else Color(0xFF475569),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle & Inventory Settings Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "الأصناف المعروضة: ${filteredItems.size} من ${allItems.size}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold
                    )

                    if (showOnlyLowStock) {
                        Text(
                            text = "الأصناف تحت الحد الأدنى",
                            fontSize = 11.sp,
                            color = MinimalistAlertBadge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Dedicated Inventory Settings Action Button (حذف وإدارة الأصناف)
                Surface(
                    onClick = { showInventorySettings = true },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFEF2F2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.testTag("inventory_settings_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "إعدادات المخزون",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "إعدادات المخزون",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // List of Items
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (allItems.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .padding(16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFEFF6FF),
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Inventory2,
                                            contentDescription = null,
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "دليل أصناف المستودع فارغ حالياً",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )

                                Text(
                                    text = "قم بإضافة الأصناف يدوياً للبدء في إدارة المخزون.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                OutlinedButton(
                                    onClick = { showAddDialog = true },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("manual_add_item_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "إضافة صنف جديد يدوياً",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (showOnlyLowStock) "لا توجد أصناف وصلت للحد الأدنى حالياً" else "لم يتم العثور على أصناف مطابقة",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("items_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredItems, key = { it.code }) { item ->
                        ItemCard(
                            item = item,
                            onEdit = { itemToEdit = item },
                            onQuickIssue = { onNavigateToIssueWithItem?.invoke(item) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // Add Item FAB (Clean Minimalist styling)
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MinimalistBluePrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_item_fab")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة صنف")
                Spacer(modifier = Modifier.width(6.dp))
                Text("إضافة صنف", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Add / Edit Dialog
    if (showAddDialog || itemToEdit != null) {
        AddItemDialog(
            itemToEdit = itemToEdit,
            onDismiss = {
                showAddDialog = false
                itemToEdit = null
            },
            onSave = { code, name, category, conversionFactor, cartons, pieces, minStock, supplier, price, packagingUnit, baseUnit ->
                if (itemToEdit != null) {
                    val totalPieces = (cartons * conversionFactor) + pieces
                    val updated = itemToEdit!!.copy(
                        name = name,
                        category = category,
                        conversionFactor = conversionFactor,
                        totalPieces = totalPieces,
                        minStockCartons = minStock,
                        supplier = supplier,
                        pricePerCarton = price,
                        packagingUnit = packagingUnit,
                        baseUnit = baseUnit
                    )
                    if (viewModel.securityManager.isProtectionEnabled()) {
                        pendingItemUpdate = updated
                    } else {
                        viewModel.updateItem(updated)
                    }
                    itemToEdit = null
                    showAddDialog = false
                } else {
                    viewModel.addNewItem(
                        code = code,
                        name = name,
                        category = category,
                        conversionFactor = conversionFactor,
                        cartons = cartons,
                        pieces = pieces,
                        minStock = minStock,
                        supplier = supplier,
                        price = price,
                        packagingUnit = packagingUnit,
                        baseUnit = baseUnit
                    )
                    showAddDialog = false
                }
            }
        )
    }

    // Inventory Settings Dialog (حذف وإدارة الأصناف بأمان)
    if (showInventorySettings) {
        InventorySettingsDialog(
            items = allItems,
            onDeleteItemRequested = { item ->
                itemToDelete = item
            },
            onDismiss = { showInventorySettings = false }
        )
    }

    // PIN Protection Dialog for Editing Item Details/Stock
    pendingItemUpdate?.let { item ->
        AdminPinDialog(
            title = "تأكيد تعديل صنف بالمخزون",
            description = "تنبيه أمني: تعديل بيانات أو رصيد صنف [${item.code} - ${item.name}] محمي برمز المشرف لمنع التلاعب في المخزون.",
            confirmButtonText = "تأكيد وحفظ التعديلات",
            onDismiss = { pendingItemUpdate = null },
            onConfirm = { pin ->
                if (viewModel.securityManager.verifyPin(pin)) {
                    viewModel.updateItemProtected(item, pin) {
                        pendingItemUpdate = null
                    }
                    true
                } else {
                    false
                }
            }
        )
    }

    // PIN Protection Dialog for Deleting Item
    itemToDelete?.let { item ->
        AdminPinDialog(
            title = "تأكيد حذف الصنف [${item.code}]",
            description = "تحذير: سيتم مسح صنف \"${item.name}\" وكامل أرصدته وحركاته من المخزون. تتطلب هذه العملية إدخال رمز المشرف.",
            confirmButtonText = "تأكيد الحذف النهائي",
            onDismiss = { itemToDelete = null },
            onConfirm = { pin ->
                if (viewModel.securityManager.verifyPin(pin)) {
                    viewModel.deleteItemProtected(item.code, pin) {
                        itemToDelete = null
                    }
                    true
                } else {
                    false
                }
            }
        )
    }
}

@Composable
fun ItemCard(
    item: ItemEntity,
    onEdit: () -> Unit,
    onQuickIssue: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("item_card_${item.code}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Code Badge & Category & Low Stock Alert
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MinimalistBlueContainer,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "كود: ${item.code}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = MinimalistOnBlueContainer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = item.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (item.isLowStock) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MinimalistAlertContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "تنبيه نقص",
                                tint = MinimalistAlertBadge,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "أقل من الحد الأدنى",
                                color = MinimalistAlertBadge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Item Name
            Text(
                text = item.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF191C1E),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stock Details Box (Clean Minimalist styling)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF8F9FF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("الرصيد الحالي بالكرتون:", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(
                                text = "${item.currentCartons} كرتون",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (item.isLowStock) MinimalistAlertBadge else MinimalistBluePrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("المتبقي بالحبة (فكة):", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(
                                text = "${item.remainingPieces} حبة",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF191C1E)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFE2E8F0))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "إجمالي الرصيد: ${item.totalPieces} حبة",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )
                        Text(
                            text = "معامل التحويل: ${item.conversionFactor} حبة/كرتون",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer info: Reorder level + Supplier + Edit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الحد الأدنى: ${item.minStockCartons} كرتون | المورد: ${item.supplier}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل الصنف",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onQuickIssue,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "صرف سريع",
                            tint = MinimalistBluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
