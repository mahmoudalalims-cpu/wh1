package com.example.data.repository

import com.example.data.dao.WarehouseDao
import com.example.data.model.DamageRecordEntity
import com.example.data.model.IssueOrderEntity
import com.example.data.model.ItemEntity
import com.example.data.model.OrderItem
import com.example.data.model.RepresentativeEntity
import com.example.data.model.ReturnOrderEntity
import com.example.data.model.SampleOrderEntity
import com.example.data.model.StockMovementEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class WarehouseRepository(private val dao: WarehouseDao) {

    val allItems: Flow<List<ItemEntity>> = dao.getAllItems()
    val lowStockItems: Flow<List<ItemEntity>> = dao.getLowStockItems()
    val issueOrders: Flow<List<IssueOrderEntity>> = dao.getAllIssueOrders()
    val returnOrders: Flow<List<ReturnOrderEntity>> = dao.getAllReturnOrders()
    val sampleOrders: Flow<List<SampleOrderEntity>> = dao.getAllSampleOrders()
    val damageRecords: Flow<List<DamageRecordEntity>> = dao.getAllDamageRecords()
    val allMovements: Flow<List<StockMovementEntity>> = dao.getAllMovements()
    val allRepresentatives: Flow<List<RepresentativeEntity>> = dao.getAllRepresentatives()

    fun getMovementsByItem(itemCode: String): Flow<List<StockMovementEntity>> {
        return dao.getMovementsByItem(itemCode)
    }

    suspend fun getItemByCode(code: String): ItemEntity? {
        return withContext(Dispatchers.IO) {
            dao.getItemByCode(code)
        }
    }

    suspend fun insertItem(item: ItemEntity) {
        withContext(Dispatchers.IO) {
            dao.insertItem(item)
            // Log addition
            val movCount = dao.getMovementsCount() + 1
            dao.insertMovement(
                StockMovementEntity(
                    movementNumber = "MOV-NEW-%04d".format(movCount),
                    movementType = "توريد صنف جديد",
                    itemCode = item.code,
                    itemName = item.name,
                    cartons = item.currentCartons,
                    pieces = item.remainingPieces,
                    totalPiecesDelta = item.totalPieces,
                    stockAfterCartons = item.currentCartons,
                    stockAfterPieces = item.remainingPieces,
                    totalPiecesAfter = item.totalPieces,
                    relatedOrderNumber = "ITEM-ADD-${item.code}",
                    notes = "إضافة صنف جديد في مستودع آصرة العرب",
                    operatorName = "مسؤول المستودع"
                )
            )
        }
    }

    suspend fun updateItem(item: ItemEntity) {
        withContext(Dispatchers.IO) {
            dao.updateItem(item)
        }
    }

    /**
     * Process an Issue Order (أمر صرف):
     * - Deducts quantities from items.
     * - Saves the IssueOrderEntity.
     * - Logs an immutable stock movement for every item.
     */
    suspend fun processIssueOrder(
        destination: String,
        recipientName: String,
        supervisorName: String,
        notes: String,
        items: List<OrderItem>
    ): Result<IssueOrderEntity> = withContext(Dispatchers.IO) {
        try {
            if (items.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("يجب اختيار صنف واحد على الأقل"))
            }

            // Verify stock availability
            for (orderItem in items) {
                val current = dao.getItemByCode(orderItem.itemCode)
                    ?: return@withContext Result.failure(IllegalArgumentException("الصنف غير موجود: ${orderItem.itemCode}"))
                val deductPieces = (orderItem.cartons * current.conversionFactor) + orderItem.pieces
                if (current.totalPieces < deductPieces) {
                    val availableCartons = current.currentCartons
                    val availablePieces = current.remainingPieces
                    return@withContext Result.failure(
                        IllegalArgumentException(
                            "الرصيد غير كافٍ للصنف [${current.name}]. المتاح: $availableCartons كرتون و $availablePieces حبة"
                        )
                    )
                }
            }

            val orderCount = dao.getIssueOrdersCount() + 1
            val orderNumber = "ISS-2026-%04d".format(orderCount)
            val now = System.currentTimeMillis()

            var totalCartons = 0
            var totalPieces = 0

            val jsonArray = JSONArray()
            for (orderItem in items) {
                totalCartons += orderItem.cartons
                totalPieces += orderItem.pieces

                val obj = JSONObject().apply {
                    put("itemCode", orderItem.itemCode)
                    put("itemName", orderItem.itemName)
                    put("cartons", orderItem.cartons)
                    put("pieces", orderItem.pieces)
                    put("conversionFactor", orderItem.conversionFactor)
                    put("price", orderItem.price)
                    put("notes", orderItem.notes)
                }
                jsonArray.put(obj)

                // Deduct stock and log movement
                val current = dao.getItemByCode(orderItem.itemCode)!!
                val deductPieces = (orderItem.cartons * current.conversionFactor) + orderItem.pieces
                val newTotalPieces = current.totalPieces - deductPieces
                dao.updateItemStock(current.code, newTotalPieces)

                val afterCartons = newTotalPieces / current.conversionFactor
                val afterPieces = newTotalPieces % current.conversionFactor

                val movCount = dao.getMovementsCount() + 1
                dao.insertMovement(
                    StockMovementEntity(
                        movementNumber = "MOV-ISS-%04d".format(movCount),
                        timestamp = now,
                        movementType = "صرف",
                        itemCode = current.code,
                        itemName = current.name,
                        cartons = orderItem.cartons,
                        pieces = orderItem.pieces,
                        totalPiecesDelta = -deductPieces,
                        stockAfterCartons = afterCartons,
                        stockAfterPieces = afterPieces,
                        totalPiecesAfter = newTotalPieces,
                        relatedOrderNumber = orderNumber,
                        notes = "صرف إلى: $destination | ${orderItem.notes}".trim(),
                        operatorName = supervisorName
                    )
                )
            }

            val orderEntity = IssueOrderEntity(
                orderNumber = orderNumber,
                timestamp = now,
                destination = destination,
                recipientName = recipientName,
                supervisorName = supervisorName,
                notes = notes,
                itemsJson = jsonArray.toString(),
                totalCartons = totalCartons,
                totalPieces = totalPieces
            )
            dao.insertIssueOrder(orderEntity)

            Result.success(orderEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Process a Return Order (أمر استرجاع):
     * - Restores quantities to stock.
     * - Saves ReturnOrderEntity.
     * - Logs immutable stock movement.
     */
    suspend fun processReturnOrder(
        relatedIssueOrderNumber: String?,
        items: List<OrderItem>,
        reason: String,
        supervisorName: String,
        notes: String
    ): Result<ReturnOrderEntity> = withContext(Dispatchers.IO) {
        try {
            if (items.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("يجب إضافة صنف واحد على الأقل للاسترجاع"))
            }

            val retCount = dao.getReturnOrdersCount() + 1
            val orderNumber = "RET-2026-%04d".format(retCount)
            val now = System.currentTimeMillis()

            var totalCartons = 0
            var totalPieces = 0

            val jsonArray = org.json.JSONArray()

            for (orderItem in items) {
                val item = dao.getItemByCode(orderItem.itemCode)
                    ?: return@withContext Result.failure(IllegalArgumentException("الصنف غير موجود: ${orderItem.itemCode}"))

                val addedPieces = (orderItem.cartons * item.conversionFactor) + orderItem.pieces
                if (addedPieces <= 0) {
                    return@withContext Result.failure(IllegalArgumentException("الرجاء إدخال كمية صالحة للاسترجاع للصنف ${item.name}"))
                }

                totalCartons += orderItem.cartons
                totalPieces += orderItem.pieces

                val obj = org.json.JSONObject().apply {
                    put("itemCode", orderItem.itemCode)
                    put("itemName", orderItem.itemName)
                    put("cartons", orderItem.cartons)
                    put("pieces", orderItem.pieces)
                    put("conversionFactor", orderItem.conversionFactor)
                    put("price", orderItem.price)
                    put("notes", orderItem.notes)
                }
                jsonArray.put(obj)

                val newTotalPieces = item.totalPieces + addedPieces
                dao.updateItemStock(item.code, newTotalPieces)

                val afterCartons = newTotalPieces / item.conversionFactor
                val afterPieces = newTotalPieces % item.conversionFactor

                val movCount = dao.getMovementsCount() + 1
                dao.insertMovement(
                    StockMovementEntity(
                        movementNumber = "MOV-RET-%04d".format(movCount),
                        timestamp = now,
                        movementType = "استرجاع",
                        itemCode = item.code,
                        itemName = item.name,
                        cartons = orderItem.cartons,
                        pieces = orderItem.pieces,
                        totalPiecesDelta = addedPieces,
                        stockAfterCartons = afterCartons,
                        stockAfterPieces = afterPieces,
                        totalPiecesAfter = newTotalPieces,
                        relatedOrderNumber = orderNumber,
                        notes = "سبب الاسترجاع: $reason | ${orderItem.notes} | $notes".trim(),
                        operatorName = supervisorName
                    )
                )
            }

            val returnOrder = ReturnOrderEntity(
                orderNumber = orderNumber,
                timestamp = now,
                relatedIssueOrderNumber = relatedIssueOrderNumber?.takeIf { it.isNotBlank() },
                reason = reason,
                supervisorName = supervisorName,
                itemsJson = jsonArray.toString(),
                totalCartons = totalCartons,
                totalPieces = totalPieces,
                notes = notes
            )
            dao.insertReturnOrder(returnOrder)

            Result.success(returnOrder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Process a Sample Order (صرف عينات للمناديب):
     * - Deducts quantities from items.
     * - Saves the SampleOrderEntity.
     * - Logs an immutable stock movement for every item.
     */
    suspend fun issueSampleOrder(
        representativeName: String,
        representativeId: Long?,
        supervisorName: String,
        notes: String,
        items: List<OrderItem>
    ): Result<SampleOrderEntity> = withContext(Dispatchers.IO) {
        try {
            if (items.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("يجب اختيار صنف واحد على الأقل للعينات"))
            }

            // Verify stock availability
            for (orderItem in items) {
                val current = dao.getItemByCode(orderItem.itemCode)
                    ?: return@withContext Result.failure(IllegalArgumentException("الصنف غير موجود: ${orderItem.itemCode}"))
                val deductPieces = (orderItem.cartons * current.conversionFactor) + orderItem.pieces
                if (current.totalPieces < deductPieces) {
                    val availableCartons = current.currentCartons
                    val availablePieces = current.remainingPieces
                    val packUnit = current.packagingUnit.ifBlank { "كرتون" }
                    val bUnit = current.baseUnit.ifBlank { "حبة" }
                    return@withContext Result.failure(
                        IllegalArgumentException(
                            "الرصيد غير كافٍ للصنف [${current.name}]. المتاح: $availableCartons $packUnit و $availablePieces $bUnit"
                        )
                    )
                }
            }

            val count = dao.getSampleOrdersCount() + 1
            val orderNumber = "SMP-2026-%04d".format(count)
            var totalCartons = 0
            var totalPieces = 0

            val jsonArray = JSONArray()
            val now = System.currentTimeMillis()

            for (orderItem in items) {
                val current = dao.getItemByCode(orderItem.itemCode)!!
                val deductPieces = (orderItem.cartons * current.conversionFactor) + orderItem.pieces
                val newPieces = current.totalPieces - deductPieces
                dao.updateItemStock(current.code, newPieces)

                totalCartons += orderItem.cartons
                totalPieces += orderItem.pieces

                val obj = JSONObject().apply {
                    put("itemCode", orderItem.itemCode)
                    put("itemName", orderItem.itemName)
                    put("cartons", orderItem.cartons)
                    put("pieces", orderItem.pieces)
                    put("conversionFactor", orderItem.conversionFactor)
                    put("price", orderItem.price)
                    put("notes", orderItem.notes)
                }
                jsonArray.put(obj)

                val movCount = dao.getMovementsCount() + 1
                val mov = StockMovementEntity(
                    movementNumber = "MOV-SMP-%04d".format(movCount),
                    timestamp = now,
                    movementType = "صرف عينات للمندوب",
                    itemCode = current.code,
                    itemName = current.name,
                    cartons = -orderItem.cartons,
                    pieces = -orderItem.pieces,
                    totalPiecesDelta = -deductPieces,
                    stockAfterCartons = newPieces / current.conversionFactor,
                    stockAfterPieces = newPieces % current.conversionFactor,
                    totalPiecesAfter = newPieces,
                    relatedOrderNumber = orderNumber,
                    notes = "صرف عينات للمندوب: $representativeName | $notes",
                    operatorName = supervisorName
                )
                dao.insertMovement(mov)
            }

            val entity = SampleOrderEntity(
                orderNumber = orderNumber,
                timestamp = now,
                representativeName = representativeName,
                representativeId = representativeId,
                itemsJson = jsonArray.toString(),
                totalCartons = totalCartons,
                totalPieces = totalPieces,
                supervisorName = supervisorName,
                notes = notes,
                status = "معتمد"
            )
            dao.insertSampleOrder(entity)
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rollbackSampleOrder(orderNumber: String, reason: String = "إلغاء أمر العينات"): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val order = dao.getSampleOrder(orderNumber)
                ?: return@withContext Result.failure(IllegalArgumentException("أمر العينات غير موجود: $orderNumber"))
            if (order.status == "ملغى") {
                return@withContext Result.failure(IllegalStateException("الأمر ملغى بالفعل"))
            }

            val items = mutableListOf<OrderItem>()
            try {
                val arr = JSONArray(order.itemsJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    items.add(
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

            val now = System.currentTimeMillis()

            for (item in items) {
                val current = dao.getItemByCode(item.itemCode)
                if (current != null) {
                    val restorePieces = (item.cartons * current.conversionFactor) + item.pieces
                    val newTotal = current.totalPieces + restorePieces
                    dao.updateItemStock(current.code, newTotal)

                    val movCount = dao.getMovementsCount() + 1
                    dao.insertMovement(
                        StockMovementEntity(
                            movementNumber = "MOV-CAN-SMP-%04d".format(movCount),
                            timestamp = now,
                            movementType = "إلغاء صرف عينات",
                            itemCode = current.code,
                            itemName = current.name,
                            cartons = item.cartons,
                            pieces = item.pieces,
                            totalPiecesDelta = restorePieces,
                            stockAfterCartons = newTotal / current.conversionFactor,
                            stockAfterPieces = newTotal % current.conversionFactor,
                            totalPiecesAfter = newTotal,
                            relatedOrderNumber = orderNumber,
                            notes = "إلغاء أمر العينات: $reason",
                            operatorName = order.supervisorName
                        )
                    )
                }
            }

            val updated = order.copy(status = "ملغى", notes = "${order.notes} | إلغاء: $reason".trim())
            dao.insertSampleOrder(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Process a Damage Record (تسجيل تالف):
     * - Deducts damaged goods from stock.
     * - Classified strictly as "تالف" (Damage / Loss).
     * - Saves DamageRecordEntity & logs movement.
     */
    suspend fun processDamageRecord(
        itemCode: String,
        cartons: Int,
        pieces: Int,
        reason: String,
        supervisorName: String,
        notes: String,
        photoUri: String? = null
    ): Result<DamageRecordEntity> = withContext(Dispatchers.IO) {
        try {
            val item = dao.getItemByCode(itemCode)
                ?: return@withContext Result.failure(IllegalArgumentException("الصنف غير موجود: $itemCode"))

            val deductPieces = (cartons * item.conversionFactor) + pieces
            if (deductPieces <= 0) {
                return@withContext Result.failure(IllegalArgumentException("الرجاء إدخال كمية صالحة للتالف"))
            }

            if (item.totalPieces < deductPieces) {
                return@withContext Result.failure(
                    IllegalArgumentException("الكمية التالفة المدخلة أكبر من الرصيد المتوفر في المستودع")
                )
            }

            val damCount = dao.getDamageRecordsCount() + 1
            val damageNumber = "DAM-2026-%04d".format(damCount)
            val now = System.currentTimeMillis()

            val newTotalPieces = item.totalPieces - deductPieces
            dao.updateItemStock(item.code, newTotalPieces)

            val afterCartons = newTotalPieces / item.conversionFactor
            val afterPieces = newTotalPieces % item.conversionFactor

            val estimatedLoss = (deductPieces.toDouble() / item.conversionFactor.toDouble()) * item.pricePerCarton

            val damageEntity = DamageRecordEntity(
                damageNumber = damageNumber,
                timestamp = now,
                itemCode = item.code,
                itemName = item.name,
                cartons = cartons,
                pieces = pieces,
                reason = reason,
                supervisorName = supervisorName,
                notes = notes,
                photoUri = photoUri,
                estimatedLossValue = estimatedLoss
            )
            dao.insertDamageRecord(damageEntity)

            val movCount = dao.getMovementsCount() + 1
            dao.insertMovement(
                StockMovementEntity(
                    movementNumber = "MOV-DAM-%04d".format(movCount),
                    timestamp = now,
                    movementType = "تالف",
                    itemCode = item.code,
                    itemName = item.name,
                    cartons = cartons,
                    pieces = pieces,
                    totalPiecesDelta = -deductPieces,
                    stockAfterCartons = afterCartons,
                    stockAfterPieces = afterPieces,
                    totalPiecesAfter = newTotalPieces,
                    relatedOrderNumber = damageNumber,
                    notes = "سبب التلف: $reason | $notes".trim(),
                    operatorName = supervisorName
                )
            )

            Result.success(damageEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cancel and rollback an Issue Order safely (requires Admin authorization)
     * Restores stock quantities and records reversing audit movements in ledger
     */
    suspend fun rollbackIssueOrder(orderNumber: String, adminName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val order = dao.getIssueOrder(orderNumber)
                ?: return@withContext Result.failure(IllegalArgumentException("أمر الصرف غير موجود: $orderNumber"))

            val items = mutableListOf<OrderItem>()
            try {
                val arr = JSONArray(order.itemsJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    items.add(
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

            val now = System.currentTimeMillis()

            // Restore stock for all items
            for (item in items) {
                val current = dao.getItemByCode(item.itemCode)
                if (current != null) {
                    val restorePieces = (item.cartons * current.conversionFactor) + item.pieces
                    val newTotal = current.totalPieces + restorePieces
                    dao.updateItemStock(current.code, newTotal)

                    val afterCartons = newTotal / current.conversionFactor
                    val afterPieces = newTotal % current.conversionFactor

                    val movCount = dao.getMovementsCount() + 1
                    dao.insertMovement(
                        StockMovementEntity(
                            movementNumber = "MOV-REV-%04d".format(movCount),
                            timestamp = now,
                            movementType = "إلغاء صرف (استرداد)",
                            itemCode = current.code,
                            itemName = current.name,
                            cartons = item.cartons,
                            pieces = item.pieces,
                            totalPiecesDelta = restorePieces,
                            stockAfterCartons = afterCartons,
                            stockAfterPieces = afterPieces,
                            totalPiecesAfter = newTotal,
                            relatedOrderNumber = orderNumber,
                            notes = "إلغاء معتمد من المشرف: $adminName لأمر الصرف $orderNumber واسترداد الكميات",
                            operatorName = adminName
                        )
                    )
                }
            }

            // Remove the issue order entity
            dao.deleteIssueOrder(orderNumber)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cancel and rollback a Return Order safely (requires Admin authorization)
     */
    suspend fun rollbackReturnOrder(orderNumber: String, adminName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val order = dao.getReturnOrder(orderNumber)
                ?: return@withContext Result.failure(IllegalArgumentException("أمر الاسترجاع غير موجود: $orderNumber"))

            val jsonArray = org.json.JSONArray(order.itemsJson)
            val now = System.currentTimeMillis()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val itemCode = obj.getString("itemCode")
                val itemName = obj.getString("itemName")
                val cartons = obj.getInt("cartons")
                val pieces = obj.getInt("pieces")

                val current = dao.getItemByCode(itemCode)
                if (current != null) {
                    val deductPieces = (cartons * current.conversionFactor) + pieces
                    if (current.totalPieces >= deductPieces) {
                        val newTotal = current.totalPieces - deductPieces
                        dao.updateItemStock(current.code, newTotal)

                        val afterCartons = newTotal / current.conversionFactor
                        val afterPieces = newTotal % current.conversionFactor

                        val movCount = dao.getMovementsCount() + 1
                        dao.insertMovement(
                            StockMovementEntity(
                                movementNumber = "MOV-REV-%04d".format(movCount),
                                timestamp = now,
                                movementType = "إلغاء استرجاع",
                                itemCode = current.code,
                                itemName = current.name,
                                cartons = cartons,
                                pieces = pieces,
                                totalPiecesDelta = -deductPieces,
                                stockAfterCartons = afterCartons,
                                stockAfterPieces = afterPieces,
                                totalPiecesAfter = newTotal,
                                relatedOrderNumber = orderNumber,
                                notes = "إلغاء معتمد لأمر الاسترجاع $orderNumber بواسطة $adminName",
                                operatorName = adminName
                            )
                        )
                    }
                }
            }

            dao.deleteReturnOrder(orderNumber)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cancel and rollback a Damage Record safely (requires Admin authorization)
     */
    suspend fun rollbackDamageRecord(damageNumber: String, adminName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val record = dao.getDamageRecord(damageNumber)
                ?: return@withContext Result.failure(IllegalArgumentException("محضر التالف غير موجود: $damageNumber"))

            val current = dao.getItemByCode(record.itemCode)
                ?: return@withContext Result.failure(IllegalArgumentException("الصنف غير موجود: ${record.itemCode}"))

            val restorePieces = (record.cartons * current.conversionFactor) + record.pieces
            val newTotal = current.totalPieces + restorePieces
            dao.updateItemStock(current.code, newTotal)

            val afterCartons = newTotal / current.conversionFactor
            val afterPieces = newTotal % current.conversionFactor
            val now = System.currentTimeMillis()

            val movCount = dao.getMovementsCount() + 1
            dao.insertMovement(
                StockMovementEntity(
                    movementNumber = "MOV-REV-%04d".format(movCount),
                    timestamp = now,
                    movementType = "إلغاء تالف (استرداد)",
                    itemCode = current.code,
                    itemName = current.name,
                    cartons = record.cartons,
                    pieces = record.pieces,
                    totalPiecesDelta = restorePieces,
                    stockAfterCartons = afterCartons,
                    stockAfterPieces = afterPieces,
                    totalPiecesAfter = newTotal,
                    relatedOrderNumber = damageNumber,
                    notes = "إلغاء معتمد لمحضر التالف $damageNumber واسترداد الرصيد بواسطة $adminName",
                    operatorName = adminName
                )
            )

            dao.deleteDamageRecord(damageNumber)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete an inventory item with audit
     */
    suspend fun deleteItemSafely(code: String, adminName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val item = dao.getItemByCode(code)
                ?: return@withContext Result.failure(IllegalArgumentException("الصنف غير موجود: $code"))

            val movCount = dao.getMovementsCount() + 1
            dao.insertMovement(
                StockMovementEntity(
                    movementNumber = "MOV-DEL-%04d".format(movCount),
                    timestamp = System.currentTimeMillis(),
                    movementType = "حذف صنف من الدليل",
                    itemCode = item.code,
                    itemName = item.name,
                    cartons = item.currentCartons,
                    pieces = item.remainingPieces,
                    totalPiecesDelta = -item.totalPieces,
                    stockAfterCartons = 0,
                    stockAfterPieces = 0,
                    totalPiecesAfter = 0,
                    relatedOrderNumber = "ITEM-DEL-${item.code}",
                    notes = "حذف الصنف نهائياً بواسطة المشرف: $adminName",
                    operatorName = adminName
                )
            )

            dao.deleteItem(code)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Representative CRUD operations ---
    suspend fun insertRepresentative(rep: RepresentativeEntity): Long = withContext(Dispatchers.IO) {
        dao.insertRepresentative(rep)
    }

    suspend fun updateRepresentative(rep: RepresentativeEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.updateRepresentative(rep)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRepresentative(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.deleteRepresentative(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
