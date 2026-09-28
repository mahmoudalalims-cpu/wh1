import re

with open('app/src/main/java/com/example/ui/screens/ReturnOrderScreen.kt', 'r') as f:
    content = f.read()

# Replace: Text(ret.itemName,
# To: Text("الأصناف المسترجعة: " + com.example.util.VoucherExporter.parseOrderItems(ret.itemsJson).joinToString("، ") { it.itemName },
content = content.replace(
    'Text(ret.itemName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))',
    'Text("أصناف متعددة (${com.example.util.VoucherExporter.parseOrderItems(ret.itemsJson).size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))'
)

content = content.replace(
    'text = "الكمية المسترجعة: ${ret.cartons} كرتون و ${ret.pieces} حبة | السبب: ${ret.reason}",',
    'text = "الكمية المسترجعة: ${ret.totalCartons} كرتون و ${ret.totalPieces} حبة | السبب: ${ret.reason}",'
)

content = content.replace(
    'description = "تنبيه أمني: إلغاء هذا السند سيقوم بخصم الكميات المسترجعة (${order.cartons} كرتون و ${order.pieces} حبة) من رصيد المستودع وتوثيق العملية في سجل التدقيق.",',
    'description = "تنبيه أمني: إلغاء هذا السند سيقوم بخصم الكميات المسترجعة (${order.totalCartons} كرتون و ${order.totalPieces} حبة) من رصيد المستودع وتوثيق العملية في سجل التدقيق.",'
)

with open('app/src/main/java/com/example/ui/screens/ReturnOrderScreen.kt', 'w') as f:
    f.write(content)

