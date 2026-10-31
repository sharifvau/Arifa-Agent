package com.arifa.assistant.core.gemini.rest

import com.arifa.assistant.core.common.Constants
import com.arifa.assistant.core.common.Result
import com.arifa.assistant.core.storage.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiRestClient {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateChatResponse(
        apiKey: String,
        systemInstruction: String,
        history: List<ChatMessage>,
        userMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.Error(IllegalStateException("Gemini API key is required."))
        }

        val requestJson = RestJsonFactory.buildChatRequestBody(systemInstruction, history, userMessage)
        val url = "${Constants.GEMINI_REST_BASE_URL}${Constants.DEFAULT_REST_MODEL}:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toRequestBody(jsonMediaType))
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = when (response.code) {
                    400 -> "Bad Request: Check parameters or input format."
                    403 -> "Invalid API Key or quota exhausted."
                    429 -> "Rate limit reached. Please wait a moment."
                    else -> "API Error: HTTP ${response.code}"
                }
                return@withContext Result.Error(IOException(errorMsg))
            }

            val text = RestJsonFactory.parseResponseText(responseBody)
            if (text.isBlank()) {
                return@withContext Result.Error(IOException("Empty response from Gemini."))
            }

            Result.Success(text)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to connect to Gemini API.")
        }
    }

    /**
     * Simulates natural fluid word-by-word streaming reveal for smooth reading UX
     */
    fun streamWordReveal(fullText: String): Flow<String> = flow {
        val words = fullText.split(" ")
        val sb = StringBuilder()
        for (i in words.indices) {
            sb.append(words[i])
            if (i < words.size - 1) sb.append(" ")
            emit(sb.toString())
            delay(18) // Natural fluid reading speed
        }
    }.flowOn(Dispatchers.Default)
}
