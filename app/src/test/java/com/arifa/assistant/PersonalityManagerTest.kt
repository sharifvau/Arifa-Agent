package com.arifa.assistant

import com.arifa.assistant.core.personality.PersonalityType
import com.arifa.assistant.core.personality.SystemInstructions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalityManagerTest {

    @Test
    fun testAllTenPersonalitiesExist() {
        val all = PersonalityType.values()
        assertEquals(10, all.size)

        val expectedIds = listOf(
            "NORMAL", "FRIENDLY", "PROFESSIONAL", "FUNNY",
            "COMPANION", "GF_MODE", "TEACHER", "DEVELOPER",
            "HUNGRY", "NAUTANKI"
        )
        for (id in expectedIds) {
            val p = PersonalityType.valueOf(id)
            assertEquals(id, p.id)
            assertTrue(p.title.isNotBlank())
            assertTrue(p.titleBn.isNotBlank())
        }
    }

    @Test
    fun testCompanionAndGfModeSafeguards() {
        val companionInstruction = SystemInstructions.buildInstruction("Arifa", PersonalityType.COMPANION)
        assertTrue(companionInstruction.contains("STRICT SAFEGUARD"))
        assertTrue(companionInstruction.contains("Never claim to be human or have a physical body"))

        val gfInstruction = SystemInstructions.buildInstruction("Arifa", PersonalityType.GF_MODE)
        assertTrue(gfInstruction.contains("STRICT SAFEGUARD"))
        assertTrue(gfInstruction.contains("Never claim to be human or have a physical body"))
    }

    @Test
    fun testHungryAndNautankiInstructionThemes() {
        val hungryInstruction = SystemInstructions.buildInstruction("Arifa", PersonalityType.HUNGRY)
        assertTrue(hungryInstruction.contains("Kacchi Biryani"))

        val nautankiInstruction = SystemInstructions.buildInstruction("Arifa", PersonalityType.NAUTANKI)
        assertTrue(nautankiInstruction.contains("Hai Allah!"))
    }
}
