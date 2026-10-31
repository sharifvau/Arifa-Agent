package com.arifa.assistant.core.language

object BengaliExpressions {

    /**
     * Common Banglish (Bengali written in English alphabet) keywords and tokens.
     * Used for language detection when user inputs Latin characters.
     */
    val BANGLISH_KEYWORDS = setOf(
        "kemon", "acho", "achi", "achis", "asen", "achen", "bhalo", "valo",
        "ki", "khobor", "obostha", "obstha", "pera", "nai", "chill", "mama",
        "bhai", "apa", "apu", "vai", "amar", "tomar", "apnar", "amader",
        "jonno", "ekta", "koro", "korba", "korben", "koren", "banao", "banaw",
        "banaye", "dao", "den", "dekhaw", "dekhao", "shono", "shunen",
        "jani", "janina", "hobe", "hobena", "thik", "ache", "asen",
        "kothay", "kobe", "keno", "kivabe", "kibhabe", "shob", "sob",
        "khabar", "kheyecho", "khaiso", "khawa", "khabo", "cha", "chaa",
        "kacchi", "biryani", "fuchka", "morog", "polao", "ilish", "macher",
        "bhat", "ghum", "ghumaw", "ghumao", "shorbonash", "hai", "allah",
        "ekhon", "ajke", "kalkey", "agami", "kal", "mone", "hoy", "hobe",
        "bolun", "bolo", "bolis", "shundor", "darun", "khub", "oneck", "onek",
        "bhaiya", "bhai re", "pagol", "naki", "ki je bolo", "areh", "ore",
        "mon", "kharap", "kanna", "haso", "haste", "shuru", "sesh"
    )

    /**
     * Common conversational starters and cultural greetings in Bangladesh
     */
    val GREETINGS_BANGLA = listOf(
        "আসসালামু আলাইকুম! কেমন আছেন?",
        "নমস্কার! কেমন আছেন আপনি?",
        "হ্যালো! আমি আরিফা, আপনার এআই সঙ্গী। কি সাহায্য করতে পারি?"
    )

    val GREETINGS_BANGLISH = listOf(
        "Assalamu Alaikum! Kemon achen?",
        "Hey! Ami Arifa, apnar personal AI assistant. Ki khobor?",
        "Hello! Kemon acho? Ajke ki help korte pari?"
    )

    val GREETINGS_ENGLISH = listOf(
        "Hello! I am Arifa, your AI companion. How can I help you today?",
        "Hey there! Ready to assist you anytime. What's on your mind?"
    )
}
