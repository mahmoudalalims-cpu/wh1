package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * سجل حركات المخزون الموحّد (Unified Stock Movement Ledger)
 * Every operation (صرف / استرجاع / تالف / توريد) generates an immutable record here.
 */
@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val movementNumber: String,          // e.g. "MOV-2026-0001"
    val timestamp: Long = System.currentTimeMillis(),
    val movementType: String,            // "صرف", "استرجاع", "تالف", "توريد رصيد"
    val itemCode: String,
    val itemName: String,
    val cartons: Int,
    val pieces: Int,
    val totalPiecesDelta: Int,           // Net change in basic units (+ or -)
    val stockAfterCartons: Int,          // الرصيد بعد العملية بالكرتون
    val stockAfterPieces: Int,           // الرصيد بعد العملية بالحبات المتبقية
    val totalPiecesAfter: Int,           // إجمالي الرصيد بالحبة بعد العملية
    val relatedOrderNumber: String,      // رقم الأمر المرتبط (ISS-..., RET-..., DAM-...)
    val notes: String = "",
    val operatorName: String             // اسم المستخدم المسؤول عن تنفيذ العملية
)
