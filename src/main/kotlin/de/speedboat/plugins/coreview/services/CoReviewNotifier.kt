package de.speedboat.plugins.coreview.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import de.speedboat.plugins.coreview.Bundle
import de.speedboat.plugins.coreview.settings.AppSettingsConfigurable

object CoReviewNotifier {
    fun notifyError(project: Project?, content: String) {
        NotificationGroupManager
                .getInstance()
                .getNotificationGroup("CoReview Notification Group")
                .createNotification(Bundle.message("coreview.notification.errortitle"), content, NotificationType.ERROR)
                .addAction(object : AnAction("Configure") {
                    override fun actionPerformed(e: AnActionEvent) {
                        ShowSettingsUtil.getInstance().showSettingsDialog(null, AppSettingsConfigurable::class.java)
                    }
                })
                .notify(project)
    }
}