package com.example.data.sync

import android.content.Context
import android.util.Log
import com.example.data.dao.WarehouseDao
import com.example.data.model.DamageRecordEntity
import com.example.data.model.IssueOrderEntity
import com.example.data.model.ItemEntity
import com.example.data.model.RepresentativeEntity
import com.example.data.model.ReturnOrderEntity
import com.example.data.model.StockMovementEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed class FirebaseSyncStatus {
    object Unconfigured : FirebaseSyncStatus()
    object Idle : FirebaseSyncStatus()
    object Syncing : FirebaseSyncStatus()
    data class Success(val message: String, val timestamp: Long) : FirebaseSyncStatus()
    data class Error(val errorMessage: String) : FirebaseSyncStatus()
}

class FirebaseSyncManager(
    private val context: Context,
    private val dao: WarehouseDao
) {
    companion object {
        private const val TAG = "FirebaseSync"
        const val APPLICATION_ID = "WH_arabbond.com"

        // Firestore collections
        const val COL_ITEMS = "warehouse_items"
        const val COL_ISSUE_ORDERS = "warehouse_issue_orders"
        const val COL_RETURN_ORDERS = "warehouse_return_orders"
        const val COL_DAMAGE_RECORDS = "warehouse_damage_records"
        const val COL_MOVEMENTS = "warehouse_movements"
        const val COL_REPRESENTATIVES = "warehouse_representatives"
    }

    private val _syncStatus = MutableStateFlow<FirebaseSyncStatus>(FirebaseSyncStatus.Unconfigured)
    val syncStatus: StateFlow<FirebaseSyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

    init {
        checkFirebaseAvailability()
    }

    /**
     * Checks if Firebase is initialized in the environment.
     */
    fun isFirebaseInitialized(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase check failed: ${e.message}")
            false
        }
    }

    private fun checkFirebaseAvailability() {
        if (isFirebaseInitialized()) {
            _syncStatus.value = FirebaseSyncStatus.Idle
        } else {
            _syncStatus.value = FirebaseSyncStatus.Unconfigured
        }
    }

    private fun getFirestore(): FirebaseFirestore? {
        return if (isFirebaseInitialized()) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get Firestore instance: ${e.message}")
                null
            }
        } else {
            null
        }
    }

    /**
     * Uploads all local database records directly from Room database to Cloud Firestore.
     */
    suspend fun uploadAllToCloudFromDatabase(): Result<String> = withContext(Dispatchers.IO) {
        val items = dao.getAllItemsList()
        val issueOrders = dao.getAllIssueOrdersList()
        val returnOrders = dao.getAllReturnOrdersList()
        val damageRecords = dao.getAllDamageRecordsList()
        val movements = dao.getAllMovementsList()
        val reps = dao.getAllRepresentativesList()
        uploadAllToCloud(items, issueOrders, returnOrders, damageRecords, movements, reps)
    }

    /**
     * Uploads all local database records to Cloud Firestore.
     */
    suspend fun uploadAllToCloud(
        items: List<ItemEntity>,
        issueOrders: List<IssueOrderEntity>,
        returnOrders: List<ReturnOrderEntity>,
        damageRecords: List<DamageRecordEntity>,
        movements: List<StockMovementEntity>,
        representatives: List<RepresentativeEntity> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!isFirebaseInitialized()) {
            val msg = "لم يتم تهيئة Firebase بعد. يرجى إضافة ملف google-services.json لمشروع التطبيق."
            _syncStatus.value = FirebaseSyncStatus.Error(msg)
            return@withContext Result.failure(IllegalStateException(msg))
        }

        val db = getFirestore() ?: run {
            val msg = "تعذر الاتصال بـ Cloud Firestore"
            _syncStatus.value = FirebaseSyncStatus.Error(msg)
            return@withContext Result.failure(IllegalStateException(msg))
        }

        _syncStatus.value = FirebaseSyncStatus.Syncing

        try {
            var uploadedCount = 0

            // 1. Upload Items
            for (item in items) {
                val movTime = dao.getLatestMovementTimestampForItem(item.code) ?: 0L
                val effectiveTime = maxOf(item.lastUpdated, movTime, System.currentTimeMillis())
                val data = mapOf(
                    "code" to item.code,
                    "name" to item.name,
                    "category" to item.category,
                    "conversionFactor" to item.conversionFactor,
                    "totalPieces" to item.totalPieces,
                    "minStockCartons" to item.minStockCartons,
                    "supplier" to item.supplier,
                    "pricePerCarton" to item.pricePerCarton,
                    "lastUpdated" to effectiveTime
                )
                db.collection(COL_ITEMS).document(item.code).set(data, SetOptions.merge()).awaitTask()
                uploadedCount++
            }

            // 2. Upload Issue Orders
            for (order in issueOrders) {
                val data = mapOf(
                    "orderNumber" to order.orderNumber,
                    "timestamp" to order.timestamp,
                    "destination" to order.destination,
                    "recipientName" to order.recipientName,
                    "supervisorName" to order.supervisorName,
                    "notes" to order.notes,
                    "totalCartons" to order.totalCartons,
                    "totalPieces" to order.totalPieces,
                    "itemsJson" to order.itemsJson
                )
                db.collection(COL_ISSUE_ORDERS).document(order.orderNumber).set(data, SetOptions.merge()).awaitTask()
                uploadedCount++
            }

            // 3. Upload Return Orders
            for (order in returnOrders) {
                val data = mapOf(
                    "orderNumber" to order.orderNumber,
                    "timestamp" to order.timestamp,
                    "itemsJson" to order.itemsJson,
                    "totalCartons" to order.totalCartons,
                    "totalPieces" to order.totalPieces,
                    "reason" to order.reason,
                    "relatedIssueOrderNumber" to (order.relatedIssueOrderNumber ?: ""),
                    "supervisorName" to order.supervisorName,
                    "notes" to order.notes,
                    "status" to order.status
                )
                db.collection(COL_RETURN_ORDERS).document(order.orderNumber).set(data, SetOptions.merge()).awaitTask()
                uploadedCount++
            }

            // 4. Upload Damage Records
            for (rec in damageRecords) {
                val data = mapOf(
                    "damageNumber" to rec.damageNumber,
                    "timestamp" to rec.timestamp,
                    "itemCode" to rec.itemCode,
                    "itemName" to rec.itemName,
                    "cartons" to rec.cartons,
                    "pieces" to rec.pieces,
                    "reason" to rec.reason,
                    "estimatedLossValue" to rec.estimatedLossValue,
                    "supervisorName" to rec.supervisorName,
                    "photoUri" to (rec.photoUri ?: ""),
                    "notes" to rec.notes
                )
                db.collection(COL_DAMAGE_RECORDS).document(rec.damageNumber).set(data, SetOptions.merge()).awaitTask()
                uploadedCount++
            }

            // 5. Upload Movements (Recent 200)
            for (mov in movements.take(200)) {
                val data = mapOf(
                    "movementNumber" to mov.movementNumber,
                    "timestamp" to mov.timestamp,
                    "movementType" to mov.movementType,
                    "itemCode" to mov.itemCode,
                    "itemName" to mov.itemName,
                    "cartons" to mov.cartons,
                    "pieces" to mov.pieces,
                    "totalPiecesDelta" to mov.totalPiecesDelta,
                    "stockAfterCartons" to mov.stockAfterCartons,
                    "stockAfterPieces" to mov.stockAfterPieces,
                    "totalPiecesAfter" to mov.totalPiecesAfter,
                    "relatedOrderNumber" to (mov.relatedOrderNumber ?: ""),
                    "notes" to mov.notes,
                    "operatorName" to mov.operatorName
                )
                db.collection(COL_MOVEMENTS).document(mov.movementNumber).set(data, SetOptions.merge()).awaitTask()
                uploadedCount++
            }

            // 6. Upload Representatives
            for (rep in representatives) {
                val data = mapOf(
                    "id" to rep.id,
                    "name" to rep.name,
                    "phone" to rep.phone,
                    "code" to rep.code,
                    "vehicleNumber" to rep.vehicleNumber,
                    "routeOrArea" to rep.routeOrArea,
                    "notes" to rep.notes,
                    "isActive" to rep.isActive,
                    "createdAt" to rep.createdAt
                )
                db.collection(COL_REPRESENTATIVES).document("rep_${rep.id}").set(data, SetOptions.merge()).awaitTask()
                uploadedCount++
            }

            val now = System.currentTimeMillis()
            _lastSyncTime.value = now
            val successMsg = "تم حفظ ورفع $uploadedCount سجلاً سحابياً بنجاح إلى Firebase"
            _syncStatus.value = FirebaseSyncStatus.Success(successMsg, now)
            Result.success(successMsg)
        } catch (e: Exception) {
            val isPermissionDenied = e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ||
                                     e.cause?.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ||
                                     e is com.google.firebase.firestore.FirebaseFirestoreException
            if (isPermissionDenied) {
                Log.w(TAG, "Firebase permission notice: Firestore security rules or authentication required. Local SQLite data is completely safe.")
                val errorMsg = "تنبيه السحابة: يتطلب Firebase تسجيل الدخول بحساب قوقل أو ضبط قواعد Firestore. جميع بياناتك آمنة ومحفوظة محلياً."
                _syncStatus.value = FirebaseSyncStatus.Error(errorMsg)
                Result.failure(Exception(errorMsg, e))
            } else {
                Log.e(TAG, "Sync error: ${e.message}", e)
                val errorMsg = "فشلت المزامنة: ${e.localizedMessage ?: e.message}"
                _syncStatus.value = FirebaseSyncStatus.Error(errorMsg)
                Result.failure(e)
            }
        }
    }

    /**
     * Smart Bidirectional Synchronization (مزامنة ذكية ثنائية الاتجاه).
     * Compares local and cloud data by timestamps and IDs:
     * - Protects local changes so newer local data is NEVER overwritten by older cloud data.
     * - Uploads newer or missing local records to Cloud Firestore.
     * - Downloads and merges newer or missing cloud records into local SQLite database.
     */
    suspend fun syncWithCloud(): Result<String> = withContext(Dispatchers.IO) {
        if (!isFirebaseInitialized()) {
            val msg = "لم يتم تهيئة Firebase بعد. يرجى إضافة ملف google-services.json."
            _syncStatus.value = FirebaseSyncStatus.Error(msg)
            return@withContext Result.failure(IllegalStateException(msg))
        }

        val db = getFirestore() ?: run {
            val msg = "تعذر الاتصال بقاعدة بيانات Cloud Firestore"
            _syncStatus.value = FirebaseSyncStatus.Error(msg)
            return@withContext Result.failure(IllegalStateException(msg))
        }

        _syncStatus.value = FirebaseSyncStatus.Syncing

        try {
            var uploadedCount = 0
            var downloadedCount = 0

            // Read authoritative local data directly from Room SQLite
            val localItems = dao.getAllItemsList()
            val localItemMap = localItems.associateBy { it.code }.toMutableMap()

            // 1. Sync Items with Conflict Resolution (Newer Timestamp Wins)
            val itemDocs = db.collection(COL_ITEMS).get().awaitTask()
            val cloudItemCodes = mutableSetOf<String>()

            for (doc in itemDocs.documents) {
                val code = doc.getString("code") ?: continue
                cloudItemCodes.add(code)
                val cloudLastUpdated = doc.getLong("lastUpdated") ?: 0L
                val name = doc.getString("name") ?: ""
                val category = doc.getString("category") ?: "عام"
                val conversionFactor = doc.getLong("conversionFactor")?.toInt() ?: 1
                val cloudTotalPieces = doc.getLong("totalPieces")?.toInt() ?: 0
                val minStockCartons = doc.getLong("minStockCartons")?.toInt() ?: 5
                val supplier = doc.getString("supplier") ?: ""
                val price = doc.getDouble("pricePerCarton") ?: 0.0

                val cloudItem = ItemEntity(
                    code = code,
                    name = name,
                    category = category,
                    conversionFactor = conversionFactor,
                    totalPieces = cloudTotalPieces,
                    minStockCartons = minStockCartons,
                    supplier = supplier,
                    pricePerCarton = price,
                    lastUpdated = cloudLastUpdated
                )

                val local = localItemMap[code]
                if (local != null) {
                    val localMovTime = dao.getLatestMovementTimestampForItem(code) ?: 0L
                    val localEffectiveTime = maxOf(local.lastUpdated, localMovTime)

                    if (localEffectiveTime >= cloudLastUpdated) {
                        // LOCAL IS NEWER OR EQUAL!
                        // If cloud is outdated or missing latest stock, push local to cloud!
                        if (local.totalPieces != cloudTotalPieces || localEffectiveTime > cloudLastUpdated) {
                            val data = mapOf(
                                "code" to local.code,
                                "name" to local.name,
                                "category" to local.category,
                                "conversionFactor" to local.conversionFactor,
                                "totalPieces" to local.totalPieces,
                                "minStockCartons" to local.minStockCartons,
                                "supplier" to local.supplier,
                                "pricePerCarton" to local.pricePerCarton,
                                "lastUpdated" to localEffectiveTime
                            )
                            db.collection(COL_ITEMS).document(local.code).set(data, SetOptions.merge()).awaitTask()
                            uploadedCount++
                        }
                        // NEVER overwrite local with older cloud data
                    } else {
                        // Cloud is strictly newer! Update local
                        dao.insertItem(cloudItem)
                        downloadedCount++
                    }
                } else {
                    // Item in cloud doesn't exist locally: add to local
                    dao.insertItem(cloudItem)
                    downloadedCount++
                }
            }

            // Any local items that do NOT exist in Cloud -> upload to Cloud
            for (local in localItems) {
                if (!cloudItemCodes.contains(local.code)) {
                    val localMovTime = dao.getLatestMovementTimestampForItem(local.code) ?: 0L
                    val localEffectiveTime = maxOf(local.lastUpdated, localMovTime, System.currentTimeMillis())
                    val data = mapOf(
                        "code" to local.code,
                        "name" to local.name,
                        "category" to local.category,
                        "conversionFactor" to local.conversionFactor,
                        "totalPieces" to local.totalPieces,
                        "minStockCartons" to local.minStockCartons,
                        "supplier" to local.supplier,
                        "pricePerCarton" to local.pricePerCarton,
                        "lastUpdated" to localEffectiveTime
                    )
                    db.collection(COL_ITEMS).document(local.code).set(data, SetOptions.merge()).awaitTask()
                    uploadedCount++
                }
            }

            // 2. Sync Issue Orders (Non-destructive merge by orderNumber)
            val localIssues = dao.getAllIssueOrdersList()
            val localIssueMap = localIssues.associateBy { it.orderNumber }
            val issueDocs = db.collection(COL_ISSUE_ORDERS).get().awaitTask()
            val cloudIssueNumbers = mutableSetOf<String>()

            for (doc in issueDocs.documents) {
                val orderNumber = doc.getString("orderNumber") ?: continue
                cloudIssueNumbers.add(orderNumber)
                if (!localIssueMap.containsKey(orderNumber)) {
                    // New issue order from cloud -> insert into local
                    val order = IssueOrderEntity(
                        orderNumber = orderNumber,
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        destination = doc.getString("destination") ?: "",
                        recipientName = doc.getString("recipientName") ?: "",
                        supervisorName = doc.getString("supervisorName") ?: "",
                        notes = doc.getString("notes") ?: "",
                        totalCartons = doc.getLong("totalCartons")?.toInt() ?: 0,
                        totalPieces = doc.getLong("totalPieces")?.toInt() ?: 0,
                        itemsJson = doc.getString("itemsJson") ?: "[]"
                    )
                    dao.insertIssueOrder(order)
                    downloadedCount++
                }
            }
            // Local issue orders not in cloud -> upload to cloud
            for (order in localIssues) {
                if (!cloudIssueNumbers.contains(order.orderNumber)) {
                    val data = mapOf(
                        "orderNumber" to order.orderNumber,
                        "timestamp" to order.timestamp,
                        "destination" to order.destination,
                        "recipientName" to order.recipientName,
                        "supervisorName" to order.supervisorName,
                        "notes" to order.notes,
                        "totalCartons" to order.totalCartons,
                        "totalPieces" to order.totalPieces,
                        "itemsJson" to order.itemsJson
                    )
                    db.collection(COL_ISSUE_ORDERS).document(order.orderNumber).set(data, SetOptions.merge()).awaitTask()
                    uploadedCount++
                }
            }

            // 3. Sync Return Orders (Merge by orderNumber)
            val localReturns = dao.getAllReturnOrdersList()
            val localReturnMap = localReturns.associateBy { it.orderNumber }
            val returnDocs = db.collection(COL_RETURN_ORDERS).get().awaitTask()
            val cloudReturnNumbers = mutableSetOf<String>()

            for (doc in returnDocs.documents) {
                val orderNumber = doc.getString("orderNumber") ?: continue
                cloudReturnNumbers.add(orderNumber)
                if (!localReturnMap.containsKey(orderNumber)) {
                    val ret = ReturnOrderEntity(
                        orderNumber = orderNumber,
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        relatedIssueOrderNumber = doc.getString("relatedIssueOrderNumber"),
                        itemsJson = doc.getString("itemsJson") ?: "",
                        totalCartons = (doc.getLong("totalCartons") ?: 0L).toInt(),
                        totalPieces = (doc.getLong("totalPieces") ?: 0L).toInt(),
                        reason = doc.getString("reason") ?: "",
                        supervisorName = doc.getString("supervisorName") ?: "مشرف المستودع",
                        notes = doc.getString("notes") ?: "",
                        status = doc.getString("status") ?: "معتمد"
                    )
                    dao.insertReturnOrder(ret)
                    downloadedCount++
                }
            }
            for (ret in localReturns) {
                if (!cloudReturnNumbers.contains(ret.orderNumber)) {
                    val data = mapOf(
                        "orderNumber" to ret.orderNumber,
                        "timestamp" to ret.timestamp,
                        "itemsJson" to ret.itemsJson,
                        "totalCartons" to ret.totalCartons,
                        "totalPieces" to ret.totalPieces,
                        "reason" to ret.reason,
                        "relatedIssueOrderNumber" to (ret.relatedIssueOrderNumber ?: ""),
                        "supervisorName" to ret.supervisorName,
                        "notes" to ret.notes,
                        "status" to ret.status
                    )
                    db.collection(COL_RETURN_ORDERS).document(ret.orderNumber).set(data, SetOptions.merge()).awaitTask()
                    uploadedCount++
                }
            }

            // 4. Sync Damage Records (Merge by damageNumber)
            val localDamages = dao.getAllDamageRecordsList()
            val localDamageMap = localDamages.associateBy { it.damageNumber }
            val damageDocs = db.collection(COL_DAMAGE_RECORDS).get().awaitTask()
            val cloudDamageNumbers = mutableSetOf<String>()

            for (doc in damageDocs.documents) {
                val damageNumber = doc.getString("damageNumber") ?: continue
                cloudDamageNumbers.add(damageNumber)
                if (!localDamageMap.containsKey(damageNumber)) {
                    val rec = DamageRecordEntity(
                        damageNumber = damageNumber,
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        itemCode = doc.getString("itemCode") ?: "",
                        itemName = doc.getString("itemName") ?: "",
                        cartons = doc.getLong("cartons")?.toInt() ?: 0,
                        pieces = doc.getLong("pieces")?.toInt() ?: 0,
                        reason = doc.getString("reason") ?: "",
                        supervisorName = doc.getString("supervisorName") ?: "مشرف المستودع",
                        notes = doc.getString("notes") ?: "",
                        photoUri = doc.getString("photoUri"),
                        estimatedLossValue = doc.getDouble("estimatedLossValue") ?: 0.0
                    )
                    dao.insertDamageRecord(rec)
                    downloadedCount++
                }
            }
            for (rec in localDamages) {
                if (!cloudDamageNumbers.contains(rec.damageNumber)) {
                    val data = mapOf(
                        "damageNumber" to rec.damageNumber,
                        "timestamp" to rec.timestamp,
                        "itemCode" to rec.itemCode,
                        "itemName" to rec.itemName,
                        "cartons" to rec.cartons,
                        "pieces" to rec.pieces,
                        "reason" to rec.reason,
                        "estimatedLossValue" to rec.estimatedLossValue,
                        "supervisorName" to rec.supervisorName,
                        "photoUri" to (rec.photoUri ?: ""),
                        "notes" to rec.notes
                    )
                    db.collection(COL_DAMAGE_RECORDS).document(rec.damageNumber).set(data, SetOptions.merge()).awaitTask()
                    uploadedCount++
                }
            }

            // 5. Sync Stock Movements (Merge by movementNumber)
            val localMovements = dao.getAllMovementsList()
            val localMovMap = localMovements.associateBy { it.movementNumber }
            val movDocs = db.collection(COL_MOVEMENTS).get().awaitTask()
            val cloudMovNumbers = mutableSetOf<String>()

            for (doc in movDocs.documents) {
                val movNumber = doc.getString("movementNumber") ?: continue
                cloudMovNumbers.add(movNumber)
                if (!localMovMap.containsKey(movNumber)) {
                    val mov = StockMovementEntity(
                        movementNumber = movNumber,
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        movementType = doc.getString("movementType") ?: "حركة مخزنية",
                        itemCode = doc.getString("itemCode") ?: "",
                        itemName = doc.getString("itemName") ?: "",
                        cartons = doc.getLong("cartons")?.toInt() ?: 0,
                        pieces = doc.getLong("pieces")?.toInt() ?: 0,
                        totalPiecesDelta = doc.getLong("totalPiecesDelta")?.toInt() ?: 0,
                        stockAfterCartons = doc.getLong("stockAfterCartons")?.toInt() ?: 0,
                        stockAfterPieces = doc.getLong("stockAfterPieces")?.toInt() ?: 0,
                        totalPiecesAfter = doc.getLong("totalPiecesAfter")?.toInt() ?: 0,
                        relatedOrderNumber = doc.getString("relatedOrderNumber") ?: "",
                        notes = doc.getString("notes") ?: "",
                        operatorName = doc.getString("operatorName") ?: "النظام"
                    )
                    dao.insertMovement(mov)
                    downloadedCount++
                }
            }
            for (mov in localMovements.take(200)) {
                if (!cloudMovNumbers.contains(mov.movementNumber)) {
                    val data = mapOf(
                        "movementNumber" to mov.movementNumber,
                        "timestamp" to mov.timestamp,
                        "movementType" to mov.movementType,
                        "itemCode" to mov.itemCode,
                        "itemName" to mov.itemName,
                        "cartons" to mov.cartons,
                        "pieces" to mov.pieces,
                        "totalPiecesDelta" to mov.totalPiecesDelta,
                        "stockAfterCartons" to mov.stockAfterCartons,
                        "stockAfterPieces" to mov.stockAfterPieces,
                        "totalPiecesAfter" to mov.totalPiecesAfter,
                        "relatedOrderNumber" to (mov.relatedOrderNumber ?: ""),
                        "notes" to mov.notes,
                        "operatorName" to mov.operatorName
                    )
                    db.collection(COL_MOVEMENTS).document(mov.movementNumber).set(data, SetOptions.merge()).awaitTask()
                    uploadedCount++
                }
            }

            // 6. Sync Representatives
            val localReps = dao.getAllRepresentativesList()
            val localRepsMap = localReps.associateBy { it.id }
            val repDocs = db.collection(COL_REPRESENTATIVES).get().awaitTask()
            val cloudRepIds = mutableSetOf<Long>()

            for (doc in repDocs.documents) {
                val repId = doc.getLong("id") ?: continue
                cloudRepIds.add(repId)
                if (!localRepsMap.containsKey(repId)) {
                    val rep = RepresentativeEntity(
                        id = repId,
                        name = doc.getString("name") ?: "",
                        phone = doc.getString("phone") ?: "",
                        code = doc.getString("code") ?: "",
                        vehicleNumber = doc.getString("vehicleNumber") ?: "",
                        routeOrArea = doc.getString("routeOrArea") ?: "",
                        notes = doc.getString("notes") ?: "",
                        isActive = doc.getBoolean("isActive") ?: true,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                    dao.insertRepresentative(rep)
                    downloadedCount++
                }
            }
            for (rep in localReps) {
                if (!cloudRepIds.contains(rep.id)) {
                    val data = mapOf(
                        "id" to rep.id,
                        "name" to rep.name,
                        "phone" to rep.phone,
                        "code" to rep.code,
                        "vehicleNumber" to rep.vehicleNumber,
                        "routeOrArea" to rep.routeOrArea,
                        "notes" to rep.notes,
                        "isActive" to rep.isActive,
                        "createdAt" to rep.createdAt
                    )
                    db.collection(COL_REPRESENTATIVES).document("rep_${rep.id}").set(data, SetOptions.merge()).awaitTask()
                    uploadedCount++
                }
            }

            val now = System.currentTimeMillis()
            _lastSyncTime.value = now
            val report = if (uploadedCount == 0 && downloadedCount == 0) {
                "بياناتك متطابقة ومحدثة بالكامل مع السحابة (لا توجد تعارضات)"
            } else {
                "تمت المزامنة الذكية بنجاح (تم رفع $uploadedCount وحفظها، وتحديث $downloadedCount من السحابة)"
            }
            _syncStatus.value = FirebaseSyncStatus.Success(report, now)
            Result.success(report)
        } catch (e: Exception) {
            val isPermissionDenied = e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ||
                                     e.cause?.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ||
                                     e is com.google.firebase.firestore.FirebaseFirestoreException
            if (isPermissionDenied) {
                Log.w(TAG, "Firebase permission notice in smart sync: Firestore security rules or authentication required. Local SQLite data is completely safe.")
                val errorMsg = "تنبيه السحابة: يتطلب Firebase تسجيل الدخول بحساب قوقل أو ضبط قواعد Firestore. جميع بياناتك آمنة ومحفوظة محلياً."
                _syncStatus.value = FirebaseSyncStatus.Error(errorMsg)
                Result.failure(Exception(errorMsg, e))
            } else {
                Log.e(TAG, "Smart sync error: ${e.message}", e)
                val errorMsg = "فشلت المزامنة السحابية: ${e.localizedMessage ?: e.message}"
                _syncStatus.value = FirebaseSyncStatus.Error(errorMsg)
                Result.failure(e)
            }
        }
    }

    /**
     * Downloads cloud records into local Room database safely.
     * Protects local items if local changes or movements are newer than cloud timestamp.
     */
    suspend fun downloadFromCloud(): Result<String> = withContext(Dispatchers.IO) {
        if (!isFirebaseInitialized()) {
            val msg = "لم يتم تهيئة Firebase بعد. يرجى إضافة ملف google-services.json لمشروع التطبيق."
            _syncStatus.value = FirebaseSyncStatus.Error(msg)
            return@withContext Result.failure(IllegalStateException(msg))
        }

        val db = getFirestore() ?: run {
            val msg = "تعذر الاتصال بـ Cloud Firestore"
            _syncStatus.value = FirebaseSyncStatus.Error(msg)
            return@withContext Result.failure(IllegalStateException(msg))
        }

        _syncStatus.value = FirebaseSyncStatus.Syncing

        try {
            var downloadedCount = 0
            var protectedCount = 0

            // 1. Download items (with protection for newer local stock)
            val itemDocs = db.collection(COL_ITEMS).get().awaitTask()
            for (doc in itemDocs.documents) {
                val code = doc.getString("code") ?: continue
                val name = doc.getString("name") ?: ""
                val category = doc.getString("category") ?: "عام"
                val conversionFactor = doc.getLong("conversionFactor")?.toInt() ?: 1
                val totalPieces = doc.getLong("totalPieces")?.toInt() ?: 0
                val minStockCartons = doc.getLong("minStockCartons")?.toInt() ?: 5
                val supplier = doc.getString("supplier") ?: ""
                val price = doc.getDouble("pricePerCarton") ?: 0.0
                val cloudLastUpdated = doc.getLong("lastUpdated") ?: 0L

                val local = dao.getItemByCode(code)
                if (local != null) {
                    val localMovTime = dao.getLatestMovementTimestampForItem(code) ?: 0L
                    val localEffectiveTime = maxOf(local.lastUpdated, localMovTime)
                    if (localEffectiveTime > cloudLastUpdated) {
                        // Local is strictly newer! Do not overwrite local with older cloud data!
                        protectedCount++
                        continue
                    }
                }

                dao.insertItem(
                    ItemEntity(
                        code = code,
                        name = name,
                        category = category,
                        conversionFactor = conversionFactor,
                        totalPieces = totalPieces,
                        minStockCartons = minStockCartons,
                        supplier = supplier,
                        pricePerCarton = price,
                        lastUpdated = cloudLastUpdated
                    )
                )
                downloadedCount++
            }

            // 2. Download issue orders (insert missing)
            val issueDocs = db.collection(COL_ISSUE_ORDERS).get().awaitTask()
            for (doc in issueDocs.documents) {
                val orderNumber = doc.getString("orderNumber") ?: continue
                if (dao.getIssueOrder(orderNumber) == null) {
                    dao.insertIssueOrder(
                        IssueOrderEntity(
                            orderNumber = orderNumber,
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            destination = doc.getString("destination") ?: "",
                            recipientName = doc.getString("recipientName") ?: "",
                            supervisorName = doc.getString("supervisorName") ?: "",
                            notes = doc.getString("notes") ?: "",
                            totalCartons = doc.getLong("totalCartons")?.toInt() ?: 0,
                            totalPieces = doc.getLong("totalPieces")?.toInt() ?: 0,
                            itemsJson = doc.getString("itemsJson") ?: "[]"
                        )
                    )
                    downloadedCount++
                }
            }

            // 3. Download return orders (insert missing)
            val returnDocs = db.collection(COL_RETURN_ORDERS).get().awaitTask()
            for (doc in returnDocs.documents) {
                val orderNumber = doc.getString("orderNumber") ?: continue
                if (dao.getReturnOrder(orderNumber) == null) {
                    dao.insertReturnOrder(
                        ReturnOrderEntity(
                            orderNumber = orderNumber,
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            relatedIssueOrderNumber = doc.getString("relatedIssueOrderNumber"),
                            itemsJson = doc.getString("itemsJson") ?: "",
                            totalCartons = (doc.getLong("totalCartons") ?: 0L).toInt(),
                            totalPieces = (doc.getLong("totalPieces") ?: 0L).toInt(),
                                        reason = doc.getString("reason") ?: "",
                            supervisorName = doc.getString("supervisorName") ?: "مشرف المستودع",
                            notes = doc.getString("notes") ?: "",
                            status = doc.getString("status") ?: "معتمد"
                        )
                    )
                    downloadedCount++
                }
            }

            // 4. Download damage records (insert missing)
            val damageDocs = db.collection(COL_DAMAGE_RECORDS).get().awaitTask()
            for (doc in damageDocs.documents) {
                val damageNumber = doc.getString("damageNumber") ?: continue
                if (dao.getDamageRecord(damageNumber) == null) {
                    dao.insertDamageRecord(
                        DamageRecordEntity(
                            damageNumber = damageNumber,
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            itemCode = doc.getString("itemCode") ?: "",
                            itemName = doc.getString("itemName") ?: "",
                            cartons = doc.getLong("cartons")?.toInt() ?: 0,
                            pieces = doc.getLong("pieces")?.toInt() ?: 0,
                                        reason = doc.getString("reason") ?: "",
                            supervisorName = doc.getString("supervisorName") ?: "مشرف المستودع",
                            notes = doc.getString("notes") ?: "",
                            photoUri = doc.getString("photoUri"),
                            estimatedLossValue = doc.getDouble("estimatedLossValue") ?: 0.0
                        )
                    )
                    downloadedCount++
                }
            }

            // 5. Download Representatives (insert missing)
            val repDocs = db.collection(COL_REPRESENTATIVES).get().awaitTask()
            for (doc in repDocs.documents) {
                val repId = doc.getLong("id") ?: continue
                if (dao.getRepresentativeById(repId) == null) {
                    dao.insertRepresentative(
                        RepresentativeEntity(
                            id = repId,
                            name = doc.getString("name") ?: "",
                            phone = doc.getString("phone") ?: "",
                            code = doc.getString("code") ?: "",
                            vehicleNumber = doc.getString("vehicleNumber") ?: "",
                            routeOrArea = doc.getString("routeOrArea") ?: "",
                            notes = doc.getString("notes") ?: "",
                            isActive = doc.getBoolean("isActive") ?: true,
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    )
                    downloadedCount++
                }
            }

            val now = System.currentTimeMillis()
            _lastSyncTime.value = now
            val successMsg = if (protectedCount > 0) {
                "تم تنزيل $downloadedCount سجلاً جديداً من السحابة (وتم الحفاظ على $protectedCount أصناف محلية لأنها أحدث من السحابة)"
            } else {
                "تم تنزيل وتحديث $downloadedCount سجلاً من السحابة بنجاح"
            }
            _syncStatus.value = FirebaseSyncStatus.Success(successMsg, now)
            Result.success(successMsg)
        } catch (e: Exception) {
            val isPermissionDenied = e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ||
                                     e.cause?.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ||
                                     e is com.google.firebase.firestore.FirebaseFirestoreException
            if (isPermissionDenied) {
                Log.w(TAG, "Firebase permission notice in download: Firestore security rules or authentication required. Local SQLite data is completely safe.")
                val errorMsg = "تنبيه السحابة: يتطلب Firebase تسجيل الدخول بحساب قوقل أو ضبط قواعد Firestore. جميع بياناتك آمنة ومحفوظة محلياً."
                _syncStatus.value = FirebaseSyncStatus.Error(errorMsg)
                Result.failure(Exception(errorMsg, e))
            } else {
                Log.e(TAG, "Download error: ${e.message}", e)
                val errorMsg = "فشل تنزيل البيانات: ${e.localizedMessage ?: e.message}"
                _syncStatus.value = FirebaseSyncStatus.Error(errorMsg)
                Result.failure(e)
            }
        }
    }
}

/**
 * Suspend extension for Play Services Task to avoid requiring extra libraries.
 */
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        if (cont.isActive) cont.resume(result)
    }
    addOnFailureListener { exception ->
        if (cont.isActive) cont.resumeWithException(exception)
    }
    addOnCanceledListener {
        if (cont.isActive) cont.cancel()
    }
}
