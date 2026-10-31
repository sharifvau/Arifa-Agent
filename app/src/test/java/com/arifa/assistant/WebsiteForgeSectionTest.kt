package com.arifa.assistant

import com.arifa.assistant.core.websiteforge.SectionDetector
import com.arifa.assistant.core.websiteforge.Templates
import org.junit.Assert.assertTrue
import org.junit.Test

class WebsiteForgeSectionTest {

    @Test
    fun testRestaurantTemplateSections() {
        val sections = SectionDetector.detectSections(Templates.RESTAURANT_TEMPLATE)

        assertTrue(sections.contains("Navigation"))
        assertTrue(sections.contains("Three.js 3D Canvas"))
        assertTrue(sections.contains("Hero Banner"))
        assertTrue(sections.contains("Gallery / Showcase"))
        assertTrue(sections.contains("Footer"))
    }

    @Test
    fun testPortfolioTemplateSections() {
        val sections = SectionDetector.detectSections(Templates.PORTFOLIO_TEMPLATE)

        assertTrue(sections.contains("Navigation"))
        assertTrue(sections.contains("Three.js 3D Canvas"))
        assertTrue(sections.contains("Hero Banner"))
    }
}
