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
import java.util.stream.Collectors


@Service(Service.Level.PROJECT)

class DiffService(val project: Project) {

    fun buildDiff(changes: List<Change>): List<String> {
        val out = mutableListOf<String>()
        changes.forEach {
            val diff = getDiffsForChange(it)
            out.add(diff)
        }
        return out
    }

    fun groupDiffs(diffs: List<String>): String {
        return diffs.stream()
                .collect(Collectors.joining("\n"));
    }

    private fun getDiffsForChange(change: Change): String {
        val rootPath = Path.of(VcsUtil.getVcsRootFor(project, change.virtualFile)!!.path)

        val changeList = listOf(change)

        val writer = StringWriter()
        val patches =
            IdeaTextPatchBuilder.buildPatch(project, changeList, rootPath, false, false)
        UnifiedDiffWriter.write(project, rootPath, patches, writer, "\n", null, null)
        BinaryPatchWriter.writeBinaries(
            rootPath, ContainerUtil.findAll(
                patches,
                BinaryFilePatch::class.java
            ), writer
        )

        // remove IDEA header
        var split = writer.toString().split("\n").toList()
        split.indexOf("===================================================================").let {
            split = split.subList(it + 1, split.size)
        }

        return split.joinToString("\n")
    }

}