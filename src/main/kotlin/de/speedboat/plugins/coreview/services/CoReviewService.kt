package de.speedboat.plugins.coreview.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
class CoReviewService(private val project: Project, private val coroutineScope: CoroutineScope) {

    private val diffService = project.service<DiffService>()
    private val openAIService = project.service<OpenAIService>()

    fun triggerCoReview() {
        val changeListManager = ChangeListManager.getInstance(project)
        triggerCoReview(changeListManager.allChanges.toList())
    }

    fun triggerCoReview(changelist: List<Change>) {
        thisLogger().warn("Triggering CoReview")

        coroutineScope.launch {
            val diff = diffService.buildDiff(changelist)
            val suggestions = openAIService.getSuggestions(diff);
            thisLogger().warn("Suggestions: $suggestions")
        }
    }
}
