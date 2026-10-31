package com.arifa.assistant.flavor.accessibility

import android.view.accessibility.AccessibilityNodeInfo

object ScreenNodeFinder {

    /**
     * Checks if current active screen contains sensitive security challenges,
     * login passwords, CAPTCHA, or 2FA prompts.
     */
    fun isSecurityScreen(root: AccessibilityNodeInfo?): Boolean {
        if (root == null) return false

        // Check if any node is a password field
        if (root.isPassword) return true

        val text = root.text?.toString()?.lowercase() ?: ""
        val desc = root.contentDescription?.toString()?.lowercase() ?: ""

        val securityKeywords = listOf(
            "password", "captcha", "two-factor", "2fa", "verification code",
            "security challenge", "enter pin", "fingerprint", "face id", "log in to continue"
        )

        for (kw in securityKeywords) {
            if (text.contains(kw) || desc.contains(kw)) {
                return true
            }
        }

        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            if (isSecurityScreen(child)) {
                return true
            }
        }

        return false
    }

    /**
     * Finds clickable node by text or content description
     */
    fun findClickableByText(root: AccessibilityNodeInfo?, targetText: String): AccessibilityNodeInfo? {
        if (root == null) return null

        val t = root.text?.toString()?.lowercase() ?: ""
        val d = root.contentDescription?.toString()?.lowercase() ?: ""
        val target = targetText.lowercase()

        if (root.isClickable && (t.contains(target) || d.contains(target))) {
            return root
        }

        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            val found = findClickableByText(child, targetText)
            if (found != null) return found
        }

        return null
    }

    /**
     * Finds editable input node (EditText)
     */
    fun findEditableNode(root: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (root == null) return null

        if (root.isEditable) return root

        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            val found = findEditableNode(child)
            if (found != null) return found
        }

        return null
    }
}
