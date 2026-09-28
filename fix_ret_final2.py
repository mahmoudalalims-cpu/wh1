import re

with open('app/src/main/java/com/example/data/sync/FirebaseSyncManager.kt', 'r') as f:
    content = f.read()

target = """                            itemCode = doc.getString("itemCode") ?: "",
                            itemName = doc.getString("itemName") ?: "",
                            cartons = doc.getLong("cartons")?.toInt() ?: 0,
                            pieces = doc.getLong("pieces")?.toInt() ?: 0,
                                        reason = doc.getString("reason") ?: "","""

replacement = """                            itemsJson = doc.getString("itemsJson") ?: "",
                            totalCartons = (doc.getLong("totalCartons") ?: 0L).toInt(),
                            totalPieces = (doc.getLong("totalPieces") ?: 0L).toInt(),
                                        reason = doc.getString("reason") ?: "","""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/data/sync/FirebaseSyncManager.kt', 'w') as f:
    f.write(content)
