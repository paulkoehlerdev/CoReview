package de.speedboat.plugins.coreview.services

import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import de.speedboat.plugins.coreview.editor.SuggestionInlaysManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.nio.file.Path

@Service(Service.Level.PROJECT)
class CoReviewService(private val project: Project, private val coroutineScope: CoroutineScope) {

    private val diffService = project.service<DiffService>()
    private val openAIService = project.service<OpenAIService>()

    private var suggestionList: ArrayList<SuggestionInformation> = ArrayList()

    fun triggerCoReview() {
        val changeListManager = ChangeListManager.getInstance(project)
        triggerCoReview(changeListManager.allChanges.toList())
    }

    fun triggerCoReview(changelist: List<Change>) {
        thisLogger().warn("Triggering CoReview")
        openCoReviewTab()

        coroutineScope.launch {
            val diff = diffService.buildDiff(changelist).apply {
                diffService.groupDiffs(this)
            }
            clearSuggestions()
            diff.map {
                async {
                    openAIService.getSuggestions(it)
                }
            }.awaitAll().stream()
                    .flatMap { it.stream() }
                    .forEach { addSuggestion(it) }
        }
    }

    private fun openCoReviewTab() {
        val projectView = ToolWindowManager.getInstance(project)
        val toolWindow = projectView.getToolWindow("CoReview")
        toolWindow?.activate(null, false)
    }

    private fun clearSuggestions() {
        suggestionList = ArrayList()
        // closeAllSuggestions()
    }

    private fun addSuggestion(suggestion: OpenAIService.Suggestion) {
        val file = project.projectFile!!.fileSystem.findFileByPath(project.guessProjectDir()!!.toNioPath().resolve(suggestion.file).toString())
        val suggestionInformation = SuggestionInformation(suggestion, file)
        if (suggestionInformation.file != null) createSuggestionInlay(suggestionInformation)
        suggestionList.add(suggestionInformation)
    }

    fun getSuggestions(): List<SuggestionInformation> {
        return suggestionList.toList()
    }

    fun getSuggestionsFromFile(filePath: String): List<SuggestionInformation> {
        return suggestionList.filter { it.file?.path == filePath }
    }

    private fun createSuggestionInlay(suggestionInformation: SuggestionInformation) {
        invokeLater {
            thisLogger().warn("opening file ${suggestionInformation.file}")

            FileEditorManager.getInstance(project).openFile(suggestionInformation.file!!)
        }
    }

    fun textFromSuggestion(suggestion: OpenAIService.Suggestion): String {
        return "${suggestion.title}. \n${suggestion.suggestion}"
    }

    private fun closeAllSuggestions() {
        FileEditorManager.getInstance(project).allEditors.forEach {
            val manager = SuggestionInlaysManager.from(it)
            manager.dispose()
        }
    }

    class SuggestionInformation(val suggestion: OpenAIService.Suggestion, val file: VirtualFile?)
}
