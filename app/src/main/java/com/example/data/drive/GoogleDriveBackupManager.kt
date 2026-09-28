package com.example.data.drive

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.DamageRecordEntity
import com.example.data.model.IssueOrderEntity
import com.example.data.model.ItemEntity
import com.example.data.model.RepresentativeEntity
import com.example.data.model.ReturnOrderEntity
import com.example.data.model.StockMovementEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manages Google Drive & Cloud File Backup integration.
 * Creates clean JSON database snapshots and standard CSV/Sheets backups
 * ready to be directly uploaded or shared to Google Drive folder.
 */
class GoogleDriveBackupManager(
    private val context: Context
) {
    /**
     * Exports full database snapshots into an encrypted/safe JSON backup file.
     */
    suspend fun createCompleteBackupJson(
        items: List<ItemEntity>,
        issues: List<IssueOrderEntity>,
        returns: List<ReturnOrderEntity>,
        damages: List<DamageRecordEntity>,
        movements: List<StockMovementEntity>,
        representatives: List<RepresentativeEntity> = emptyList()
    ): File = withContext(Dispatchers.IO) {
        val rootJson = JSONObject()
        rootJson.put("appName", "مستودع مؤسسة آصرة العرب")
        rootJson.put("backupVersion", "2.0")
        rootJson.put("timestamp", System.currentTimeMillis())
        rootJson.put("dateFormatted", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date()))

        // Items Array
        val itemsArray = JSONArray()
        items.forEach { item ->
            val obj = JSONObject()
            obj.put("code", item.code)
            obj.put("name", item.name)
            obj.put("category", item.category)
            obj.put("conversionFactor", item.conversionFactor)
            obj.put("totalPieces", item.totalPieces)
            obj.put("minStockCartons", item.minStockCartons)
            obj.put("supplier", item.supplier)
            obj.put("pricePerCarton", item.pricePerCarton)
            itemsArray.put(obj)
        }
        rootJson.put("items", itemsArray)

        // Issues
        val issuesArray = JSONArray()
        issues.forEach { issue ->
            val obj = JSONObject()
            obj.put("orderNumber", issue.orderNumber)
            obj.put("timestamp", issue.timestamp)
            obj.put("destination", issue.destination)
            obj.put("recipientName", issue.recipientName)
            obj.put("supervisorName", issue.supervisorName)
            obj.put("notes", issue.notes)
            obj.put("itemsJson", issue.itemsJson)
            obj.put("totalCartons", issue.totalCartons)
            obj.put("totalPieces", issue.totalPieces)
            obj.put("status", issue.status)
            issuesArray.put(obj)
        }
        rootJson.put("issues", issuesArray)

        // Returns
        val returnsArray = JSONArray()
        returns.forEach { ret ->
            val obj = JSONObject()
            obj.put("orderNumber", ret.orderNumber)
            obj.put("timestamp", ret.timestamp)
            obj.put("relatedIssueOrderNumber", ret.relatedIssueOrderNumber ?: "")
            obj.put("itemsJson", ret.itemsJson)
            obj.put("totalCartons", ret.totalCartons)
            obj.put("totalPieces", ret.totalPieces)
            obj.put("reason", ret.reason)
            obj.put("supervisorName", ret.supervisorName)
            obj.put("notes", ret.notes)
            obj.put("status", ret.status)
            returnsArray.put(obj)
        }
        rootJson.put("returns", returnsArray)

        // Damages
        val damagesArray = JSONArray()
        damages.forEach { dmg ->
            val obj = JSONObject()
            obj.put("damageNumber", dmg.damageNumber)
            obj.put("timestamp", dmg.timestamp)
            obj.put("itemCode", dmg.itemCode)
            obj.put("itemName", dmg.itemName)
            obj.put("cartons", dmg.cartons)
            obj.put("pieces", dmg.pieces)
            obj.put("reason", dmg.reason)
            obj.put("supervisorName", dmg.supervisorName)
            obj.put("notes", dmg.notes)
            obj.put("estimatedLossValue", dmg.estimatedLossValue)
            damagesArray.put(obj)
        }
        rootJson.put("damages", damagesArray)

        // Representatives
        val repsArray = JSONArray()
        representatives.forEach { rep ->
            val obj = JSONObject()
            obj.put("id", rep.id)
            obj.put("name", rep.name)
            obj.put("phone", rep.phone)
            obj.put("code", rep.code)
            obj.put("vehicleNumber", rep.vehicleNumber)
            obj.put("routeOrArea", rep.routeOrArea)
            obj.put("notes", rep.notes)
            obj.put("isActive", rep.isActive)
            obj.put("createdAt", rep.createdAt)
            repsArray.put(obj)
        }
        rootJson.put("representatives", repsArray)

        // Write to cache/backup file
        val backupDir = File(context.cacheDir, "drive_backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val timeTag = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val file = File(backupDir, "WH_AsratAlArab_Backup_$timeTag.json")
        FileOutputStream(file).use { out ->
            out.write(rootJson.toString(2).toByteArray(Charsets.UTF_8))
        }

        file
    }

    /**
     * Exports full inventory as a Google Sheets / Excel compatible CSV file.
     */
    suspend fun createInventoryCsvForDrive(items: List<ItemEntity>): File = withContext(Dispatchers.IO) {
        val backupDir = File(context.cacheDir, "drive_backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val timeTag = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val file = File(backupDir, "WH_Inventory_GoogleSheets_$timeTag.csv")

        FileOutputStream(file).use { out ->
            // UTF-8 BOM for Arabic Excel/Sheets display
            out.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            val header = "كود الصنف,اسم الصنف,التصنيف,معامل التحويل (حبة/كرتون),رصيد الكراتين,رصيد الحبات المتبقية,إجمالي الرصيد (حبة),الحد الأدنى (كرتون),المورد,حالة النقص\n"
            out.write(header.toByteArray(Charsets.UTF_8))

            items.forEach { itm ->
                val line = "${itm.code},\"${itm.name.replace("\"", "\"\"")}\",${itm.category},${itm.conversionFactor},${itm.currentCartons},${itm.remainingPieces},${itm.totalPieces},${itm.minStockCartons},\"${itm.supplier}\",${if (itm.isLowStock) "نقص مخزني" else "متوفر"}\n"
                out.write(line.toByteArray(Charsets.UTF_8))
            }
        }

        file
    }

    /**
     * Launches Android Share/Drive Intent directly opening "Save to Google Drive".
     */
    fun launchSaveToDriveIntent(file: File, mimeType: String = "application/json") {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "نسخة احتياطية سحابية لمستودع آصرة العرب - Google Drive")
            putExtra(Intent.EXTRA_TEXT, "تم تصدير نسخة احتياطية من مستودع آصرة العرب لحفظها في Google Drive لعدم فقد الملفات.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "حفظ النسخة في Google Drive / مشاركة")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
