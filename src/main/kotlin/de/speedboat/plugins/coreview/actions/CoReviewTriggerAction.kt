package de.speedboat.plugins.coreview.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vcs.changes.ChangeListManager
import de.speedboat.plugins.coreview.services.DiffService


class CoReviewTriggerAction() : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val diffService = e.project!!.service<DiffService>()

        var changeListManager = ChangeListManager.getInstance(e.project!!)
        var diff = diffService.buildDiff(changeListManager.allChanges.toList())
    }
}
