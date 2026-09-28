package com.example.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ItemEntity
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.MinimalistAlertBadge
import com.example.ui.theme.MinimalistAlertContainer
import com.example.ui.theme.MinimalistBlueContainer
import com.example.ui.theme.MinimalistBluePrimary
import com.example.ui.theme.MinimalistOnBlueContainer
import com.example.ui.theme.NavyPrimary
import androidx.compose.material.icons.filled.TableChart
import com.example.util.ExcelExporter
import com.example.util.VoucherExporter
import com.example.viewmodel.WarehouseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    val lowStockItems by viewModel.lowStockItems.collectAsStateWithLifecycle()
    val allMovements by viewModel.allMovements.collectAsStateWithLifecycle()
    val damageRecords by viewModel.damageRecords.collectAsStateWithLifecycle()
    val issueOrders by viewModel.issueOrders.collectAsStateWithLifecycle()
    val returnOrders by viewModel.returnOrders.collectAsStateWithLifecycle()
    val sampleOrders by viewModel.sampleOrders.collectAsStateWithLifecycle()
    val representatives by viewModel.allRepresentatives.collectAsStateWithLifecycle()
    val storekeeperName by viewModel.storekeeperName.collectAsStateWithLifecycle()

    // Specific Item Tracer State
    var selectedItemForReport by remember { mutableStateOf<ItemEntity?>(null) }
    var itemDropdownExpanded by remember { mutableStateOf(false) }

    // Overall KPI metrics
    val totalWarehouseCartons = allItems.sumOf { it.currentCartons }
    val totalWarehousePieces = allItems.sumOf { it.remainingPieces }
    val totalWarehouseAllPieces = allItems.sumOf { it.totalPieces }

    val totalDamagedPieces = damageRecords.sumOf { (it.cartons * 24) + it.pieces }
    val totalDamageLossValue = damageRecords.sumOf { it.estimatedLossValue }

    // Item-specific stats
    val itemMovements = remember(selectedItemForReport, allMovements) {
        if (selectedItemForReport == null) emptyList()
        else allMovements.filter { it.itemCode == selectedItemForReport!!.code }
    }

    val itemIssuedPieces = remember(itemMovements) {
        itemMovements.filter { it.movementType.contains("صرف") }.sumOf { -it.totalPiecesDelta }
    }

    val itemReturnedPieces = remember(itemMovements) {
        itemMovements.filter { it.movementType.contains("استرجاع") }.sumOf { it.totalPiecesDelta }
    }

    val itemDamagedPieces = remember(itemMovements) {
        itemMovements.filter { it.movementType.contains("تالف") }.sumOf { -it.totalPiecesDelta }
    }

    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("reports_screen")
    ) {
        // Top Banner
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MinimalistBlueContainer,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
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
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = MinimalistBluePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "تقارير الرقابة والمؤشرات المخزنية",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalistOnBlueContainer
                        )
                        Text(
                            text = "مؤسسة آصرة العرب - تحليل المخزون والحركات",
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            ExcelExporter.exportComprehensiveWarehouseExcel(
                                context = context,
                                storekeeperName = storekeeperName,
                                items = allItems,
                                issueOrders = issueOrders,
                                returnOrders = returnOrders,
                                sampleOrders = sampleOrders,
                                damageRecords = damageRecords,
                                movements = allMovements,
                                representatives = representatives
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تصدير Excel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val reportHtml = generateWarehouseReportHtml(allItems, lowStockItems, damageRecords)
                            VoucherExporter.printVoucherHtml(context, reportHtml, "تقرير_المستودع_الشامل")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MinimalistBluePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("طباعة PDF", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // High-level KPI Cards Grid (Clean Minimalism 20.dp rounded corners)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("إجمالي الأصناف", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text("${allItems.size} صنف", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MinimalistBluePrimary)
                    Text("المخزون: $totalWarehouseCartons كرتون", fontSize = 11.sp, color = Color(0xFF475569))
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (lowStockItems.isNotEmpty()) MinimalistAlertContainer else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (lowStockItems.isNotEmpty()) Color(0xFFFCA5A5) else MaterialTheme.colorScheme.outlineVariant
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (lowStockItems.isNotEmpty()) MinimalistAlertBadge else Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نواقص الحد الأدنى", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    Text(
                        text = "${lowStockItems.size} صنف",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (lowStockItems.isNotEmpty()) MinimalistAlertBadge else Color(0xFF16A34A)
                    )
                    Text(
                        text = if (lowStockItems.isNotEmpty()) "يتطلب إعادة طلب توريد" else "جميع الأرصدة كافية",
                        fontSize = 10.sp,
                        color = if (lowStockItems.isNotEmpty()) MinimalistAlertBadge else Color(0xFF16A34A)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("إجمالي الحبات بالمستودع", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text("$totalWarehouseAllPieces حبة", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                    Text("($totalWarehousePieces حبة فكة إضافية)", fontSize = 10.sp, color = Color(0xFF64748B))
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("سجلات التالف والفاقد", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text("${damageRecords.size} محضر", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    if (totalDamageLossValue > 0) {
                        Text("خسارة: %.1f ر.س".format(totalDamageLossValue), fontSize = 10.sp, color = Color(0xFFB45309))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 2: تقرير حركات الصنف (تتبع تاريخ كل صنف)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "تقرير حركات الصنف (تتبع تاريخ كل صنف)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "اختر صنفاً لتتبع كم صُرف منه، وكم استُرجع، وكم هلك بالتفصيل",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Item Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedItemForReport?.let { "[${it.code}] ${it.name}" } ?: "اختر الصنف المطلوب تحليله...",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("الصنف الغذائي") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { itemDropdownExpanded = true }
                            .testTag("report_item_selector"),
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(
                            textColor = if (selectedItemForReport != null) null else Color(0xFF64748B)
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
                                text = { Text("[${item.code}] ${item.name}", fontWeight = FontWeight.SemiBold) },
                                onClick = {
                                    selectedItemForReport = item
                                    itemDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                if (selectedItemForReport != null) {
                    val itm = selectedItemForReport!!
                    val conv = itm.conversionFactor

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3 KPI blocks: Issued, Returned, Damaged
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF2F2),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("إجمالي المصروف", fontSize = 10.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                Text("${itemIssuedPieces / conv} كرتون", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                Text("(${itemIssuedPieces} حبة)", fontSize = 9.sp, color = Color(0xFF991B1B))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF0FDF4),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("إجمالي المسترجع", fontSize = 10.sp, color = EmeraldTertiary, fontWeight = FontWeight.Bold)
                                Text("${itemReturnedPieces / conv} كرتون", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldTertiary)
                                Text("(${itemReturnedPieces} حبة)", fontSize = 9.sp, color = Color(0xFF166534))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFFBEB),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("إجمالي التالف/الهالك", fontSize = 10.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
                                Text("${itemDamagedPieces / conv} كرتون", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                Text("(${itemDamagedPieces} حبة)", fontSize = 9.sp, color = Color(0xFF92400E))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Current Balance Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("الرصيد المتبقي حالياً في المستودع:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("${itm.currentCartons} كرتون و ${itm.remainingPieces} حبة", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = NavyPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("سجل حركات هذا الصنف (${itemMovements.size} حركة):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(6.dp))

                    itemMovements.take(5).forEach { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${m.movementType} (${m.movementNumber})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("${m.cartons} كرتون و ${m.pieces} حبة | ${m.stockAfterCartons} ك بعد", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 3: تقرير الفاقد والتالف (إجمالي التالف مصنفاً حسب السبب والمسؤول)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "تقرير الفاقد والتالف والمسؤوليات",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (damageRecords.isEmpty()) {
                    Text("لا توجد سجلات تالف مسجلة في المستودع حالياً.", fontSize = 12.sp, color = Color.Gray)
                } else {
                    val groupedByReason = damageRecords.groupBy { it.reason }
                    groupedByReason.forEach { (reason, records) ->
                        val cartonsSum = records.sumOf { it.cartons }
                        val piecesSum = records.sumOf { it.pieces }
                        val lossSum = records.sumOf { it.estimatedLossValue }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(reason, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                Text("عدد الحوادث: ${records.size} | المسؤولين: ${records.map { it.supervisorName }.distinct().joinToString("، ")}", fontSize = 10.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("$cartonsSum كرتون و $piecesSum حبة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                if (lossSum > 0) {
                                    Text("خسارة: %.1f ر.س".format(lossSum), fontSize = 10.sp, color = Color(0xFFB45309))
                                }
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Representative Distribution & Loading Performance Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MinimalistBluePrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تقرير كفاءة وتوزيع المناديب (أسبوعي / شهري)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }
                    Text(
                        text = "${representatives.size} مندوب مسجل",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalistBluePrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ملخص كميات التحميل والكراتين المسحوبة لكل مندوب لمتابعة مسارات التوزيع:",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (representatives.isEmpty()) {
                    Text("لا يوجد مناديب مسجلين في النظام بعد.", fontSize = 12.sp, color = Color.Gray)
                } else {
                    representatives.forEach { rep ->
                        val repOrders = issueOrders.filter {
                            it.recipientName.contains(rep.name, ignoreCase = true) ||
                            rep.name.contains(it.recipientName, ignoreCase = true)
                        }
                        val totalCartons = repOrders.sumOf { it.totalCartons }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.5f)) {
                                    Text(
                                        text = rep.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "خط السير: ${if (rep.routeOrArea.isNotBlank()) rep.routeOrArea else "غير محدد"} | سيارة: ${if (rep.vehicleNumber.isNotBlank()) rep.vehicleNumber else "-"}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$totalCartons كرتون",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MinimalistBluePrimary
                                    )
                                    Text(
                                        text = "${repOrders.size} أوامر صرف",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

/**
 * Generate full HTML inventory summary report for print/PDF
 */
fun generateWarehouseReportHtml(
    items: List<ItemEntity>,
    lowStock: List<ItemEntity>,
    damages: List<com.example.data.model.DamageRecordEntity>
): String {
    val dateStr = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar")).format(Date())
    val rows = StringBuilder()
    items.forEachIndexed { i, itm ->
        rows.append(
            """
            <tr>
                <td style="text-align: center; padding: 8px; border-bottom: 1px solid #E2E8F0;">${i + 1}</td>
                <td style="text-align: center; padding: 8px; border-bottom: 1px solid #E2E8F0; font-family: monospace;">${itm.code}</td>
                <td style="text-align: right; padding: 8px; border-bottom: 1px solid #E2E8F0; font-weight: bold;">${itm.name}</td>
                <td style="text-align: center; padding: 8px; border-bottom: 1px solid #E2E8F0;">${itm.category}</td>
                <td style="text-align: center; padding: 8px; border-bottom: 1px solid #E2E8F0;">${itm.conversionFactor}</td>
                <td style="text-align: center; padding: 8px; border-bottom: 1px solid #E2E8F0; font-weight: bold;">${itm.currentCartons}</td>
                <td style="text-align: center; padding: 8px; border-bottom: 1px solid #E2E8F0;">${itm.remainingPieces}</td>
                <td style="text-align: center; padding: 8px; border-bottom: 1px solid #E2E8F0; font-weight: bold; color: #1E3A8A;">${itm.totalPieces}</td>
                <td style="text-align: center; padding: 8px; border-bottom: 1px solid #E2E8F0;">
                    ${if (itm.isLowStock) "<span style='color: red; font-weight: bold;'>نقص (${itm.minStockCartons})</span>" else "طبيعي"}
                </td>
            </tr>
            """.trimIndent()
        )
    }

    return """
    <!DOCTYPE html>
    <html dir="rtl" lang="ar">
    <head>
        <meta charset="UTF-8">
        <title>تقرير الجرد والمخزون الشامل - مؤسسة آصرة العرب</title>
        <style>
            body { font-family: Tahoma, Arial, sans-serif; margin: 20px; color: #0F172A; }
            .header { text-align: center; border-bottom: 2px solid #1E3A8A; padding-bottom: 14px; margin-bottom: 18px; }
            h1 { color: #1E3A8A; margin: 0; font-size: 22px; }
            table { width: 100%; border-collapse: collapse; margin-top: 14px; }
            th { background: #1E3A8A; color: white; padding: 8px; font-size: 12px; }
            td { font-size: 11px; }
            .summary-box { background: #F8FAFC; padding: 12px; border-radius: 8px; margin-bottom: 16px; border: 1px solid #E2E8F0; }
        </style>
    </head>
    <body>
        <div class="header">
            <h1>مؤسسة آصرة العرب</h1>
            <div>تقرير جرد المخزون العام والرقابة المخزنية</div>
            <div style="font-size: 12px; color: #64748B;">تاريخ التقرير: $dateStr</div>
        </div>

        <div class="summary-box">
            <b>ملخص المخزون:</b> إجمالي الأصناف: ${items.size} | أصناف منخفضة (نواقص): ${lowStock.size} | إجمالي الكراتين: ${items.sumOf { it.currentCartons }} كرتون | إجمالي الحبات: ${items.sumOf { it.totalPieces }} حبة
        </div>

        <table>
            <thead>
                <tr>
                    <th>#</th>
                    <th>كود الصنف</th>
                    <th>اسم الصنف</th>
                    <th>التصنيف</th>
                    <th>معامل التحويل</th>
                    <th>رصيد (كرتون)</th>
                    <th>رصيد (حبة)</th>
                    <th>الإجمالي (حبة)</th>
                    <th>حالة الرصيد</th>
                </tr>
            </thead>
            <tbody>
                $rows
            </tbody>
        </table>
    </body>
    </html>
    """.trimIndent()
}
