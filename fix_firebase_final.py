import re

with open('app/src/main/java/com/example/data/sync/FirebaseSyncManager.kt', 'r') as f:
    content = f.read()

# Fix ReturnOrderEntity (lines 175)
content = content.replace("""                    "itemCode" to order.itemCode,
                    "itemName" to order.itemName,
                    "cartons" to order.cartons,
                    "pieces" to order.pieces,""", """                    "itemsJson" to order.itemsJson,
                    "totalCartons" to order.totalCartons,
                    "totalPieces" to order.totalPieces,""")

# Fix ReturnOrderEntity instantiation
content = content.replace("""                        itemCode = doc.getString("itemCode") ?: "",
                        itemName = doc.getString("itemName") ?: "",
                        cartons = doc.getLong("cartons")?.toInt() ?: 0,
                        pieces = doc.getLong("pieces")?.toInt() ?: 0,""", """                        itemsJson = doc.getString("itemsJson") ?: "",
                        totalCartons = doc.getLong("totalCartons")?.toInt() ?: 0,
                        totalPieces = doc.getLong("totalPieces")?.toInt() ?: 0,""")

content = content.replace("""                        itemCode = retMap["itemCode"] as? String ?: "",
                        itemName = retMap["itemName"] as? String ?: "",
                        cartons = (retMap["cartons"] as? Number)?.toInt() ?: 0,
                        pieces = (retMap["pieces"] as? Number)?.toInt() ?: 0,""", """                        itemsJson = retMap["itemsJson"] as? String ?: "",
                        totalCartons = (retMap["totalCartons"] as? Number)?.toInt() ?: 0,
                        totalPieces = (retMap["totalPieces"] as? Number)?.toInt() ?: 0,""")

with open('app/src/main/java/com/example/data/sync/FirebaseSyncManager.kt', 'w') as f:
    f.write(content)
