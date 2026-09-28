import re

with open('app/src/main/java/com/example/data/sync/FirebaseSyncManager.kt', 'r') as f:
    content = f.read()

# I will find "ReturnOrderEntity(" and replace inside it only.
def replace_in_return_order(match):
    body = match.group(0)
    body = body.replace('itemCode = doc.getString("itemCode") ?: "",', 'itemsJson = doc.getString("itemsJson") ?: "",')
    body = body.replace('itemName = doc.getString("itemName") ?: "",', 'totalCartons = doc.getLong("totalCartons")?.toInt() ?: 0,')
    body = body.replace('cartons = doc.getLong("cartons")?.toInt() ?: 0,', 'totalPieces = doc.getLong("totalPieces")?.toInt() ?: 0,')
    body = body.replace('pieces = doc.getLong("pieces")?.toInt() ?: 0,', '')
    
    body = body.replace('itemCode = retMap["itemCode"] as? String ?: "",', 'itemsJson = retMap["itemsJson"] as? String ?: "",')
    body = body.replace('itemName = retMap["itemName"] as? String ?: "",', 'totalCartons = (retMap["totalCartons"] as? Number)?.toInt() ?: 0,')
    body = body.replace('cartons = (retMap["cartons"] as? Number)?.toInt() ?: 0,', 'totalPieces = (retMap["totalPieces"] as? Number)?.toInt() ?: 0,')
    body = body.replace('pieces = (retMap["pieces"] as? Number)?.toInt() ?: 0,', '')
    
    return body

# Replace for ReturnOrderEntity calls
content = re.sub(r'ReturnOrderEntity\([\s\S]*?\)', replace_in_return_order, content)

with open('app/src/main/java/com/example/data/sync/FirebaseSyncManager.kt', 'w') as f:
    f.write(content)
