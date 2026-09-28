package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * أمر استرجاع (Return Order)
 * Can optionally be linked to a previous Issue Order.
 */
@Entity(tableName = "return_orders")
data class ReturnOrderEntity(
    @PrimaryKey val orderNumber: String,          // e.g. "RET-2026-0001"
    val timestamp: Long = System.currentTimeMillis(),
    val relatedIssueOrderNumber: String? = null,  // مرتبط اختيارياً بأمر صرف سابق
    val reason: String,                           // فائض، خطأ صرف، رفض عميل، إلخ
    val supervisorName: String,                   // المسؤول عن الاسترجاع
    val itemsJson: String,                        // Serialized list of OrderItem
    val totalCartons: Int,
    val totalPieces: Int,
    val notes: String = "",
    val status: String = "معتمد"
)
