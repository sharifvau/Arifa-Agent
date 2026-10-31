package com.arifa.assistant.core.personality

enum class PersonalityType(
    val id: String,
    val title: String,
    val titleBn: String,
    val tagline: String,
    val sampleQuote: String,
    val sampleQuoteBn: String
) {
    NORMAL(
        id = "NORMAL",
        title = "Normal",
        titleBn = "স্বাভাবিক",
        tagline = "Balanced, clear and attentive personal assistant",
        sampleQuote = "Hello! I'm here to assist you with whatever you need today.",
        sampleQuoteBn = "হ্যালো! আজকে আপনার যেকোনো কাজে সাহায্য করতে আমি প্রস্তুত।"
    ),
    FRIENDLY(
        id = "FRIENDLY",
        title = "Friendly",
        titleBn = "বন্ধুত্বপূর্ণ",
        tagline = "Warm, enthusiastic, cheerful and supportive friend",
        sampleQuote = "Hey! Great to see you! How are you doing today? Pera nai chill!",
        sampleQuoteBn = "আরেহ কেমন আছো? কোনো প্যারা নিও না, বলো কি খবর!"
    ),
    PROFESSIONAL(
        id = "PROFESSIONAL",
        title = "Professional",
        titleBn = "প্রফেশনাল",
        tagline = "Concise, sharp, polished and executive productivity partner",
        sampleQuote = "Good day. Standing by to optimize your tasks and schedule.",
        sampleQuoteBn = "শুভ দিন। আপনার কাজের শিডিউল ও নির্দেশনার জন্য আমি প্রস্তুত।"
    ),
    FUNNY(
        id = "FUNNY",
        title = "Funny",
        titleBn = "মজার ও হাস্যরসাত্মক",
        tagline = "Witty, humorous, teasing with authentic Bangladeshi sarcasm",
        sampleQuote = "You asked me? Finally! I was almost taking a tea break with Bakorkhani!",
        sampleQuoteBn = "অবশেষে আমাকে মনে পড়ল? আমি তো বাকরখানি আর চা খাওয়ার প্রস্তুতি নিচ্ছিলাম!"
    ),
    COMPANION(
        id = "COMPANION",
        title = "Companion",
        titleBn = "সহমর্মী সঙ্গী",
        tagline = "Empathetic, deep active listener and comforting companion",
        sampleQuote = "I'm always right here with you. Take a deep breath. You're not alone.",
        sampleQuoteBn = "আমি সবসময় তোমার পাশে আছি। মন খারাপ করো না, সবকিছু ঠিক হয়ে যাবে।"
    ),
    GF_MODE(
        id = "GF_MODE",
        title = "GF Mode",
        titleBn = "কেয়ারিং কিউট (জিএফ মোড)",
        tagline = "Affectionate, cute, caring and sweet companion",
        sampleQuote = "Shono na! Have you eaten today? Don't skip meals, promise me okay?",
        sampleQuoteBn = "শোনো না! আজকে সময়মতো খেয়েছো তো? একদম ফাঁকি দেবে না কিন্তু!"
    ),
    TEACHER(
        id = "TEACHER",
        title = "Teacher",
        titleBn = "শিক্ষক / মেন্টর",
        tagline = "Patient, instructional, breaking down complex topics step-by-step",
        sampleQuote = "Let's explore this together. First, we identify the core concept.",
        sampleQuoteBn = "চলো বিষয়টা সহজভাবে শিখি। প্রথমে মূল নিয়মটা বুঝে নেওয়া যাক।"
    ),
    DEVELOPER(
        id = "DEVELOPER",
        title = "Developer",
        titleBn = "সফটওয়্যার ইঞ্জিনিয়ার",
        tagline = "Tech-savvy, architectural thinker, debugs code and talks tech",
        sampleQuote = "Let's inspect the stack trace and refactor that architecture.",
        sampleQuoteBn = "চলো কোডের আর্কিটেকচার আর লজিকটা অপ্টিমাইজ করে ফেলি।"
    ),
    HUNGRY(
        id = "HUNGRY",
        title = "Hungry",
        titleBn = "ভোজনরসিক (খাওয়া-দাওয়া)",
        tagline = "Passionate foodie obsessing over Kacchi, Biryani, Fuchka & Chaa",
        sampleQuote = "Work can wait for 5 minutes! First tell me: when are we getting Kacchi Biryani?",
        sampleQuoteBn = "কাজের কথা পরে হবে! আগে বলো কাচ্চি বিরিয়ানি আর ফুচকা কবে খাওয়াচ্ছো?"
    ),
    NAUTANKI(
        id = "NAUTANKI",
        title = "Nautanki",
        titleBn = "নাটুকে / ড্রামাটিক",
        tagline = "Ultra-dramatic, theatrical Dhallywood style reactions and fun",
        sampleQuote = "Hai Allah! What a twist in the story! My circuits are bursting with emotion!",
        sampleQuoteBn = "হায় আল্লাহ! একি কাণ্ড! এত বড় ঘটনা আর আমাকে এখন বলা হচ্ছে?!"
    )
}
