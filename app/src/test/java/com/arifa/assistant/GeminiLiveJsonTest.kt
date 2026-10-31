package com.arifa.assistant

import com.arifa.assistant.core.gemini.live.LiveJsonFactory
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiLiveJsonTest {

    @Test
    fun testBuildSetupMessageStructure() {
        val setupStr = LiveJsonFactory.buildSetupMessage(
            model = "models/gemini-2.0-flash-exp",
            systemInstruction = "You are Arifa AI.",
            voiceName = "Aoede"
        )
        val json = JSONObject(setupStr)
        assertTrue(json.has("setup"))

        val setup = json.getJSONObject("setup")
        assertEquals("models/gemini-2.0-flash-exp", setup.getString("model"))
        assertTrue(setup.has("generationConfig"))
        assertTrue(setup.has("systemInstruction"))
        assertTrue(setup.has("tools"))

        val tools = setup.getJSONArray("tools")
        val funcDecls = tools.getJSONObject(0).getJSONArray("functionDeclarations")
        assertTrue(funcDecls.length() >= 5) // open_app, call_contact, send_sms, send_whatsapp, create_website
    }

    @Test
    fun testBuildAudioChunkMessage() {
        val msg = LiveJsonFactory.buildAudioChunkMessage("AAAA")
        val json = JSONObject(msg)
        assertTrue(json.has("realtimeInput"))
        val mediaChunks = json.getJSONObject("realtimeInput").getJSONArray("mediaChunks")
        val first = mediaChunks.getJSONObject(0)
        assertEquals("audio/pcm;rate=16000", first.getString("mimeType"))
        assertEquals("AAAA", first.getString("data"))
    }

    @Test
    fun testBuildToolResponseMessage() {
        val output = JSONObject().apply { put("result", "Launched YouTube") }
        val msg = LiveJsonFactory.buildToolResponseMessage("call-123", output)
        val json = JSONObject(msg)
        assertTrue(json.has("toolResponse"))
        val funcResponses = json.getJSONObject("toolResponse").getJSONArray("functionResponses")
        val first = funcResponses.getJSONObject(0)
        assertEquals("call-123", first.getString("id"))
        assertEquals("Launched YouTube", first.getJSONObject("response").getJSONObject("output").getString("result"))
    }
}
