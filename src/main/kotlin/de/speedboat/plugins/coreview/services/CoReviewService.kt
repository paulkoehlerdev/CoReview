package de.speedboat.plugins.coreview.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeListManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
class CoReviewService(private val project: Project, private val coroutineScope: CoroutineScope) {
    fun triggerCoReview() {
        println("CoReview triggered!")

        coroutineScope.launch {
            val diffService = project.service<DiffService>()
            val openAIService = project.service<OpenAIService>()

            val changeListManager = ChangeListManager.getInstance(project)
            val diff = diffService.buildDiff(changeListManager.allChanges.toList())
            val suggestions = openAIService.getSuggestions(diff);

            println("Suggestions: $suggestions")
        }
    }
}
