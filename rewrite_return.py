import re

with open('app/src/main/java/com/example/ui/screens/ReturnOrderScreen.kt', 'r') as f:
    content = f.read()

# Add orderItems state
content = content.replace('var repDropdownExpanded by remember { mutableStateOf(false) }', 
'''var repDropdownExpanded by remember { mutableStateOf(false) }
    val orderItems = remember { mutableStateListOf<OrderItem>() }''')

# Calculate total pieces and cartons for the whole order
content = content.replace('val calculatedPieces = (cartons * factor) + pieces', 
'''val calculatedLinePieces = (cartons * factor) + pieces
    val totalCartons = orderItems.sumOf { it.cartons }
    val totalPieces = orderItems.sumOf { it.pieces }''')

# Change import for OrderItem
content = content.replace('import com.example.data.model.ReturnOrderEntity', 
'import com.example.data.model.ReturnOrderEntity\nimport com.example.data.model.OrderItem\nimport androidx.compose.runtime.mutableStateListOf')

# Replace the single item form submission button with an "Add Item" button
# and display the list of items.
# Let's use string manipulation to find the "Submit Return Order Button"

