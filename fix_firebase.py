import re

with open('app/src/main/java/com/example/data/sync/FirebaseSyncManager.kt', 'r') as f:
    content = f.read()

# Replace mapOf assignments
content = content.replace('"itemCode" to ret.itemCode,', '"itemsJson" to ret.itemsJson,\n                        "totalCartons" to ret.totalCartons,\n                        "totalPieces" to ret.totalPieces,')
content = content.replace('                        "itemName" to ret.itemName,\n', '')
content = content.replace('                        "cartons" to ret.cartons,\n', '')
content = content.replace('                        "pieces" to ret.pieces,\n', '')

content = content.replace('itemCode = doc.getString("itemCode") ?: "",', 'itemsJson = doc.getString("itemsJson") ?: "",\n                        totalCartons = (doc.getLong("totalCartons") ?: 0L).toInt(),\n                        totalPieces = (doc.getLong("totalPieces") ?: 0L).toInt(),')
content = content.replace('                        itemName = doc.getString("itemName") ?: "",\n', '')
content = content.replace('                        cartons = (doc.getLong("cartons") ?: 0L).toInt(),\n', '')
content = content.replace('                        pieces = (doc.getLong("pieces") ?: 0L).toInt(),\n', '')

content = content.replace('itemCode = retMap["itemCode"] as? String ?: "",', 'itemsJson = retMap["itemsJson"] as? String ?: "",\n                        totalCartons = (retMap["totalCartons"] as? Number)?.toInt() ?: 0,\n                        totalPieces = (retMap["totalPieces"] as? Number)?.toInt() ?: 0,')
content = content.replace('                        itemName = retMap["itemName"] as? String ?: "",\n', '')
content = content.replace('                        cartons = (retMap["cartons"] as? Number)?.toInt() ?: 0,\n', '')
content = content.replace('                        pieces = (retMap["pieces"] as? Number)?.toInt() ?: 0,\n', '')

# One more place for map generation (there were 2)
# Wait, let's just make sure all of them are replaced.

with open('app/src/main/java/com/example/data/sync/FirebaseSyncManager.kt', 'w') as f:
    f.write(content)
