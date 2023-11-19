package de.speedboat.plugins.coreview.services

import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import de.speedboat.plugins.coreview.listeners.EditorTrackerListenerImpl
import kotlinx.coroutines.CoroutineScope
import java.nio.file.Files
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future


@Service(Service.Level.PROJECT)
class CoReviewService(private val project: Project, private val coroutineScope: CoroutineScope) {

    private val diffService = project.service<DiffService>()
    private val openAIService = project.service<OpenAIService>()

    private var suggestionList: ArrayList<SuggestionInformation> = ArrayList()

    fun triggerCoReview() {
        val changeListManager = ChangeListManager.getInstance(project)
        triggerCoReview(changeListManager.allChanges.toList()).get()
    }

    fun triggerCoReview(changelist: List<Change>): Future<List<SuggestionInformation>> {
        thisLogger().warn("Triggering CoReview")

        val future = CompletableFuture<List<SuggestionInformation>>()
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Reviewing code...") {
            override fun run(progressIndicator: ProgressIndicator) {
                progressIndicator.isIndeterminate = true

                val diff = diffService.buildDiff(changelist).apply {
                    diffService.groupDiffs(this)
                }
                clearSuggestions()

                val suggestions = diff.parallelStream().flatMap { openAIService.getSuggestions(it).stream() }
                    .map { mapSuggestion(it) }
                    .filter { it.file != null && it.file.isValid }
                    .toList()

                suggestions.forEach { addSuggestion(it) }
                future.complete(suggestions)

                EditorTrackerListenerImpl.updateCurrentActiveEditor(project, this@CoReviewService)

                invokeLater {
                    openCoReviewTab()
                }
            }
        })

        return future
    }

    private fun openCoReviewTab() {
        val projectView = ToolWindowManager.getInstance(project)
        val toolWindow = projectView.getToolWindow("CoReview")
        toolWindow?.activate(null, false)
    }

    private fun clearSuggestions() {
        suggestionList = ArrayList()
        EditorTrackerListenerImpl.updateCurrentActiveEditor(project, this)
    }

    private fun mapSuggestion(suggestion: OpenAIService.Suggestion): SuggestionInformation {
        val file = project.projectFile!!.fileSystem.findFileByPath(
            project.guessProjectDir()!!.toNioPath().resolve(suggestion.file).toString()
        )

        if (file != null && !file.fileType.isBinary) {
            val lines = Files.readAllLines(file.toNioPath())

            val maxContext = 20
            val lineNumber = suggestion.lineNumber
            val lineContent = suggestion.lineContent.trim()
            var offset = 0
            for (i in 0..maxContext) {
                if (lines.getOrNull(lineNumber + i - 1)?.trim() == lineContent) {
                    offset = i
                    break
                }

                if (lines.getOrNull(lineNumber - i - 1)?.trim() == lineContent) {
                    offset = -i
                    break
                }
            }

            suggestion.lineNumber += offset
            suggestion.title = suggestion.comment.replace(Regex("line ([0-9]+)")) {
                "line ${it.groupValues[1].toInt() + offset}"
            }
            suggestion.comment = suggestion.comment.replace(Regex("line ([0-9]+)")) {
                "line ${it.groupValues[1].toInt() + offset}"
            }
            suggestion.suggestion = suggestion.suggestion.replace(Regex("line ([0-9]+)")) {
                "line ${it.groupValues[1].toInt() + offset}"
            }

            suggestion.lineNumber = maxOf(suggestion.lineNumber, 1)
            suggestion.lineNumber = minOf(suggestion.lineNumber, lines.size)
            thisLogger().warn("line ${suggestion.lineNumber} for '$lineContent' (offset: $offset)")
        }


        return SuggestionInformation(suggestion, file)
    }

    private fun addSuggestion(suggestion: SuggestionInformation) {
        if (suggestion.file != null) createSuggestionInlay(suggestion)
        suggestionList.add(suggestion)
    }

    fun getSuggestions(): List<SuggestionInformation> {
        return suggestionList.toList()
    }

    fun getSuggestionsFromFile(filePath: String): List<SuggestionInformation> {
        return suggestionList.filter { it.file?.path == filePath }
    }

    fun removeSuggestion(suggestion: SuggestionInformation) {
        val removed = suggestionList.remove(suggestion)
        if (!removed) {
            thisLogger().warn("could not remove ${suggestion} from ${suggestionList.toArray()}")
        }

        EditorTrackerListenerImpl.updateCurrentActiveEditor(project, this)
    }

    private fun createSuggestionInlay(suggestionInformation: SuggestionInformation) {
        invokeLater {
            thisLogger().warn("opening file ${suggestionInformation.file}")

            FileEditorManager.getInstance(project).openFile(suggestionInformation.file!!)
        }
    }

    fun textFromSuggestion(suggestion: OpenAIService.Suggestion): String {
        return "${suggestion.title}. \n${suggestion.comment}\n ${suggestion.suggestion}"
    }

    class SuggestionInformation(val suggestion: OpenAIService.Suggestion, val file: VirtualFile?)
}
