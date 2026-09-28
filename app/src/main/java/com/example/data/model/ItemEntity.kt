package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Item in the warehouse inventory.
 * Quantities are tracked in base unit (حبة) and packaging unit (كرتون).
 */
@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val code: String, // Unique item code from images (e.g. "216", "244", "511")
    val name: String,             // Item name
    val category: String,         // e.g., "شبس وتسالي", "بسكويت وحلويات", "معلبات ومواد جافة"
    val baseUnit: String = "حبة",
    val packagingUnit: String = "كرتون",
    val conversionFactor: Int,    // Pieces per carton (معامل التحويل)
    val totalPieces: Int,         // Current balance in basic unit (حبة)
    val minStockCartons: Int,     // Reorder alert threshold in cartons (الحد الأدنى)
    val supplier: String,         // Associated supplier (المورد المرتبط)
    val pricePerCarton: Double = 0.0, // Optional unit price
    val lastUpdated: Long = System.currentTimeMillis() // Timestamp of last stock or item change
) {
    /**
     * Balance in cartons (الرصيد بالكرتون)
     */
    val currentCartons: Int
        get() = if (conversionFactor > 0) totalPieces / conversionFactor else 0

    /**
     * Remaining pieces that do not form a full carton (الرصيد المتبقي بالحبة)
     */
    val remainingPieces: Int
        get() = if (conversionFactor > 0) totalPieces % conversionFactor else 0

    /**
     * Is stock currently at or below the minimum alert threshold?
     */
    val isLowStock: Boolean
        get() = currentCartons <= minStockCartons

    /**
     * Formatted string showing cartons and pieces in Arabic
     */
    fun formatStockText(): String {
        return if (remainingPieces > 0) {
            "$currentCartons كرتون و $remainingPieces حبة"
        } else {
            "$currentCartons كرتون"
        }
    }
}
