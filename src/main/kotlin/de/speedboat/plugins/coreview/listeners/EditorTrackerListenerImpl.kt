package de.speedboat.plugins.coreview.listeners

import com.intellij.codeInsight.daemon.impl.EditorTrackerListener
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import de.speedboat.plugins.coreview.actions.SuggestionInlayComponentsFactory
import de.speedboat.plugins.coreview.editor.SuggestionInlaysManager
import de.speedboat.plugins.coreview.services.CoReviewService

class EditorTrackerListenerImpl(val project: Project) : EditorTrackerListener {
    override fun activeEditorsChanged(activeEditors: List<Editor>) {
        val coReviewService = project.service<CoReviewService>()

        activeEditors.forEach { editor ->
            updateEditor(coReviewService, editor)
        }
    }

    companion object {
        fun updateEditor(coReviewService: CoReviewService, editor: Editor) {
            val manager = SuggestionInlaysManager.from(editor as EditorImpl)
            val file = editor.virtualFile
            if (file == null) {
                manager.clear()
                return
            }
            manager.clear()

            coReviewService.getSuggestionsFromFile(file.path).forEach {
                manager.insertAfter(
                    it.suggestion.lineNumber - 1, SuggestionInlayComponentsFactory.createSuggestionInlayComponent(
                        coReviewService,
                        it,
                    ), it
                )
            }
        }

        fun updateCurrentActiveEditor(project: Project, coReviewService: CoReviewService) {
            invokeLater {
                val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return@invokeLater
                updateEditor(coReviewService, editor)
            }
        }
    }
}