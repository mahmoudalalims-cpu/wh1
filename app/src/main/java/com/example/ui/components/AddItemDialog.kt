package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ItemEntity
import com.example.ui.theme.NavyPrimary

@Composable
fun AddItemDialog(
    itemToEdit: ItemEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        code: String,
        name: String,
        category: String,
        conversionFactor: Int,
        cartons: Int,
        pieces: Int,
        minStock: Int,
        supplier: String,
        price: Double,
        packagingUnit: String,
        baseUnit: String
    ) -> Unit
) {
    val isEdit = itemToEdit != null

    var code by remember { mutableStateOf(itemToEdit?.code ?: "") }
    var name by remember { mutableStateOf(itemToEdit?.name ?: "") }
    var category by remember { mutableStateOf(itemToEdit?.category ?: "شبس وتسالي") }
    var packagingUnit by remember { mutableStateOf(itemToEdit?.packagingUnit ?: "كرتون") }
    var baseUnit by remember { mutableStateOf(itemToEdit?.baseUnit ?: "حبة") }
    var conversionFactorStr by remember { mutableStateOf(itemToEdit?.conversionFactor?.toString() ?: "24") }
    var cartonsStr by remember { mutableStateOf(itemToEdit?.currentCartons?.toString() ?: "0") }
    var piecesStr by remember { mutableStateOf(itemToEdit?.remainingPieces?.toString() ?: "0") }
    var minStockStr by remember { mutableStateOf(itemToEdit?.minStockCartons?.toString() ?: "15") }
    var supplier by remember { mutableStateOf(itemToEdit?.supplier ?: "مؤسسة آصرة العرب") }
    var priceStr by remember { mutableStateOf(itemToEdit?.pricePerCarton?.toString() ?: "0.0") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val conversionFactor = conversionFactorStr.toIntOrNull() ?: 1
    val cartons = cartonsStr.toIntOrNull() ?: 0
    val pieces = piecesStr.toIntOrNull() ?: 0
    val totalCalculatedPieces = (cartons * conversionFactor) + pieces

    val packagingOptions = listOf("كرتون", "كيس", "شدة", "حبة")
    val baseOptions = listOf("حبة", "قطعة", "كيس صغير", "علبة")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("add_item_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEdit) "تعديل بيانات الصنف" else "إضافة صنف مخزني جديد",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إلغاء")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Item Code & Name
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("كود الصنف (فريد)") },
                    placeholder = { Text("مثال: 216 أو 244 أو 511") },
                    enabled = !isEdit,
                    shape = RoundedCornerShape(12.dp),
                    colors = warehouseTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("item_code_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الصنف الغذائي") },
                    placeholder = { Text("مثال: MEGA CHIPS SEA SALT شيبس ملح البحر") },
                    shape = RoundedCornerShape(12.dp),
                    colors = warehouseTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("item_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category & Supplier
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("التصنيف / الفئة") },
                    placeholder = { Text("مثال: شبس وتسالي، فشار وبفك، بسكويت") },
                    shape = RoundedCornerShape(12.dp),
                    colors = warehouseTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("المورد المرتبط") },
                    placeholder = { Text("مؤسسة آصرة العرب / مصنع الأغذية") },
                    shape = RoundedCornerShape(12.dp),
                    colors = warehouseTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Packaging & Base Unit Selection (وحدات القياس والتعبئة)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "إعدادات وحدات القياس والتعبئة",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC2410C)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "وحدة التعبئة الكبرى (العبوة/الحزمة):",
                            fontSize = 11.sp,
                            color = Color(0xFF9A3412),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            packagingOptions.forEach { option ->
                                val selected = packagingUnit == option
                                Surface(
                                    selected = selected,
                                    onClick = { packagingUnit = option },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (selected) Color(0xFFEA580C) else Color.White,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (selected) Color(0xFFEA580C) else Color(0xFFCBD5E1)
                                    )
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) Color.White else Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "الوحدة الأساسية الصغرى (الفردية):",
                            fontSize = 11.sp,
                            color = Color(0xFF9A3412),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            baseOptions.forEach { option ->
                                val selected = baseUnit == option
                                Surface(
                                    selected = selected,
                                    onClick = { baseUnit = option },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (selected) Color(0xFFEA580C) else Color.White,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (selected) Color(0xFFEA580C) else Color(0xFFCBD5E1)
                                    )
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) Color.White else Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Conversion factor
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "معامل التحويل (وحدة التعبئة: $packagingUnit / الوحدة الأساسية: $baseUnit)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = conversionFactorStr,
                            onValueChange = { conversionFactorStr = it },
                            label = { Text("عدد $baseUnit داخل الـ $packagingUnit الواحد") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = warehouseTextFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stock inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cartonsStr,
                        onValueChange = { cartonsStr = it },
                        label = { Text("الرصيد بـ ($packagingUnit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = piecesStr,
                        onValueChange = { piecesStr = it },
                        label = { Text("الرصيد بـ ($baseUnit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Dynamic Expression Result
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("إجمالي الرصيد المحسوب بـ ($baseUnit):", fontSize = 12.sp, color = Color(0xFF78350F), fontWeight = FontWeight.SemiBold)
                        Text("$totalCalculatedPieces $baseUnit", fontSize = 14.sp, color = Color(0xFF78350F), fontWeight = FontWeight.Bold)
                    }
                }

                // Min stock alert & price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = minStockStr,
                        onValueChange = { minStockStr = it },
                        label = { Text("الحد الأدنى (كرتون)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("سعر الكرتون (اختياري)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = {
                            if (code.isBlank() || name.isBlank()) {
                                errorMessage = "الرجاء إدخال كود واسم الصنف"
                                return@Button
                            }
                            if (conversionFactor <= 0) {
                                errorMessage = "معامل التحويل يجب أن يكون أكبر من 0"
                                return@Button
                            }
                            onSave(
                                code,
                                name,
                                category.ifBlank { "عام" },
                                conversionFactor,
                                cartons,
                                pieces,
                                minStockStr.toIntOrNull() ?: 10,
                                supplier.ifBlank { "مؤسسة آصرة العرب" },
                                priceStr.toDoubleOrNull() ?: 0.0,
                                packagingUnit,
                                baseUnit
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_item_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text(if (isEdit) "حفظ التعديل" else "إضافة الصنف")
                    }
                }
            }
        }
    }
}
