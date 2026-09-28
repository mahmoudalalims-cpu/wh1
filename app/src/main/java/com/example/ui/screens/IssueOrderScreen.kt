package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.ui.theme.warehouseTextFieldColors
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
import com.example.data.model.IssueOrderEntity
import com.example.data.model.ItemEntity
import com.example.data.model.OrderItem
import com.example.ui.components.AdminPinDialog
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.MinimalistBlueContainer
import com.example.ui.theme.MinimalistBluePrimary
import com.example.ui.theme.MinimalistOnBlueContainer
import com.example.ui.theme.NavyPrimary
import com.example.viewmodel.WarehouseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.ui.components.AiVoucherScannerDialog
import androidx.compose.material.icons.filled.AutoAwesome

@Composable
fun IssueOrderScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    var showAiScannerDialog by remember { mutableStateOf(false) }
    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    val issueOrders by viewModel.issueOrders.collectAsStateWithLifecycle()
    val representatives by viewModel.allRepresentatives.collectAsStateWithLifecycle()

    var orderToCancel by remember { mutableStateOf<IssueOrderEntity?>(null) }
    val orderDateFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar")) }

    val savedStorekeeperName by viewModel.storekeeperName.collectAsStateWithLifecycle()
    var destination by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf("") }
    var supervisorName by remember(savedStorekeeperName) { mutableStateOf(savedStorekeeperName) }
    var orderNotes by remember { mutableStateOf("") }
    var repDropdownExpanded by remember { mutableStateOf(false) }

    val orderItems = remember { mutableStateListOf<OrderItem>() }

    // Line input states
    var selectedItem by remember { mutableStateOf<ItemEntity?>(null) }
    var itemDropdownExpanded by remember { mutableStateOf(false) }
    var cartonsInput by remember { mutableStateOf("1") }
    var piecesInput by remember { mutableStateOf("0") }
    var lineNotesInput by remember { mutableStateOf("") }
    var lineError by remember { mutableStateOf<String?>(null) }

    // Pre-calculate line total pieces (Expression: Cartons * Conversion + Pieces)
    val inputCartons = cartonsInput.toIntOrNull() ?: 0
    val inputPieces = piecesInput.toIntOrNull() ?: 0
    val lineFactor = selectedItem?.conversionFactor ?: 1
    val calculatedLinePieces = (inputCartons * lineFactor) + inputPieces

    val totalCartons = orderItems.sumOf { it.cartons }
    val totalPieces = orderItems.sumOf { it.pieces }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("issue_order_screen")
    ) {
        // Title & Header Banner
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MinimalistBlueContainer,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MinimalistBluePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "إنشاء أمر صرف مخزني جديد",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalistOnBlueContainer
                        )
                        Text(
                            text = "خصم تلقائي للكميات وإصدار سند صرف رسمية",
                            fontSize = 10.5.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }

                Button(
                    onClick = { showAiScannerDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MinimalistBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مسح أمر تحميل بالذكاء", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Order Details Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "بيانات أمر الصرف",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("جهة الصرف (عميل / قسم / فرع)") },
                    placeholder = { Text("مثال: فرع الرياض الشمالي / شركة التوزيع") },
                    shape = RoundedCornerShape(12.dp),
                    colors = warehouseTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("issue_destination_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = recipientName,
                            onValueChange = { recipientName = it },
                            label = { Text("المندوب أو المستلم المفوض") },
                            placeholder = { Text("اختر مناديب مسجلين أو اكتب اسماً...") },
                            shape = RoundedCornerShape(12.dp),
                            colors = warehouseTextFieldColors(),
                            trailingIcon = {
                                IconButton(onClick = { repDropdownExpanded = !repDropdownExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "قائمة المناديب المسجلين")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("issue_recipient_input"),
                            singleLine = true
                        )

                        DropdownMenu(
                            expanded = repDropdownExpanded,
                            onDismissRequest = { repDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            if (representatives.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("لا يوجد مناديب مسجلين بعد", color = Color.Gray, fontSize = 12.sp) },
                                    onClick = { repDropdownExpanded = false }
                                )
                            } else {
                                representatives.forEach { rep ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(rep.name, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                                val subText = listOfNotNull(
                                                    if (rep.code.isNotBlank()) "كود: ${rep.code}" else null,
                                                    if (rep.vehicleNumber.isNotBlank()) "لوحة: ${rep.vehicleNumber}" else null,
                                                    if (rep.routeOrArea.isNotBlank()) "خط: ${rep.routeOrArea}" else null
                                                ).joinToString(" | ")
                                                if (subText.isNotBlank()) {
                                                    Text(subText, fontSize = 10.sp, color = Color(0xFF64748B))
                                                }
                                            }
                                        },
                                        onClick = {
                                            recipientName = rep.name
                                            if (destination.isBlank() && rep.routeOrArea.isNotBlank()) {
                                                destination = rep.routeOrArea
                                            }
                                            repDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = supervisorName,
                        onValueChange = { supervisorName = it },
                        label = { Text("مسؤول الصرف") },
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("issue_supervisor_input"),
                        singleLine = true
                    )
                }

                // Quick Select Representative Chips
                if (representatives.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("اختر مندوب مسجل:", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        representatives.take(3).forEach { rep ->
                            val isSel = recipientName == rep.name
                            Surface(
                                onClick = {
                                    recipientName = rep.name
                                    if (destination.isBlank() && rep.routeOrArea.isNotBlank()) {
                                        destination = rep.routeOrArea
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) MinimalistBluePrimary else Color(0xFFF1F5F9),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.5.dp,
                                    if (isSel) MinimalistBluePrimary else Color(0xFFCBD5E1)
                                )
                            ) {
                                Text(
                                    text = rep.name.split(" ").take(2).joinToString(" "),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = orderNotes,
                    onValueChange = { orderNotes = it },
                    label = { Text("ملاحظات عامة على الأمر (اختياري)") },
                    shape = RoundedCornerShape(12.dp),
                    colors = warehouseTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Items Picker Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "إضافة أصناف لأمر الصرف",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Item Selector Box
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedItem?.let { "[${it.code}] ${it.name}" } ?: "اختر الصنف من المخزون...",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("الصنف الغذائي") },
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "قائمة الأصناف")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { itemDropdownExpanded = true }
                            .testTag("select_item_dropdown"),
                        enabled = false,
                        colors = warehouseTextFieldColors(
                            textColor = if (selectedItem != null) null else Color(0xFF64748B)
                        )
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { itemDropdownExpanded = true }
                    )

                    DropdownMenu(
                        expanded = itemDropdownExpanded,
                        onDismissRequest = { itemDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        allItems.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "[${item.code}] ${item.name}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "المتاح: ${item.currentCartons} كرتون و ${item.remainingPieces} حبة (${item.totalPieces} حبة) | المعامل: ${item.conversionFactor}",
                                            fontSize = 11.sp,
                                            color = if (item.isLowStock) Color(0xFFDC2626) else Color(0xFF64748B)
                                        )
                                    }
                                },
                                onClick = {
                                    selectedItem = item
                                    itemDropdownExpanded = false
                                    lineError = null
                                }
                            )
                        }
                    }
                }

                // Selected Item Stock Indicator
                if (selectedItem != null) {
                    val itm = selectedItem!!
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (itm.isLowStock) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المتوفر في المستودع: ${itm.currentCartons} كرتون و ${itm.remainingPieces} حبة",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (itm.isLowStock) Color(0xFFDC2626) else Color(0xFF166534)
                            )
                            Text(
                                text = "معامل: ${itm.conversionFactor} حبة/كرتون",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quantity inputs: Cartons and Pieces
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cartonsInput,
                        onValueChange = {
                            cartonsInput = it
                            lineError = null
                        },
                        label = { Text("الكمية (كرتون)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("issue_cartons_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = piecesInput,
                        onValueChange = {
                            piecesInput = it
                            lineError = null
                        },
                        label = { Text("الكمية (حبة)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("issue_pieces_input"),
                        singleLine = true
                    )
                }

                // Dynamic Expression Calculation Box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الإجمالي المحسوب بالحبة (الكرتون × المعامل + حبات):",
                            fontSize = 11.sp,
                            color = Color(0xFF78350F)
                        )
                        Text(
                            text = "$calculatedLinePieces حبة",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF78350F)
                        )
                    }
                }

                OutlinedTextField(
                    value = lineNotesInput,
                    onValueChange = { lineNotesInput = it },
                    label = { Text("ملاحظة على هذا الصنف (اختياري)") },
                    shape = RoundedCornerShape(12.dp),
                    colors = warehouseTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (lineError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = lineError ?: "",
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        if (selectedItem == null) {
                            lineError = "الرجاء اختيار صنف أولاً"
                            return@OutlinedButton
                        }
                        if (calculatedLinePieces <= 0) {
                            lineError = "الرجاء تحديد كمية صالحة للصرف"
                            return@OutlinedButton
                        }
                        if (selectedItem!!.totalPieces < calculatedLinePieces) {
                            lineError = "الكمية المطلوبة ($calculatedLinePieces حبة) تتجاوز الرصيد المتوفر (${selectedItem!!.totalPieces} حبة)"
                            return@OutlinedButton
                        }

                        orderItems.add(
                            OrderItem(
                                itemCode = selectedItem!!.code,
                                itemName = selectedItem!!.name,
                                cartons = inputCartons,
                                pieces = inputPieces,
                                conversionFactor = selectedItem!!.conversionFactor,
                                price = selectedItem!!.pricePerCarton,
                                notes = lineNotesInput
                            )
                        )

                        // Reset line inputs
                        selectedItem = null
                        cartonsInput = "1"
                        piecesInput = "0"
                        lineNotesInput = ""
                        lineError = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_item_to_order_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إضافة الصنف للقائمة")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Items List Table
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "قائمة الأصناف المراد صرفها (${orderItems.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (orderItems.isEmpty()) {
                    Text(
                        text = "لم يتم إضافة أي صنف بعد. اختر صنفاً وأضفه من النموذج أعلاه.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    orderItems.forEachIndexed { idx, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${idx + 1}. [${item.itemCode}] ${item.itemName}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "الكمية: ${item.formatQuantity()} (إجمالي ${item.totalPieces} حبة)",
                                    fontSize = 12.sp,
                                    color = NavyPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                if (item.notes.isNotBlank()) {
                                    Text(
                                        text = "ملاحظة: ${item.notes}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                            IconButton(onClick = { orderItems.removeAt(idx) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFDC2626))
                            }
                        }
                        if (idx < orderItems.size - 1) {
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الإجمالي الكلي للأمر:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("$totalCartons كرتون و $totalPieces حبة", fontWeight = FontWeight.Bold, color = NavyPrimary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Confirm & Submit Button
        Button(
            onClick = {
                viewModel.submitIssueOrder(
                    destination = destination.ifBlank { "عميل عام" },
                    recipientName = recipientName.ifBlank { "المستلم المفوض" },
                    supervisorName = supervisorName.ifBlank { "مسؤول المستودع" },
                    notes = orderNotes,
                    items = orderItems.toList(),
                    onSuccess = {
                        // Clear form
                        destination = ""
                        recipientName = ""
                        orderNotes = ""
                        orderItems.clear()
                    }
                )
            },
            enabled = orderItems.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("confirm_issue_order_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MinimalistBluePrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "اعتماد أمر الصرف وتوليد السند الرسمي",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Recent Final Issue Orders List
        if (issueOrders.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "أوامر الصرف والتحميل النهائية المعتمدة (${issueOrders.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFF6FF)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MinimalistBluePrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "محمية برمز المشرف",
                            fontSize = 11.sp,
                            color = MinimalistBluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            issueOrders.forEach { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .testTag("issue_order_card_${order.orderNumber}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MinimalistBlueContainer
                            ) {
                                Text(
                                    text = order.orderNumber,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MinimalistOnBlueContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = orderDateFormat.format(Date(order.timestamp)),
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "جهة الصرف: ${order.destination}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "المستلم: ${order.recipientName} | المسؤول: ${order.supervisorName}",
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                        Text(
                            text = "الإجمالي المصروف: ${order.totalCartons} كرتون و ${order.totalPieces} حبة",
                            fontSize = 12.sp,
                            color = MinimalistBluePrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // View / Print Voucher Button
                            OutlinedButton(
                                onClick = { viewModel.showIssueVoucher(order) },
                                shape = RoundedCornerShape(10.dp),
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    contentColor = MinimalistBluePrimary
                                )
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("عرض السند وطباعته", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Secure Delete / Rollback Button
                            OutlinedButton(
                                onClick = { orderToCancel = order },
                                shape = RoundedCornerShape(10.dp),
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFDC2626)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إلغاء الأمر 🔒", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Admin PIN confirmation dialog for Issue Order cancellation
    orderToCancel?.let { order ->
        AdminPinDialog(
            title = "تأكيد إلغاء أمر الصرف ${order.orderNumber}",
            description = "تنبيه أمني: إلغاء هذا الأمر سيقوم بإعادة كافة الكميات المصروفة (${order.totalCartons} كرتون و ${order.totalPieces} حبة) إلى رصيد المستودع وتوثيق العملية في سجل التدقيق المالي.",
            confirmButtonText = "إلغاء الأمر وإعادة المخزون",
            onDismiss = { orderToCancel = null },
            onConfirm = { pin ->
                if (viewModel.securityManager.verifyPin(pin)) {
                    viewModel.cancelIssueOrderProtected(order.orderNumber, pin) {
                        orderToCancel = null
                    }
                    true
                } else {
                    false
                }
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
