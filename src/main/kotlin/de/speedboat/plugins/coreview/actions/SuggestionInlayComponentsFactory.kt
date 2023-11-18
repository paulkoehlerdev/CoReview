package de.speedboat.plugins.coreview.actions;

import com.intellij.collaboration.ui.codereview.comment.CodeReviewCommentUIUtil
import javax.swing.JComponent
import javax.swing.JTextArea

object SuggestionInlayComponentsFactory {
    fun createSuggestionInlayComponent(): JComponent {
        return CodeReviewCommentUIUtil.createEditorInlayPanel(JTextArea("Hi this is something!"))
    }
}
