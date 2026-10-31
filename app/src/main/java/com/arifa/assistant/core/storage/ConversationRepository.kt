package com.arifa.assistant.core.storage

import android.content.Context
import com.arifa.assistant.core.common.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String = "AUTO",
    val isForgeCommand: Boolean = false,
    val forgeProjectTitle: String? = null
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("role", role)
            put("content", content)
            put("timestamp", timestamp)
            put("language", language)
            put("isForgeCommand", isForgeCommand)
            if (forgeProjectTitle != null) put("forgeProjectTitle", forgeProjectTitle)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): ChatMessage {
            return ChatMessage(
                id = json.optString("id", UUID.randomUUID().toString()),
                role = json.optString("role", "user"),
                content = json.optString("content", ""),
                timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                language = json.optString("language", "AUTO"),
                isForgeCommand = json.optBoolean("isForgeCommand", false),
                forgeProjectTitle = if (json.has("forgeProjectTitle")) json.getString("forgeProjectTitle") else null
            )
        }
    }
}

data class ConversationSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messages: List<ChatMessage> = emptyList()
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("title", title)
        json.put("createdAt", createdAt)
        json.put("updatedAt", updatedAt)
        val msgsArray = JSONArray()
        for (m in messages) {
            msgsArray.put(m.toJson())
        }
        json.put("messages", msgsArray)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): ConversationSession {
            val msgs = mutableListOf<ChatMessage>()
            val msgsArray = json.optJSONArray("messages")
            if (msgsArray != null) {
                for (i in 0 until msgsArray.length()) {
                    msgs.add(ChatMessage.fromJson(msgsArray.getJSONObject(i)))
                }
            }
            return ConversationSession(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = json.optString("title", "New Conversation"),
                createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = json.optLong("updatedAt", System.currentTimeMillis()),
                messages = msgs
            )
        }
    }
}

class ConversationRepository private constructor(private val context: Context) {

    private val storageFile: File by lazy {
        File(context.filesDir, "arifa_conversations.json")
    }

    private val _sessions = MutableStateFlow<List<ConversationSession>>(emptyList())
    val sessions: StateFlow<List<ConversationSession>> = _sessions.asStateFlow()

    private val _currentSession = MutableStateFlow<ConversationSession?>(null)
    val currentSession: StateFlow<ConversationSession?> = _currentSession.asStateFlow()

    suspend fun initialize() = withContext(Dispatchers.IO) {
        loadFromDisk()
        if (_sessions.value.isEmpty()) {
            val defaultSession = createNewSession("New Conversation")
            _currentSession.value = defaultSession
        } else {
            _currentSession.value = _sessions.value.first()
        }
    }

    private suspend fun loadFromDisk() = withContext(Dispatchers.IO) {
        try {
            if (!storageFile.exists()) {
                _sessions.value = emptyList()
                return@withContext
            }
            val content = storageFile.readText()
            if (content.isBlank()) {
                _sessions.value = emptyList()
                return@withContext
            }
            val array = JSONArray(content)
            val list = mutableListOf<ConversationSession>()
            for (i in 0 until array.length()) {
                list.add(ConversationSession.fromJson(array.getJSONObject(i)))
            }
            _sessions.value = list.sortedByDescending { it.updatedAt }
        } catch (e: Exception) {
            _sessions.value = emptyList()
        }
    }

    private suspend fun saveToDisk() = withContext(Dispatchers.IO) {
        try {
            val array = JSONArray()
            for (s in _sessions.value) {
                array.put(s.toJson())
            }
            storageFile.writeText(array.toString(2))
        } catch (e: Exception) {
            // Silently recover or handle IO exception
        }
    }

    suspend fun createNewSession(title: String = "New Conversation"): ConversationSession = withContext(Dispatchers.IO) {
        val session = ConversationSession(
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            messages = emptyList()
        )
        _sessions.value = listOf(session) + _sessions.value
        _currentSession.value = session
        saveToDisk()
        session
    }

    suspend fun selectSession(sessionId: String) {
        val found = _sessions.value.find { it.id == sessionId }
        if (found != null) {
            _currentSession.value = found
        }
    }

    suspend fun addMessageToCurrentSession(message: ChatMessage) = withContext(Dispatchers.IO) {
        val current = _currentSession.value ?: createNewSession(message.content.take(30))
        val updatedMessages = current.messages + message
        val updatedTitle = if (current.messages.isEmpty() && message.role == "user") {
            message.content.take(32).replace("\n", " ").trim()
        } else {
            current.title
        }

        val updatedSession = current.copy(
            title = updatedTitle,
            updatedAt = System.currentTimeMillis(),
            messages = updatedMessages
        )

        _currentSession.value = updatedSession
        _sessions.value = _sessions.value.map {
            if (it.id == updatedSession.id) updatedSession else it
        }.sortedByDescending { it.updatedAt }

        saveToDisk()
    }

    suspend fun deleteSession(sessionId: String) = withContext(Dispatchers.IO) {
        _sessions.value = _sessions.value.filter { it.id != sessionId }
        if (_currentSession.value?.id == sessionId) {
            _currentSession.value = _sessions.value.firstOrNull()
        }
        saveToDisk()
    }

    suspend fun clearCurrentSessionMessages() = withContext(Dispatchers.IO) {
        val current = _currentSession.value ?: return@withContext
        val updated = current.copy(
            messages = emptyList(),
            updatedAt = System.currentTimeMillis()
        )
        _currentSession.value = updated
        _sessions.value = _sessions.value.map {
            if (it.id == updated.id) updated else it
        }
        saveToDisk()
    }

    companion object {
        @Volatile
        private var instance: ConversationRepository? = null

        fun getInstance(context: Context): ConversationRepository {
            return instance ?: synchronized(this) {
                instance ?: ConversationRepository(context).also { instance = it }
            }
        }
    }
}
