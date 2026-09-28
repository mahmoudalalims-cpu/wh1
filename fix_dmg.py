import re

with open('app/src/main/java/com/example/data/sync/FirebaseSyncManager.kt', 'r') as f:
    content = f.read()

content = content.replace('itemName = doc.getString("itemName") ?: "",', 'itemName = doc.getString("itemName") ?: "",\n                        cartons = doc.getLong("cartons")?.toInt() ?: 0,\n                        pieces = doc.getLong("pieces")?.toInt() ?: 0,')

with open('app/src/main/java/com/example/data/sync/FirebaseSyncManager.kt', 'w') as f:
    f.write(content)
