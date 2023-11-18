package de.speedboat.plugins.coreview.listeners

import com.intellij.codeInsight.daemon.impl.EditorTrackerListener
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import de.speedboat.plugins.coreview.actions.SuggestionInlayComponentsFactory
import de.speedboat.plugins.coreview.editor.SuggestionInlaysManager

class EditorTrackerListenerImpl : EditorTrackerListener {
    override fun activeEditorsChanged(activeEditors: List<Editor>) {
        activeEditors.forEach { editor ->
            val manager = SuggestionInlaysManager.from(editor)
            val file = editor.virtualFile
            thisLogger().warn("editor changed for file ${file.name} (managed inlays: ${manager.managedInlays()}); updating suggestions")
            manager.dispose()

            manager.insertAfter(
                0, SuggestionInlayComponentsFactory.createSuggestionInlayComponent(
                    body = "suggestion in file ${file.name}"
                )
            )
        }
    }
}