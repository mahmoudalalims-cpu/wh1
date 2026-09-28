package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * أمر صرف (Issue Order)
 * Contains order number, date/time, destination, supervisor, recipient, items JSON, and notes.
 */
@Entity(tableName = "issue_orders")
data class IssueOrderEntity(
    @PrimaryKey val orderNumber: String, // e.g. "ISS-2026-0001"
    val timestamp: Long = System.currentTimeMillis(),
    val destination: String,             // جهة الصرف (عميل / قسم / فرع)
    val recipientName: String,           // اسم المستلم
    val supervisorName: String,          // المسؤول عن الصرف
    val notes: String = "",              // ملاحظات
    val itemsJson: String,               // Serialized list of OrderItem
    val totalCartons: Int,
    val totalPieces: Int,
    val status: String = "معتمد"         // CONFIRMED
)
