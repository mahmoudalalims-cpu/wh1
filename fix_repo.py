import re

with open('app/src/main/java/com/example/data/repository/WarehouseRepository.kt', 'r') as f:
    content = f.read()

start_str = """    suspend fun rollbackReturnOrder(orderNumber: String, adminName: String): Result<Unit> = withContext(Dispatchers.IO) {"""
end_str = """            dao.deleteReturnOrder(orderNumber)
            Result.success(Unit)
        } catch (e: Exception) {"""

start_idx = content.find(start_str)
end_idx = content.find(end_str)

if start_idx != -1 and end_idx != -1:
    before = content[:start_idx]
    after = content[end_idx:]
    
    new_func = """    suspend fun rollbackReturnOrder(orderNumber: String, adminName: String): Result<Unit> = withContext(Dispatchers.IO) {
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

"""
    with open('app/src/main/java/com/example/data/repository/WarehouseRepository.kt', 'w') as f:
        f.write(before + new_func + after)
