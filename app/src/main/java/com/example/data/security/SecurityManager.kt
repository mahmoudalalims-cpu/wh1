package com.example.data.security

import android.content.Context
import android.content.SharedPreferences

class SecurityManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("arab_bond_warehouse_security", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ADMIN_PIN = "admin_pin"
        private const val KEY_PROTECTION_ENABLED = "protection_enabled"
        private const val KEY_REQUIRE_PIN_DELETE = "require_pin_delete"
        private const val KEY_REQUIRE_PIN_STOCK_EDIT = "require_pin_stock_edit"
        private const val DEFAULT_PIN = "1234"
    }

    /**
     * Check if entered PIN matches the configured admin PIN
     */
    fun verifyPin(pin: String): Boolean {
        val currentPin = getAdminPin()
        return pin.trim() == currentPin.trim()
    }

    /**
     * Get current Admin PIN (defaults to 1234)
     */
    fun getAdminPin(): String {
        return prefs.getString(KEY_ADMIN_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
    }

    /**
     * Change admin PIN after validating old PIN
     */
    fun changePin(oldPin: String, newPin: String): Result<Unit> {
        if (!verifyPin(oldPin)) {
            return Result.failure(IllegalArgumentException("كلمة المرور الحالية غير صحيحة"))
        }
        if (newPin.trim().length < 4) {
            return Result.failure(IllegalArgumentException("كلمة المرور الجديدة يجب ألا تقل عن 4 أرقام"))
        }
        prefs.edit().putString(KEY_ADMIN_PIN, newPin.trim()).apply()
        return Result.success(Unit)
    }

    /**
     * Is security protection active
     */
    fun isProtectionEnabled(): Boolean {
        return prefs.getBoolean(KEY_PROTECTION_ENABLED, true)
    }

    fun setProtectionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PROTECTION_ENABLED, enabled).apply()
    }

    fun isDeleteOrderProtected(): Boolean {
        return prefs.getBoolean(KEY_REQUIRE_PIN_DELETE, true)
    }

    fun setDeleteOrderProtected(protected: Boolean) {
        prefs.edit().putBoolean(KEY_REQUIRE_PIN_DELETE, protected).apply()
    }

    fun isStockEditProtected(): Boolean {
        return prefs.getBoolean(KEY_REQUIRE_PIN_STOCK_EDIT, true)
    }

    fun setStockEditProtected(protected: Boolean) {
        prefs.edit().putBoolean(KEY_REQUIRE_PIN_STOCK_EDIT, protected).apply()
    }
}
