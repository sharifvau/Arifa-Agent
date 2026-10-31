package com.arifa.assistant.core.flavor

import com.arifa.assistant.core.common.Result

data class LicenseInfo(
    val isValid: Boolean,
    val licenseKey: String,
    val remainingDays: Int,
    val expiryTimestamp: Long,
    val tier: String = "STANDARD"
)

interface LicenseProvider {
    fun isLicensingEnabled(): Boolean
    fun getLicenseInfo(): LicenseInfo
    fun activateKey(key: String): Result<LicenseInfo>

    companion object {
        var instance: LicenseProvider = object : LicenseProvider {
            override fun isLicensingEnabled(): Boolean = false
            override fun getLicenseInfo(): LicenseInfo = LicenseInfo(
                isValid = true,
                licenseKey = "PLAY_STORE_UNRESTRICTED",
                remainingDays = 9999,
                expiryTimestamp = Long.MAX_VALUE,
                tier = "PLAY_STANDARD"
            )
            override fun activateKey(key: String): Result<LicenseInfo> =
                Result.Success(getLicenseInfo())
        }
    }
}
