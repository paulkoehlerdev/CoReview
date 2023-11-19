package de.speedboat.plugins.coreview.services;

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import de.speedboat.plugins.coreview.Bundle
import de.speedboat.plugins.coreview.settings.AppSecrets
import de.speedboat.plugins.coreview.settings.AppSettingsSecrets
import de.speedboat.plugins.coreview.settings.AppSettingsState
import dev.langchain4j.internal.Json
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.UserMessage
import dev.langchain4j.service.V
import java.time.Duration

@Service(Service.Level.APP)
class OpenAIService {

    class Suggestion(
        val file: String,
        var lineNumber: Int,
        val lineContent: String,
        val severity: Float,
        var title: String,
        var comment: String,
        var suggestion: String,
    )

    enum class DeveloperExperience(val value: String) {
        BEGINNER("Beginner"),
        INTERMEDIATE("Intermediate"),
        ADVANCED("Advanced")
    }

    interface CodeReviewer {
        @SystemMessage(
            """
You are a senior software developer responsible for reviewing Pull Requests from a {{experienceLevel}}.
 Generate potential review comments with additional metadata, including lines and files referenced.

The user will provide you with the Git diff as input. Follow the Git diff conventions properly with the given notation:
"@@ -26,7 +28,9 @@" is in the format "@@ from-file-range to-file-range @@" while to-file-range is in the following format: "+<start line>,<number of modified lines>".
Ignore from-file-range. Only to-file-range is relevant.

Only refer to modified or added lines, not deleted lines. Always use the line number of the changed file, not the original file.
The comments and suggestions should be well-structured, easy to understand, and provide actionable feedback to the developer.

Respond strictly and only in the following JSON format:
[{
  "file": (File path of the code file),
  "lineNumber": (Line number in the original file *after applying the Git diff*),
  "lineContent": (The content of the referred line *after applying the Git diff*),
  "severity": (Severity of the issue, ranging from 0 (not severe) to 1 (very severe)),
  "title": (Concise title for the issue),
  "comment": (In-depth feedback),
  "suggestion": (Code rewrite suggestions, if applicable)
}, ...]
        """
        )
        fun getSuggestions(@V("experienceLevel") experienceLevel: String, @UserMessage diff: String): String
    }

    private var codeReviewer: CodeReviewer? = null

    init {
        initializeOpenAI()
    }

    fun initializeOpenAI() {
        val openAiApiKey = AppSettingsSecrets.getSecret(AppSecrets.OPEN_AI_API_KEY)

        try {
            val chatLanguageModel = OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .timeout(Duration.ofSeconds(30))
                .modelName("gpt-3.5-turbo-1106")
                .build()

            codeReviewer = AiServices.create(CodeReviewer::class.java, chatLanguageModel)
        } catch (e: Exception) {
            thisLogger().error(e)
            CoReviewNotifier.notifyError(null, Bundle.message("coreview.openaiservice.initialapikeyerror"))
        }
    }

    fun getSuggestions(diff: String): List<Suggestion> {
        val experienceLevel = AppSettingsState.getInstance().experienceLevel

        if (codeReviewer == null) {
            CoReviewNotifier.notifyError(null, Bundle.message("coreview.openaiservice.apikeyerror"))
            return emptyList()
        }

        return try {
            thisLogger().warn("Calling OpenAI for $experienceLevel with $diff")
            val suggestions = codeReviewer!!.getSuggestions(experienceLevel.value, diff)
            thisLogger().warn("OpenAI response received: $suggestions")

            return try {
                val extractedJsonString =
                    suggestions.substring(suggestions.indexOf('['), suggestions.lastIndexOf(']') + 1)
                Json.fromJson(extractedJsonString, Array<Suggestion>::class.java).toList()
            } catch (e: Exception) {
                thisLogger().warn(e)
                CoReviewNotifier.notifyError(null, Bundle.message("coreview.openaiservice.jsonerror"))
                emptyList();
            }
        } catch (e: Exception) {
            thisLogger().error(e)
            CoReviewNotifier.notifyError(null, Bundle.message("coreview.openaiservice.apikeyerror"))
            emptyList();
        }
    }
}