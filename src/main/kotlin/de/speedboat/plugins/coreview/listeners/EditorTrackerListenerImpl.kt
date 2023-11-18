package de.speedboat.plugins.coreview.listeners

import com.intellij.codeInsight.daemon.impl.EditorTrackerListener
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import de.speedboat.plugins.coreview.actions.SuggestionInlayComponentsFactory
import de.speedboat.plugins.coreview.editor.SuggestionInlaysManager
import de.speedboat.plugins.coreview.services.CoReviewService

class EditorTrackerListenerImpl(val project: Project) : EditorTrackerListener {
    override fun activeEditorsChanged(activeEditors: List<Editor>) {
        val coReviewService = project.service<CoReviewService>()

        activeEditors.forEach { editor ->
            val manager = SuggestionInlaysManager.from(editor)
            val file = editor.virtualFile
            if (file == null) {
                thisLogger().warn("editor changed for non-file editor; disposing suggestions")
                manager.dispose()
                return@forEach
            }
            thisLogger().warn("editor changed for file ${file.name} (managed inlays: ${manager.managedInlays()}); updating suggestions")
            manager.dispose()

            coReviewService.getSuggestionsFromFile(file.path).forEach {
                manager.insertAfter(
                    it.suggestion.lineEnd, SuggestionInlayComponentsFactory.createSuggestionInlayComponent(
                        body = coReviewService.textFromSuggestion(it.suggestion),
                    )
                )
            }
        }
    }
}