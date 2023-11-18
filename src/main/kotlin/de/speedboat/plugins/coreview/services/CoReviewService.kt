package de.speedboat.plugins.coreview.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diff.impl.patch.BinaryFilePatch
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.patch.BinaryPatchWriter
import com.intellij.util.containers.ContainerUtil
import com.intellij.vcsUtil.VcsUtil
import git4idea.GitUtil
import git4idea.changes.GitChangeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.StringWriter
import java.nio.file.Path

@Service(Service.Level.PROJECT)
class CoReviewService(private val project: Project, private val coroutineScope: CoroutineScope) {
    fun triggerCoReview() {
        println("CoReview triggered!")

        coroutineScope.launch {
            val repositoryManager = GitUtil.getRepositoryManager(project)

            val writer = StringWriter()

            val repositories = repositoryManager.repositories
            for (gitRepository in repositories) {
                val revision = gitRepository.currentRevision
                val root = VcsUtil.getFilePath(gitRepository.root);
                val rootPath = Path.of(root.path)

                val changes = GitChangeUtils.getDiffWithWorkingDir(
                    project,
                    gitRepository.root,
                    revision!!,
                    listOf(root),
                    false
                )

                val patches =
                    IdeaTextPatchBuilder.buildPatch(project, changes, rootPath, false, false)
                UnifiedDiffWriter.write(project, rootPath, patches, writer, "\n", null, null)
                BinaryPatchWriter.writeBinaries(
                    rootPath, ContainerUtil.findAll(
                        patches,
                        BinaryFilePatch::class.java
                    ), writer
                )
            }

            println("patches:")
            println(writer)
        }
    }
}
