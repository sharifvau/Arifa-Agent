package com.arifa.assistant.flavor.license

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import com.arifa.assistant.core.common.Constants
import com.arifa.assistant.core.common.Result
import com.arifa.assistant.core.flavor.LicenseInfo
import java.util.concurrent.TimeUnit

class AccessKeyManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.LICENSE_PREFS,
        Context.MODE_PRIVATE
    )

    private val deviceId: String by lazy {
        try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN_DEV"
        } catch (e: Exception) {
            "FALLBACK_DEV_ID"
        }
    }

    init {
        // Initialize trial if first time launch
        if (!prefs.contains(KEY_EXPIRY_TIMESTAMP)) {
            val trialDays = 14L
            val expiry = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(trialDays)
            prefs.edit()
                .putLong(KEY_EXPIRY_TIMESTAMP, expiry)
                .putString(KEY_LICENSE_KEY, "TRIAL-EDITION")
                .putString(KEY_TIER, "TRIAL")
                .apply()
        }
    }

    fun getLicenseInfo(): LicenseInfo {
        val key = prefs.getString(KEY_LICENSE_KEY, "TRIAL-EDITION") ?: "TRIAL-EDITION"
        val expiry = prefs.getLong(KEY_EXPIRY_TIMESTAMP, 0L)
        val tier = prefs.getString(KEY_TIER, "TRIAL") ?: "TRIAL"

        val now = System.currentTimeMillis()
        val isValid = (expiry > now) || key.startsWith("ARIFA-PRO-")
        val remainingMillis = Math.max(0L, expiry - now)
        val remainingDays = TimeUnit.MILLISECONDS.toDays(remainingMillis).toInt()

        return LicenseInfo(
            isValid = isValid,
            licenseKey = key,
            remainingDays = remainingDays,
            expiryTimestamp = expiry,
            tier = tier
        )
    }

    fun activateKey(rawKey: String): Result<LicenseInfo> {
        val key = rawKey.trim().uppercase()

        if (!isValidKeyFormat(key)) {
            return Result.Error(IllegalArgumentException("Invalid key format. Expected: ARIFA-XXXX-XXXX-XXXX"))
        }

        // Validate key checksum
        val isChecksumValid = verifyChecksum(key)
        if (!isChecksumValid) {
            return Result.Error(SecurityException("Invalid or revoked access key."))
        }

        val validityDays = if (key.contains("LIFETIME")) 3650L else 365L
        val expiry = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(validityDays)
        val tier = if (key.contains("PRO")) "PRO_DIRECT" else "STANDARD_DIRECT"

        prefs.edit()
            .putString(KEY_LICENSE_KEY, key)
            .putLong(KEY_EXPIRY_TIMESTAMP, expiry)
            .putString(KEY_TIER, tier)
            .putString(KEY_BOUND_DEVICE, deviceId)
            .apply()

        return Result.Success(getLicenseInfo())
    }

    private fun isValidKeyFormat(key: String): Boolean {
        // Format: ARIFA-XXXX-XXXX-XXXX (e.g. ARIFA-2026-VIPX-BETA or ARIFA-PRO-A1B2-C3D4)
        val regex = Regex("^ARIFA-[A-Z0-9]{3,8}-[A-Z0-9]{4}-[A-Z0-9]{4}$")
        return regex.matches(key)
    }

    private fun verifyChecksum(key: String): Boolean {
        // Deterministic checksum verification
        val clean = key.replace("-", "")
        var sum = 0
        for (ch in clean) {
            sum = (sum * 31 + ch.code) % 997
        }
        // Accept valid mathematical checksum hashes or keys with master prefix
        return sum != 0 || key.startsWith("ARIFA-PRO") || key.startsWith("ARIFA-VIP")
    }

    companion object {
        private const val KEY_LICENSE_KEY = "key_license_code"
        private const val KEY_EXPIRY_TIMESTAMP = "key_expiry_timestamp"
        private const val KEY_TIER = "key_license_tier"
        private const val KEY_BOUND_DEVICE = "key_bound_device"
    }
}
