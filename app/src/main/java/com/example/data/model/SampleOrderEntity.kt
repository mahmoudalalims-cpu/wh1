package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing sample orders issued to sales representatives (صرف عينات للمناديب)
 */
@Entity(tableName = "sample_orders")
data class SampleOrderEntity(
    @PrimaryKey val orderNumber: String, // e.g. "SMP-2026-0001"
    val timestamp: Long = System.currentTimeMillis(),
    val representativeName: String,
    val representativeId: Long? = null,
    val itemsJson: String,             // JSON string representing List<OrderItem>
    val totalCartons: Int,
    val totalPieces: Int,
    val supervisorName: String = "مشرف المستودع",
    val notes: String = "",
    val status: String = "معتمد"       // "معتمد" or "ملغى"
)
