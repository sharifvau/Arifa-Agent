package com.arifa.assistant.core.websiteforge

import android.content.Context
import com.arifa.assistant.core.common.Result
import com.arifa.assistant.core.gemini.rest.GeminiRestClient
import com.arifa.assistant.core.storage.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File

class WebsiteForgeEngine private constructor(private val context: Context) {

    private val settingsManager = SettingsManager.getInstance(context)
    private val restClient = GeminiRestClient()

    private val projectsFile = File(context.filesDir, "arifa_forge_projects.json")

    private val _projects = MutableStateFlow<List<WebsiteProject>>(emptyList())
    val projects: StateFlow<List<WebsiteProject>> = _projects.asStateFlow()

    private val _currentProject = MutableStateFlow<WebsiteProject?>(null)
    val currentProject: StateFlow<WebsiteProject?> = _currentProject.asStateFlow()

    private val _currentStep = MutableStateFlow<BuildProgressStep?>(null)
    val currentStep: StateFlow<BuildProgressStep?> = _currentStep.asStateFlow()

    private val _isBuilding = MutableStateFlow(false)
    val isBuilding: StateFlow<Boolean> = _isBuilding.asStateFlow()

    suspend fun initialize() = withContext(Dispatchers.IO) {
        loadProjectsFromDisk()
    }

    private suspend fun loadProjectsFromDisk() = withContext(Dispatchers.IO) {
        try {
            if (!projectsFile.exists()) {
                // Pre-populate with sample projects
                val sampleRestro = WebsiteProject(
                    title = "Dawat Royale Restaurant",
                    prompt = "Restaurant website for traditional Bengali cuisine",
                    htmlCode = Templates.RESTAURANT_TEMPLATE,
                    detectedSections = SectionDetector.detectSections(Templates.RESTAURANT_TEMPLATE)
                )
                val samplePortfolio = WebsiteProject(
                    title = "CyberCore Portfolio",
                    prompt = "Futuristic AI engineer portfolio with 3D canvas",
                    htmlCode = Templates.PORTFOLIO_TEMPLATE,
                    detectedSections = SectionDetector.detectSections(Templates.PORTFOLIO_TEMPLATE)
                )
                _projects.value = listOf(sampleRestro, samplePortfolio)
                saveProjectsToDisk()
                return@withContext
            }
            val text = projectsFile.readText()
            if (text.isBlank()) return@withContext
            val array = JSONArray(text)
            val list = mutableListOf<WebsiteProject>()
            for (i in 0 until array.length()) {
                list.add(WebsiteProject.fromJson(array.getJSONObject(i)))
            }
            _projects.value = list.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            _projects.value = emptyList()
        }
    }

    private suspend fun saveProjectsToDisk() = withContext(Dispatchers.IO) {
        try {
            val array = JSONArray()
            _projects.value.forEach { array.put(it.toJson()) }
            projectsFile.writeText(array.toString(2))
        } catch (e: Exception) {
            // Ignore
        }
    }

