package de.speedboat.plugins.coreview.actions

import com.intellij.collaboration.ui.codereview.comment.CodeReviewCommentUIUtil
import com.intellij.icons.AllIcons
import de.speedboat.plugins.coreview.Bundle
import de.speedboat.plugins.coreview.services.CoReviewService
import javax.swing.JComponent

object SuggestionInlayComponentsFactory {
    fun createSuggestionInlayComponent(
        coReviewService: CoReviewService,
        suggestionInformation: CoReviewService.SuggestionInformation,
    ): JComponent {
        val commentComponent = ReviewCommentComponent.create(
            coReviewService,
            Bundle.message("coreview.chatbox.label"),
            AllIcons.General.User,
            suggestionInformation,
        )

        return CodeReviewCommentUIUtil.createEditorInlayPanel(commentComponent)
    }
}
