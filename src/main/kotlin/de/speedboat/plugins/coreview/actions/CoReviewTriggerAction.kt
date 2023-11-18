package de.speedboat.plugins.coreview.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import de.speedboat.plugins.coreview.services.CoReviewService


class CoReviewTriggerAction() : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val coReviewService = e.project!!.service<CoReviewService>()
        coReviewService.triggerCoReview();
    }
}
