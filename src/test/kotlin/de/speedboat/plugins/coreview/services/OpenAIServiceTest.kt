package de.speedboat.plugins.coreview.services

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import de.speedboat.plugins.coreview.settings.AppSecrets
import de.speedboat.plugins.coreview.settings.AppSettingsSecrets
import junit.framework.TestCase

class OpenAIServiceTest : BasePlatformTestCase() {

    private val diff = """
        --- a/src/main/kotlin/de/speedboat/plugins/coreview/services/myprojectservice.kt
        +++ b/src/main/kotlin/de/speedboat/plugins/coreview/services/myprojectservice.kt
        @@ -6,7 +6,7 @@ import com.intellij.openapi.project.Project
         import de.speedboat.plugins.coreview.MyBundle
         
         @Service(Service.Level.PROJECT)
        -class MyProjectService(project: Project) {
        +class myprojectservice(project: Project) {
         
             init {
                 thisLogger().info(MyBundle.message("projectService", project.name))
    """.trimIndent()

    fun testPrompt() {
        AppSettingsSecrets.setSecret(AppSecrets.OPEN_AI_API_KEY, System.getenv("OPENAI_API_KEY"))
        val openAIService = project.service<OpenAIService>()

        val suggestions = openAIService.getSuggestions(diff)
        assertNotEmpty(suggestions)

        val suggestion = suggestions[0]
        TestCase.assertNotNull(suggestion.file)
        TestCase.assertNotNull(suggestion.suggestion)
        TestCase.assertNotNull(suggestion.comment)
        TestCase.assertNotNull(suggestion.title)
        TestCase.assertNotSame(suggestion.lineNumber, 0)
    }

}