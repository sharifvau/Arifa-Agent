package com.arifa.assistant.core.gemini.live

import org.json.JSONArray
import org.json.JSONObject

sealed class LiveServerEvent {
    data object SetupComplete : LiveServerEvent()
    data class AudioChunk(val pcmData: ByteArray) : LiveServerEvent()
    data class TextChunk(val text: String) : LiveServerEvent()
    data class TurnComplete(val isComplete: Boolean) : LiveServerEvent()
    data object Interrupted : LiveServerEvent() // Barge-in detected
    data class ToolCall(val calls: List<FunctionCall>) : LiveServerEvent()
    data class Error(val message: String, val throwable: Throwable? = null) : LiveServerEvent()
}

data class FunctionCall(
    val id: String,
    val name: String,
    val args: JSONObject
)

object LiveJsonFactory {

    fun buildSetupMessage(
        model: String,
        systemInstruction: String,
        voiceName: String = "Aoede"
    ): String {
        val root = JSONObject()
        val setup = JSONObject()

        setup.put("model", model)

        val genConfig = JSONObject()
        val responseModalities = JSONArray().apply { put("AUDIO") }
        genConfig.put("responseModalities", responseModalities)

        val speechConfig = JSONObject()
        val voiceConfig = JSONObject()
        val prebuiltVoiceConfig = JSONObject()
        prebuiltVoiceConfig.put("voiceName", voiceName)
        voiceConfig.put("prebuiltVoiceConfig", prebuiltVoiceConfig)
        speechConfig.put("voiceConfig", voiceConfig)
        genConfig.put("speechConfig", speechConfig)

        setup.put("generationConfig", genConfig)

        val sysInstructionObj = JSONObject()
        val parts = JSONArray().apply {
            put(JSONObject().apply { put("text", systemInstruction) })
        }
        sysInstructionObj.put("parts", parts)
        setup.put("systemInstruction", sysInstructionObj)

        // Tools / Function declarations
        val toolsArray = JSONArray()
        val toolObj = JSONObject()
        val funcDecls = JSONArray()

        // 1. open_app
        funcDecls.put(JSONObject().apply {
            put("name", "open_app")
            put("description", "Open an installed mobile application by name")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("appName", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Name of the app (e.g., YouTube, Camera, Calculator)")
                    })
                })
                put("required", JSONArray().apply { put("appName") })
            })
        })

        // 2. call_contact
        funcDecls.put(JSONObject().apply {
            put("name", "call_contact")
            put("description", "Place a phone call or search contact on user phone")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("contactName", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Name or phone number of the person to call")
                    })
                })
                put("required", JSONArray().apply { put("contactName") })
            })
        })

        // 3. send_sms
        funcDecls.put(JSONObject().apply {
            put("name", "send_sms")
            put("description", "Prepare an SMS message in the default messaging app")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("contact", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Phone number or contact name")
                    })
                    put("message", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Text content of the message")
                    })
                })
                put("required", JSONArray().apply {
                    put("contact")
                    put("message")
                })
            })
        })

        // 4. send_whatsapp
        funcDecls.put(JSONObject().apply {
            put("name", "send_whatsapp")
            put("description", "Open WhatsApp with pre-filled message text")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("phoneOrName", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Phone number or recipient name")
                    })
                    put("message", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Message text")
                    })
                })
                put("required", JSONArray().apply {
                    put("phoneOrName")
                    put("message")
                })
            })
        })

        // 5. create_website
        funcDecls.put(JSONObject().apply {
            put("name", "create_website")
            put("description", "Launch Website Forge to build a complete 3D interactive website")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("topic", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Topic, business type or theme for the website")
                    })
                })
                put("required", JSONArray().apply { put("topic") })
            })
        })

        toolObj.put("functionDeclarations", funcDecls)
        toolsArray.put(toolObj)
        setup.put("tools", toolsArray)

        root.put("setup", setup)
        return root.toString()
    }

    fun buildAudioChunkMessage(base64Pcm: String): String {
        val root = JSONObject()
        val realtimeInput = JSONObject()
        val mediaChunks = JSONArray()
        val chunk = JSONObject()
        chunk.put("mimeType", "audio/pcm;rate=16000")
        chunk.put("data", base64Pcm)
        mediaChunks.put(chunk)
        realtimeInput.put("mediaChunks", mediaChunks)
        root.put("realtimeInput", realtimeInput)
        return root.toString()
    }

    fun buildToolResponseMessage(callId: String, resultJson: JSONObject): String {
        val root = JSONObject()
        val toolResponse = JSONObject()
        val functionResponses = JSONArray()
        val funcResp = JSONObject()
        funcResp.put("id", callId)
        val response = JSONObject()
        response.put("output", resultJson)
        funcResp.put("response", response)
        functionResponses.put(funcResp)
        toolResponse.put("functionResponses", functionResponses)
        root.put("toolResponse", toolResponse)
        return root.toString()
    }
}
