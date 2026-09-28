package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import com.example.ui.theme.warehouseTextFieldColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.IssueOrderEntity
import com.example.data.model.OrderItem
import com.example.data.model.RepresentativeEntity
import com.example.ui.theme.MinimalistBlueContainer
import com.example.ui.theme.MinimalistBluePrimary
import com.example.ui.theme.MinimalistOnBlueContainer
import com.example.ui.theme.NavyPrimary
import com.example.viewmodel.WarehouseViewModel
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class PeriodFilter(val title: String) {
    THIS_WEEK("خلال الأسبوع الحالي"),
    THIS_MONTH("خلال الشهر الحالي"),
    ALL_TIME("كامل الفترة")
}

data class RepAggregatedItem(
    val itemCode: String,
    val itemName: String,
    var totalCartons: Int = 0,
    var totalPieces: Int = 0,
    var totalUnits: Int = 0
)

@Composable
fun RepresentativesScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val representatives by viewModel.allRepresentatives.collectAsStateWithLifecycle()
    val issueOrders by viewModel.issueOrders.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf(PeriodFilter.THIS_WEEK) }
    var selectedRepForDetails by remember { mutableStateOf<RepresentativeEntity?>(null) }

    // Dialog state for adding/editing representative
    var showRepFormDialog by remember { mutableStateOf(false) }
    var editingRep by remember { mutableStateOf<RepresentativeEntity?>(null) }
    var repToDelete by remember { mutableStateOf<RepresentativeEntity?>(null) }

    // Form states
    var formName by remember { mutableStateOf("") }
    var formPhone by remember { mutableStateOf("") }
    var formCode by remember { mutableStateOf("") }
    var formVehicle by remember { mutableStateOf("") }
    var formRoute by remember { mutableStateOf("") }
    var formNotes by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    val filteredReps = remember(representatives, searchQuery) {
        if (searchQuery.isBlank()) representatives
        else representatives.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.code.contains(searchQuery, ignoreCase = true) ||
            it.phone.contains(searchQuery, ignoreCase = true) ||
            it.routeOrArea.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("representatives_screen")
    ) {
        // Compact Header Banner for Mobile
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MinimalistBlueContainer,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = MinimalistBluePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "مناديب التوزيع والتحميل",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalistOnBlueContainer
                        )
                        Text(
                            text = "${representatives.size} مندوب مسجل",
                            fontSize = 10.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = {
                        editingRep = null
                        formName = ""
                        formPhone = ""
                        formCode = "REP-%02d".format(representatives.size + 1)
                        formVehicle = ""
                        formRoute = ""
                        formNotes = ""
                        formError = null
                        showRepFormDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MinimalistBluePrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة مندوب", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar & Period Selector Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("بحث عن مندوب بالاسم أو الكود أو الجوال...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_rep_input"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = warehouseTextFieldColors()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Period filter Chips (Week / Month / All)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PeriodFilter.values().forEach { period ->
                val isSelected = selectedPeriod == period
                Surface(
                    onClick = { selectedPeriod = period },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) MinimalistBluePrimary else Color.White,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) MinimalistBluePrimary else Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when(period) {
                                PeriodFilter.THIS_WEEK -> Icons.Default.DateRange
                                PeriodFilter.THIS_MONTH -> Icons.Default.CalendarMonth
                                PeriodFilter.ALL_TIME -> Icons.Default.ReceiptLong
                            },
                            contentDescription = null,
                            tint = if (isSelected) Color.White else Color(0xFF475569),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = period.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF334155)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredReps.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("لا يوجد مناديب مسجلين حالياً", fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("اضغط على زر «إضافة مندوب» لتسجيل مناديب التوزيع وسياراتهم", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredReps, key = { it.id }) { rep ->
                    RepresentativeCardWithStats(
                        rep = rep,
                        issueOrders = issueOrders,
                        period = selectedPeriod,
                        onEdit = {
                            editingRep = rep
                            formName = rep.name
                            formPhone = rep.phone
                            formCode = rep.code
                            formVehicle = rep.vehicleNumber
                            formRoute = rep.routeOrArea
                            formNotes = rep.notes
                            formError = null
                            showRepFormDialog = true
                        },
                        onDelete = {
                            repToDelete = rep
                        },
                        onViewDetails = {
                            selectedRepForDetails = rep
                        }
                    )
                }
            }
        }
    }

    // Dialog: Add / Edit Representative
    if (showRepFormDialog) {
        AlertDialog(
            onDismissRequest = { showRepFormDialog = false },
            title = {
                Text(
                    text = if (editingRep == null) "إضافة مندوب جديد" else "تعديل بيانات المندوب",
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = formName,
                        onValueChange = { formName = it },
                        label = { Text("اسم المندوب الكامل *") },
                        placeholder = { Text("مثال: عبد الله بن فهد الدوسري") },
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = formCode,
                            onValueChange = { formCode = it },
                            label = { Text("كود المندوب") },
                            placeholder = { Text("REP-01") },
                            shape = RoundedCornerShape(12.dp),
                            colors = warehouseTextFieldColors(),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = formPhone,
                            onValueChange = { formPhone = it },
                            label = { Text("رقم الجوال") },
                            placeholder = { Text("05xxxxxxxx") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp),
                            colors = warehouseTextFieldColors(),
                            modifier = Modifier.weight(1.3f),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = formVehicle,
                            onValueChange = { formVehicle = it },
                            label = { Text("لوحة / نوع الشاحنة") },
                            placeholder = { Text("أ ب ج 1234") },
                            shape = RoundedCornerShape(12.dp),
                            colors = warehouseTextFieldColors(),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = formRoute,
                            onValueChange = { formRoute = it },
                            label = { Text("خط السير أو المنطقة") },
                            placeholder = { Text("الرياض - الشمال") },
                            shape = RoundedCornerShape(12.dp),
                            colors = warehouseTextFieldColors(),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = formNotes,
                        onValueChange = { formNotes = it },
                        label = { Text("ملاحظات إضافية") },
                        shape = RoundedCornerShape(12.dp),
                        colors = warehouseTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (formError != null) {
                        Text(
                            text = formError ?: "",
                            color = Color(0xFFDC2626),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (formName.isBlank()) {
                            formError = "اسم المندوب مطلوب"
                            return@Button
                        }
                        if (editingRep == null) {
                            viewModel.addRepresentative(
                                name = formName,
                                phone = formPhone,
                                code = formCode,
                                vehicleNumber = formVehicle,
                                routeOrArea = formRoute,
                                notes = formNotes
                            ) {
                                showRepFormDialog = false
                            }
                        } else {
                            viewModel.updateRepresentative(
                                editingRep!!.copy(
                                    name = formName.trim(),
                                    phone = formPhone.trim(),
                                    code = formCode.trim(),
                                    vehicleNumber = formVehicle.trim(),
                                    routeOrArea = formRoute.trim(),
                                    notes = formNotes.trim()
                                )
                            ) {
                                showRepFormDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MinimalistBluePrimary)
                ) {
                    Text("حفظ البيانات")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRepFormDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Dialog: Delete confirmation
    if (repToDelete != null) {
        AlertDialog(
            onDismissRequest = { repToDelete = null },
            title = { Text("تأكيد حذف المندوب", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)) },
            text = {
                Text("هل أنت متأكد من حذف المندوب [${repToDelete!!.name}]؟ لن يتم حذف أذونات الصرف السابقة ولكن لن يظهر في قائمة المناديب النشطة.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRepresentative(repToDelete!!.id, repToDelete!!.name)
                        repToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("تأكيد الحذف")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { repToDelete = null }) {
                    Text("تراجع")
                }
            }
        )
    }

    // Dialog: Full Loaded Items Table for selected representative
    if (selectedRepForDetails != null) {
        RepresentativeLoadingHistoryDialog(
            rep = selectedRepForDetails!!,
            issueOrders = issueOrders,
            period = selectedPeriod,
            onDismiss = { selectedRepForDetails = null }
        )
    }
}

/**
 * Filter orders by selected representative name and time frame (week / month / all)
 */
fun filterOrdersByRepAndPeriod(
    orders: List<IssueOrderEntity>,
    repName: String,
    period: PeriodFilter
): List<IssueOrderEntity> {
    val now = Calendar.getInstance()

    val startTimestamp: Long = when (period) {
        PeriodFilter.THIS_WEEK -> {
            val cal = Calendar.getInstance().apply {
                firstDayOfWeek = Calendar.SATURDAY
                set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        }
        PeriodFilter.THIS_MONTH -> {
            val cal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        }
        PeriodFilter.ALL_TIME -> 0L
    }

    return orders.filter { order ->
        val matchesRep = order.recipientName.contains(repName, ignoreCase = true) ||
                         repName.contains(order.recipientName, ignoreCase = true)
        val matchesTime = order.timestamp >= startTimestamp
        matchesRep && matchesTime
    }
}

/**
 * Extract items loaded from list of Issue Orders and aggregate them
 */
fun extractAggregatedItemsFromOrders(orders: List<IssueOrderEntity>): List<RepAggregatedItem> {
    val map = mutableMapOf<String, RepAggregatedItem>()

    orders.forEach { order ->
        try {
            val arr = JSONArray(order.itemsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val code = obj.optString("itemCode", "")
                val name = obj.optString("itemName", "")
                val cartons = obj.optInt("cartons", 0)
                val pieces = obj.optInt("pieces", 0)
                val factor = obj.optInt("conversionFactor", 1)
                val totalUnits = (cartons * factor) + pieces

                val existing = map.getOrPut(code) {
                    RepAggregatedItem(itemCode = code, itemName = name)
                }
                existing.totalCartons += cartons
                existing.totalPieces += pieces
                existing.totalUnits += totalUnits
            }
        } catch (_: Exception) {
        }
    }

    return map.values.sortedByDescending { it.totalCartons }
}

@Composable
fun RepresentativeCardWithStats(
    rep: RepresentativeEntity,
    issueOrders: List<IssueOrderEntity>,
    period: PeriodFilter,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewDetails: () -> Unit
) {
    val repOrders = remember(issueOrders, rep, period) {
        filterOrdersByRepAndPeriod(issueOrders, rep.name, period)
    }

    val loadedItems = remember(repOrders) {
        extractAggregatedItemsFromOrders(repOrders)
    }

    val totalOrdersCount = repOrders.size
    val totalCartonsLoaded = repOrders.sumOf { it.totalCartons }
    val totalDistinctItems = loadedItems.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Rep Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MinimalistBlueContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = rep.name.take(1),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalistBluePrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = rep.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            if (rep.code.isNotBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = rep.code,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (rep.phone.isNotBlank()) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(rep.phone, fontSize = 11.sp, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (rep.vehicleNumber.isNotBlank()) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("لوحة: ${rep.vehicleNumber}", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        }
                        if (rep.routeOrArea.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("الخط: ${rep.routeOrArea}", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Summary stats for this delegate in selected period
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Orders
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("أوامر الصرف (${period.title.take(7)})", fontSize = 9.sp, color = Color(0xFF64748B))
                        Text("$totalOrdersCount أمر", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MinimalistBluePrimary)
                    }
                }

                // Total Cartons
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEFF6FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("إجمالي الكراتين المحملة", fontSize = 9.sp, color = Color(0xFF1E40AF))
                        Text("$totalCartonsLoaded كرتون", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                    }
                }

                // Unique Items Count
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0FDF4),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("عدد الأصناف المحملة", fontSize = 9.sp, color = Color(0xFF166534))
                        Text("$totalDistinctItems صنف مختلف", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                    }
                }
            }

            // Preview Table of Top Loaded Items
            if (loadedItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "جدول الأصناف المحملة ${period.title}:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Mini Table Header
                Surface(
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("الصنف الغذائي", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(2f))
                        Text("الكراتين", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(1f))
                        Text("الحبات الإضافية", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(1f))
                    }
                }

                // First 3 items preview
                loadedItems.take(3).forEachIndexed { idx, item ->
                    val bg = if (idx % 2 == 0) Color.White else Color(0xFFFAFAFA)
                    Surface(
                        color = bg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "[${item.itemCode}] ${item.itemName}",
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(2f),
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "${item.totalCartons} كرتون",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalistBluePrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${item.totalPieces} حبة",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                if (loadedItems.size > 3) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = onViewDetails,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("عرض بقية الأصناف (${loadedItems.size} صنف بالكامل) ▾", fontSize = 11.sp, color = MinimalistBluePrimary)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "لا توجد بضائع محملة لهذا المندوب ${period.title}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

/**
 * Modal dialog showing the complete breakdown table of items loaded by a representative
 */
@Composable
fun RepresentativeLoadingHistoryDialog(
    rep: RepresentativeEntity,
    issueOrders: List<IssueOrderEntity>,
    period: PeriodFilter,
    onDismiss: () -> Unit
) {
    val repOrders = remember(issueOrders, rep, period) {
        filterOrdersByRepAndPeriod(issueOrders, rep.name, period)
    }

    val loadedItems = remember(repOrders) {
        extractAggregatedItemsFromOrders(repOrders)
    }

    val totalCartons = loadedItems.sumOf { it.totalCartons }
    val totalPieces = loadedItems.sumOf { it.totalPieces }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "جدول تحميلات المندوب: ${rep.name}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Text(
                    text = "الفترة: ${period.title} | إجمالي ${loadedItems.size} صنف مختلف",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                // Totals Bar
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MinimalistBlueContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("إجمالي المحمل في الفترة:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MinimalistOnBlueContainer)
                        Text("$totalCartons كرتون و $totalPieces حبة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MinimalistBluePrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Table Header
                Surface(
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("كود واسم الصنف", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(2.2f))
                        Text("الكراتين", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                        Text("الحبات", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                    }
                }

                // Table Rows
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(loadedItems) { item ->
                        Surface(
                            shape = RoundedCornerShape(0.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(2.2f)) {
                                    Text(
                                        text = item.itemName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "كود: ${item.itemCode}",
                                        fontSize = 9.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Text(
                                    text = "${item.totalCartons}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalistBluePrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${item.totalPieces}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MinimalistBluePrimary)
            ) {
                Text("إغلاق")
            }
        }
    )
}
