package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.StockMovementEntity
import com.example.ui.theme.MinimalistAlertBadge
import com.example.ui.theme.MinimalistAlertContainer
import com.example.ui.theme.MinimalistAlertText
import com.example.ui.theme.MinimalistBlueContainer
import com.example.ui.theme.MinimalistBluePrimary
import com.example.ui.theme.MinimalistOnBlueContainer
import com.example.ui.theme.warehouseTextFieldColors
import com.example.viewmodel.WarehouseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MovementsLedgerScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val filteredMovements by viewModel.filteredMovements.collectAsStateWithLifecycle()
    val allMovements by viewModel.allMovements.collectAsStateWithLifecycle()
    val filterType by viewModel.movementTypeFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val typeFilters = listOf("الكل", "صرف", "استرجاع", "تالف", "توريد")
    val dateFormat = SimpleDateFormat("hh:mm a - yyyy/MM/dd", Locale("ar"))

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("movements_ledger_screen")
    ) {
        // Clean Audit Notice Pill
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MinimalistBlueContainer,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MinimalistBluePrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "سجل موحّد دائم وغير قابل للتعديل لضمان دقة الرقابة المخزنية",
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search in Ledger
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("بحث برقم الحركة، كود الصنف، أو رقم الأمر...", fontSize = 13.sp) },
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
                .testTag("search_movements_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Movement Type Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            typeFilters.forEach { t ->
                val isSelected = (filterType == t) || (t == "توريد" && filterType.startsWith("توريد"))
                Surface(
                    onClick = { viewModel.setMovementTypeFilter(t) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) MinimalistBluePrimary else Color.White,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) MinimalistBluePrimary else Color(0xFFE2E8F0)
                    )
                ) {
                    Text(
                        text = t,
                        color = if (isSelected) Color.White else Color(0xFF475569),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "حركات المخزون الأخيرة (${filteredMovements.size} حركة):",
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (filteredMovements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لا توجد حركات مسجلة مطابقة للبحث",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("movements_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredMovements, key = { it.id }) { mov ->
                    CleanMovementItemCard(mov = mov, dateFormat = dateFormat)
                }
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

@Composable
fun CleanMovementItemCard(
    mov: StockMovementEntity,
    dateFormat: SimpleDateFormat
) {
    val (typeColor, typeBg, iconVector) = when {
        mov.movementType.contains("صرف") -> Triple(Color(0xFFDC2626), Color(0xFFFEF2F2), Icons.Default.ReceiptLong)
        mov.movementType.contains("استرجاع") -> Triple(Color(0xFF16A34A), Color(0xFFF0FDF4), Icons.Default.AssignmentReturn)
        mov.movementType.contains("تالف") -> Triple(MinimalistAlertBadge, MinimalistAlertContainer, Icons.Default.Close)
        else -> Triple(MinimalistBluePrimary, MinimalistBlueContainer, Icons.Default.Inventory2)
    }

    val deltaSign = if (mov.totalPiecesDelta > 0) "+" else ""

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("movement_item_${mov.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Type & Number & Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = typeBg,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${mov.itemName} (${mov.itemCode})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191C1E)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = typeBg
                            ) {
                                Text(
                                    text = "${mov.movementType} #${mov.relatedOrderNumber}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = typeColor
                                )
                            }
                            Text(
                                text = mov.movementNumber,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                Text(
                    text = dateFormat.format(Date(mov.timestamp)),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quantities & Balance Box (Clean Minimalism styling)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8F9FF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("كمية الحركة:", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text(
                            text = "$deltaSign${mov.cartons} كرتون و ${mov.pieces} حبة",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("الرصيد بعد الحركة:", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text(
                            text = "${mov.stockAfterCartons} كرتون / ${mov.stockAfterPieces} حبة",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            if (mov.notes.isNotBlank() || mov.operatorName.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (mov.notes.isNotBlank()) "ملاحظة: ${mov.notes}" else "",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "المسؤول: ${mov.operatorName}",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}
