package de.speedboat.plugins.coreview.actions

import com.intellij.collaboration.messages.CollaborationToolsBundle
import com.intellij.collaboration.ui.CollaborationToolsUIUtil
import com.intellij.collaboration.ui.HorizontalListPanel
import com.intellij.collaboration.ui.SimpleHtmlPane
import com.intellij.collaboration.ui.codereview.CodeReviewChatItemUIUtil
import com.intellij.collaboration.ui.codereview.comment.CodeReviewCommentUIUtil
import com.intellij.openapi.util.text.HtmlBuilder
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.ui.components.JBLabel
import com.intellij.util.text.JBDateFormat
import com.intellij.util.ui.UIUtil
import de.speedboat.plugins.coreview.services.CoReviewService
import java.util.*
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JEditorPane

// copied mainly from JetBrains
object ReviewCommentComponent {

    fun create(
        coReviewService: CoReviewService,
        commentAuthorName: String,
        commentAuthorAvatar: Icon,
        suggestionInformation: CoReviewService.SuggestionInformation,
        type: CodeReviewChatItemUIUtil.ComponentType = CodeReviewChatItemUIUtil.ComponentType.COMPACT
    ): JComponent {

        val titlePane = createTitleTextPane(commentAuthorName).apply {
            putClientProperty(UIUtil.HIDE_EDITOR_FROM_DATA_CONTEXT_PROPERTY, true)
        }

        val bodyPane =
            JBLabel("<html>${coReviewService.textFromSuggestion(suggestionInformation.suggestion)}</html>").apply {
                setAllowAutoWrapping(true)
            }

        val pendingLabel =
            CollaborationToolsUIUtil.createTagLabel(CollaborationToolsBundle.message("review.thread.pending.tag"))
                .apply {
                    isVisible = false
                }
        val resolvedLabel =
            CollaborationToolsUIUtil.createTagLabel(CollaborationToolsBundle.message("review.thread.resolved.tag"))
                .apply {
                    isVisible = false
                }

        val deleteButton = CodeReviewCommentUIUtil.createDeleteCommentIconButton {
            coReviewService.removeSuggestion(suggestionInformation)
        }.apply {
            isVisible = true
        }

        val actionsPanel = HorizontalListPanel(CodeReviewCommentUIUtil.Actions.HORIZONTAL_GAP).apply {
            isVisible = deleteButton.isVisible

            add(deleteButton)
        }

        val title = HorizontalListPanel(CodeReviewCommentUIUtil.Title.HORIZONTAL_GAP).apply {
            add(titlePane)
            add(pendingLabel)
            add(resolvedLabel)
        }

        return CodeReviewChatItemUIUtil.build(
            type,
            { commentAuthorAvatar },
            bodyPane
        ) {
            iconTooltip = commentAuthorName
            withHeader(title, actionsPanel)
            this.maxContentWidth = CodeReviewChatItemUIUtil.TEXT_CONTENT_WIDTH
        }
    }

    private fun createTitleTextPane(actorName: String, date: Date? = null): JEditorPane {
        val titleText = HtmlBuilder()
            .append(actorName)
            .append(HtmlChunk.nbsp())
            .apply {
                if (date != null) {
                    append(JBDateFormat.getFormatter().formatPrettyDateTime(date))
                }
            }.toString()
        val titleTextPane = SimpleHtmlPane(titleText).apply {
            foreground = UIUtil.getContextHelpForeground()
        }
        return titleTextPane
    }


}
