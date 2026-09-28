package com.example.util

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.model.DamageRecordEntity
import com.example.data.model.IssueOrderEntity
import com.example.data.model.OrderItem
import com.example.data.model.ReturnOrderEntity
import com.example.data.model.SampleOrderEntity
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object VoucherExporter {

    private val fullRealTimeFormat = SimpleDateFormat("EEEE، d MMMM yyyy - hh:mm:ss a", Locale("ar"))
    private val standardDateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm:ss a", Locale("ar"))

    // Official Arab Bond Est Vector Logo (SVG) with Golden Geometric Monogram
    private const val ARAB_BOND_SVG_LOGO = """
    <svg width="76" height="76" viewBox="0 0 200 200" fill="none" xmlns="http://www.w3.org/2000/svg">
        <!-- Outer Golden Circle -->
        <circle cx="100" cy="74" r="46" stroke="#F59E0B" stroke-width="6" fill="none" />
        <!-- Top Central Chevron / Triangle -->
        <polygon points="100,20 128,74 100,62 72,74" fill="#D97706" />
        <polygon points="100,20 100,62 72,74" fill="#FBBF24" />
        <!-- Left Wing Struts -->
        <polygon points="70,70 95,118 83,125 48,80" fill="#F59E0B" />
        <polygon points="50,92 77,138 65,145 35,100" fill="#FBBF24" />
        <!-- Right Wing Struts -->
        <polygon points="130,70 105,118 117,125 152,80" fill="#D97706" />
        <polygon points="150,92 123,138 135,145 165,100" fill="#F59E0B" />
        <!-- Lower Accent Diamond -->
        <polygon points="100,136 106,146 100,154 94,146" fill="#D97706" />
        <!-- Brand Typography -->
        <text x="100" y="174" text-anchor="middle" font-family="'Segoe UI', Arial, sans-serif" font-weight="900" font-size="20" fill="#0F172A" letter-spacing="2">ARAB BOND</text>
        <text x="100" y="193" text-anchor="middle" font-family="'Segoe UI', Arial, sans-serif" font-weight="700" font-size="12" fill="#D97706" letter-spacing="4">EST</text>
    </svg>
    """

    fun parseOrderItems(itemsJson: String): List<OrderItem> {
        val list = mutableListOf<OrderItem>()
        try {
            val arr = JSONArray(itemsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    OrderItem(
                        itemCode = obj.optString("itemCode"),
                        itemName = obj.optString("itemName"),
                        cartons = obj.optInt("cartons"),
                        pieces = obj.optInt("pieces"),
                        conversionFactor = obj.optInt("conversionFactor", 1),
                        price = obj.optDouble("price", 0.0),
                        notes = obj.optString("notes")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    /**
     * Generate HTML for official issue voucher / loading order (سند صرف بضاعة / أمر تحميل وتوزيع)
     */
    fun generateIssueVoucherHtml(order: IssueOrderEntity): String {
        val items = parseOrderItems(order.itemsJson)
        val orderRecordedDate = fullRealTimeFormat.format(Date(order.timestamp))
        val currentActualDate = fullRealTimeFormat.format(Date())

        val itemsRows = StringBuilder()
        var index = 1
        var totalQuantityCartons = 0
        var totalQuantityPieces = 0

        for (item in items) {
            totalQuantityCartons += item.cartons
            totalQuantityPieces += item.pieces
            val totalPiecesLine = (item.cartons * item.conversionFactor) + item.pieces
            itemsRows.append(
                """
                <tr>
                    <td style="text-align: center; padding: 10px; border-bottom: 1px solid #E2E8F0;">$index</td>
                    <td style="text-align: center; padding: 10px; border-bottom: 1px solid #E2E8F0; font-family: monospace; font-weight: bold; color: #1E3A8A;">${item.itemCode}</td>
                    <td style="text-align: right; padding: 10px; border-bottom: 1px solid #E2E8F0; font-weight: bold; color: #0F172A;">${item.itemName}</td>
                    <td style="text-align: center; padding: 10px; border-bottom: 1px solid #E2E8F0; font-weight: bold;">${item.cartons}</td>
                    <td style="text-align: center; padding: 10px; border-bottom: 1px solid #E2E8F0; font-weight: bold;">${item.pieces}</td>
                    <td style="text-align: center; padding: 10px; border-bottom: 1px solid #E2E8F0; color: #2563EB; font-weight: bold;">$totalPiecesLine حبة</td>
                    <td style="text-align: right; padding: 10px; border-bottom: 1px solid #E2E8F0; color: #64748B;">${item.notes.ifBlank { "—" }}</td>
                </tr>
                """.trimIndent()
            )
            index++
        }

        return """
        <!DOCTYPE html>
        <html dir="rtl" lang="ar">
        <head>
            <meta charset="UTF-8">
            <title>سند صرف مخزني - أمر تحميل - ${order.orderNumber}</title>
            <style>
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif, Arial;
                    margin: 20px;
                    color: #0F172A;
                    background-color: #FFFFFF;
                }
                .voucher-container {
                    border: 2px solid #2563EB;
                    border-radius: 16px;
                    padding: 24px;
                    max-width: 820px;
                    margin: auto;
                    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
                }
                .header-table {
                    width: 100%;
                    border-bottom: 2px solid #2563EB;
                    padding-bottom: 14px;
                    margin-bottom: 16px;
                }
                .brand-title {
                    font-size: 22px;
                    font-weight: 900;
                    color: #1E293B;
                    margin: 0;
                }
                .brand-sub {
                    font-size: 12px;
                    color: #64748B;
                    margin-top: 3px;
                }
                .badge-title {
                    background: #2563EB;
                    color: #FFFFFF;
                    padding: 8px 18px;
                    border-radius: 8px;
                    font-size: 17px;
                    font-weight: bold;
                    display: inline-block;
                }
                .realtime-box {
                    background: #EFF6FF;
                    border: 1px solid #BFDBFE;
                    border-radius: 10px;
                    padding: 10px 14px;
                    margin-bottom: 16px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }
                .meta-table {
                    width: 100%;
                    background: #F8FAFC;
                    border: 1px solid #E2E8F0;
                    border-radius: 10px;
                    padding: 12px;
                    margin-bottom: 20px;
                }
                .meta-cell {
                    padding: 6px 12px;
                    font-size: 13px;
                }
                .meta-label {
                    color: #64748B;
                    font-weight: bold;
                }
                .meta-val {
                    color: #0F172A;
                    font-weight: bold;
                }
                .items-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 24px;
                }
                .items-table th {
                    background: #1E293B;
                    color: #FFFFFF;
                    padding: 10px;
                    font-size: 13px;
                }
                .signatures {
                    margin-top: 30px;
                    width: 100%;
                }
                .sig-box {
                    width: 45%;
                    border: 1px dashed #CBD5E1;
                    border-radius: 8px;
                    padding: 12px;
                    text-align: center;
                    font-size: 13px;
                    font-weight: bold;
                    background: #F8FAFC;
                }
                .footer {
                    margin-top: 24px;
                    text-align: center;
                    font-size: 11px;
                    color: #64748B;
                    border-top: 1px solid #E2E8F0;
                    padding-top: 10px;
                }
            </style>
        </head>
        <body>
            <div class="voucher-container">
                <table class="header-table">
                    <tr>
                        <td style="width: 85px; vertical-align: middle;">
                            $ARAB_BOND_SVG_LOGO
                        </td>
                        <td style="text-align: right; vertical-align: middle; padding-right: 12px;">
                            <h1 class="brand-title">مؤسسة آصرة العرب <span style="font-size: 15px; color: #D97706; font-weight: 800;">ARAB BOND EST</span></h1>
                            <div class="brand-sub">إدارة المستودعات والخدمات اللوجستية | قسم الأغذية والتوزيع</div>
                            <div class="brand-sub">نظام إلكتروني معتمد لمراقبة وحماية الأرصدة المخزنية</div>
                        </td>
                        <td style="text-align: left; vertical-align: middle; width: 35%;">
                            <div class="badge-title">سند صرف / أمر تحميل</div>
                            <div style="font-family: monospace; font-size: 16px; font-weight: bold; color: #2563EB; margin-top: 6px;">
                                ${order.orderNumber}
                            </div>
                        </td>
                    </tr>
                </table>

                <div class="realtime-box">
                    <div style="font-size: 13px; color: #1E3A8A; font-weight: 800;">
                        ⏰ الوقت والتاريخ الفعلي للطباعة: <span style="color: #0F172A; font-weight: bold;">$currentActualDate</span>
                    </div>
                    <div style="font-size: 12px; color: #64748B;">
                        تاريخ تسجيل الأمر: $orderRecordedDate
                    </div>
                </div>

                <table class="meta-table">
                    <tr>
                        <td class="meta-cell"><span class="meta-label">جهة الصرف / التوجيه: </span><span class="meta-val">${order.destination}</span></td>
                        <td class="meta-cell"><span class="meta-label">رقم السند: </span><span class="meta-val" style="color: #2563EB; font-family: monospace;">${order.orderNumber}</span></td>
                    </tr>
                    <tr>
                        <td class="meta-cell"><span class="meta-label">مسؤول الصرف المعتمد: </span><span class="meta-val">${order.supervisorName}</span></td>
                        <td class="meta-cell"><span class="meta-label">اسم السائق / المستلم: </span><span class="meta-val">${order.recipientName}</span></td>
                    </tr>
                    ${if (order.notes.isNotBlank()) "<tr><td colspan='2' class='meta-cell'><span class='meta-label'>ملاحظات إضافية: </span><span class='meta-val'>${order.notes}</span></td></tr>" else ""}
                </table>

                <table class="items-table">
                    <thead>
                        <tr>
                            <th style="width: 5%;">#</th>
                            <th style="width: 14%;">كود الصنف</th>
                            <th style="width: 35%;">اسم الصنف الغذائي</th>
                            <th style="width: 12%;">الكمية (كرتون)</th>
                            <th style="width: 12%;">الكمية (حبة)</th>
                            <th style="width: 12%;">الإجمالي بالحبة</th>
                            <th style="width: 10%;">ملاحظات</th>
                        </tr>
                    </thead>
                    <tbody>
                        $itemsRows
                    </tbody>
                    <tfoot>
                        <tr style="background-color: #FEF3C7; font-weight: bold;">
                            <td colspan="3" style="padding: 10px; text-align: left; color: #92400E;">إجمالي الكميات المعتمدة في أمر الصرف:</td>
                            <td style="text-align: center; padding: 10px; color: #92400E;">${order.totalCartons} كرتون</td>
                            <td style="text-align: center; padding: 10px; color: #92400E;">${order.totalPieces} حبة</td>
                            <td colspan="2" style="text-align: center; padding: 10px; color: #2563EB;">معتمد ومحصن أمنياً</td>
                        </tr>
                    </tfoot>
                </table>

                <table class="signatures">
                    <tr>
                        <td class="sig-box">
                            مسؤول المستودع والصرف<br>
                            <span style="color: #64748B; font-size: 12px;">${order.supervisorName}</span><br><br>
                            التوقيع / الختم: .......................................
                        </td>
                        <td style="width: 10%;"></td>
                        <td class="sig-box">
                            المستلم المفوض / السائق<br>
                            <span style="color: #64748B; font-size: 12px;">${order.recipientName}</span><br><br>
                            التوقيع: .......................................
                        </td>
                    </tr>
                </table>

                <div class="footer">
                    تم إصدار هذا السند إلكترونياً وبحماية أمنية من نظام إدارة مخزون <strong>مؤسسة آصرة العرب (ARAB BOND EST)</strong><br>
                    الوقت والتاريخ الفعلي عند الإصدار: $currentActualDate
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Generate HTML for official return voucher (سند استرجاع مخزني)
     */
    fun generateReturnVoucherHtml(order: ReturnOrderEntity): String {
        val orderRecordedDate = fullRealTimeFormat.format(Date(order.timestamp))
        val currentActualDate = fullRealTimeFormat.format(Date())

        return """
        <!DOCTYPE html>
        <html dir="rtl" lang="ar">
        <head>
            <meta charset="UTF-8">
            <title>سند استرجاع مخزني - ${order.orderNumber}</title>
            <style>
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif, Arial;
                    margin: 20px;
                    color: #0F172A;
                    background-color: #FFFFFF;
                }
                .voucher-container {
                    border: 2px solid #16A34A;
                    border-radius: 16px;
                    padding: 24px;
                    max-width: 820px;
                    margin: auto;
                    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
                }
                .header-table {
                    width: 100%;
                    border-bottom: 2px solid #16A34A;
                    padding-bottom: 14px;
                    margin-bottom: 16px;
                }
                .brand-title {
                    font-size: 22px;
                    font-weight: 900;
                    color: #1E293B;
                    margin: 0;
                }
                .brand-sub {
                    font-size: 12px;
                    color: #64748B;
                    margin-top: 3px;
                }
                .badge-title {
                    background: #16A34A;
                    color: #FFFFFF;
                    padding: 8px 18px;
                    border-radius: 8px;
                    font-size: 17px;
                    font-weight: bold;
                    display: inline-block;
                }
                .realtime-box {
                    background: #F0FDF4;
                    border: 1px solid #BBF7D0;
                    border-radius: 10px;
                    padding: 10px 14px;
                    margin-bottom: 16px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }
                .meta-table {
                    width: 100%;
                    background: #F8FAFC;
                    border: 1px solid #E2E8F0;
                    border-radius: 10px;
                    padding: 12px;
                    margin-bottom: 20px;
                }
                .meta-cell {
                    padding: 6px 12px;
                    font-size: 13px;
                }
                .meta-label {
                    color: #64748B;
                    font-weight: bold;
                }
                .meta-val {
                    color: #0F172A;
                    font-weight: bold;
                }
                .items-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 24px;
                }
                .items-table th {
                    background: #1E293B;
                    color: #FFFFFF;
                    padding: 10px;
                    font-size: 13px;
                }
                .signatures {
                    margin-top: 30px;
                    width: 100%;
                }
                .sig-box {
                    width: 45%;
                    border: 1px dashed #CBD5E1;
                    border-radius: 8px;
                    padding: 12px;
                    text-align: center;
                    font-size: 13px;
                    font-weight: bold;
                    background: #F8FAFC;
                }
                .footer {
                    margin-top: 24px;
                    text-align: center;
                    font-size: 11px;
                    color: #64748B;
                    border-top: 1px solid #E2E8F0;
                    padding-top: 10px;
                }
            </style>
        </head>
        <body>
            <div class="voucher-container">
                <table class="header-table">
                    <tr>
                        <td style="width: 85px; vertical-align: middle;">
                            $ARAB_BOND_SVG_LOGO
                        </td>
                        <td style="text-align: right; vertical-align: middle; padding-right: 12px;">
                            <h1 class="brand-title">مؤسسة آصرة العرب <span style="font-size: 15px; color: #D97706; font-weight: 800;">ARAB BOND EST</span></h1>
                            <div class="brand-sub">إدارة المستودعات والخدمات اللوجستية | سند استرجاع بضاعة</div>
                            <div class="brand-sub">نظام إلكتروني معتمد لمراقبة وحماية الأرصدة المخزنية</div>
                        </td>
                        <td style="text-align: left; vertical-align: middle; width: 35%;">
                            <div class="badge-title">سند استرجاع مخزني</div>
                            <div style="font-family: monospace; font-size: 16px; font-weight: bold; color: #16A34A; margin-top: 6px;">
                                ${order.orderNumber}
                            </div>
                        </td>
                    </tr>
                </table>

                <div class="realtime-box">
                    <div style="font-size: 13px; color: #166534; font-weight: 800;">
                        ⏰ الوقت والتاريخ الفعلي للطباعة: <span style="color: #0F172A; font-weight: bold;">$currentActualDate</span>
                    </div>
                    <div style="font-size: 12px; color: #64748B;">
                        تاريخ الاسترجاع: $orderRecordedDate
                    </div>
                </div>

                <table class="meta-table">
                    <tr>
                        <td class="meta-cell"><span class="meta-label">رقم السند: </span><span class="meta-val" style="color: #16A34A; font-family: monospace;">${order.orderNumber}</span></td>
                        <td class="meta-cell"><span class="meta-label">أمر الصرف المرتبط: </span><span class="meta-val">${order.relatedIssueOrderNumber ?: "استرجاع مباشر (بدون ربط)"}</span></td>
                    </tr>
                    <tr>
                        <td class="meta-cell"><span class="meta-label">سبب الاسترجاع: </span><span class="meta-val" style="color: #16A34A;">${order.reason}</span></td>
                        <td class="meta-cell"><span class="meta-label">المسؤول المستلم: </span><span class="meta-val">${order.supervisorName}</span></td>
                    </tr>
                    ${if (order.notes.isNotBlank()) "<tr><td colspan='2' class='meta-cell'><span class='meta-label'>ملاحظات: </span><span class='meta-val'>${order.notes}</span></td></tr>" else ""}
                </table>

                <table class="items-table">
                    <thead>
                        <tr>
                            <th style="width: 15%;">كود الصنف</th>
                            <th style="width: 45%;">اسم الصنف الغذائي</th>
                            <th style="width: 40%;">الكمية المسترجعة (كرتون / حبة)</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${
                            parseOrderItems(order.itemsJson).joinToString("\n") { item ->
                                """
                                <tr>
                                    <td style="text-align: center; padding: 12px; border-bottom: 1px solid #E2E8F0; font-family: monospace; font-weight: bold; color: #1E3A8A;">${item.itemCode}</td>
                                    <td style="text-align: right; padding: 12px; border-bottom: 1px solid #E2E8F0; font-weight: bold; color: #0F172A;">${item.itemName}</td>
                                    <td style="text-align: center; padding: 12px; border-bottom: 1px solid #E2E8F0; font-weight: bold; color: #16A34A;">${item.formatQuantity()}</td>
                                </tr>
                                """.trimIndent()
                            }
                        }
                    </tbody>
                </table>

                <table class="signatures">
                    <tr>
                        <td class="sig-box">
                            مسؤول فحص واستلام المرتجع<br>
                            <span style="color: #64748B; font-size: 12px;">${order.supervisorName}</span><br><br>
                            التوقيع / الختم: .......................................
                        </td>
                        <td style="width: 10%;"></td>
                        <td class="sig-box">
                            المسلّم / المندوب المفوض<br><br><br>
                            التوقيع: .......................................
                        </td>
                    </tr>
                </table>

                <div class="footer">
                    تم إصدار هذا السند إلكترونياً وبحماية أمنية من نظام إدارة مخزون <strong>مؤسسة آصرة العرب (ARAB BOND EST)</strong><br>
                    الوقت والتاريخ الفعلي عند الإصدار: $currentActualDate
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun uriToBase64(context: Context, uriString: String?): String? {
        if (uriString.isNullOrBlank()) return null
        try {
            val uri = android.net.Uri.parse(uriString)
            val inputStream = if (uri.scheme == "content") {
                context.contentResolver.openInputStream(uri)
            } else {
                val path = uriString.replace("file://", "")
                val file = java.io.File(path)
                if (file.exists()) file.inputStream() else null
            } ?: return null

            val bytes = inputStream.readBytes()
            inputStream.close()
            val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            return "data:image/jpeg;base64,$base64"
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Generate HTML for official Load Order (أمر تحميل - خصم من المخزون)
     * Matches Arab Bond EST Load Order template from user's images
     */
    fun generateLoadOrderHtml(order: IssueOrderEntity): String {
        val items = parseOrderItems(order.itemsJson)
        val orderRecordedDate = standardDateFormat.format(Date(order.timestamp))

        val itemsRows = StringBuilder()
        for (item in items) {
            val qtyDisplay = if (item.cartons > 0 && item.pieces > 0) {
                "${item.cartons} كرتون و ${item.pieces} حبة"
            } else if (item.cartons > 0) {
                "${item.cartons}"
            } else {
                "${item.pieces}"
            }
            val unitDisplay = if (item.cartons > 0) "كرتونة" else "حبة"

            itemsRows.append(
                """
                <tr>
                    <td style="border: 1px solid #000; padding: 6px 8px; text-align: center; font-family: monospace; font-weight: bold;">${item.itemCode}</td>
                    <td style="border: 1px solid #000; padding: 6px 8px; text-align: right; font-weight: bold;">${item.itemName}</td>
                    <td style="border: 1px solid #000; padding: 6px 8px; text-align: center;">$unitDisplay</td>
                    <td style="border: 1px solid #000; padding: 6px 8px; text-align: center; font-weight: bold;">$qtyDisplay</td>
                </tr>
                """.trimIndent()
            )
        }

        return """
        <!DOCTYPE html>
        <html dir="rtl" lang="ar">
        <head>
            <meta charset="UTF-8">
            <title>أمر تحميل - ${order.orderNumber}</title>
            <style>
                @page { size: A4; margin: 12mm; }
                body { font-family: 'Segoe UI', Tahoma, Arial, sans-serif; margin: 0; padding: 10px; color: #000000; background: #FFFFFF; }
                .header-top { width: 100%; border-collapse: collapse; margin-bottom: 12px; }
                .header-top td { vertical-align: top; }
                .title-box { text-align: center; margin: 12px 0 10px 0; }
                .title-main { font-size: 22px; font-weight: bold; color: #000; margin: 0; }
                .title-sub { font-size: 15px; font-weight: bold; color: #333; margin-top: 2px; }
                
                .meta-table { width: 100%; border-collapse: collapse; margin-bottom: 15px; border: 1px solid #000; }
                .meta-table td { border: 1px solid #000; padding: 6px 10px; font-size: 12px; }
                .meta-label { font-weight: bold; background-color: #F3F4F6; text-align: center; }
                
                .data-table { width: 100%; border-collapse: collapse; border: 1px solid #000; margin-bottom: 20px; }
                .data-table th { border: 1px solid #000; padding: 8px; background-color: #E5E7EB; text-align: center; font-weight: bold; font-size: 12px; color: #000; }
                .data-table td { border: 1px solid #000; padding: 6px 8px; font-size: 12px; }
                
                .footer-text { text-align: center; font-size: 11px; color: #4B5563; margin-top: 30px; border-top: 1px solid #E5E7EB; padding-top: 8px; }
            </style>
        </head>
        <body>
            <table class="header-top">
                <tr>
                    <td style="text-align: right; width: 35%; font-size: 12px; line-height: 1.5;">
                        <strong style="font-size: 14px;">مؤسسة آصرة العرب</strong><br>
                        سجل تجاري : 2050202574<br>
                        جوال: 0532334727
                    </td>
                    <td style="text-align: center; width: 30%;">
                        $ARAB_BOND_SVG_LOGO
                    </td>
                    <td style="text-align: left; width: 35%; font-size: 12px; line-height: 1.5; direction: ltr;">
                        <strong style="font-size: 14px;">Arab Bond EST</strong><br>
                        C.R : 2050202574<br>
                        Mob : 0532334727
                    </td>
                </tr>
            </table>

            <div class="title-box">
                <div class="title-main">أمر تحميل</div>
                <div class="title-sub">Load Order</div>
            </div>

            <table class="meta-table">
                <tr>
                    <td class="meta-label" style="width: 14%;">رقم الطلب</td>
                    <td style="width: 16%; text-align: center; font-weight: bold;">Order No</td>
                    <td style="width: 20%; text-align: center; font-family: monospace; font-size: 13px; font-weight: bold;">${order.orderNumber}</td>
                    <td class="meta-label" style="width: 14%;">تاريخ الطلب</td>
                    <td style="width: 16%; text-align: center; font-weight: bold;">Order Date</td>
                    <td style="width: 20%; text-align: center;">$orderRecordedDate</td>
                </tr>
                <tr>
                    <td class="meta-label">اسم البائع/المندوب</td>
                    <td colspan="5" style="text-align: right; padding-right: 15px; font-weight: bold;">${order.recipientName} ${if (order.destination.isNotBlank()) "(${order.destination})" else ""}</td>
                </tr>
            </table>

            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 20%;">كود الصنف<br><span style="font-weight: normal; font-size: 11px;">Item No</span></th>
                        <th style="width: 50%;">اسم الصنف<br><span style="font-weight: normal; font-size: 11px;">Item Name</span></th>
                        <th style="width: 15%;">الوحدة<br><span style="font-weight: normal; font-size: 11px;">Unit</span></th>
                        <th style="width: 15%;">الكمية<br><span style="font-weight: normal; font-size: 11px;">Qty</span></th>
                    </tr>
                </thead>
                <tbody>
                    $itemsRows
                </tbody>
            </table>

            <div style="margin-top: 15px; font-size: 12px; font-weight: bold; background: #F9FAFB; padding: 10px; border: 1px solid #000; border-radius: 4px;">
                إجمالي الكميات المحملة: ${order.totalCartons} كرتون / عبوة | ${order.totalPieces} حبة
                ${if (order.notes.isNotBlank()) " | ملاحظات: ${order.notes}" else ""}
            </div>

            <table style="width: 100%; margin-top: 35px; font-size: 12px;">
                <tr>
                    <td style="width: 50%; text-align: center;">
                        <strong>أمين المستودع (مسؤول الصرف):</strong> ${order.supervisorName}<br><br>
                        التوقيع: ...........................................
                    </td>
                    <td style="width: 50%; text-align: center;">
                        <strong>اسم المندوب / السائق المستلم:</strong> ${order.recipientName}<br><br>
                        التوقيع: ...........................................
                    </td>
                </tr>
            </table>

            <div class="footer-text">
                صفحة 1 من 1 &nbsp;&nbsp;|&nbsp;&nbsp; Page 1 of 1
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Generate HTML for official Unload Order (أمر تفريغ - إضافة/إعادة للمخزون)
     * Matches Arab Bond EST Unload Order template from user's images
     */
    fun generateUnloadOrderHtml(order: ReturnOrderEntity): String {
        val items = parseOrderItems(order.itemsJson)
        val orderRecordedDate = standardDateFormat.format(Date(order.timestamp))

        val itemsRows = StringBuilder()
        for (item in items) {
            val qtyDisplay = if (item.cartons > 0 && item.pieces > 0) {
                "${item.cartons} كرتون و ${item.pieces} حبة"
            } else if (item.cartons > 0) {
                "${item.cartons}"
            } else {
                "${item.pieces}"
            }
            val unitDisplay = if (item.cartons > 0) "كرتونة" else "حبة"

            itemsRows.append(
                """
                <tr>
                    <td style="border: 1px solid #000; padding: 6px 8px; text-align: center; font-family: monospace; font-weight: bold;">${item.itemCode}</td>
                    <td style="border: 1px solid #000; padding: 6px 8px; text-align: right; font-weight: bold;">${item.itemName}</td>
                    <td style="border: 1px solid #000; padding: 6px 8px; text-align: center;">$unitDisplay</td>
                    <td style="border: 1px solid #000; padding: 6px 8px; text-align: center; font-weight: bold;">$qtyDisplay</td>
                </tr>
                """.trimIndent()
            )
        }

        return """
        <!DOCTYPE html>
        <html dir="rtl" lang="ar">
        <head>
            <meta charset="UTF-8">
            <title>أمر تفريغ - ${order.orderNumber}</title>
            <style>
                @page { size: A4; margin: 12mm; }
                body { font-family: 'Segoe UI', Tahoma, Arial, sans-serif; margin: 0; padding: 10px; color: #000000; background: #FFFFFF; }
                .header-top { width: 100%; border-collapse: collapse; margin-bottom: 12px; }
                .header-top td { vertical-align: top; }
                .title-box { text-align: center; margin: 12px 0 10px 0; }
                .title-main { font-size: 22px; font-weight: bold; color: #000; margin: 0; }
                .title-sub { font-size: 15px; font-weight: bold; color: #333; margin-top: 2px; }
                
                .meta-table { width: 100%; border-collapse: collapse; margin-bottom: 15px; border: 1px solid #000; }
                .meta-table td { border: 1px solid #000; padding: 6px 10px; font-size: 12px; }
                .meta-label { font-weight: bold; background-color: #F3F4F6; text-align: center; }
                
                .data-table { width: 100%; border-collapse: collapse; border: 1px solid #000; margin-bottom: 20px; }
                .data-table th { border: 1px solid #000; padding: 8px; background-color: #E5E7EB; text-align: center; font-weight: bold; font-size: 12px; color: #000; }
                .data-table td { border: 1px solid #000; padding: 6px 8px; font-size: 12px; }
                
                .footer-text { text-align: center; font-size: 11px; color: #4B5563; margin-top: 30px; border-top: 1px solid #E5E7EB; padding-top: 8px; }
            </style>
        </head>
        <body>
            <table class="header-top">
                <tr>
                    <td style="text-align: right; width: 35%; font-size: 12px; line-height: 1.5;">
                        <strong style="font-size: 14px;">مؤسسة آصرة العرب</strong><br>
                        سجل تجاري : 2050202574<br>
                        جوال: 0532334727
                    </td>
                    <td style="text-align: center; width: 30%;">
                        $ARAB_BOND_SVG_LOGO
                    </td>
                    <td style="text-align: left; width: 35%; font-size: 12px; line-height: 1.5; direction: ltr;">
                        <strong style="font-size: 14px;">Arab Bond EST</strong><br>
                        C.R : 2050202574<br>
                        Mob : 0532334727
                    </td>
                </tr>
            </table>

            <div class="title-box">
                <div class="title-main">أمر تفريغ</div>
                <div class="title-sub">Unload Order</div>
            </div>

            <table class="meta-table">
                <tr>
                    <td class="meta-label" style="width: 14%;">رقم الطلب</td>
                    <td style="width: 16%; text-align: center; font-weight: bold;">Order No</td>
                    <td style="width: 20%; text-align: center; font-family: monospace; font-size: 13px; font-weight: bold;">${order.orderNumber}</td>
                    <td class="meta-label" style="width: 14%;">تاريخ الطلب</td>
                    <td style="width: 16%; text-align: center; font-weight: bold;">Order Date</td>
                    <td style="width: 20%; text-align: center;">$orderRecordedDate</td>
                </tr>
                <tr>
                    <td class="meta-label">اسم البائع/المندوب</td>
                    <td colspan="5" style="text-align: right; padding-right: 15px; font-weight: bold;">${order.supervisorName} | سبب التفريغ: ${order.reason}</td>
                </tr>
            </table>

            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 20%;">كود الصنف<br><span style="font-weight: normal; font-size: 11px;">Item No</span></th>
                        <th style="width: 50%;">اسم الصنف<br><span style="font-weight: normal; font-size: 11px;">Item Name</span></th>
                        <th style="width: 15%;">الوحدة<br><span style="font-weight: normal; font-size: 11px;">Unit</span></th>
                        <th style="width: 15%;">الكمية<br><span style="font-weight: normal; font-size: 11px;">Qty</span></th>
                    </tr>
                </thead>
                <tbody>
                    $itemsRows
                </tbody>
            </table>

            <div style="margin-top: 15px; font-size: 12px; font-weight: bold; background: #F9FAFB; padding: 10px; border: 1px solid #000; border-radius: 4px;">
                إجمالي الكميات المفرغة والمسترجعة للمخزون: ${order.totalCartons} كرتون / عبوة | ${order.totalPieces} حبة
                ${if (order.notes.isNotBlank()) " | ملاحظات: ${order.notes}" else ""}
            </div>

            <table style="width: 100%; margin-top: 35px; font-size: 12px;">
                <tr>
                    <td style="width: 50%; text-align: center;">
                        <strong>مسؤول استلام التفريغ بالمستودع:</strong> ${order.supervisorName}<br><br>
                        التوقيع: ...........................................
                    </td>
                    <td style="width: 50%; text-align: center;">
                        <strong>اسم المندوب المسلّم:</strong> ...........................................<br><br>
                        التوقيع: ...........................................
                    </td>
                </tr>
            </table>

            <div class="footer-text">
                صفحة 1 من 1 &nbsp;&nbsp;|&nbsp;&nbsp; Page 1 of 1
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Generate HTML for official damage / destruction voucher (محضر إتلاف بضاعة رسمية)
     * Embeds the attached photo in Base64 if available
     */
    fun generateDamageVoucherHtml(record: DamageRecordEntity, context: Context? = null): String {
        val recordRecordedDate = fullRealTimeFormat.format(Date(record.timestamp))
        val currentActualDate = fullRealTimeFormat.format(Date())

        val base64Img = context?.let { uriToBase64(it, record.photoUri) }
        val photoSectionHtml = if (base64Img != null) {
            """
            <div style="margin-top: 20px; text-align: center; border: 1px solid #FECACA; border-radius: 10px; padding: 12px; background: #FEF2F2;">
                <div style="font-weight: bold; color: #DC2626; margin-bottom: 8px; font-size: 13px;">📸 الصورة المرفقة كإثبات للتالف:</div>
                <img src="$base64Img" style="max-width: 100%; max-height: 280px; border-radius: 8px; border: 1px solid #CBD5E1;" />
            </div>
            """.trimIndent()
        } else ""

        return """
        <!DOCTYPE html>
        <html dir="rtl" lang="ar">
        <head>
            <meta charset="UTF-8">
            <title>محضر إتلاف بضاعة - ${record.damageNumber}</title>
            <style>
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif, Arial;
                    margin: 20px;
                    color: #0F172A;
                    background-color: #FFFFFF;
                }
                .voucher-container {
                    border: 2px solid #DC2626;
                    border-radius: 16px;
                    padding: 24px;
                    max-width: 820px;
                    margin: auto;
                    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
                }
                .header-table {
                    width: 100%;
                    border-bottom: 2px solid #DC2626;
                    padding-bottom: 14px;
                    margin-bottom: 16px;
                }
                .brand-title {
                    font-size: 22px;
                    font-weight: 900;
                    color: #1E293B;
                    margin: 0;
                }
                .brand-sub {
                    font-size: 12px;
                    color: #64748B;
                    margin-top: 3px;
                }
                .badge-title {
                    background: #DC2626;
                    color: #FFFFFF;
                    padding: 8px 18px;
                    border-radius: 8px;
                    font-size: 17px;
                    font-weight: bold;
                    display: inline-block;
                }
                .realtime-box {
                    background: #FEF2F2;
                    border: 1px solid #FECACA;
                    border-radius: 10px;
                    padding: 10px 14px;
                    margin-bottom: 16px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }
                .meta-table {
                    width: 100%;
                    background: #F8FAFC;
                    border: 1px solid #E2E8F0;
                    border-radius: 10px;
                    padding: 12px;
                    margin-bottom: 20px;
                }
                .meta-cell {
                    padding: 6px 12px;
                    font-size: 13px;
                }
                .meta-label {
                    color: #64748B;
                    font-weight: bold;
                }
                .meta-val {
                    color: #0F172A;
                    font-weight: bold;
                }
                .items-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 24px;
                }
                .items-table th {
                    background: #1E293B;
                    color: #FFFFFF;
                    padding: 10px;
                    font-size: 13px;
                }
                .signatures {
                    margin-top: 30px;
                    width: 100%;
                }
                .sig-box {
                    width: 45%;
                    border: 1px dashed #CBD5E1;
                    border-radius: 8px;
                    padding: 12px;
                    text-align: center;
                    font-size: 13px;
                    font-weight: bold;
                    background: #F8FAFC;
                }
                .footer {
                    margin-top: 24px;
                    text-align: center;
                    font-size: 11px;
                    color: #64748B;
                    border-top: 1px solid #E2E8F0;
                    padding-top: 10px;
                }
            </style>
        </head>
        <body>
            <div class="voucher-container">
                <table class="header-table">
                    <tr>
                        <td style="width: 85px; vertical-align: middle;">
                            $ARAB_BOND_SVG_LOGO
                        </td>
                        <td style="text-align: right; vertical-align: middle; padding-right: 12px;">
                            <h1 class="brand-title">مؤسسة آصرة العرب <span style="font-size: 15px; color: #D97706; font-weight: 800;">ARAB BOND EST</span></h1>
                            <div class="brand-sub">إدارة المستودعات والرقابة المخزنية | محضر إتلاف رسمي معتمد</div>
                            <div class="brand-sub">نظام إلكتروني معتمد لمراقبة وحماية الأرصدة المخزنية</div>
                        </td>
                        <td style="text-align: left; vertical-align: middle; width: 35%;">
                            <div class="badge-title">محضر إتلاف بضاعة</div>
                            <div style="font-family: monospace; font-size: 16px; font-weight: bold; color: #DC2626; margin-top: 6px;">
                                ${record.damageNumber}
                            </div>
                        </td>
                    </tr>
                </table>

                <div class="realtime-box">
                    <div style="font-size: 13px; color: #991B1B; font-weight: 800;">
                        ⏰ الوقت والتاريخ الفعلي للطباعة: <span style="color: #0F172A; font-weight: bold;">$currentActualDate</span>
                    </div>
                    <div style="font-size: 12px; color: #64748B;">
                        تاريخ توثيق المحضر: $recordRecordedDate
                    </div>
                </div>

                <table class="meta-table">
                    <tr>
                        <td class="meta-cell"><span class="meta-label">رقم المحضر: </span><span class="meta-val" style="color: #DC2626; font-family: monospace;">${record.damageNumber}</span></td>
                        <td class="meta-cell"><span class="meta-label">سبب التلف: </span><span class="meta-val" style="color: #DC2626;">${record.reason}</span></td>
                    </tr>
                    <tr>
                        <td class="meta-cell"><span class="meta-label">مسؤول الفحص والإتلاف: </span><span class="meta-val">${record.supervisorName}</span></td>
                        <td class="meta-cell"><span class="meta-label">قيمة الخسارة التقديرية: </span><span class="meta-val">${"%.2f".format(record.estimatedLossValue)} ريال سعودي</span></td>
                    </tr>
                    ${if (record.notes.isNotBlank()) "<tr><td colspan='2' class='meta-cell'><span class='meta-label'>ملاحظات المعاينة: </span><span class='meta-val'>${record.notes}</span></td></tr>" else ""}
                </table>

                <table class="items-table">
                    <thead>
                        <tr>
                            <th style="width: 15%;">كود الصنف</th>
                            <th style="width: 45%;">اسم الصنف الغذائي التالف</th>
                            <th style="width: 20%;">الكمية التالفة (كرتون)</th>
                            <th style="width: 20%;">الكمية التالفة (حبة)</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td style="text-align: center; padding: 12px; border-bottom: 1px solid #E2E8F0; font-family: monospace; font-weight: bold; color: #1E3A8A;">${record.itemCode}</td>
                            <td style="text-align: right; padding: 12px; border-bottom: 1px solid #E2E8F0; font-weight: bold; color: #0F172A;">${record.itemName}</td>
                            <td style="text-align: center; padding: 12px; border-bottom: 1px solid #E2E8F0; font-weight: bold; color: #DC2626;">${record.cartons} كرتون</td>
                            <td style="text-align: center; padding: 12px; border-bottom: 1px solid #E2E8F0; font-weight: bold; color: #DC2626;">${record.pieces} حبة</td>
                        </tr>
                    </tbody>
                </table>

                $photoSectionHtml

                <table class="signatures">
                    <tr>
                        <td class="sig-box">
                            مسؤول الجرد والإتلاف<br>
                            <span style="color: #64748B; font-size: 12px;">${record.supervisorName}</span><br><br>
                            التوقيع / الختم: .......................................
                        </td>
                        <td style="width: 10%;"></td>
                        <td class="sig-box">
                            مدير المستودع العام<br><br><br>
                            التوقيع والاعتماد: .......................................
                        </td>
                    </tr>
                </table>

                <div class="footer">
                    تم إصدار هذا المحضر إلكترونياً وبحماية أمنية من نظام إدارة مخزون <strong>مؤسسة آصرة العرب (ARAB BOND EST)</strong><br>
                    الوقت والتاريخ الفعلي عند الإصدار: $currentActualDate
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Print or export to PDF via Android's native PrintManager
     */
    fun printVoucherHtml(context: Context, htmlContent: String, jobName: String) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                printManager?.let { pm ->
                    val printAdapter = webView.createPrintDocumentAdapter(jobName)
                    pm.print(
                        jobName,
                        printAdapter,
                        PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                            .build()
                    )
                }
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    /**
     * Generate HTML for Sample Voucher (سند صرف عينات للمناديب)
     */
    fun generateSampleVoucherHtml(order: SampleOrderEntity): String {
        val items = parseOrderItems(order.itemsJson)
        val orderRecordedDate = fullRealTimeFormat.format(Date(order.timestamp))
        val currentActualDate = fullRealTimeFormat.format(Date())

        val itemsRows = StringBuilder()
        var index = 1
        var totalQuantityCartons = 0
        var totalQuantityPieces = 0

        for (item in items) {
            totalQuantityCartons += item.cartons
            totalQuantityPieces += item.pieces
            val totalPiecesLine = (item.cartons * item.conversionFactor) + item.pieces
            itemsRows.append(
                """
                <tr>
                    <td style="text-align: center; padding: 10px; border-bottom: 1px solid #E2E8F0;">$index</td>
                    <td style="text-align: center; padding: 10px; border-bottom: 1px solid #E2E8F0; font-family: monospace; font-weight: bold; color: #EA580C;">${item.itemCode}</td>
                    <td style="text-align: right; padding: 10px; border-bottom: 1px solid #E2E8F0; font-weight: bold; color: #0F172A;">${item.itemName}</td>
                    <td style="text-align: center; padding: 10px; border-bottom: 1px solid #E2E8F0; font-weight: bold;">${item.cartons}</td>
                    <td style="text-align: center; padding: 10px; border-bottom: 1px solid #E2E8F0; font-weight: bold;">${item.pieces}</td>
                    <td style="text-align: center; padding: 10px; border-bottom: 1px solid #E2E8F0; color: #EA580C; font-weight: bold;">$totalPiecesLine حبة</td>
                    <td style="text-align: right; padding: 10px; border-bottom: 1px solid #E2E8F0; color: #64748B;">${item.notes.ifBlank { "—" }}</td>
                </tr>
                """.trimIndent()
            )
            index++
        }

        return """
        <!DOCTYPE html>
        <html dir="rtl" lang="ar">
        <head>
            <meta charset="UTF-8">
            <title>سند صرف عينات - ${order.orderNumber}</title>
            <style>
                body { font-family: 'Segoe UI', Tahoma, Arial, sans-serif; background-color: #F8FAFC; color: #0F172A; padding: 20px; direction: rtl; }
                .container { max-width: 850px; margin: 0 auto; background: #FFFFFF; padding: 30px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); border-top: 6px solid #EA580C; }
                .header-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
                .badge { background-color: #FFF7ED; color: #C2410C; border: 1px solid #FDBA74; padding: 6px 12px; border-radius: 6px; font-weight: bold; font-size: 14px; display: inline-block; }
                .info-card { background: #FFF7ED; border: 1px solid #FFEDD5; padding: 16px; border-radius: 8px; margin-bottom: 20px; }
                table.data-table { width: 100%; border-collapse: collapse; margin-top: 15px; }
                table.data-table th { background-color: #EA580C; color: #FFFFFF; padding: 12px; text-align: center; font-size: 13px; }
                .sig-box { text-align: center; padding-top: 30px; font-weight: bold; font-size: 13px; color: #334155; }
                .footer { margin-top: 40px; text-align: center; font-size: 11px; color: #94A3B8; border-top: 1px solid #E2E8F0; padding-top: 15px; }
            </style>
        </head>
        <body>
            <div class="container">
                <table class="header-table">
                    <tr>
                        <td style="width: 20%; text-align: right;">$ARAB_BOND_SVG_LOGO</td>
                        <td style="width: 60%; text-align: center;">
                            <h2 style="margin: 0; color: #EA580C;">مؤسسة آصرة العرب للتجارة والتوزيع</h2>
                            <p style="margin: 4px 0; font-size: 13px; color: #64748B;">إدارة المستودعات - قسم صرف العينات للمناديب</p>
                            <h3 style="margin: 8px 0; color: #0F172A;">سند صرف عينات للمندوب</h3>
                        </td>
                        <td style="width: 20%; text-align: left;">
                            <span class="badge">${order.status}</span>
                        </td>
                    </tr>
                </table>

                <div class="info-card">
                    <table style="width: 100%; font-size: 13px; line-height: 1.8;">
                        <tr>
                            <td><strong>رقم أمر العينات:</strong> <span style="font-family: monospace; font-weight: bold; color: #EA580C;">${order.orderNumber}</span></td>
                            <td><strong>المندوب المستلم:</strong> <span style="font-weight: bold; color: #0F172A;">${order.representativeName}</span></td>
                        </tr>
                        <tr>
                            <td><strong>تاريخ التسجيل:</strong> $orderRecordedDate</td>
                            <td><strong>مسؤول الصرف:</strong> ${order.supervisorName}</td>
                        </tr>
                        ${if (order.notes.isNotBlank()) "<tr><td colspan='2'><strong>ملاحظات:</strong> ${order.notes}</td></tr>" else ""}
                    </table>
                </div>

                <table class="data-table">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>كود الصنف</th>
                            <th>اسم الصنف</th>
                            <th>الكرتون/العبوة</th>
                            <th>الحبة</th>
                            <th>إجمالي الحبات</th>
                            <th>ملاحظات</th>
                        </tr>
                    </thead>
                    <tbody>
                        $itemsRows
                    </tbody>
                </table>

                <div style="background-color: #FFF7ED; padding: 12px; border-radius: 8px; margin-top: 20px; font-weight: bold; text-align: left; color: #9A3412;">
                    إجمالي العينات: $totalQuantityCartons كرتون و $totalQuantityPieces حبة
                </div>

                <table style="width: 100%; margin-top: 40px;">
                    <tr>
                        <td class="sig-box">
                            توقيع المندوب المستلم<br><br><br>
                            .......................................
                        </td>
                        <td class="sig-box">
                            مسؤول قسم العينات والمستودع<br><br><br>
                            .......................................
                        </td>
                    </tr>
                </table>

                <div class="footer">
                    تم إصدار هذا السند إلكترونياً من <strong>مؤسسة آصرة العرب (ARAB BOND EST)</strong><br>
                    الوقت والتاريخ: $currentActualDate
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Share sample order summary text
     */
    fun shareSampleOrderText(context: Context, order: SampleOrderEntity) {
        val items = parseOrderItems(order.itemsJson)
        val orderRecordedDate = fullRealTimeFormat.format(Date(order.timestamp))
        val sb = StringBuilder()
        sb.append("🎁 *مؤسسة آصرة العرب (ARAB BOND EST) - سند صرف عينات*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("🔢 رقم السند: ${order.orderNumber}\n")
        sb.append("📅 التاريخ: $orderRecordedDate\n")
        sb.append("🚗 المندوب المستلم: ${order.representativeName}\n")
        sb.append("👔 مسؤول الصرف: ${order.supervisorName}\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📦 *العينات المصروفة:*\n")
        items.forEachIndexed { i, item ->
            sb.append("${i + 1}) [${item.itemCode}] ${item.itemName}\n")
            sb.append("   • الكمية: ${item.formatQuantity()}\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📊 الإجمالي: ${order.totalCartons} كرتون و ${order.totalPieces} حبة\n")
        if (order.notes.isNotBlank()) {
            sb.append("📝 ملاحظات: ${order.notes}\n")
        }
        sb.append("\n🔒 *سند عينات معتمد*")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة سند العينات")
        context.startActivity(shareIntent)
    }

    /**
     * Share voucher summary via Android Share Sheet
     */
    fun shareIssueOrderText(context: Context, order: IssueOrderEntity) {
        val items = parseOrderItems(order.itemsJson)
        val formattedActualDate = fullRealTimeFormat.format(Date())
        val orderRecordedDate = fullRealTimeFormat.format(Date(order.timestamp))
        val sb = StringBuilder()
        sb.append("📋 *مؤسسة آصرة العرب (ARAB BOND EST) - أمر صرف وتحميل*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("🔢 رقم السند: ${order.orderNumber}\n")
        sb.append("⏰ الوقت والتاريخ الفعلي: $formattedActualDate\n")
        sb.append("📅 تاريخ تسجيل الأمر: $orderRecordedDate\n")
        sb.append("🏢 جهة الصرف: ${order.destination}\n")
        sb.append("👤 المستلم / السائق: ${order.recipientName}\n")
        sb.append("👔 مسؤول الصرف: ${order.supervisorName}\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📦 *الأصناف المعتمدة المصروفة:*\n")
        items.forEachIndexed { i, item ->
            sb.append("${i + 1}) [${item.itemCode}] ${item.itemName}\n")
            sb.append("   • الكمية: ${item.formatQuantity()}\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📊 الإجمالي: ${order.totalCartons} كرتون و ${order.totalPieces} حبة\n")
        if (order.notes.isNotBlank()) {
            sb.append("📝 ملاحظات: ${order.notes}\n")
        }
        sb.append("\n🔒 *سند معتمد ومحصن أمنياً ضد التلاعب*")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة السند المخزني")
        context.startActivity(shareIntent)
    }

    /**
     * Share Unload Order summary text
     */
    fun shareUnloadOrderText(context: Context, order: ReturnOrderEntity) {
        val items = parseOrderItems(order.itemsJson)
        val formattedActualDate = fullRealTimeFormat.format(Date())
        val orderRecordedDate = fullRealTimeFormat.format(Date(order.timestamp))
        val sb = StringBuilder()
        sb.append("🔄 *مؤسسة آصرة العرب (ARAB BOND EST) - أمر تفريغ واسترجاع*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("🔢 رقم السند: ${order.orderNumber}\n")
        sb.append("⏰ الوقت والتاريخ الفعلي: $formattedActualDate\n")
        sb.append("📅 تاريخ التوثيق: $orderRecordedDate\n")
        sb.append("👔 مسؤول التفريغ: ${order.supervisorName}\n")
        sb.append("💡 سبب التفريغ: ${order.reason}\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📦 *الأصناف المفرغة والمعادة للمخزون:*\n")
        items.forEachIndexed { i, item ->
            sb.append("${i + 1}) [${item.itemCode}] ${item.itemName}\n")
            sb.append("   • الكمية: ${item.formatQuantity()}\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📊 الإجمالي المعاد: ${order.totalCartons} كرتون و ${order.totalPieces} حبة\n")
        if (order.notes.isNotBlank()) {
            sb.append("📝 ملاحظات: ${order.notes}\n")
        }
        sb.append("\n🔒 *أمر تفريغ معتمد ومسجل بالنظام*")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة أمر التفريغ")
        context.startActivity(shareIntent)
    }

    /**
     * Share Damage Record summary text
     */
    fun shareDamageRecordText(context: Context, record: DamageRecordEntity) {
        val formattedActualDate = fullRealTimeFormat.format(Date())
        val recordRecordedDate = fullRealTimeFormat.format(Date(record.timestamp))
        val sb = StringBuilder()
        sb.append("⚠️ *مؤسسة آصرة العرب (ARAB BOND EST) - محضر إتلاف بضاعة*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("🔢 رقم المحضر: ${record.damageNumber}\n")
        sb.append("⏰ الوقت والتاريخ: $formattedActualDate\n")
        sb.append("👤 مسؤول المعاينة: ${record.supervisorName}\n")
        sb.append("❌ سبب التلف: ${record.reason}\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📦 *الصنف التالف:*\n")
        sb.append("• [${record.itemCode}] ${record.itemName}\n")
        sb.append("• الكمية التالفة: ${record.cartons} كرتون و ${record.pieces} حبة\n")
        sb.append("• قيمة الخسارة: ${"%.2f".format(record.estimatedLossValue)} ريال\n")
        if (record.photoUri?.isNotBlank() == true) {
            sb.append("📸 يوجد صورة مرفقة للمعاينة بالنظام\n")
        }
        if (record.notes.isNotBlank()) {
            sb.append("📝 ملاحظات: ${record.notes}\n")
        }
        sb.append("\n🔒 *محضر إتلاف رسمي معتمد*")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة محضر التالف")
        context.startActivity(shareIntent)
    }
}
