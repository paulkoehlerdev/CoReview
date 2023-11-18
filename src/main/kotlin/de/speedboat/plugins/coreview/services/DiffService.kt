package de.speedboat.plugins.coreview.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change


@Service(Service.Level.PROJECT)

class DiffService(project: Project) {

    fun buildDiff(changes: List<Change>): Array<String> {
        var out: Array<String> = Array(changes.size) { "" }
        changes.forEachIndexed { index, it ->
            out[index] = buildDiffFromChange(it)
            thisLogger().warn(out[index])
        }
        return out
    }

    private fun buildDiffFromChange(change: Change): String {
        val before = change.beforeRevision?.content ?: ""
        val after = change.afterRevision?.content ?: ""

        return ""
    }

}