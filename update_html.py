import re

with open('app/src/main/java/com/example/util/VoucherExporter.kt', 'r') as f:
    content = f.read()

start_str = "fun generateReturnVoucherHtml(order: ReturnOrderEntity): String {"
end_str = "        """.trimIndent()" # at the end of the return function

# We'll just replace the html generation using a regex.
# Let's extract the function body first.
