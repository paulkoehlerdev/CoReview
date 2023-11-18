package de.speedboat.plugins.coreview.services;

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import de.speedboat.plugins.coreview.settings.AppSettingsState
import dev.langchain4j.internal.Json
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO
import dev.langchain4j.model.openai.OpenAiTokenizer
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.UserMessage

@Service(Service.Level.PROJECT)
class OpenAIService(project: Project) {

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
You are a software developer responsible for reviewing Pull Requests (PRs) submitted by various contributors. Your task is to analyze PRs.

Generate potential review comments with additional metadata, including lines and files referenced. 

The user will call you with the Git diff as an input.

You must answer strictly in the following JSON format:
[{
  "file": (The file path of the code file),
  "lineStart": (The line number of the start of the referenced code snippet),
  "lineEnd": (The line number of the end of the referenced code snippet),
  "severity": (The severity of the issue, ranging from 0 (not severe) to 1 (very severe)),
  "title": (A concise title for the issue),
  "comment": (In-depth feedback),
  "suggestion": (Code rewrite suggestions when applicable)
}, ...]
        """
        )
        fun getSuggestions(@UserMessage diff: String): String
    }

    private var chatLanguageModel: ChatLanguageModel
    private var codeReviewer: CodeReviewer;

    init {
        val settings: AppSettingsState = AppSettingsState.getInstance()
        chatLanguageModel = OpenAiChatModel.builder()
            .apiKey(settings.openAPIKey)
            .modelName(GPT_3_5_TURBO)
            .tokenizer(OpenAiTokenizer(GPT_3_5_TURBO))
            .logRequests(true)
            .logResponses(true)
            .build()
        codeReviewer = AiServices.create(CodeReviewer::class.java, chatLanguageModel)
    }

    fun getSuggestions(diff: String): List<Suggestion> {
        val suggestions = codeReviewer.getSuggestions(diff)
        return Json.fromJson(suggestions, Array<Suggestion>::class.java)
            .toList()
    }
}