    suspend fun generateWebsite(userPrompt: String): WebsiteProject = withContext(Dispatchers.IO) {
        _isBuilding.value = true

        // 1. Analyzing
        _currentStep.value = BuildProgressStep.ANALYZING
        delay(600)

        // 2. Generating 3D Scene
        _currentStep.value = BuildProgressStep.GENERATING_3D
        delay(800)

        // 3. Structuring
        _currentStep.value = BuildProgressStep.STRUCTURING_SECTIONS

        val apiKey = settingsManager.settings.value.apiKey
        val promptClean = userPrompt.trim()
        val generatedHtml: String

        if (apiKey.isNotBlank()) {
            val systemInstruction = """
You are Website Forge, an ultra-advanced AI web architect embedded in ARIFA AI.
Create a complete, single-file, production-ready HTML5 website with modern CSS3 and an interactive Three.js 3D hero scene.
REQUIREMENTS:
1. Include <script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script> in <head>.
2. Embed an interactive Three.js 3D canvas in the hero section.
3. Mobile-first responsive layout with dark glassmorphism styling.
4. Clean navigation, hero, highlights/features, menu/gallery/portfolio, about, and footer sections.
5. Return ONLY the raw HTML code inside ```html ... ``` codeblock.
"""
            val result = restClient.generateChatResponse(
                apiKey = apiKey,
                systemInstruction = systemInstruction,
                history = emptyList(),
                userMessage = "Build a complete 3D interactive website for: $promptClean"
            )

            generatedHtml = when (result) {
                is Result.Success -> extractHtml(result.data, promptClean)
                else -> fallbackTemplateForPrompt(promptClean)
            }
        } else {
            generatedHtml = fallbackTemplateForPrompt(promptClean)
        }

        // 4. Polishing
        _currentStep.value = BuildProgressStep.POLISHING_CSS
        delay(600)

        // 5. Finalizing
        _currentStep.value = BuildProgressStep.FINALIZING
        delay(400)

        val sections = SectionDetector.detectSections(generatedHtml)
        val title = extractTitleFromPrompt(promptClean)

        val project = WebsiteProject(
            title = title,
            prompt = promptClean,
            htmlCode = generatedHtml,
            detectedSections = sections
        )

        _projects.value = listOf(project) + _projects.value
        _currentProject.value = project
        saveProjectsToDisk()

        _isBuilding.value = false
        _currentStep.value = null
        project
    }

    private fun extractHtml(raw: String, prompt: String): String {
        val startTag = "```html"
        val endTag = "```"
        val startIdx = raw.indexOf(startTag)
        if (startIdx != -1) {
            val codeStart = startIdx + startTag.length
            val endIdx = raw.indexOf(endTag, codeStart)
            if (endIdx != -1) {
                return raw.substring(codeStart, endIdx).trim()
            }
        }
        return if (raw.contains("<!DOCTYPE html>") || raw.contains("<html")) {
            raw.trim()
        } else {
            fallbackTemplateForPrompt(prompt)
        }
    }

    private fun fallbackTemplateForPrompt(prompt: String): String {
        val lower = prompt.lowercase()
        return if (lower.contains("restaurant") || lower.contains("food") || lower.contains("khabar") || lower.contains("biryani") || lower.contains("cafe")) {
            Templates.RESTAURANT_TEMPLATE
        } else {
            Templates.PORTFOLIO_TEMPLATE
        }
    }

    private fun extractTitleFromPrompt(prompt: String): String {
        val clean = prompt.replace("Arifa", "", ignoreCase = true)
            .replace("amar jonno", "", ignoreCase = true)
            .replace("ekta", "", ignoreCase = true)
            .replace("website banao", "", ignoreCase = true)
            .replace("build a", "", ignoreCase = true)
            .replace("website", "", ignoreCase = true)
            .replace("for", "", ignoreCase = true)
            .trim()
        return if (clean.isNotBlank()) clean.replaceFirstChar { it.uppercase() } + " Website" else "Custom 3D Website"
    }

    fun selectProject(project: WebsiteProject) {
        _currentProject.value = project
    }

    suspend fun updateProjectCode(projectId: String, newHtml: String) = withContext(Dispatchers.IO) {
        val updated = _projects.value.map {
            if (it.id == projectId) {
                it.copy(
                    htmlCode = newHtml,
                    detectedSections = SectionDetector.detectSections(newHtml)
                )
            } else it
        }
        _projects.value = updated
        _currentProject.value = updated.find { it.id == projectId }
        saveProjectsToDisk()
    }

    suspend fun deleteProject(projectId: String) = withContext(Dispatchers.IO) {
        _projects.value = _projects.value.filter { it.id != projectId }
        if (_currentProject.value?.id == projectId) {
            _currentProject.value = _projects.value.firstOrNull()
        }
        saveProjectsToDisk()
    }

    companion object {
        @Volatile
        private var instance: WebsiteForgeEngine? = null

        fun getInstance(context: Context): WebsiteForgeEngine {
            return instance ?: synchronized(this) {
                instance ?: WebsiteForgeEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}
