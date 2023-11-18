package de.speedboat.plugins.coreview.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object CoReviewNotifier {
    fun notifyError(project: Project?, content: String) {
        NotificationGroupManager
            .getInstance()
            .getNotificationGroup("CoReview Notification Group")
            .createNotification(content, NotificationType.ERROR)
            .notify(project)
    }
}