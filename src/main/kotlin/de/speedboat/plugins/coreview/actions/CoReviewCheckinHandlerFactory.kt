package de.speedboat.plugins.coreview.actions

import com.intellij.codeInsight.CodeSmellInfo
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.changes.ui.BooleanCommitOption
import com.intellij.openapi.vcs.checkin.*
import com.intellij.openapi.vcs.ui.RefreshableOnComponent
import de.speedboat.plugins.coreview.Bundle
import de.speedboat.plugins.coreview.services.CoReviewService
import de.speedboat.plugins.coreview.settings.CoReviewCheckinHandlerSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class CoReviewCheckinHandlerFactory : CheckinHandlerFactory() {
    override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
        return CoReviewCheckinHandler(panel.project)
    }
}

private class CoReviewCheckinHandler(val project: Project) : CheckinHandler(), CommitCheck {

    private val settingsService = project.service<CoReviewCheckinHandlerSettings>()
    private val coReviewService = project.service<CoReviewService>()

    override fun beforeCheckin(): ReturnResult {
        return super.beforeCheckin()
    }

    override fun getBeforeCheckinConfigurationPanel(): RefreshableOnComponent {
        return BooleanCommitOption.create(
                project,
                this,
                false,
                Bundle.message("before.checkin.update.coreview.label"),
                settingsService.state::isCoReviewCheckEnabled
        )
    }

    override fun getExecutionOrder(): CommitCheck.ExecutionOrder = CommitCheck.ExecutionOrder.LATE

    override fun isEnabled(): Boolean {
        return settingsService.state.isCoReviewCheckEnabled
    }

    override suspend fun runCheck(commitInfo: CommitInfo): CommitProblem? {
        val suggestions = withContext(Dispatchers.IO) {
            coReviewService.triggerCoReview(commitInfo.committedChanges).get().map {
                runReadAction {
                    val doc = FileDocumentManager.getInstance().getDocument(it.file!!)
                    val textRange = TextRange(it.suggestion.lineStart, it.suggestion.lineEnd)

                    CodeSmellInfo(doc!!, it.suggestion.title, textRange, HighlightSeverity.WARNING)
                }
            }
        }

        if (suggestions.isEmpty()) {
            return null
        }

        return CodeAnalysisCommitProblem(
               suggestions,
                0,
                suggestions.size
        )
    }
}