package de.speedboat.plugins.coreview.actions

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.changes.ui.BooleanCommitOption
import com.intellij.openapi.vcs.checkin.*
import com.intellij.openapi.vcs.ui.RefreshableOnComponent
import de.speedboat.plugins.coreview.Bundle
import de.speedboat.plugins.coreview.services.CoReviewCheckinHandlerSettings

class CoReviewCheckinHandlerFactory : CheckinHandlerFactory() {
    override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
        return CoReviewCheckinHandler(panel.project)
    }
}

private class CoReviewCheckinHandler(val project: Project) : CheckinHandler(), CommitCheck {

    private val settingsService = project.service<CoReviewCheckinHandlerSettings>()
    override fun beforeCheckin(): ReturnResult {
        return super.beforeCheckin()
    }

    override fun getBeforeCheckinConfigurationPanel(): RefreshableOnComponent? {
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
        thisLogger().warn(commitInfo.commitMessage)
        return null
    }
}