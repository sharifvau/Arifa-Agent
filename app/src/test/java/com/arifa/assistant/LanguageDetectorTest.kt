package com.arifa.assistant

import com.arifa.assistant.core.language.DetectedLanguage
import com.arifa.assistant.core.language.LanguageDetector
import com.arifa.assistant.core.storage.AssistantLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageDetectorTest {

    @Test
    fun testBengaliUnicodeDetection() {
        val result = LanguageDetector.detect("কেমন আছেন? আশা করি ভালো আছেন।")
        assertEquals(DetectedLanguage.BANGLA, result.language)
        assertTrue(result.confidence > 0.8f)
    }

    @Test
    fun testBanglishPhoneticDetection() {
        val result = LanguageDetector.detect("kemon acho? amar jonno ekta website banao.")
        assertEquals(DetectedLanguage.BANGLISH, result.language)
        assertTrue(result.confidence > 0.6f)
    }

    @Test
    fun testBanglishColloquialPhrases() {
        val result = LanguageDetector.detect("kire mama ki obstha pera nai chill")
        assertEquals(DetectedLanguage.BANGLISH, result.language)
    }

    @Test
    fun testEnglishDetection() {
        val result = LanguageDetector.detect("Hello Arifa, please set a timer for 10 minutes.")
        assertEquals(DetectedLanguage.ENGLISH, result.language)
    }

    @Test
    fun testPreferenceOverride() {
        val forcedBangla = LanguageDetector.detect("Hello world", AssistantLanguage.BANGLA)
        assertEquals(DetectedLanguage.BANGLA, forcedBangla.language)

        val forcedEnglish = LanguageDetector.detect("কেমন আছেন", AssistantLanguage.ENGLISH)
        assertEquals(DetectedLanguage.ENGLISH, forcedEnglish.language)
    }
}
