import re

with open('app/src/main/java/com/example/data/sync/FirebaseSyncManager.kt', 'r') as f:
    content = f.read()

# Lines 477-479
content = content.replace("""                        itemsJson = doc.getString("itemsJson") ?: "",
                        totalCartons = doc.getLong("totalCartons")?.toInt() ?: 0,
                        totalPieces = doc.getLong("totalPieces")?.toInt() ?: 0,""", """                        itemCode = doc.getString("itemCode") ?: "",
                        itemName = doc.getString("itemName") ?: "",
                        cartons = doc.getLong("cartons")?.toInt() ?: 0,
                        pieces = doc.getLong("pieces")?.toInt() ?: 0,""")

# Lines 524-526
# Wait, let's see where else it's wrong: "e: file:///app/src/main/java/com/example/data/sync/FirebaseSyncManager.kt:524:25 No parameter with name 'itemsJson' found."
content = content.replace("""                        itemsJson = doc.getString("itemsJson") ?: "",
                        totalCartons = (doc.getLong("totalCartons") ?: 0L).toInt(),
                        totalPieces = (doc.getLong("totalPieces") ?: 0L).toInt(),""", """                        itemCode = doc.getString("itemCode") ?: "",
                        itemName = doc.getString("itemName") ?: "",
                        cartons = doc.getLong("cartons")?.toInt() ?: 0,
                        pieces = doc.getLong("pieces")?.toInt() ?: 0,""")

# Lines 739-741:
content = content.replace("""                        itemsJson = retMap["itemsJson"] as? String ?: "",
                        totalCartons = (retMap["totalCartons"] as? Number)?.toInt() ?: 0,
                        totalPieces = (retMap["totalPieces"] as? Number)?.toInt() ?: 0,""", """                        itemCode = retMap["itemCode"] as? String ?: "",
                        itemName = retMap["itemName"] as? String ?: "",
                        cartons = (retMap["cartons"] as? Number)?.toInt() ?: 0,
                        pieces = (retMap["pieces"] as? Number)?.toInt() ?: 0,""")

with open('app/src/main/java/com/example/data/sync/FirebaseSyncManager.kt', 'w') as f:
    f.write(content)
