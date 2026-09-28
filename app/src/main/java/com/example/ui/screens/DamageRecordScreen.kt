package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.DamageRecordEntity
import com.example.data.model.ItemEntity
import com.example.ui.components.AdminPinDialog
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.NavyPrimary
import com.example.util.ExcelExporter
import com.example.util.VoucherExporter
import com.example.viewmodel.WarehouseViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DamageRecordScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    val damageRecords by viewModel.damageRecords.collectAsStateWithLifecycle()
    val representatives by viewModel.allRepresentatives.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var recordToCancel by remember { mutableStateOf<DamageRecordEntity?>(null) }

    var selectedItem by remember { mutableStateOf<ItemEntity?>(null) }
    var itemDropdownExpanded by remember { mutableStateOf(false) }

    var cartonsInput by remember { mutableStateOf("0") }
    var piecesInput by remember { mutableStateOf("1") }
    val savedStorekeeperName by viewModel.storekeeperName.collectAsStateWithLifecycle()
    var selectedReason by remember { mutableStateOf("كسر أو تمزق عبوات") }
    var customReason by remember { mutableStateOf("") }
    var supervisorName by remember(savedStorekeeperName) { mutableStateOf(savedStorekeeperName) }
    var repDropdownExpanded by remember { mutableStateOf(false) }
    var damageNotes by remember { mutableStateOf("") }
    var attachedPhotoUriString by remember { mutableStateOf<String?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { inputUri ->
            try {
                val photosDir = File(context.filesDir, "damage_photos")
                if (!photosDir.exists()) photosDir.mkdirs()
                val destFile = File(photosDir, "dmg_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(inputUri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                attachedPhotoUriString = destFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { b ->
            try {
                val photosDir = File(context.filesDir, "damage_photos")
                if (!photosDir.exists()) photosDir.mkdirs()
                val destFile = File(photosDir, "dmg_cam_${System.currentTimeMillis()}.jpg")
                FileOutputStream(destFile).use { out ->
                    b.compress(Bitmap.CompressFormat.JPEG, 92, out)
                }
                attachedPhotoUriString = destFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val reasons = listOf("كسر أو تمزق عبوات", "انتهاء صلاحية", "سوء تخزين / حرارة", "رطوبة وتلف كرتون", "أخرى")

    val cartons = cartonsInput.toIntOrNull() ?: 0
    val pieces = piecesInput.toIntOrNull() ?: 0
    val factor = selectedItem?.conversionFactor ?: 1
    val calculatedDeductPieces = (cartons * factor) + pieces

    // Estimated financial loss
    val price = selectedItem?.pricePerCarton ?: 0.0
    val estimatedLoss = if (factor > 0) (calculatedDeductPieces.toDouble() / factor.toDouble()) * price else 0.0

    val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("damage_record_screen")
    ) {
        // Warning Banner
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFFEF2F2),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "تسجيل وتوثيق بضاعة تالفة",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF991B1B)
                    )
                    Text(
                        text = "تُخصم من المخزون وتُصنّف في التقارير كـ \"تالف\" وليس صرفاً",
                        fontSize = 11.sp,
                        color = Color(0xFFB91C1C)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "تفاصيل التلف",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Item Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedItem?.let { "[${it.code}] ${it.name}" } ?: "اختر الصنف المتضرر...",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("الصنف المتضرر") },
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { itemDropdownExpanded = true }
                            .testTag("select_damage_item_dropdown"),
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
                                        Text("[${item.code}] ${item.name}", fontWeight = FontWeight.Bold)
                                        Text("الرصيد المتاح: ${item.currentCartons} كرتون و ${item.remainingPieces} حبة", fontSize = 11.sp, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    selectedItem = item
                                    itemDropdownExpanded = false
                                    formError = null
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity inputs: Cartons & Pieces
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cartonsInput,
                        onValueChange = { cartonsInput = it },
                        label = { Text("الكمية التالفة (كرتون)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("damage_cartons_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = piecesInput,
                        onValueChange = { piecesInput = it },
                        label = { Text("الكمية التالفة (حبة)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("damage_pieces_input"),
                        singleLine = true
                    )
                }

                // Total Pieces and Loss Expression
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFFBEB),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("إجمالي التالف بالحبة:", fontSize = 11.sp, color = Color(0xFF92400E))
                            Text("$calculatedDeductPieces حبة", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                        }
                        if (price > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("القيمة التقديرية للخسارة:", fontSize = 11.sp, color = Color(0xFFB45309))
                                Text("%.2f ر.س".format(estimatedLoss), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Reason Selection Chips
                Text("سبب التلف:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
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
                            color = if (isSel) Color(0xFFDC2626) else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Color(0xFFDC2626) else Color(0xFFCBD5E1))
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
                            color = if (isSel) Color(0xFFDC2626) else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Color(0xFFDC2626) else Color(0xFFCBD5E1))
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
                        label = { Text("حدد سبب التلف بالتفصيل") },
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Photo Attachment Option with Image Picker & Camera
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (attachedPhotoUriString != null) Icons.Default.CheckCircle else Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = if (attachedPhotoUriString != null) Color(0xFF16A34A) else Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (attachedPhotoUriString != null) "تم إرفاق صورة الإثبات للمحضر ✓" else "توثيق وإرفاق صورة التالف:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (attachedPhotoUriString != null) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (attachedPhotoUriString != null) {
                                IconButton(
                                    onClick = { attachedPhotoUriString = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "حذف الصورة", tint = Color.Red)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { cameraLauncher.launch(null) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تصوير بالكاميرا", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("رفع من المعرض", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        attachedPhotoUriString?.let { photoPath ->
                            Spacer(modifier = Modifier.height(10.dp))
                            AsyncImage(
                                model = photoPath,
                                contentDescription = "معاينة صورة التالف",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = supervisorName,
                        onValueChange = { supervisorName = it },
                        label = { Text("المندوب المبلغ / مسؤول التوثيق") },
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
                                color = if (isSel) Color(0xFFEF4444) else Color(0xFFF1F5F9),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.5.dp,
                                    if (isSel) Color(0xFFEF4444) else Color(0xFFCBD5E1)
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
                    value = damageNotes,
                    onValueChange = { damageNotes = it },
                    label = { Text("ملاحظات إضافية وتوصيات المعاينة") },
                    shape = RoundedCornerShape(12.dp),
                    colors = warehouseTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (formError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = formError ?: "", color = Color(0xFFDC2626), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                if (selectedItem == null) {
                    formError = "الرجاء اختيار الصنف المتضرر"
                    return@Button
                }
                if (calculatedDeductPieces <= 0) {
                    formError = "الرجاء تحديد كمية تالفة صالحة"
                    return@Button
                }
                if (selectedItem!!.totalPieces < calculatedDeductPieces) {
                    formError = "الكمية التالفة ($calculatedDeductPieces حبة) أكبر من رصيد المستودع الحالي (${selectedItem!!.totalPieces} حبة)"
                    return@Button
                }

                val finalReason = if (selectedReason == "أخرى") customReason.ifBlank { "أسباب تلف أخرى" } else selectedReason

                viewModel.submitDamageRecord(
                    itemCode = selectedItem!!.code,
                    cartons = cartons,
                    pieces = pieces,
                    reason = finalReason,
                    supervisorName = supervisorName.ifBlank { "مسؤول المستودع" },
                    notes = damageNotes,
                    photoUri = attachedPhotoUriString,
                    onSuccess = {
                        selectedItem = null
                        cartonsInput = "0"
                        piecesInput = "1"
                        damageNotes = ""
                        attachedPhotoUriString = null
                        customReason = ""
                        formError = null
                    }
                )
            },
            enabled = selectedItem != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("confirm_damage_record_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("اعتماد محضر التالف وخصم المخزون", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Damage Records History
        if (damageRecords.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سجل التوالف والفاقد السابق (${damageRecords.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B)
                )

                Button(
                    onClick = {
                        ExcelExporter.exportDamageRecordsExcel(
                            context = context,
                            damageRecords = damageRecords,
                            storekeeperName = savedStorekeeperName
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تصدير Excel/شيت", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            damageRecords.forEach { rec ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .testTag("damage_record_card_${rec.damageNumber}"),
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
                                color = Color(0xFFFEF2F2)
                            ) {
                                Text(
                                    text = rec.damageNumber,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(dateFormat.format(Date(rec.timestamp)), fontSize = 11.sp, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("[${rec.itemCode}] ${rec.itemName}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text(
                            text = "الكمية التالفة: ${rec.cartons} كرتون و ${rec.pieces} حبة | السبب: ${rec.reason}",
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.SemiBold
                        )
                        if (rec.estimatedLossValue > 0) {
                            Text("الخسارة التقديرية: %.2f ر.س".format(rec.estimatedLossValue), fontSize = 11.sp, color = Color(0xFFB45309))
                        }
                        Text("مسؤول المعاينة: ${rec.supervisorName}", fontSize = 11.sp, color = Color(0xFF64748B))

                        if (rec.photoUri?.isNotBlank() == true) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF2F2),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = rec.photoUri,
                                        contentDescription = "صورة التالف",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("📸 صورة إثبات مرفقة للمحضر", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                        Text("مدرجة تلقائياً ضمن PDF السند النهائي", fontSize = 10.sp, color = Color(0xFF7F1D1D))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val html = VoucherExporter.generateDamageVoucherHtml(rec, context)
                                    VoucherExporter.printVoucherHtml(
                                        context = context,
                                        htmlContent = html,
                                        jobName = "محضر_إتلاف_${rec.damageNumber}"
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF991B1B)
                                )
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("طباعة PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    VoucherExporter.shareDamageRecordText(context, rec)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    contentColor = NavyPrimary
                                )
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("مشاركة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { recordToCancel = rec },
                                shape = RoundedCornerShape(10.dp),
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFDC2626)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("إلغاء 🔒", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Admin PIN confirmation dialog for Damage Record cancellation
    recordToCancel?.let { rec ->
        AdminPinDialog(
            title = "تأكيد إلغاء محضر التالف ${rec.damageNumber}",
            description = "تنبيه أمني: إلغاء هذا المحضر سيقوم بإعادة الكميات المشطوبة (${rec.cartons} كرتون و ${rec.pieces} حبة) إلى رصيد المستودع الصالح للاستخدام وتوثيق العملية في سجل التدقيق.",
            confirmButtonText = "إلغاء المحضر واسترداد الرصيد",
            onDismiss = { recordToCancel = null },
            onConfirm = { pin ->
                if (viewModel.securityManager.verifyPin(pin)) {
                    viewModel.cancelDamageRecordProtected(rec.damageNumber, pin) {
                        recordToCancel = null
                    }
                    true
                } else {
                    false
                }
            }
        )
    }
}
