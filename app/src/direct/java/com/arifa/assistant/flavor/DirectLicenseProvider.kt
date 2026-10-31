package com.arifa.assistant.flavor

import android.content.Context
import com.arifa.assistant.core.common.Result
import com.arifa.assistant.core.flavor.LicenseInfo
import com.arifa.assistant.core.flavor.LicenseProvider
import com.arifa.assistant.flavor.license.AccessKeyManager

class DirectLicenseProvider(context: Context) : LicenseProvider {

    private val manager = AccessKeyManager(context)

    override fun isLicensingEnabled(): Boolean = true

    override fun getLicenseInfo(): LicenseInfo {
        return manager.getLicenseInfo()
    }

    override fun activateKey(key: String): Result<LicenseInfo> {
        return manager.activateKey(key)
    }
}
