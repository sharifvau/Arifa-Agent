package com.arifa.assistant.core.websiteforge

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class BuildProgressStep(val title: String, val subtitle: String, val progress: Float) {
    ANALYZING("Analyzing Request", "Extracting business context, theme and visual style...", 0.15f),
    GENERATING_3D("Generating 3D Scene", "Injecting Three.js interactive 3D hero canvas...", 0.40f),
    STRUCTURING_SECTIONS("Structuring Layout", "Synthesizing semantic HTML5 & responsive grid...", 0.65f),
    POLISHING_CSS("Polishing 3D & CSS", "Applying glassmorphism cards and animations...", 0.85f),
    FINALIZING("Finalizing Build", "Compiling production bundle and previewing...", 1.0f)
}

data class WebsiteSection(
    val name: String,
    val iconName: String,
    val isDetected: Boolean
)

data class WebsiteProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val prompt: String,
    val htmlCode: String,
    val createdAt: Long = System.currentTimeMillis(),
    val detectedSections: List<String> = emptyList()
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("title", title)
            put("prompt", prompt)
            put("htmlCode", htmlCode)
            put("createdAt", createdAt)
            val secArray = JSONArray()
            detectedSections.forEach { secArray.put(it) }
            put("detectedSections", secArray)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): WebsiteProject {
            val sections = mutableListOf<String>()
            val secArray = json.optJSONArray("detectedSections")
            if (secArray != null) {
                for (i in 0 until secArray.length()) {
                    sections.add(secArray.getString(i))
                }
            }
            return WebsiteProject(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = json.optString("title", "Untitled Project"),
                prompt = json.optString("prompt", ""),
                htmlCode = json.optString("htmlCode", ""),
                createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                detectedSections = sections
            )
        }
    }
}
