package com.arifa.assistant.core.gemini.rest

import com.arifa.assistant.core.storage.ChatMessage
import org.json.JSONArray
import org.json.JSONObject

object RestJsonFactory {

    fun buildChatRequestBody(
        systemInstruction: String,
        history: List<ChatMessage>,
        newMessage: String
    ): String {
        val root = JSONObject()

        // System Instruction
        val sysInst = JSONObject()
        val parts = JSONArray().apply {
            put(JSONObject().apply { put("text", systemInstruction) })
        }
        sysInst.put("parts", parts)
        root.put("systemInstruction", sysInst)

        // Contents (Conversation history + new prompt)
        val contents = JSONArray()

        // Include recent history up to 20 turns
        val recentMessages = history.takeLast(20)
        for (msg in recentMessages) {
            val contentObj = JSONObject()
            contentObj.put("role", if (msg.role == "model") "model" else "user")
            val pArray = JSONArray().apply {
                put(JSONObject().apply { put("text", msg.content) })
            }
            contentObj.put("parts", pArray)
            contents.put(contentObj)
        }

        // Add current user prompt
        val currentPromptObj = JSONObject()
        currentPromptObj.put("role", "user")
        val curParts = JSONArray().apply {
            put(JSONObject().apply { put("text", newMessage) })
        }
        currentPromptObj.put("parts", curParts)
        contents.put(currentPromptObj)

        root.put("contents", contents)

        // Generation config
        val genConfig = JSONObject().apply {
            put("temperature", 0.7)
            put("maxOutputTokens", 2048)
        }
        root.put("generationConfig", genConfig)

        return root.toString()
    }

    fun parseResponseText(responseBody: String): String {
        return try {
            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates") ?: return ""
            if (candidates.length() == 0) return ""
            val first = candidates.getJSONObject(0)
            val content = first.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            val sb = StringBuilder()
            for (i in 0 until parts.length()) {
                val p = parts.getJSONObject(i)
                sb.append(p.optString("text", ""))
            }
            sb.toString()
        } catch (e: Exception) {
            ""
        }
    }
}
