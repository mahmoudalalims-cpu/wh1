package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * كيان مندوب التوزيع والمبيعات (Sales & Distribution Representative Entity)
 */
@Entity(tableName = "representatives")
data class RepresentativeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,                    // اسم المندوب الكامل
    val phone: String = "",              // رقم الجوال / الهاتف
    val code: String = "",               // كود المندوب (مثل REP-01)
    val vehicleNumber: String = "",      // رقم لوحة السيارة / الشاحنة
    val routeOrArea: String = "",        // خط السير أو المنطقة (مثال: خط الشمال، توزيع المطاعم)
    val notes: String = "",              // ملاحظات إضافية
    val isActive: Boolean = true,        // نشط / غير نشط
    val createdAt: Long = System.currentTimeMillis()
)
