package com.arifa.assistant.core.language

import com.arifa.assistant.core.storage.AssistantLanguage

enum class DetectedLanguage(val code: String, val displayName: String) {
    BANGLA("bn", "বাংলা"),
    BANGLISH("banglish", "Banglish"),
    ENGLISH("en", "English")
}

data class DetectionResult(
    val language: DetectedLanguage,
    val confidence: Float,
    val isMixed: Boolean = false
)

object LanguageDetector {

    private val BENGALI_UNICODE_REGEX = Regex("[\\u0980-\\u09FF]")
    private val WORD_SPLIT_REGEX = Regex("[^a-zA-Z\\u0980-\\u09FF0-9]+")

    /**
     * Detects language from user text taking into account configured preference
     */
    fun detect(text: String, preference: AssistantLanguage = AssistantLanguage.AUTO): DetectionResult {
        if (text.isBlank()) {
            return when (preference) {
                AssistantLanguage.BANGLA -> DetectionResult(DetectedLanguage.BANGLA, 1.0f)
                AssistantLanguage.BANGLISH -> DetectionResult(DetectedLanguage.BANGLISH, 1.0f)
                AssistantLanguage.ENGLISH -> DetectionResult(DetectedLanguage.ENGLISH, 1.0f)
                AssistantLanguage.AUTO -> DetectionResult(DetectedLanguage.BANGLA, 0.5f)
            }
        }

        // If manual override is forced
        if (preference != AssistantLanguage.AUTO) {
            val detected = when (preference) {
                AssistantLanguage.BANGLA -> DetectedLanguage.BANGLA
                AssistantLanguage.BANGLISH -> DetectedLanguage.BANGLISH
                AssistantLanguage.ENGLISH -> DetectedLanguage.ENGLISH
                else -> DetectedLanguage.BANGLA
            }
            return DetectionResult(detected, 1.0f)
        }

        val totalChars = text.length
        val bengaliCharsCount = BENGALI_UNICODE_REGEX.findAll(text).count()

        // 1. Direct Bengali script check
        if (bengaliCharsCount > 0) {
            val ratio = bengaliCharsCount.toFloat() / totalChars.toFloat()
            val isMixed = ratio in 0.15f..0.85f
            return DetectionResult(
                language = DetectedLanguage.BANGLA,
                confidence = (ratio * 1.2f).coerceAtMost(1.0f),
                isMixed = isMixed
            )
        }

        // 2. Latin script analysis: Banglish vs English
        val words = text.lowercase().split(WORD_SPLIT_REGEX).filter { it.isNotBlank() }
        if (words.isEmpty()) {
            return DetectionResult(DetectedLanguage.ENGLISH, 0.5f)
        }

        var banglishScore = 0
        for (w in words) {
            if (BengaliExpressions.BANGLISH_KEYWORDS.contains(w)) {
                banglishScore += 2
            } else if (w.endsWith("so") || w.endsWith("cho") || w.endsWith("chen") ||
                w.endsWith("lam") || w.endsWith("ben") || w.endsWith("tesi") ||
                w.endsWith("techi") || w.endsWith("gulo") || w.endsWith("der")
            ) {
                banglishScore += 1
            }
        }

        val wordCount = words.size
        val banglishRatio = banglishScore.toFloat() / (wordCount * 1.5f)

        return if (banglishScore >= 2 || (wordCount <= 2 && banglishScore >= 1)) {
            DetectionResult(
                language = DetectedLanguage.BANGLISH,
                confidence = banglishRatio.coerceIn(0.6f, 0.98f),
                isMixed = banglishScore > 0 && banglishRatio < 0.6f
            )
        } else {
            DetectionResult(
                language = DetectedLanguage.ENGLISH,
                confidence = 0.9f,
                isMixed = false
            )
        }
    }
}
