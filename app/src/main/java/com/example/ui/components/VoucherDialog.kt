package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.IssueOrderEntity
import com.example.data.model.ReturnOrderEntity
import com.example.data.model.SampleOrderEntity
import com.example.ui.theme.NavyPrimary
import com.example.util.VoucherExporter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IssueVoucherDialog(
    order: IssueOrderEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val items = VoucherExporter.parseOrderItems(order.itemsJson)
    val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))
    val formattedDate = dateFormat.format(Date(order.timestamp))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("issue_voucher_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "سند صرف مخزني رسمي",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Printable Card Preview
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "مؤسسة آصرة العرب",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NavyPrimary
                                )
                                Text(
                                    text = "المستودع الرئيسي - قسم الأغذية",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEFF6FF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NavyPrimary)
                            ) {
                                Text(
                                    text = order.orderNumber,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Meta details
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("جهة الصرف:", fontSize = 11.sp, color = Color(0xFF64748B))
                                Text(order.destination, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("التاريخ والوقت:", fontSize = 11.sp, color = Color(0xFF64748B))
                                Text(formattedDate, fontSize = 12.sp, color = Color(0xFF0F172A))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("اسم المستلم:", fontSize = 11.sp, color = Color(0xFF64748B))
                                Text(order.recipientName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("مسؤول الصرف:", fontSize = 11.sp, color = Color(0xFF64748B))
                                Text(order.supervisorName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                            }
                        }

                        if (order.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("ملاحظات: ${order.notes}", fontSize = 12.sp, color = Color(0xFF475569))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Items Table Header
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1E3A8A)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("الصنف", modifier = Modifier.weight(2f), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("كرتون", modifier = Modifier.weight(1f), color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                                Text("حبة", modifier = Modifier.weight(1f), color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                                Text("الإجمالي", modifier = Modifier.weight(1.2f), color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Items rows
                        items.forEachIndexed { i, item ->
                            val totalItemPieces = (item.cartons * item.conversionFactor) + item.pieces
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (i % 2 == 0) Color(0xFFF8FAFC) else Color.White)
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(2f)) {
                                    Text(item.itemName, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                                    Text("كود: ${item.itemCode}", fontSize = 10.sp, color = Color(0xFF64748B), fontFamily = FontFamily.Monospace)
                                }
                                Text("${item.cartons}", modifier = Modifier.weight(1f), fontSize = 12.sp, textAlign = TextAlign.Center)
                                Text("${item.pieces}", modifier = Modifier.weight(1f), fontSize = 12.sp, textAlign = TextAlign.Center)
                                Text("$totalItemPieces حبة", modifier = Modifier.weight(1.2f), fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = NavyPrimary)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // Totals Summary
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("إجمالي الصرف المعتمد:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF78350F))
                                Text("${order.totalCartons} كرتون و ${order.totalPieces} حبة", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFF78350F))
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Signatures
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("توقيع مسؤول الصرف", fontSize = 11.sp, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(order.supervisorName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("..................................", color = Color.Gray)
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("توقيع المستلم", fontSize = 11.sp, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(order.recipientName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("..................................", color = Color.Gray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            val html = VoucherExporter.generateLoadOrderHtml(order)
                            VoucherExporter.printVoucherHtml(context, html, "أمر_تحميل_${order.orderNumber}")
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("print_load_order_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("أمر تحميل PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val html = VoucherExporter.generateIssueVoucherHtml(order)
                            VoucherExporter.printVoucherHtml(context, html, "سند_صرف_${order.orderNumber}")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("سند صرف", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            VoucherExporter.shareIssueOrderText(context, order)
                        },
                        modifier = Modifier
                            .weight(0.9f)
                            .testTag("share_voucher_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مشاركة", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ReturnVoucherDialog(
    order: ReturnOrderEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val items = VoucherExporter.parseOrderItems(order.itemsJson)
    val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))
    val formattedDate = dateFormat.format(Date(order.timestamp))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("return_voucher_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سند / أمر تفريغ واسترجاع",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF059669)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("مؤسسة آصرة العرب - أمر تفريغ واسترجاع", fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                        Text("رقم السند: ${order.orderNumber}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text("التاريخ: $formattedDate", fontSize = 12.sp, color = Color(0xFF64748B))
                        if (order.relatedIssueOrderNumber != null) {
                            Text("أمر الصرف المرتبط: ${order.relatedIssueOrderNumber}", fontSize = 12.sp, color = NavyPrimary)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Text("الأصناف المفرغة المعادة للمخزون:", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("- ${item.itemName} [${item.itemCode}]", fontSize = 13.sp)
                                Text(item.formatQuantity(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("إجمالي الكمية المسترجعة: ${order.totalCartons} كرتون و ${order.totalPieces} حبة", color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                        Text("سبب التفريغ: ${order.reason}", fontSize = 13.sp)
                        Text("مسؤول الاستلام والتفريغ: ${order.supervisorName}", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            val html = VoucherExporter.generateUnloadOrderHtml(order)
                            VoucherExporter.printVoucherHtml(context, html, "أمر_تفريغ_${order.orderNumber}")
                        },
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("أمر تفريغ PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val html = VoucherExporter.generateReturnVoucherHtml(order)
                            VoucherExporter.printVoucherHtml(context, html, "سند_استرجاع_${order.orderNumber}")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("سند استرجاع", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            VoucherExporter.shareUnloadOrderText(context, order)
                        },
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مشاركة", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SampleVoucherDialog(
    order: SampleOrderEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val items = VoucherExporter.parseOrderItems(order.itemsJson)
    val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))
    val formattedDate = dateFormat.format(Date(order.timestamp))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("sample_voucher_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سند صرف عينات للمندوب",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEA580C)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEA580C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("مؤسسة آصرة العرب - سند صرف عينات", fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                        Text("رقم السند: ${order.orderNumber}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text("التاريخ: $formattedDate", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text("المندوب المستلم: ${order.representativeName}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Text("العينات المصروفة:", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("- ${item.itemName} [${item.itemCode}]", fontSize = 13.sp)
                                Text(item.formatQuantity(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("إجمالي الكمية المصروفة: ${order.totalCartons} كرتون و ${order.totalPieces} حبة", color = Color(0xFFEA580C), fontWeight = FontWeight.Bold)
                        Text("مسؤول الصرف: ${order.supervisorName}", fontSize = 13.sp)
                        if (order.notes.isNotBlank()) {
                            Text("ملاحظات: ${order.notes}", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val html = VoucherExporter.generateSampleVoucherHtml(order)
                            VoucherExporter.printVoucherHtml(context, html, "سند_عينات_${order.orderNumber}")
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C))
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("طباعة / PDF")
                    }
                    OutlinedButton(
                        onClick = {
                            VoucherExporter.shareSampleOrderText(context, order)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة")
                    }
                }
            }
        }
    }
}
