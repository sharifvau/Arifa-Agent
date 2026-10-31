package com.arifa.assistant.core.phone

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

data class AppInfo(
    val label: String,
    val packageName: String
)

data class ContactEntry(
    val name: String,
    val phoneNumber: String
)

class PhoneActionController private constructor(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    /**
     * Finds installed apps matching query name and launches the best match
     */
    fun openApp(appName: String): JSONObject {
        val result = JSONObject()
        val query = appName.trim().lowercase()

        try {
            val installedApps = getInstalledApps()
            val match = installedApps.find { it.label.lowercase() == query }
                ?: installedApps.find { it.label.lowercase().contains(query) }
                ?: installedApps.find { it.packageName.lowercase().contains(query) }

            if (match != null) {
                val launchIntent = packageManager.getLaunchIntentForPackage(match.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    result.put("status", "success")
                    result.put("message", "Opened ${match.label}")
                    result.put("packageName", match.packageName)
                    return result
                }
            }

            result.put("status", "not_found")
            result.put("message", "Application '$appName' was not found on this device.")
        } catch (e: Exception) {
            result.put("status", "error")
            result.put("message", "Error opening application: ${e.message}")
        }
        return result
    }

    fun searchApps(query: String): JSONObject {
        val result = JSONObject()
        val q = query.trim().lowercase()
        val list = getInstalledApps().filter {
            it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q)
        }
        val array = JSONArray()
        for (item in list) {
            array.put(JSONObject().apply {
                put("label", item.label)
                put("packageName", item.packageName)
            })
        }
        result.put("status", "success")
        result.put("results", array)
        return result
    }

    private fun getInstalledApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
        return resolveInfoList.mapNotNull {
            val label = it.loadLabel(packageManager)?.toString() ?: ""
            val pkg = it.activityInfo?.packageName ?: ""
            if (label.isNotBlank() && pkg.isNotBlank()) AppInfo(label, pkg) else null
        }
    }

    /**
     * Search contacts by name using ContactsContract
     */
    @SuppressLint("Range")
    fun findContact(nameQuery: String): List<ContactEntry> {
        val contacts = mutableListOf<ContactEntry>()
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$nameQuery%")

        try {
            val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            cursor?.use {
                while (it.moveToNext()) {
                    val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) ?: ""
                    val number = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: ""
                    if (name.isNotBlank() && number.isNotBlank()) {
                        contacts.add(ContactEntry(name, number))
                    }
                }
            }
        } catch (e: Exception) {
            // Permission or querying exception
        }
        return contacts
    }

    /**
     * Call contact or open dialer
     */
    fun callContact(contactOrNumber: String): JSONObject {
        val result = JSONObject()
        val cleanInput = contactOrNumber.trim()

        val phoneNumber: String = if (cleanInput.matches(Regex("^[+]?[0-9 -]{5,20}$"))) {
            cleanInput
        } else {
            val found = findContact(cleanInput)
            if (found.isNotEmpty()) {
                found.first().phoneNumber
            } else {
                cleanInput
            }
        }

        try {
            // Check CALL_PHONE permission
            val hasCallPermission = context.checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
            val intent = if (hasCallPermission && phoneNumber.matches(Regex("^[+]?[0-9 -]{5,20}$"))) {
                Intent(Intent.ACTION_CALL, Uri.parse("tel:${Uri.encode(phoneNumber)}"))
            } else {
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phoneNumber)}"))
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)

            result.put("status", "success")
            result.put("message", "Initiating call to $phoneNumber")
        } catch (e: Exception) {
            result.put("status", "error")
            result.put("message", "Unable to start call: ${e.message}")
        }
        return result
    }

    /**
     * Prepare SMS using ACTION_SENDTO (Strict Google Play compliant: never silent, no SEND_SMS permission)
     */
    fun prepareSms(contactOrNumber: String, messageText: String): JSONObject {
        val result = JSONObject()
        val cleanInput = contactOrNumber.trim()
        val phoneNumber: String = if (cleanInput.matches(Regex("^[+]?[0-9 -]{5,20}$"))) {
            cleanInput
        } else {
            val found = findContact(cleanInput)
            if (found.isNotEmpty()) found.first().phoneNumber else cleanInput
        }

        try {
            val uri = Uri.parse("smsto:${Uri.encode(phoneNumber)}")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", messageText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            result.put("status", "success")
            result.put("message", "SMS prepared for $phoneNumber")
        } catch (e: Exception) {
            result.put("status", "error")
            result.put("message", "Unable to prepare SMS: ${e.message}")
        }
        return result
    }

    /**
     * Prepare WhatsApp message
     */
    fun prepareWhatsApp(phoneOrName: String, messageText: String): JSONObject {
        val result = JSONObject()
        val target = phoneOrName.trim()
        val cleanNumber = target.filter { it.isDigit() || it == '+' }

        try {
            val encodedMsg = URLEncoder.encode(messageText, "UTF-8")
            val uriStr = if (cleanNumber.isNotBlank()) {
                "https://api.whatsapp.com/send?phone=$cleanNumber&text=$encodedMsg"
            } else {
                "https://api.whatsapp.com/send?text=$encodedMsg"
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Verify if WhatsApp is installed
            val activities = packageManager.queryIntentActivities(intent, 0)
            if (activities.isNotEmpty()) {
                context.startActivity(intent)
                result.put("status", "success")
                result.put("message", "WhatsApp message prepared")
            } else {
                // Fallback to browser WhatsApp web
                intent.setPackage(null)
                context.startActivity(intent)
                result.put("status", "success")
                result.put("message", "Opened in browser")
            }
        } catch (e: Exception) {
            result.put("status", "error")
            result.put("message", "Unable to open WhatsApp: ${e.message}")
        }
        return result
    }

    /**
     * Open website in browser
     */
    fun openWebsite(url: String): JSONObject {
        val result = JSONObject()
        try {
            val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else {
                url
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            result.put("status", "success")
            result.put("message", "Opened $validUrl")
        } catch (e: Exception) {
            result.put("status", "error")
            result.put("message", "Error opening website: ${e.message}")
        }
        return result
    }

    /**
     * Launch system settings screen
     */
    fun openSettings(target: String = "general"): JSONObject {
        val result = JSONObject()
        try {
            val action = when (target.lowercase()) {
                "wifi" -> Settings.ACTION_WIFI_SETTINGS
                "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
                "sound" -> Settings.ACTION_SOUND_SETTINGS
                "display" -> Settings.ACTION_DISPLAY_SETTINGS
                "accessibility" -> Settings.ACTION_ACCESSIBILITY_SETTINGS
                "apps" -> Settings.ACTION_APPLICATION_SETTINGS
                else -> Settings.ACTION_SETTINGS
            }
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            result.put("status", "success")
            result.put("message", "Opened Settings: $target")
        } catch (e: Exception) {
            result.put("status", "error")
            result.put("message", "Error opening settings: ${e.message}")
        }
        return result
    }

    companion object {
        @Volatile
        private var instance: PhoneActionController? = null

        fun getInstance(context: Context): PhoneActionController {
            return instance ?: synchronized(this) {
                instance ?: PhoneActionController(context.applicationContext).also { instance = it }
            }
        }
    }
}
