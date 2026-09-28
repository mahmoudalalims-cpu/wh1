import re

with open('app/src/main/java/com/example/data/sync/FirebaseSyncManager.kt', 'r') as f:
    content = f.read()

# Fix ReturnOrderEntity which had old fields remaining:
content = content.replace('                        cartons = doc.getLong("cartons")?.toInt() ?: 0,\n', '')
content = content.replace('                        pieces = doc.getLong("pieces")?.toInt() ?: 0,\n', '')

# Fix DamageRecordEntity which was accidentally modified
content = content.replace('itemsJson = doc.getString("itemsJson") ?: "",\n                        totalCartons = (doc.getLong("totalCartons") ?: 0L).toInt(),\n                        totalPieces = (doc.getLong("totalPieces") ?: 0L).toInt(),', 'itemCode = doc.getString("itemCode") ?: "",\n                        itemName = doc.getString("itemName") ?: "",')

content = content.replace('itemsJson = retMap["itemsJson"] as? String ?: "",\n                        totalCartons = (retMap["totalCartons"] as? Number)?.toInt() ?: 0,\n                        totalPieces = (retMap["totalPieces"] as? Number)?.toInt() ?: 0,', 'itemCode = retMap["itemCode"] as? String ?: "",\n                        itemName = retMap["itemName"] as? String ?: "",')

# Add back missing fields for DamageRecordEntity
content = content.replace('                        pieces = doc.getLong("pieces")?.toInt() ?: 0,', '                        cartons = doc.getLong("cartons")?.toInt() ?: 0,\n                        pieces = doc.getLong("pieces")?.toInt() ?: 0,')

with open('app/src/main/java/com/example/data/sync/FirebaseSyncManager.kt', 'w') as f:
    f.write(content)
