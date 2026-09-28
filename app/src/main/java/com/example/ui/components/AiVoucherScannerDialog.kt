package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.data.ai.GeminiVoucherScanner
import com.example.data.ai.ScannedVoucherItem
import com.example.data.ai.ScannedVoucherResult
import com.example.data.model.ItemEntity
import com.example.data.model.OrderItem
import com.example.ui.theme.MinimalistBluePrimary
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.warehouseTextFieldColors
import com.example.viewmodel.WarehouseViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AiVoucherScannerDialog(
    viewModel: WarehouseViewModel,
    initialImageUri: Uri? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    val savedStorekeeperName by viewModel.storekeeperName.collectAsStateWithLifecycle()
    val savedDefaultRep by viewModel.defaultRepresentativeName.collectAsStateWithLifecycle()

    var selectedImageUri by remember { mutableStateOf<Uri?>(initialImageUri) }
    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisErrorMessage by remember { mutableStateOf<String?>(null) }

    var scannedResult by remember { mutableStateOf<ScannedVoucherResult?>(null) }

    // Editable result form state
    var documentType by remember { mutableStateOf("امر_تحميل") } // "امر_تحميل" (Issue) or "امر_تفريغ" (Return)
    var orderNumber by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf("") }
    var supervisorName by remember { mutableStateOf(savedStorekeeperName) }
    var scanNotes by remember { mutableStateOf("") }

    val editableItems = remember { mutableStateListOf<ScannedVoucherItem>() }

    // Media launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            scannedResult = null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraPhotoUri != null) {
            selectedImageUri = cameraPhotoUri
            scannedResult = null
        }
    }

    fun launchCamera() {
        try {
            val photoFile = File.createTempFile("voucher_scan_", ".jpg", context.cacheDir)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraPhotoUri = uri
            cameraLauncher.launch(uri)
        } catch (_: Exception) {
            analysisErrorMessage = "تعذر فتح الكاميرا"
        }
    }

    fun analyzeImageNow(uri: Uri) {
        isAnalyzing = true
        analysisErrorMessage = null
        scope.launch {
            val result = GeminiVoucherScanner.scanVoucherImage(context, uri, allItems)
            result.onSuccess { data ->
                scannedResult = data
                documentType = data.documentType
                orderNumber = data.orderNumber
                recipientName = data.recipientOrDriver.ifBlank { savedDefaultRep }
                supervisorName = data.supervisorName.ifBlank { savedStorekeeperName }
                scanNotes = data.notes

                editableItems.clear()
                editableItems.addAll(data.items)
            }.onFailure { err ->
                analysisErrorMessage = "خطأ في قراءة الصورة: ${err.localizedMessage}"
            }
            isAnalyzing = false
        }
    }

    // Auto trigger analysis if initial image passed
    remember(initialImageUri) {
        if (initialImageUri != null && scannedResult == null && !isAnalyzing) {
            analyzeImageNow(initialImageUri)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
                .testTag("ai_voucher_scanner_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Top Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MinimalistBluePrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "الماكينة الذكية لخصم وتفريغ المخزون (AI OCR)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "تحليل صور أوامر التحميل والتفريغ وتطبيقها فورياً بالمخزون",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Image Picker & Source Selector Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "1. التقاط أو اختيار صورة أمر التحميل / التفريغ:",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { launchCamera() },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MinimalistBluePrimary),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("ai_camera_btn")
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("تصوير كاميرا", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { galleryLauncher.launch("image/*") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("ai_gallery_btn")
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("معرض الصور", fontSize = 11.5.sp, color = Color(0xFF0F172A))
                                    }
                                }

                                // Image Preview Box
                                selectedImageUri?.let { uri ->
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.Black),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(uri),
                                            contentDescription = "صورة أمر التحميل/التفريغ",
                                            modifier = Modifier.fillMaxWidth(),
                                            contentScale = ContentScale.Fit
                                        )

                                        Button(
                                            onClick = { analyzeImageNow(uri) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                            shape = RoundedCornerShape(20.dp),
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(8.dp)
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("تحليل الصورة الآن", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Loading State Banner
                    if (isAnalyzing) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = MinimalistBluePrimary,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                    Column {
                                        Text(
                                            text = "جاري فحص وقراءة الصورة بالذكاء الاصطناعي (Gemini Vision)...",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E3A8A)
                                        )
                                        Text(
                                            text = "استخراج نوع المستند والأصناف والكميات ومطابقتها مع المستودع",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF3B82F6)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (analysisErrorMessage != null) {
                        item {
                            Text(
                                text = analysisErrorMessage ?: "",
                                color = Color(0xFFDC2626),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Scanned Voucher Form Review Section
                    if (scannedResult != null && !isAnalyzing) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (documentType == "امر_تحميل") Color(0xFFFFF1F2) else Color(0xFFF0FDF4)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (documentType == "امر_تحميل") Color(0xFFFECDD3) else Color(0xFFBBF7D0)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "2. نتائج الفحص الضوئي وتأكيد نوع السند:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )

                                    // Document Type Selector (Loading / Unloading)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            onClick = { documentType = "امر_تحميل" },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (documentType == "امر_تحميل") Color(0xFFE11D48) else Color.White,
                                            border = BorderStroke(1.dp, Color(0xFFFDA4AF)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocalShipping,
                                                    contentDescription = null,
                                                    tint = if (documentType == "امر_تحميل") Color.White else Color(0xFFE11D48),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "أمر تحميل (خصم)",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (documentType == "امر_تحميل") Color.White else Color(0xFF9F1239)
                                                )
                                            }
                                        }

                                        Surface(
                                            onClick = { documentType = "امر_تفريغ" },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (documentType == "امر_تفريغ") Color(0xFF16A34A) else Color.White,
                                            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Inventory2,
                                                    contentDescription = null,
                                                    tint = if (documentType == "امر_تفريغ") Color.White else Color(0xFF16A34A),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "أمر تفريغ (إضافة)",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (documentType == "امر_تفريغ") Color.White else Color(0xFF14532D)
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = orderNumber,
                                            onValueChange = { orderNumber = it },
                                            label = { Text("رقم الأمر / السند") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = warehouseTextFieldColors()
                                        )

                                        OutlinedTextField(
                                            value = recipientName,
                                            onValueChange = { recipientName = it },
                                            label = { Text("المندوب / المستلم") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1.2f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = warehouseTextFieldColors()
                                        )
                                    }

                                    OutlinedTextField(
                                        value = supervisorName,
                                        onValueChange = { supervisorName = it },
                                        label = { Text("أمين المستودع / المشرف") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = warehouseTextFieldColors()
                                    )
                                }
                            }
                        }

                        // Items list title
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "3. الأصناف المستخرجة لمطابقتها مع المخزون (${editableItems.size} أصناف):",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )

                                IconButton(
                                    onClick = {
                                        // Add new blank item line
                                        editableItems.add(
                                            ScannedVoucherItem(
                                                itemCode = "",
                                                itemName = "صنف جديد",
                                                cartons = 1,
                                                pieces = 0
                                            )
                                        )
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "إضافة صنف", tint = MinimalistBluePrimary)
                                }
                            }
                        }

                        // Render each item line in table
                        itemsIndexed(editableItems) { index, item ->
                            val matchedDbItem = item.matchedDbItem ?: allItems.firstOrNull { it.code == item.itemCode }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFE2E8F0)
                                            ) {
                                                Text(
                                                    text = item.itemCode.ifBlank { "#${index + 1}" },
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF334155),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            Text(
                                                text = matchedDbItem?.name ?: item.itemName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A),
                                                maxLines = 1
                                            )
                                        }

                                        IconButton(
                                            onClick = { editableItems.removeAt(index) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف الصنف", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = item.cartons.toString(),
                                            onValueChange = { newVal ->
                                                val valInt = newVal.toIntOrNull() ?: 0
                                                editableItems[index] = item.copy(cartons = valInt)
                                            },
                                            label = { Text("عدد الكراتين", fontSize = 10.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = warehouseTextFieldColors()
                                        )

                                        OutlinedTextField(
                                            value = item.pieces.toString(),
                                            onValueChange = { newVal ->
                                                val valInt = newVal.toIntOrNull() ?: 0
                                                editableItems[index] = item.copy(pieces = valInt)
                                            },
                                            label = { Text("عدد الحبات", fontSize = 10.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = warehouseTextFieldColors()
                                        )
                                    }

                                    if (matchedDbItem != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "الرصيد الحالي بالمستودع: ${matchedDbItem.currentCartons} كرتون و ${matchedDbItem.remainingPieces} حبة",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF059669),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "تنبيه: صنف جديد غير مسجل بكود مستودعي دقيق",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons: Approve Stock Deduction or Addition
                if (scannedResult != null && !isAnalyzing) {
                    Button(
                        onClick = {
                            val orderItemsList = editableItems.map { scannedItem ->
                                val matched = scannedItem.matchedDbItem ?: allItems.firstOrNull { it.code == scannedItem.itemCode }
                                OrderItem(
                                    itemCode = matched?.code ?: scannedItem.itemCode.ifBlank { "UNC" },
                                    itemName = matched?.name ?: scannedItem.itemName,
                                    cartons = scannedItem.cartons,
                                    pieces = scannedItem.pieces,
                                    conversionFactor = matched?.conversionFactor ?: 1,
                                    price = matched?.pricePerCarton ?: 0.0,
                                    notes = scannedItem.notes
                                )
                            }.filter { it.cartons > 0 || it.pieces > 0 }

                            if (orderItemsList.isEmpty()) {
                                analysisErrorMessage = "يجب تسجيل كمية صنف واحد على الأقل"
                                return@Button
                            }

                            if (documentType == "امر_تحميل") {
                                // Process Stock Issue / Deduction
                                viewModel.submitIssueOrder(
                                    destination = "تحميل للمندوب: $recipientName",
                                    recipientName = recipientName,
                                    supervisorName = supervisorName,
                                    notes = "أمر تحميل مخصوم عبر ماسح الذكاء الاصطناعي - رقم $orderNumber",
                                    items = orderItemsList,
                                    onSuccess = {
                                        onDismiss()
                                    }
                                )
                            } else {
                                // Process Stock Return / Receipt
                                viewModel.submitReturnOrder(
                                    relatedIssueOrderNumber = orderNumber,
                                    items = orderItemsList,
                                    reason = "تفريغ حمولة مسترجعة",
                                    supervisorName = supervisorName,
                                    notes = "أمر تفريغ مضاف للمخزون عبر الذكاء الاصطناعي - رقم $orderNumber",
                                    onSuccess = {
                                        onDismiss()
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("ai_approve_stock_action_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (documentType == "امر_تحميل") Color(0xFFE11D48) else Color(0xFF16A34A)
                        )
                    ) {
                        Icon(
                            imageVector = if (documentType == "امر_تحميل") Icons.Default.LocalShipping else Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (documentType == "امر_تحميل") "اعتماد خصم الكميات فورياً من المخزون" else "اعتماد إضافة وإرجاع الكميات للمخزون",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
