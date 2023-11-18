package de.speedboat.plugins.coreview.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction


class CoReviewTriggerAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        println("CoReview triggered!")
    }
}
