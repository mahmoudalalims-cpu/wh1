import re

with open('app/src/main/java/com/example/ui/screens/ReturnOrderScreen.kt', 'r') as f:
    content = f.read()

start_str = """        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),"""
end_str = "        // Recent Return Orders List"

start_idx = content.find(start_str)
end_idx = content.find(end_str)

if start_idx != -1 and end_idx != -1:
    before = content[:start_idx]
    after = content[end_idx:]
    
    new_ui = """        // Order General Details Section
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
                            Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Color(0xFFDC2626))
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
"""

    with open('app/src/main/java/com/example/ui/screens/ReturnOrderScreen.kt', 'w') as f:
        f.write(before + new_ui + after)
else:
    print("Could not find start or end string.")
