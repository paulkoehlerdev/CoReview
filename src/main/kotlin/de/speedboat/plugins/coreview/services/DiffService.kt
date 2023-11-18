package de.speedboat.plugins.coreview.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diff.impl.patch.BinaryFilePatch
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.patch.BinaryPatchWriter
import com.intellij.util.containers.ContainerUtil
import com.intellij.vcsUtil.VcsUtil
import java.io.StringWriter
import java.nio.file.Path


@Service(Service.Level.PROJECT)

class DiffService(val project: Project) {

    fun buildDiff(changes: List<Change>): List<Pair<Change, String>> {
        val out = mutableListOf<Pair<Change, String>>()
        changes.forEach {
            val stringWriter = StringWriter()
            addDiffsToWriter(stringWriter, it)
            out.add(Pair(it, stringWriter.toString()))
        }
        return out
    }

    private fun addDiffsToWriter(writer: StringWriter, change: Change) {
        val rootPath = Path.of(VcsUtil.getVcsRootFor(project, change.virtualFile)!!.path)

        val changeList = listOf(change)

        val patches =
            IdeaTextPatchBuilder.buildPatch(project, changeList, rootPath, false, false)
        UnifiedDiffWriter.write(project, rootPath, patches, writer, "\n", null, null)
        BinaryPatchWriter.writeBinaries(
            rootPath, ContainerUtil.findAll(
                patches,
                BinaryFilePatch::class.java
            ), writer
        )
    }

}