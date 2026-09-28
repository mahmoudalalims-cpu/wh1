import re

with open('app/src/main/java/com/example/data/sync/FirebaseSyncManager.kt', 'r') as f:
    content = f.read()

# Fix 'order.' references
content = content.replace('"itemCode" to order.itemCode,', '"itemsJson" to order.itemsJson,\n                    "totalCartons" to order.totalCartons,\n                    "totalPieces" to order.totalPieces,')
content = content.replace('                    "itemName" to order.itemName,\n', '')
content = content.replace('                    "cartons" to order.cartons,\n', '')
content = content.replace('                    "pieces" to order.pieces,\n', '')

# For some reason, the previous python script changed something that now gives "No parameter with name 'cartons' found" for DamageRecordEntity or something?
# Let's check line 437.
