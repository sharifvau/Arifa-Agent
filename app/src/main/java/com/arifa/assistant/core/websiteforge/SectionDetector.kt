package com.arifa.assistant.core.websiteforge

object SectionDetector {

    fun detectSections(html: String): List<String> {
        val lower = html.lowercase()
        val sections = mutableListOf<String>()

        if (lower.contains("<nav") || lower.contains("class=\"nav") || lower.contains("id=\"nav")) {
            sections.add("Navigation")
        }
        if (lower.contains("three.min.js") || lower.contains("webgl") || lower.contains("canvas") || lower.contains("three.")) {
            sections.add("Three.js 3D Canvas")
        }
        if (lower.contains("class=\"hero") || lower.contains("id=\"hero") || lower.contains("<header")) {
            sections.add("Hero Banner")
        }
        if (lower.contains("feature") || lower.contains("service") || lower.contains("highlight")) {
            sections.add("Features / Highlights")
        }
        if (lower.contains("menu") || lower.contains("gallery") || lower.contains("portfolio") || lower.contains("card")) {
            sections.add("Gallery / Showcase")
        }
        if (lower.contains("about") || lower.contains("story") || lower.contains("team")) {
            sections.add("About Us")
        }
        if (lower.contains("contact") || lower.contains("form") || lower.contains("book") || lower.contains("order")) {
            sections.add("Contact / Booking")
        }
        if (lower.contains("<footer") || lower.contains("class=\"footer") || lower.contains("id=\"footer")) {
            sections.add("Footer")
        }

        return sections
    }
}
