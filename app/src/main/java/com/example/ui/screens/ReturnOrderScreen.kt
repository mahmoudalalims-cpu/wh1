package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Print
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
import com.example.data.model.ReturnOrderEntity
import com.example.data.model.OrderItem
import androidx.compose.runtime.mutableStateListOf
import com.example.ui.components.AdminPinDialog
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.MinimalistBluePrimary
import com.example.ui.theme.NavyPrimary
import com.example.viewmodel.WarehouseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.ui.components.AiVoucherScannerDialog
import androidx.compose.material.icons.filled.AutoAwesome

@Composable
fun ReturnOrderScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    var showAiScannerDialog by remember { mutableStateOf(false) }
    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    val issueOrders by viewModel.issueOrders.collectAsStateWithLifecycle()
    val returnOrders by viewModel.returnOrders.collectAsStateWithLifecycle()
    val representatives by viewModel.allRepresentatives.collectAsStateWithLifecycle()

    var orderToCancel by remember { mutableStateOf<ReturnOrderEntity?>(null) }

    var relatedIssueOrder by remember { mutableStateOf("") }
    var issueDropdownExpanded by remember { mutableStateOf(false) }

    val orderItems = remember { mutableStateListOf<OrderItem>() }

    var selectedItem by remember { mutableStateOf<ItemEntity?>(null) }
    var itemDropdownExpanded by remember { mutableStateOf(false) }

    var cartonsInput by remember { mutableStateOf("1") }
    var piecesInput by remember { mutableStateOf("0") }
    var lineNotesInput by remember { mutableStateOf("") }
    var lineError by remember { mutableStateOf<String?>(null) }

    val savedStorekeeperName by viewModel.storekeeperName.collectAsStateWithLifecycle()
    var selectedReason by remember { mutableStateOf("فائض طلبية") }
    var customReason by remember { mutableStateOf("") }
    var supervisorName by remember(savedStorekeeperName) { mutableStateOf(savedStorekeeperName) }
    var repDropdownExpanded by remember { mutableStateOf(false) }
    var returnNotes by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    val reasons = listOf("فائض طلبية", "خطأ في الصرف", "رفض العميل", "استرجاع من فرع", "أخرى")

    val cartons = cartonsInput.toIntOrNull() ?: 0
    val pieces = piecesInput.toIntOrNull() ?: 0
    val factor = selectedItem?.conversionFactor ?: 1
    val calculatedLinePieces = (cartons * factor) + pieces

    val totalCartons = orderItems.sumOf { it.cartons }
    val totalPieces = orderItems.sumOf { it.pieces }

    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("return_order_screen")
    ) {
        // Banner
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF0FDF4),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
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
                                imageVector = Icons.Default.AssignmentReturn,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "تسجيل أمر تفريغ / استرجاع بضاعة",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534)
                        )
                        Text(
                            text = "إعادة إضافة الكميات تلقائياً إلى رصيد المخزون",
                            fontSize = 10.5.sp,
                            color = Color(0xFF15803D)
                        )
                    }
                }

                Button(
                    onClick = { showAiScannerDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مسح أمر تفريغ بالذكاء", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Order General Details Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "البيانات العامة للاسترجاع",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Related Issue Order
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = relatedIssueOrder,
                        onValueChange = { relatedIssueOrder = it },
                        label = { Text("أمر الصرف المرتبط (اختياري)") },
                        placeholder = { Text("ربط اختياري بأمر صرف سابق...") },
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = MinimalistBluePrimary) },
                        trailingIcon = {
                            IconButton(onClick = { issueDropdownExpanded = !issueDropdownExpanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "قائمة أوامر الصرف")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    DropdownMenu(
                        expanded = issueDropdownExpanded,
                        onDismissRequest = { issueDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        issueOrders.take(10).forEach { order ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(order.orderNumber, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                        Text("إلى: ${order.destination}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    relatedIssueOrder = order.orderNumber
                                    issueDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // Reason Selection Chips
                Text("سبب الاسترجاع:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    reasons.take(3).forEach { r ->
                        val isSel = selectedReason == r
                        Surface(
                            onClick = { selectedReason = r },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSel) EmeraldTertiary else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) EmeraldTertiary else Color(0xFFCBD5E1))
                        ) {
                            Text(
                                text = r,
                                fontSize = 11.sp,
                                color = if (isSel) Color.White else Color(0xFF334155),
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    reasons.drop(3).forEach { r ->
                        val isSel = selectedReason == r
                        Surface(
                            onClick = { selectedReason = r },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSel) EmeraldTertiary else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) EmeraldTertiary else Color(0xFFCBD5E1))
                        ) {
                            Text(
                                text = r,
                                fontSize = 11.sp,
                                color = if (isSel) Color.White else Color(0xFF334155),
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                if (selectedReason == "أخرى") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        label = { Text("اذكر سبب الاسترجاع") },
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = supervisorName,
                        onValueChange = { supervisorName = it },
                        label = { Text("المندوب المرجع / مسؤول الاستلام") },
                        placeholder = { Text("اختر من قائمة المناديب أو اكتب اسماً...") },
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        trailingIcon = {
                            IconButton(onClick = { repDropdownExpanded = !repDropdownExpanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "قائمة المناديب")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    DropdownMenu(
                        expanded = repDropdownExpanded,
                        onDismissRequest = { repDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        representatives.forEach { rep ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(rep.name, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                        Text("مندوب ${rep.routeOrArea} | ${rep.vehicleNumber}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    supervisorName = "${rep.name} (مندوب)"
                                    repDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                if (representatives.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("مناديب مسجلون:", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        representatives.take(3).forEach { rep ->
                            val isSel = supervisorName.contains(rep.name)
                            Surface(
                                onClick = { supervisorName = "${rep.name} (مندوب)" },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) EmeraldTertiary else Color(0xFFF1F5F9),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.5.dp,
                                    if (isSel) EmeraldTertiary else Color(0xFFCBD5E1)
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
                    value = returnNotes,
                    onValueChange = { returnNotes = it },
                    label = { Text("ملاحظات إضافية") },
                    shape = RoundedCornerShape(12.dp),
                    colors = warehouseTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Order Items List
        if (orderItems.isNotEmpty()) {
            Text(
                text = "الأصناف المضافة للاسترجاع (${orderItems.size}):",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            orderItems.forEachIndexed { index, item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.itemName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text(
                                text = "الكمية: ${item.formatQuantity()}",
                                fontSize = 12.sp, color = EmeraldTertiary, fontWeight = FontWeight.SemiBold
                            )
                            if (item.notes.isNotBlank()) {
                                Text(
                                    text = "ملاحظة: ${item.notes}",
                                    fontSize = 11.sp, color = Color(0xFF64748B)
                                )
                            }
                        }
                        IconButton(onClick = { orderItems.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFDC2626))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF0FDF4),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("إجمالي الكمية المسترجعة:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldTertiary)
                    Text("$totalCartons كرتون و $totalPieces حبة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldTertiary)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Add New Item Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "إضافة صنف للاسترجاع",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedItem?.let { "[${it.code}] ${it.name}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("الصنف المسترجع") },
                        placeholder = { Text("اضغط لاختيار الصنف من الدليل...") },
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        trailingIcon = {
                            IconButton(onClick = { itemDropdownExpanded = !itemDropdownExpanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "قائمة الأصناف")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Transparent box to capture clicks over the text field
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { itemDropdownExpanded = !itemDropdownExpanded }
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
                                        Text(item.name, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                        Text("الكود: ${item.code} | التعبئة: ${item.conversionFactor}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    selectedItem = item
                                    itemDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cartonsInput,
                        onValueChange = { cartonsInput = it },
                        label = { Text("الكمية المسترجعة (كرتون)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("return_cartons_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = piecesInput,
                        onValueChange = { piecesInput = it },
                        label = { Text("الكمية المسترجعة (حبة)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("return_pieces_input"),
                        singleLine = true
                    )
                }

                // Expression Preview
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFDCFCE7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("إجمالي الكمية المضافة للمخزون بالحبة:", fontSize = 11.sp, color = Color(0xFF166534))
                        Text("$calculatedLinePieces حبة", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                    }
                }

                if (lineError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = lineError ?: "", color = Color(0xFFDC2626), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val inputCartons = cartonsInput.toIntOrNull() ?: 0
                        val inputPieces = piecesInput.toIntOrNull() ?: 0
                        
                        if (selectedItem == null) {
                            lineError = "الرجاء اختيار الصنف المسترجع"
                            return@OutlinedButton
                        }
                        if (calculatedLinePieces <= 0) {
                            lineError = "الرجاء تحديد كمية صالحة للاسترجاع"
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = EmeraldTertiary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldTertiary)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إضافة الصنف للقائمة", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        
        if (formError != null) {
            Text(text = formError ?: "", color = Color(0xFFDC2626), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Confirm Button
        Button(
            onClick = {
                if (orderItems.isEmpty()) {
                    formError = "الرجاء إضافة صنف واحد على الأقل للاسترجاع"
                    return@Button
                }

                val finalReason = if (selectedReason == "أخرى") customReason.ifBlank { "أسباب أخرى" } else selectedReason

                viewModel.submitReturnOrder(
                    relatedIssueOrderNumber = relatedIssueOrder.takeIf { it.isNotBlank() },
                    items = orderItems.toList(),
                    reason = finalReason,
                    supervisorName = supervisorName.ifBlank { "مسؤول المستودع" },
                    notes = returnNotes,
                    onSuccess = {
                        // Reset form
                        orderItems.clear()
                        relatedIssueOrder = ""
                        returnNotes = ""
                        customReason = ""
                        formError = null
                    }
                )
            },
            enabled = orderItems.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("confirm_return_order_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MinimalistBluePrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("اعتماد أمر الاسترجاع وإضافة للرصيد", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
        // Recent Return Orders List
        if (returnOrders.isNotEmpty()) {
            Text(
                text = "سجل أوامر الاسترجاع السابقة (${returnOrders.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF166534)
            )

            Spacer(modifier = Modifier.height(8.dp))

            returnOrders.forEach { ret ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .testTag("return_order_card_${ret.orderNumber}"),
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
                                color = Color(0xFFF0FDF4)
                            ) {
                                Text(
                                    text = ret.orderNumber,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldTertiary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(dateFormat.format(Date(ret.timestamp)), fontSize = 11.sp, color = Color(0xFF64748B))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("أصناف متعددة (${com.example.util.VoucherExporter.parseOrderItems(ret.itemsJson).size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text(
                            text = "الكمية المسترجعة: ${ret.totalCartons} كرتون و ${ret.totalPieces} حبة | السبب: ${ret.reason}",
                            fontSize = 12.sp,
                            color = Color(0xFF166534),
                            fontWeight = FontWeight.SemiBold
                        )
                        if (ret.relatedIssueOrderNumber != null) {
                            Text(
                                text = "مرتبط بأمر الصرف: ${ret.relatedIssueOrderNumber}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.showReturnVoucher(ret) },
                                shape = RoundedCornerShape(10.dp),
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    contentColor = EmeraldTertiary
                                )
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("عرض السند وطباعته", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { orderToCancel = ret },
                                shape = RoundedCornerShape(10.dp),
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFDC2626)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إلغاء الاسترجاع 🔒", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Admin PIN confirmation dialog for Return Order cancellation
    orderToCancel?.let { order ->
        AdminPinDialog(
            title = "تأكيد إلغاء أمر الاسترجاع ${order.orderNumber}",
            description = "تنبيه أمني: إلغاء هذا السند سيقوم بخصم الكميات المسترجعة (${order.totalCartons} كرتون و ${order.totalPieces} حبة) من رصيد المستودع وتوثيق العملية في سجل التدقيق.",
            confirmButtonText = "إلغاء الاسترجاع وخصم الرصيد",
            onDismiss = { orderToCancel = null },
            onConfirm = { pin ->
                if (viewModel.securityManager.verifyPin(pin)) {
                    viewModel.cancelReturnOrderProtected(order.orderNumber, pin) {
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
