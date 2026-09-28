import re

with open('app/src/main/java/com/example/data/sync/FirebaseSyncManager.kt', 'r') as f:
    content = f.read()

# Replace first match
target1 = """                        relatedIssueOrderNumber = doc.getString("relatedIssueOrderNumber"),
                        itemCode = doc.getString("itemCode") ?: "",
                        itemName = doc.getString("itemName") ?: "",
                        cartons = doc.getLong("cartons")?.toInt() ?: 0,
                        pieces = doc.getLong("pieces")?.toInt() ?: 0,"""
replacement1 = """                        relatedIssueOrderNumber = doc.getString("relatedIssueOrderNumber"),
                        itemsJson = doc.getString("itemsJson") ?: "",
                        totalCartons = (doc.getLong("totalCartons") ?: 0L).toInt(),
                        totalPieces = (doc.getLong("totalPieces") ?: 0L).toInt(),"""

content = content.replace(target1, replacement1)

target2 = """                        relatedIssueOrderNumber = retMap["relatedIssueOrderNumber"] as? String,
                        itemCode = retMap["itemCode"] as? String ?: "",
                        itemName = retMap["itemName"] as? String ?: "",
                        cartons = (retMap["cartons"] as? Number)?.toInt() ?: 0,
                        pieces = (retMap["pieces"] as? Number)?.toInt() ?: 0,"""
replacement2 = """                        relatedIssueOrderNumber = retMap["relatedIssueOrderNumber"] as? String,
                        itemsJson = retMap["itemsJson"] as? String ?: "",
                        totalCartons = (retMap["totalCartons"] as? Number)?.toInt() ?: 0,
                        totalPieces = (retMap["totalPieces"] as? Number)?.toInt() ?: 0,"""

content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/data/sync/FirebaseSyncManager.kt', 'w') as f:
    f.write(content)
