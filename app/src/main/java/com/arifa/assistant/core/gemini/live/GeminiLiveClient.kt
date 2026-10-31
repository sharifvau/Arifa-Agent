package com.arifa.assistant.core.gemini.live

import com.arifa.assistant.core.common.Constants
import com.arifa.assistant.core.common.fromBase64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class GeminiLiveClient(
    private val scope: CoroutineScope
) {
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // Indefinite for WebSocket
        .writeTimeout(15, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val isConnected = AtomicBoolean(false)
    private val isExplicitDisconnect = AtomicBoolean(false)
    private var reconnectAttempts = 0
    private var reconnectJob: Job? = null

    // Cached connection params for auto-reconnect
    private var currentApiKey: String = ""
    private var currentSystemInstruction: String = ""
    private var currentVoiceName: String = "Aoede"

    private val _events = MutableSharedFlow<LiveServerEvent>(replay = 0, extraBufferCapacity = 64)
    val events: SharedFlow<LiveServerEvent> = _events.asSharedFlow()

    fun connect(apiKey: String, systemInstruction: String, voiceName: String = "Aoede") {
        if (apiKey.isBlank()) {
            _events.tryEmit(LiveServerEvent.Error("Gemini API key is not configured."))
            return
        }

        currentApiKey = apiKey
        currentSystemInstruction = systemInstruction
        currentVoiceName = voiceName
        isExplicitDisconnect.set(false)
        reconnectAttempts = 0

        establishWebSocket()
    }

    private fun establishWebSocket() {
        val url = "${Constants.GEMINI_LIVE_WS_URL}?key=$currentApiKey"
        val request = Request.Builder().url(url).build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected.set(true)
                reconnectAttempts = 0
                // Send setup message immediately
                val setupJson = LiveJsonFactory.buildSetupMessage(
                    model = Constants.DEFAULT_LIVE_MODEL,
                    systemInstruction = currentSystemInstruction,
                    voiceName = currentVoiceName
                )
                webSocket.send(setupJson)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                parseServerMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                isConnected.set(false)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected.set(false)
                if (!isExplicitDisconnect.get()) {
                    triggerReconnect()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected.set(false)
                _events.tryEmit(LiveServerEvent.Error("Connection failure: ${t.message}", t))
                if (!isExplicitDisconnect.get()) {
                    triggerReconnect()
                }
            }
        })
    }

    private fun parseServerMessage(text: String) {
        try {
            val json = JSONObject(text)

            // 1. Setup complete
            if (json.has("setupComplete")) {
                _events.tryEmit(LiveServerEvent.SetupComplete)
                return
            }

            // 2. Server content (audio, transcript, turns, barge-in)
            if (json.has("serverContent")) {
                val serverContent = json.getJSONObject("serverContent")

                // Check interrupted / barge-in
                if (serverContent.optBoolean("interrupted", false)) {
                    _events.tryEmit(LiveServerEvent.Interrupted)
                }

                // Check model turn parts
                if (serverContent.has("modelTurn")) {
                    val modelTurn = serverContent.getJSONObject("modelTurn")
                    val parts = modelTurn.optJSONArray("parts")
                    if (parts != null) {
                        for (i in 0 until parts.length()) {
                            val part = parts.getJSONObject(i)
                            // Audio chunk
                            if (part.has("inlineData")) {
                                val inlineData = part.getJSONObject("inlineData")
                                val base64Data = inlineData.optString("data")
                                if (base64Data.isNotBlank()) {
                                    val pcm = base64Data.fromBase64()
                                    _events.tryEmit(LiveServerEvent.AudioChunk(pcm))
                                }
                            }
                            // Text transcript
                            if (part.has("text")) {
                                val transcript = part.getString("text")
                                if (transcript.isNotBlank()) {
                                    _events.tryEmit(LiveServerEvent.TextChunk(transcript))
                                }
                            }
                        }
                    }
                }

                // Turn complete
                if (serverContent.optBoolean("turnComplete", false)) {
                    _events.tryEmit(LiveServerEvent.TurnComplete(true))
                }
            }

            // 3. Tool call / Function call
            if (json.has("toolCall")) {
                val toolCall = json.getJSONObject("toolCall")
                val funcCalls = toolCall.optJSONArray("functionCalls")
                if (funcCalls != null) {
                    val list = mutableListOf<FunctionCall>()
                    for (i in 0 until funcCalls.length()) {
                        val item = funcCalls.getJSONObject(i)
                        val id = item.optString("id", "")
                        val name = item.optString("name", "")
                        val args = item.optJSONObject("args") ?: JSONObject()
                        list.add(FunctionCall(id, name, args))
                    }
                    if (list.isNotEmpty()) {
                        _events.tryEmit(LiveServerEvent.ToolCall(list))
                    }
                }
            }
        } catch (e: Exception) {
            // Malformed packet safely ignored or dispatched
        }
    }

    private fun triggerReconnect() {
        if (isExplicitDisconnect.get()) return
        if (reconnectAttempts >= 5) {
            _events.tryEmit(LiveServerEvent.Error("Unable to reconnect after multiple attempts."))
            return
        }

        reconnectAttempts++
        val delayMillis = (1000L * (1 shl (reconnectAttempts - 1))).coerceAtMost(16000L)

        reconnectJob?.cancel()
        reconnectJob = scope.launch(Dispatchers.IO) {
            delay(delayMillis)
            if (!isExplicitDisconnect.get() && !isConnected.get()) {
                establishWebSocket()
            }
        }
    }

    fun sendAudioChunk(pcmBase64: String) {
        if (isConnected.get()) {
            val msg = LiveJsonFactory.buildAudioChunkMessage(pcmBase64)
            webSocket?.send(msg)
        }
    }

    fun sendToolResponse(callId: String, result: JSONObject) {
        if (isConnected.get()) {
            val msg = LiveJsonFactory.buildToolResponseMessage(callId, result)
            webSocket?.send(msg)
        }
    }

    fun disconnect() {
        isExplicitDisconnect.set(true)
        reconnectJob?.cancel()
        reconnectJob = null
        isConnected.set(false)
        webSocket?.close(1000, "Client initiated disconnect")
        webSocket = null
    }

    fun isLive(): Boolean = isConnected.get()
}
