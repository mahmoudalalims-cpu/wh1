import re

with open('app/src/main/java/com/example/util/VoucherExporter.kt', 'r') as f:
    content = f.read()

start_str = """                <table class="items-table">
                    <thead>
                        <tr>
                            <th style="width: 15%;">كود الصنف</th>
                            <th style="width: 45%;">اسم الصنف الغذائي</th>
                            <th style="width: 20%;">الكمية المسترجعة (كرتون)</th>
                            <th style="width: 20%;">الكمية المسترجعة (حبة)</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td style="text-align: center; padding: 12px; border-bottom: 1px solid #E2E8F0; font-family: monospace; font-weight: bold; color: #1E3A8A;">${order.itemCode}</td>
                            <td style="text-align: right; padding: 12px; border-bottom: 1px solid #E2E8F0; font-weight: bold; color: #0F172A;">${order.itemName}</td>
                            <td style="text-align: center; padding: 12px; border-bottom: 1px solid #E2E8F0; font-weight: bold; color: #16A34A;">${order.cartons} كرتون</td>
                            <td style="text-align: center; padding: 12px; border-bottom: 1px solid #E2E8F0; font-weight: bold; color: #16A34A;">${order.pieces} حبة</td>
                        </tr>
                    </tbody>
                </table>"""

end_str = """                <table class="items-table">
                    <thead>
                        <tr>
                            <th style="width: 15%;">كود الصنف</th>
                            <th style="width: 45%;">اسم الصنف الغذائي</th>
                            <th style="width: 40%;">الكمية المسترجعة (كرتون / حبة)</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${
                            parseOrderItems(order.itemsJson).joinToString("\\n") { item ->
                                \"\"\"
                                <tr>
                                    <td style="text-align: center; padding: 12px; border-bottom: 1px solid #E2E8F0; font-family: monospace; font-weight: bold; color: #1E3A8A;">${item.itemCode}</td>
                                    <td style="text-align: right; padding: 12px; border-bottom: 1px solid #E2E8F0; font-weight: bold; color: #0F172A;">${item.itemName}</td>
                                    <td style="text-align: center; padding: 12px; border-bottom: 1px solid #E2E8F0; font-weight: bold; color: #16A34A;">${item.formatQuantity()}</td>
                                </tr>
                                \"\"\".trimIndent()
                            }
                        }
                    </tbody>
                </table>"""

content = content.replace(start_str, end_str)

with open('app/src/main/java/com/example/util/VoucherExporter.kt', 'w') as f:
    f.write(content)
