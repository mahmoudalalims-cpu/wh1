package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * تسجيل تالف (Damage / Loss Record)
 * Separate classification for loss analytics.
 */
@Entity(tableName = "damage_records")
data class DamageRecordEntity(
    @PrimaryKey val damageNumber: String, // e.g. "DAM-2026-0001"
    val timestamp: Long = System.currentTimeMillis(),
    val itemCode: String,
    val itemName: String,
    val cartons: Int,
    val pieces: Int,
    val reason: String,                  // انتهاء صلاحية، كسر/تمزق، سوء تخزين، رطوبة، تلف ناتج عن النقل
    val supervisorName: String,
    val notes: String = "",
    val photoUri: String? = null,        // إرفاق صورة إثبات (اختياري)
    val estimatedLossValue: Double = 0.0
)
