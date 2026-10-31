package com.arifa.assistant.flavor

import com.arifa.assistant.core.common.Result
import com.arifa.assistant.core.flavor.LicenseInfo
import com.arifa.assistant.core.flavor.LicenseProvider

class PlayLicenseProvider : LicenseProvider {
    override fun isLicensingEnabled(): Boolean = false

    override fun getLicenseInfo(): LicenseInfo {
        return LicenseInfo(
            isValid = true,
            licenseKey = "PLAY_STORE_EDITION",
            remainingDays = 9999,
            expiryTimestamp = Long.MAX_VALUE,
            tier = "PLAY_STORE"
        )
    }

    override fun activateKey(key: String): Result<LicenseInfo> {
        return Result.Success(getLicenseInfo())
    }
}
