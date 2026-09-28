import re

with open('app/src/main/java/com/example/ui/screens/ReturnOrderScreen.kt', 'r') as f:
    content = f.read()

# Replace calculatedPieces usages to calculatedLinePieces
content = content.replace('calculatedPieces', 'calculatedLinePieces')

# Let's find the Add Line button area or the Submit Order button area
# We have a Submit button with "اعتماد أمر الاسترجاع وإضافة للرصيد"
# And we need to add the `orderItems` list rendering.
# It's better to just extract the middle part of `IssueOrderScreen.kt` and adapt it.

