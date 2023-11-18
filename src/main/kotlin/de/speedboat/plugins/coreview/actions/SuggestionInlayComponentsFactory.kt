package de.speedboat.plugins.coreview.actions;

import com.intellij.collaboration.ui.codereview.comment.CodeReviewCommentUIUtil
import com.intellij.icons.AllIcons
import javax.swing.JComponent

object SuggestionInlayComponentsFactory {
    fun createSuggestionInlayComponent(body: String = "This is the comment body"): JComponent {
        val commentComponent = ReviewCommentComponent.create(
            "CoReview (GPT-powered)",
            AllIcons.General.User,
            body,
        )

        return CodeReviewCommentUIUtil.createEditorInlayPanel(commentComponent)
    }
}
