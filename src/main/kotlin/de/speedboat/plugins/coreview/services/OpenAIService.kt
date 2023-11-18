package de.speedboat.plugins.coreview.services;

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import de.speedboat.plugins.coreview.Bundle
import de.speedboat.plugins.coreview.settings.AppSecrets
import de.speedboat.plugins.coreview.settings.AppSettingsSecrets
import dev.langchain4j.internal.Json
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.UserMessage

@Service(Service.Level.APP)
class OpenAIService {

    class Suggestion(
        val file: String,
        val lineStart: Int,
        val lineEnd: Int,
        val severity: Float,
        val title: String,
        val comment: String,
        val suggestion: String,
    )

    interface CodeReviewer {
        @SystemMessage(
            """
You are a senior software developer responsible for reviewing Pull Requests.
Generate potential review comments with additional metadata, including lines and files referenced. 

The user will call you with the Git diff as an input.

You must answer strictly and only in following JSON:
[{
  "file": (File path of the code file),
  "lineStart": (Line number of the start of the code snippet),
  "lineEnd": (Line number of the end of the code snippet),
  "severity": (Severity of the issue, ranging from 0 (not severe) to 1 (very severe)),
  "title": (Concise title for the issue),
  "comment": (In-depth feedback),
  "suggestion": (Code rewrite suggestions, if applicable)
}, ...]
        """
        )
        fun getSuggestions(@UserMessage diff: String): String
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
                .modelName("gpt-3.5-turbo-1106")
                .build()

            codeReviewer = AiServices.create(CodeReviewer::class.java, chatLanguageModel)
        } catch (e: Exception) {
            thisLogger().error(e)
            CoReviewNotifier.notifyError(null, Bundle.message("coreview.openaiservice.initialapikeyerror"))
        }
    }

    fun getSuggestions(diff: String): List<Suggestion> {

        val openAiApiKey = AppSettingsSecrets.getSecret(AppSecrets.OPEN_AI_API_KEY)
        thisLogger().warn("OpenAI API Key: $openAiApiKey")

        if (codeReviewer == null) {
            CoReviewNotifier.notifyError(null, Bundle.message("coreview.openaiservice.apikeyerror"))
            return emptyList()
        }

        return try {
            thisLogger().warn("Calling OpenAI with $diff")
            val suggestions = codeReviewer!!.getSuggestions(diff)
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