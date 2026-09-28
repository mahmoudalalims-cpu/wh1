package com.example.data.model

/**
 * An item line inside an Issue Order.
 */
data class OrderItem(
    val itemCode: String,
    val itemName: String,
    val cartons: Int,
    val pieces: Int,
    val conversionFactor: Int,
    val price: Double = 0.0,
    val notes: String = ""
) {
    val totalPieces: Int
        get() = (cartons * conversionFactor) + pieces

    fun formatQuantity(): String {
        return when {
            cartons > 0 && pieces > 0 -> "$cartons كرتون + $pieces حبة"
            cartons > 0 -> "$cartons كرتون"
            else -> "$pieces حبة"
        }
    }
}
