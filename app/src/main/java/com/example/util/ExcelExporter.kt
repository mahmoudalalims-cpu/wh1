package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.DamageRecordEntity
import com.example.data.model.IssueOrderEntity
import com.example.data.model.ItemEntity
import com.example.data.model.RepresentativeEntity
import com.example.data.model.ReturnOrderEntity
import com.example.data.model.SampleOrderEntity
import com.example.data.model.StockMovementEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExporter {

    private val fullDateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))
    private val fileNameDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    /**
     * Exports a comprehensive Excel file (.xls / HTML Spreadsheet) containing:
     * 1. Items inventory with Item Code, Item Name, Opening Balance, Total Issued, Total Returned, Damaged, and Available Stock.
     * 2. Representatives Section with total items & cartons issued to each sales representative.
     * 3. Damage Records Section with Photo Documentation embedded directly inside spreadsheet cells.
     */
    fun exportComprehensiveWarehouseExcel(
        context: Context,
        storekeeperName: String,
        items: List<ItemEntity>,
        issueOrders: List<IssueOrderEntity>,
        returnOrders: List<ReturnOrderEntity>,
        sampleOrders: List<SampleOrderEntity>,
        damageRecords: List<DamageRecordEntity>,
        movements: List<StockMovementEntity>,
        representatives: List<RepresentativeEntity>
    ) {
        try {
            val fileName = "Warehouse_Report_${fileNameDateFormat.format(Date())}.xls"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)

            val writer = OutputStreamWriter(outputStream, Charsets.UTF_8)

            // HTML Spreadsheet Header with UTF-8 encoding & RTL styling for Excel / Google Sheets
            writer.write("""
                <!DOCTYPE html>
                <html dir="rtl" lang="ar">
                <head>
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Arial, sans-serif; background-color: #f8fafc; color: #0f172a; margin: 15px; }
                    .report-header { text-align: center; margin-bottom: 20px; padding: 15px; background: #0f172a; color: white; border-radius: 8px; }
                    .section-title { font-size: 16px; font-weight: bold; color: #1e293b; margin-top: 25px; margin-bottom: 10px; padding-bottom: 5px; border-bottom: 3px solid #0284c7; }
                    table { border-collapse: collapse; width: 100%; margin-bottom: 20px; background-color: white; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
                    th { background-color: #1e293b; color: white; padding: 10px; border: 1px solid #cbd5e1; font-size: 13px; text-align: center; }
                    td { padding: 8px; border: 1px solid #e2e8f0; font-size: 12px; text-align: center; vertical-align: middle; }
                    tr:nth-child(even) { background-color: #f1f5f9; }
                    .badge-ok { background-color: #dcfce7; color: #166534; padding: 4px 8px; border-radius: 12px; font-weight: bold; }
                    .badge-warn { background-color: #fee2e2; color: #991b1b; padding: 4px 8px; border-radius: 12px; font-weight: bold; }
                    .damage-header th { background-color: #991b1b; }
                    .rep-header th { background-color: #15803d; }
                    .img-container { max-width: 130px; max-height: 95px; border-radius: 6px; border: 1px solid #cbd5e1; }
                </style>
                </head>
                <body>
                
                <div class="report-header">
                    <h2>تقرير جرد المخزون وسجل التوالف والمناديب الشامل</h2>
                    <h3>مؤسسة آصرة العرب (ARAB BOND EST)</h3>
                    <p>تاريخ التقرير: ${fullDateFormat.format(Date())} | مسؤول المستودع: ${storekeeperName.ifBlank { "مشرف المستودع" }}</p>
                </div>
            """.trimIndent())

            // --- SECTION 1: ITEMS INVENTORY TABLE ---
            writer.write("""
                <div class="section-title">1. جدول جرد الأصناف والمخزون التفصيلي</div>
                <table>
                <thead>
                <tr>
                    <th>رقم الصنف</th>
                    <th>اسم الصنف</th>
                    <th>الفئة</th>
                    <th>معادل الكرتون</th>
                    <th>الرصيد الافتتاحي (حبة)</th>
                    <th>المنصرف (حبة)</th>
                    <th>المسترجع (حبة)</th>
                    <th>التالف (حبة)</th>
                    <th>المتوفر حالياً (حبة)</th>
                    <th>تفصيل المتبقي</th>
                    <th>حالة المخزون</th>
                </tr>
                </thead>
                <tbody>
            """.trimIndent())

            for (item in items) {
                val itemMovements = movements.filter { it.itemCode == item.code }

                val issuedPiecesFromOrders = calculateIssuedPiecesForItem(item.code, issueOrders, sampleOrders)
                val issuedPiecesFromMovements = itemMovements
                    .filter { it.movementType.contains("صرف") }
                    .sumOf { -it.totalPiecesDelta }
                val totalIssuedPieces = maxOf(issuedPiecesFromOrders, issuedPiecesFromMovements)

                val returnedPiecesFromOrders = calculateReturnedPiecesForItem(item.code, returnOrders)
                val returnedPiecesFromMovements = itemMovements
                    .filter { it.movementType.contains("استرجاع") }
                    .sumOf { it.totalPiecesDelta }
                val totalReturnedPieces = maxOf(returnedPiecesFromOrders, returnedPiecesFromMovements)

                val damagedPiecesFromRecords = damageRecords
                    .filter { it.itemCode == item.code }
                    .sumOf { (it.cartons * item.conversionFactor) + it.pieces }
                val damagedPiecesFromMovements = itemMovements
                    .filter { it.movementType.contains("تالف") }
                    .sumOf { -it.totalPiecesDelta }
                val totalDamagedPieces = maxOf(damagedPiecesFromRecords, damagedPiecesFromMovements)

                val openingBalancePieces = item.totalPieces + totalIssuedPieces - totalReturnedPieces + totalDamagedPieces

                val statusHtml = if (item.isLowStock) "<span class=\"badge-warn\">منخفض</span>" else "<span class=\"badge-ok\">متوفر</span>"
                val stockDetails = "${item.currentCartons} كرتون و ${item.remainingPieces} حبة"

                writer.write("""
                    <tr>
                        <td><b>${item.code}</b></td>
                        <td style="text-align: right;">${item.name}</td>
                        <td>${item.category}</td>
                        <td>${item.conversionFactor}</td>
                        <td>$openingBalancePieces</td>
                        <td>$totalIssuedPieces</td>
                        <td>$totalReturnedPieces</td>
                        <td>$totalDamagedPieces</td>
                        <td><b>${item.totalPieces}</b></td>
                        <td>$stockDetails</td>
                        <td>$statusHtml</td>
                    </tr>
                """.trimIndent())
            }

            writer.write("</tbody></table>")

            // --- SECTION 2: REPRESENTATIVES SUMMARY TABLE ---
            writer.write("""
                <div class="section-title">2. قسم المناديب - إجمالي المسحوبات والمنصرف لكل مندوب</div>
                <table>
                <thead>
                <tr class="rep-header">
                    <th>كود المندوب</th>
                    <th>اسم المندوب</th>
                    <th>رقم الهاتف</th>
                    <th>المنطقة / خط السير</th>
                    <th>عدد الأوامر</th>
                    <th>إجمالي الكراتين المصروفة</th>
                    <th>إجمالي الحبات المصروفة</th>
                    <th>إجمالي الكمية الكلية (حبات)</th>
                </tr>
                </thead>
                <tbody>
            """.trimIndent())

            val repNamesList = representatives.map { it.name }.toMutableSet()
            issueOrders.forEach { if (it.recipientName.isNotBlank()) repNamesList.add(it.recipientName) }
            sampleOrders.forEach { if (it.representativeName.isNotBlank()) repNamesList.add(it.representativeName) }

            for (rep in representatives) {
                val repIssueOrders = issueOrders.filter { it.recipientName.contains(rep.name, ignoreCase = true) }
                val repSampleOrders = sampleOrders.filter { it.representativeName.contains(rep.name, ignoreCase = true) }

                val totalIssueCartons = repIssueOrders.sumOf { it.totalCartons }
                val totalIssuePieces = repIssueOrders.sumOf { it.totalPieces }
                val totalSampleCartons = repSampleOrders.sumOf { it.totalCartons }
                val totalSamplePieces = repSampleOrders.sumOf { it.totalPieces }

                val combinedCartons = totalIssueCartons + totalSampleCartons
                val combinedPieces = totalIssuePieces + totalSamplePieces
                val totalOrdersCount = repIssueOrders.size + repSampleOrders.size
                val totalCumulativePieces = combinedPieces + (combinedCartons * 24)

                writer.write("""
                    <tr>
                        <td><b>${rep.code.ifBlank { "REP-${rep.id}" }}</b></td>
                        <td style="text-align: right;">${rep.name}</td>
                        <td>${rep.phone.ifBlank { "—" }}</td>
                        <td>${rep.routeOrArea.ifBlank { "—" }}</td>
                        <td>$totalOrdersCount</td>
                        <td>$combinedCartons</td>
                        <td>$combinedPieces</td>
                        <td><b>$totalCumulativePieces</b></td>
                    </tr>
                """.trimIndent())
            }

            val extraDriverNames = repNamesList.filter { repName ->
                representatives.none { it.name.equals(repName, ignoreCase = true) }
            }

            for (driverName in extraDriverNames) {
                val repIssueOrders = issueOrders.filter { it.recipientName.equals(driverName, ignoreCase = true) }
                val repSampleOrders = sampleOrders.filter { it.representativeName.equals(driverName, ignoreCase = true) }

                val combinedCartons = repIssueOrders.sumOf { it.totalCartons } + repSampleOrders.sumOf { it.totalCartons }
                val combinedPieces = repIssueOrders.sumOf { it.totalPieces } + repSampleOrders.sumOf { it.totalPieces }
                val totalOrdersCount = repIssueOrders.size + repSampleOrders.size
                val totalCumulativePieces = combinedPieces + (combinedCartons * 24)

                writer.write("""
                    <tr>
                        <td><b>DRIVER-EXT</b></td>
                        <td style="text-align: right;">$driverName</td>
                        <td>—</td>
                        <td>مستلم خارجي</td>
                        <td>$totalOrdersCount</td>
                        <td>$combinedCartons</td>
                        <td>$combinedPieces</td>
                        <td><b>$totalCumulativePieces</b></td>
                    </tr>
                """.trimIndent())
            }

            writer.write("</tbody></table>")

            // --- SECTION 3: DAMAGE RECORDS & PHOTO DOCUMENTATION TABLE ---
            writer.write("""
                <div class="section-title">3. سجل التوالف والتوثيق المصور (Damage Records & Proof Photos)</div>
                <table>
                <thead>
                <tr class="damage-header">
                    <th>رقم المحضر</th>
                    <th>التاريخ والوقت</th>
                    <th>كود الصنف</th>
                    <th>اسم الصنف المتضرر</th>
                    <th>الكمية التالفة</th>
                    <th>سبب التلف</th>
                    <th>مسؤول المعاينة / المندوب</th>
                    <th>خسارة تقديرية (ر.س)</th>
                    <th>صورة التوثيق والإثبات</th>
                    <th>ملاحظات</th>
                </tr>
                </thead>
                <tbody>
            """.trimIndent())

            if (damageRecords.isEmpty()) {
                writer.write("<tr><td colspan=\"10\" style=\"color: #64748b;\">لا توجد محاضر إتلاف مسجلة حتى الآن</td></tr>")
            } else {
                for (rec in damageRecords) {
                    val imgHtml = getEmbeddedImageHtml(rec.photoUri)
                    val dateStr = fullDateFormat.format(Date(rec.timestamp))
                    val qtyStr = "${rec.cartons} كرتون و ${rec.pieces} حبة"

                    writer.write("""
                        <tr>
                            <td><b style="color: #dc2626;">${rec.damageNumber}</b></td>
                            <td>$dateStr</td>
                            <td><b>${rec.itemCode}</b></td>
                            <td style="text-align: right;">${rec.itemName}</td>
                            <td><b>$qtyStr</b></td>
                            <td>${rec.reason}</td>
                            <td>${rec.supervisorName}</td>
                            <td>${"%.2f".format(rec.estimatedLossValue)}</td>
                            <td>$imgHtml</td>
                            <td>${rec.notes.ifBlank { "—" }}</td>
                        </tr>
                    """.trimIndent())
                }
            }

            writer.write("</tbody></table></body></html>")

            writer.flush()
            writer.close()
            outputStream.close()

            // Share/Open file via FileProvider
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.ms-excel"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "تقرير الجرد وسجل التوالف والمناديب - مؤسسة آصرة العرب")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "مرفق تقرير الجرد التفصيلي وسجل التوالف مع صور الإثبات الموثقة وسجل المناديب بصيغة Excel (.xls).\nأمين المستودع: $storekeeperName"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "تصدير تقرير Excel الشامل مع صور التالف")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Exports a dedicated Excel file specifically for Damage Records & Photo Proofs
     */
    fun exportDamageRecordsExcel(
        context: Context,
        damageRecords: List<DamageRecordEntity>,
        storekeeperName: String
    ) {
        try {
            val fileName = "Damage_Records_Report_${fileNameDateFormat.format(Date())}.xls"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            val writer = OutputStreamWriter(outputStream, Charsets.UTF_8)

            writer.write("""
                <!DOCTYPE html>
                <html dir="rtl" lang="ar">
                <head>
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Arial, sans-serif; background-color: #f8fafc; color: #0f172a; margin: 15px; }
                    .header { text-align: center; margin-bottom: 20px; padding: 15px; background: #991b1b; color: white; border-radius: 8px; }
                    table { border-collapse: collapse; width: 100%; background-color: white; }
                    th { background-color: #7f1d1d; color: white; padding: 10px; border: 1px solid #fca5a5; font-size: 13px; text-align: center; }
                    td { padding: 10px; border: 1px solid #e2e8f0; font-size: 12px; text-align: center; vertical-align: middle; }
                    tr:nth-child(even) { background-color: #fef2f2; }
                    .img-box { max-width: 140px; max-height: 100px; border-radius: 6px; border: 1px solid #cbd5e1; }
                </style>
                </head>
                <body>
                <div class="header">
                    <h2>تقرير محاضر البضاعة التالفة وتوثيق الصور</h2>
                    <h3>مؤسسة آصرة العرب (ARAB BOND EST)</h3>
                    <p>التاريخ: ${fullDateFormat.format(Date())} | إجمالي المحاضر: ${damageRecords.size} | المشرف: ${storekeeperName.ifBlank { "مسؤول المستودع" }}</p>
                </div>

                <table>
                <thead>
                <tr>
                    <th>رقم المحضر</th>
                    <th>التاريخ والوقت</th>
                    <th>كود الصنف</th>
                    <th>اسم الصنف المتضرر</th>
                    <th>الكمية التالفة</th>
                    <th>سبب التلف</th>
                    <th>المندوب / المشرف</th>
                    <th>خسارة تقديرية (ر.س)</th>
                    <th>صورة التوثيق والمستند</th>
                    <th>ملاحظات المعاينة</th>
                </tr>
                </thead>
                <tbody>
            """.trimIndent())

            if (damageRecords.isEmpty()) {
                writer.write("<tr><td colspan=\"10\">لا توجد محاضر إتلاف مسجلة</td></tr>")
            } else {
                for (rec in damageRecords) {
                    val imgHtml = getEmbeddedImageHtml(rec.photoUri)
                    val dateStr = fullDateFormat.format(Date(rec.timestamp))
                    val qtyStr = "${rec.cartons} كرتون و ${rec.pieces} حبة"

                    writer.write("""
                        <tr>
                            <td><b style="color: #dc2626;">${rec.damageNumber}</b></td>
                            <td>$dateStr</td>
                            <td><b>${rec.itemCode}</b></td>
                            <td style="text-align: right;">${rec.itemName}</td>
                            <td><b>$qtyStr</b></td>
                            <td>${rec.reason}</td>
                            <td>${rec.supervisorName}</td>
                            <td>${"%.2f".format(rec.estimatedLossValue)}</td>
                            <td>$imgHtml</td>
                            <td>${rec.notes.ifBlank { "—" }}</td>
                        </tr>
                    """.trimIndent())
                }
            }

            writer.write("</tbody></table></body></html>")

            writer.flush()
            writer.close()
            outputStream.close()

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.ms-excel"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "تقرير توثيق التوالف بالصور - مؤسسة آصرة العرب")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "مرفق ملف Excel محضر توثيق التوالف بالصور مع تفاصيل الأسباب والمناديب الخسائر التقديرية."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "تصدير ملف Excel للتوالف والمستندات المصورة")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getEmbeddedImageHtml(photoPath: String?): String {
        if (photoPath.isNullOrBlank()) return "<span style=\"color: #94a3b8;\">بدون صورة</span>"
        return try {
            val file = File(photoPath)
            if (!file.exists()) return "<span style=\"color: #ef4444;\">الصورة غير موجودة</span>"
            val bytes = file.readBytes()
            val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            """
            <div>
                <img src="data:image/jpeg;base64,$base64" class="img-container" alt="صورة التالف" /><br/>
                <span style="font-size: 10px; color: #16a34a; font-weight: bold;">✓ صورة توثيق مرفقة</span>
            </div>
            """.trimIndent()
        } catch (e: Exception) {
            "<span style=\"color: #ef4444;\">خطأ في جلب الصورة</span>"
        }
    }

    private fun calculateIssuedPiecesForItem(
        itemCode: String,
        issueOrders: List<IssueOrderEntity>,
        sampleOrders: List<SampleOrderEntity>
    ): Int {
        var total = 0
        for (order in issueOrders) {
            val items = VoucherExporter.parseOrderItems(order.itemsJson)
            val match = items.find { it.itemCode == itemCode }
            if (match != null) {
                total += (match.cartons * match.conversionFactor) + match.pieces
            }
        }
        for (order in sampleOrders) {
            val items = VoucherExporter.parseOrderItems(order.itemsJson)
            val match = items.find { it.itemCode == itemCode }
            if (match != null) {
                total += (match.cartons * match.conversionFactor) + match.pieces
            }
        }
        return total
    }

    private fun calculateReturnedPiecesForItem(
        itemCode: String,
        returnOrders: List<ReturnOrderEntity>
    ): Int {
        var total = 0
        for (order in returnOrders) {
            val items = VoucherExporter.parseOrderItems(order.itemsJson)
            val match = items.find { it.itemCode == itemCode }
            if (match != null) {
                total += (match.cartons * match.conversionFactor) + match.pieces
            }
        }
        return total
    }
}
