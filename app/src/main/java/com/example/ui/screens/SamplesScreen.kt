package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ItemEntity
import com.example.data.model.OrderItem
import com.example.data.model.SampleOrderEntity
import com.example.ui.theme.warehouseTextFieldColors
import com.example.util.VoucherExporter
import com.example.viewmodel.WarehouseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SamplesScreen(
    viewModel: WarehouseViewModel
) {
    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    val allRepresentatives by viewModel.allRepresentatives.collectAsStateWithLifecycle()
    val sampleOrders by viewModel.sampleOrders.collectAsStateWithLifecycle()

    val savedStorekeeperName by viewModel.storekeeperName.collectAsStateWithLifecycle()
    val savedDefaultRepName by viewModel.defaultRepresentativeName.collectAsStateWithLifecycle()

    var selectedRepName by remember(savedDefaultRepName) { mutableStateOf(savedDefaultRepName) }
    var selectedRepId by remember { mutableStateOf<Long?>(null) }
    var repDropdownExpanded by remember { mutableStateOf(false) }

    var supervisorName by remember(savedStorekeeperName) { mutableStateOf(savedStorekeeperName) }
    var notes by remember { mutableStateOf("") }

    // Item selection state
    var selectedItem by remember { mutableStateOf<ItemEntity?>(null) }
    var itemSearchQuery by remember { mutableStateOf("") }
    var itemDropdownExpanded by remember { mutableStateOf(false) }
    var cartonsInput by remember { mutableStateOf("0") }
    var piecesInput by remember { mutableStateOf("1") }
    var itemNotesInput by remember { mutableStateOf("") }

    val orderItems = remember { mutableStateListOf<OrderItem>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // History filter
    var historySearchQuery by remember { mutableStateOf("") }

    val filteredHistory = remember(sampleOrders, historySearchQuery) {
        if (historySearchQuery.isBlank()) sampleOrders
        else sampleOrders.filter {
            it.orderNumber.contains(historySearchQuery, ignoreCase = true) ||
                    it.representativeName.contains(historySearchQuery, ignoreCase = true)
        }
    }

    val filteredItems = remember(allItems, itemSearchQuery) {
        if (itemSearchQuery.isBlank()) allItems
        else allItems.filter {
            it.name.contains(itemSearchQuery, ignoreCase = true) ||
                    it.code.contains(itemSearchQuery, ignoreCase = true)
        }
    }

    val totalOrderCartons = orderItems.sumOf { it.cartons }
    val totalOrderPieces = orderItems.sumOf { it.pieces }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Header Banner ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("samples_screen_header"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEA580C))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.CardGiftcard,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "قسم العينات للمناديب",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "تسجيل وصرف العينات المجانية للترويج مع تحديث تلقائي للمخزون",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // --- 2. Create Sample Order Card ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "إصدار أمر صرف عينات جديد",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC2410C)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Representative Picker Dropdown
                    Text(
                        text = "اختر المندوب المستلم للعينات:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedRepName,
                            onValueChange = {
                                selectedRepName = it
                                selectedRepId = null
                            },
                            label = { Text("اسم المندوب") },
                            placeholder = { Text("اختر من القائمة أو اكتب اسماً جديداً") },
                            trailingIcon = {
                                IconButton(onClick = { repDropdownExpanded = !repDropdownExpanded }) {
                                    Icon(Icons.Default.Person, contentDescription = "اختر المندوب")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = warehouseTextFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        DropdownMenu(
                            expanded = repDropdownExpanded && allRepresentatives.isNotEmpty(),
                            onDismissRequest = { repDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            allRepresentatives.forEach { rep ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(rep.name, fontWeight = FontWeight.Bold)
                                            Text(rep.phone.ifBlank { rep.code }, fontSize = 12.sp, color = Color.Gray)
                                        }
                                    },
                                    onClick = {
                                        selectedRepName = rep.name
                                        selectedRepId = rep.id
                                        repDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Supervisor Name
                    OutlinedTextField(
                        value = supervisorName,
                        onValueChange = { supervisorName = it },
                        label = { Text("مسؤول صرف العينات / المشرف") },
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Item Selection Area
                    Text(
                        text = "إضافة أصناف العينات للأمر:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Search Item Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = if (selectedItem != null) "${selectedItem!!.name} [${selectedItem!!.code}]" else itemSearchQuery,
                            onValueChange = {
                                itemSearchQuery = it
                                selectedItem = null
                                itemDropdownExpanded = true
                            },
                            label = { Text("ابحث عن الصنف بالاسم أو الكود") },
                            placeholder = { Text("اكتب كود أو اسم الصنف...") },
                            trailingIcon = {
                                IconButton(onClick = { itemDropdownExpanded = !itemDropdownExpanded }) {
                                    Icon(Icons.Default.Search, contentDescription = "بحث")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = warehouseTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = itemDropdownExpanded && filteredItems.isNotEmpty(),
                            onDismissRequest = { itemDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            filteredItems.take(10).forEach { item ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("${item.name} [${item.code}]", fontWeight = FontWeight.Bold)
                                            Text(
                                                "الرصيد: ${item.currentCartons} ${item.packagingUnit.ifBlank { "كرتون" }} و ${item.remainingPieces} ${item.baseUnit.ifBlank { "حبة" }}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF059669)
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedItem = item
                                        itemSearchQuery = "${item.name} [${item.code}]"
                                        itemDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Stock Availability Banner for selected item
                    selectedItem?.let { item ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF7ED),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFEDD5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الرصيد المتوفر المخزني:",
                                    fontSize = 12.sp,
                                    color = Color(0xFFC2410C),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${item.currentCartons} ${item.packagingUnit.ifBlank { "كرتون" }} و ${item.remainingPieces} ${item.baseUnit.ifBlank { "حبة" }}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC2410C)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quantity Inputs (Cartons & Pieces)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cartonsInput,
                            onValueChange = { cartonsInput = it },
                            label = { Text("عدد (${selectedItem?.packagingUnit?.ifBlank { "كرتون" } ?: "كرتون"})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = warehouseTextFieldColors(),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = piecesInput,
                            onValueChange = { piecesInput = it },
                            label = { Text("عدد (${selectedItem?.baseUnit?.ifBlank { "حبة" } ?: "حبة"})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = warehouseTextFieldColors(),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = itemNotesInput,
                        onValueChange = { itemNotesInput = it },
                        label = { Text("ملاحظة على الصنف (اختياري)") },
                        placeholder = { Text("مثال: عينات للتسويق في السوبرماركت") },
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Add item button
                    Button(
                        onClick = {
                            if (selectedItem == null) {
                                errorMessage = "الرجاء اختيار صنف من القائمة أولاً"
                                return@Button
                            }
                            val cartons = cartonsInput.toIntOrNull() ?: 0
                            val pieces = piecesInput.toIntOrNull() ?: 0
                            if (cartons <= 0 && pieces <= 0) {
                                errorMessage = "الرجاء إدخال كمية صحيحة للعينات"
                                return@Button
                            }

                            val item = selectedItem!!
                            val existingIndex = orderItems.indexOfFirst { it.itemCode == item.code }
                            if (existingIndex >= 0) {
                                val old = orderItems[existingIndex]
                                orderItems[existingIndex] = old.copy(
                                    cartons = old.cartons + cartons,
                                    pieces = old.pieces + pieces,
                                    notes = if (itemNotesInput.isNotBlank()) itemNotesInput else old.notes
                                )
                            } else {
                                orderItems.add(
                                    OrderItem(
                                        itemCode = item.code,
                                        itemName = item.name,
                                        cartons = cartons,
                                        pieces = pieces,
                                        conversionFactor = item.conversionFactor,
                                        price = item.pricePerCarton,
                                        notes = itemNotesInput
                                    )
                                )
                            }

                            // Reset inputs
                            selectedItem = null
                            itemSearchQuery = ""
                            cartonsInput = "0"
                            piecesInput = "1"
                            itemNotesInput = ""
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة الصنف لقائمة العينات")
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage!!, color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Table / List of Selected Items
                    if (orderItems.isNotEmpty()) {
                        Text(
                            text = "قائمة عينات هذا الأمر (${orderItems.size} صنف):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        orderItems.forEachIndexed { index, orderItem ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFAF8F5),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFED7AA)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${index + 1}. ${orderItem.itemName} [${orderItem.itemCode}]",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "الكمية: ${orderItem.cartons} عبوة/كرتون و ${orderItem.pieces} حبة",
                                            fontSize = 12.sp,
                                            color = Color(0xFFEA580C),
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (orderItem.notes.isNotBlank()) {
                                            Text("ملاحظة: ${orderItem.notes}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }

                                    IconButton(
                                        onClick = { orderItems.removeAt(index) }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "حذف الصنف",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Summary Banner
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFF7ED),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("إجمالي العينات بالأمر:", fontWeight = FontWeight.Bold, color = Color(0xFFC2410C))
                                Text(
                                    "$totalOrderCartons كرتون/عبوة و $totalOrderPieces حبة",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC2410C),
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Main Issue Action Button
                        Button(
                            onClick = {
                                if (selectedRepName.isBlank()) {
                                    errorMessage = "الرجاء تحديد أو إدخال اسم المندوب المستلم للعينات"
                                    return@Button
                                }
                                if (orderItems.isEmpty()) {
                                    errorMessage = "قائمة العينات فارغة"
                                    return@Button
                                }

                                viewModel.issueSampleOrder(
                                    representativeName = selectedRepName,
                                    representativeId = selectedRepId,
                                    supervisorName = supervisorName.ifBlank { "مشرف المستودع" },
                                    notes = notes,
                                    items = orderItems.toList(),
                                    onSuccess = {
                                        orderItems.clear()
                                        selectedRepName = ""
                                        selectedRepId = null
                                        notes = ""
                                        errorMessage = null
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("issue_samples_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("اعتماد وصرف العينات للمندوب", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        // --- 3. Previous Sample Vouchers History ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سجل وأوامر العينات السابقة (${sampleOrders.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = historySearchQuery,
                        onValueChange = { historySearchQuery = it },
                        label = { Text("بحث برقم السند أو اسم المندوب") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (filteredHistory.isEmpty()) {
                        Text(
                            text = "لا توجد أوامر عينات مسجلة حالياً",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        filteredHistory.forEach { order ->
                            SampleOrderHistoryCard(
                                order = order,
                                onViewVoucher = { viewModel.viewSampleVoucher(order) },
                                onRollbackOrder = { reason ->
                                    viewModel.rollbackSampleOrder(order.orderNumber, reason)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SampleOrderHistoryCard(
    order: SampleOrderEntity,
    onViewVoucher: () -> Unit,
    onRollbackOrder: (reason: String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar")) }
    val formattedDate = dateFormat.format(Date(order.timestamp))
    val isCancelled = order.status == "ملغى"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCancelled) Color(0xFFFEF2F2) else Color(0xFFFFF7ED)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCancelled) Color(0xFFFCA5A5) else Color(0xFFFFEDD5)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = order.orderNumber,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEA580C)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isCancelled) Color(0xFFEF4444) else Color(0xFFEA580C)
                    ) {
                        Text(
                            text = order.status,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "المندوب: ${order.representativeName}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Text(
                text = "الإجمالي: ${order.totalCartons} كرتون/عبوة و ${order.totalPieces} حبة",
                fontSize = 12.sp,
                color = Color(0xFFC2410C)
            )

            if (order.notes.isNotBlank()) {
                Text(
                    text = "ملاحظة: ${order.notes}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewVoucher,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("عرض السند", fontSize = 12.sp)
                }

                if (!isCancelled) {
                    OutlinedButton(
                        onClick = { onRollbackOrder("إلغاء بناء على طلب المستخدم") },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إلغاء الأمر", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
