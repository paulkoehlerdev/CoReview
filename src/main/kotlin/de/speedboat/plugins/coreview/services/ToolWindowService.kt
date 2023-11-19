package de.speedboat.plugins.coreview.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.util.PsiUtilCore
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.tree.TreeUtil
import de.speedboat.plugins.coreview.Bundle
import de.speedboat.plugins.coreview.Icons
import de.speedboat.plugins.coreview.editor.SeverityIconRenderer
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

@Service(Service.Level.PROJECT)
class ToolWindowService(val project: Project) {

    private var toolWindow: ToolWindow? = null

    fun openToolWindow() {
        val toolWindowManager = ToolWindowManager.getInstance(project)

        toolWindowManager.invokeLater {
            if (toolWindow == null) {
                toolWindow = toolWindowManager.registerToolWindow(
                    Bundle.message("coreview.toolwindow.title")
                ) {
                    this.sideTool = true
                    this.anchor = ToolWindowAnchor.LEFT
                    this.icon = Icons.logo
                    this.stripeTitle = Bundle.messagePointer("coreview.toolwindow.title")
                }
            }

            val oldContent = toolWindow?.contentManager?.getContent(0)
            if (oldContent != null) {
                toolWindow?.contentManager?.removeContent(oldContent, true)
            }

            val content =
                ContentFactory
                    .getInstance()
                    .createContent(
                        CoReviewToolWindow(project).getContent(),
                        null,
                        false
                    )
            toolWindow?.contentManager?.addContent(content) ?: thisLogger().warn("No Content Added")
            toolWindow?.activate(null, false)
        }
    }


    class CoReviewToolWindow(private val project: Project) {
        fun getContent(): JComponent {
            val service = project.service<CoReviewService>()

            val model = FileTreeModel(DefaultMutableTreeNode(Bundle.message("coreview.toolwindow.rootnode")))

            service.getFiles().forEachIndexed { index, it ->
                val fileNode = DefaultMutableTreeNode(it)
                model.insertNodeInto(fileNode, model.root as DefaultMutableTreeNode, index)

                service.getSuggestionsFromFile(it).forEachIndexed { index, suggestion ->
                    val suggestionNode = DefaultMutableTreeNode(suggestion)
                    model.insertNodeInto(suggestionNode, fileNode, index)
                }
            }

            val tree = Tree(model).apply {
                val handlerMethod = {
                    val node = lastSelectedPathComponent as DefaultMutableTreeNode
                    val userObject = node.userObject

                    if (userObject is CoReviewService.SuggestionInformation) {
                        val ofd =
                            OpenFileDescriptor(
                                project,
                                userObject.file!!,
                                userObject.suggestion.lineNumber - 1,
                                0
                            )

                        FileEditorManager
                            .getInstance(project)
                            .openTextEditor(ofd, true)
                    }
                }

                addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent?) {
                        handlerMethod()
                    }
                })

                addKeyListener(object : KeyListener {
                    override fun keyTyped(e: KeyEvent?) {
                    }

                    override fun keyPressed(e: KeyEvent?) {
                    }

                    override fun keyReleased(e: KeyEvent?) {
                        if (e?.keyCode == KeyEvent.VK_ENTER) {
                            handlerMethod()
                        }
                    }
                })
            }
            tree.setCellRenderer(CustomTreeCellRenderer(project))
            tree.isRootVisible = false

            TreeUtil.expandAll(tree)

            return JBScrollPane(tree).apply {
                border = null
            }
        }
    }


    class FileTreeModel(root: DefaultMutableTreeNode?) : DefaultTreeModel(root)

    class CustomTreeCellRenderer(val project: Project) : ColoredTreeCellRenderer() {
        override fun customizeCellRenderer(
            tree: JTree,
            value: Any?,
            selected: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean
        ) {
            val node = value as DefaultMutableTreeNode

            if (node.userObject is VirtualFile) {
                customizeCellRendererForFile(node.userObject as VirtualFile)
                return
            }

            if (node.userObject is CoReviewService.SuggestionInformation) {
                customizeCellRendererForSuggestion(node.userObject as CoReviewService.SuggestionInformation)
                return
            }
        }

        private fun customizeCellRendererForFile(file: VirtualFile) {
            val psiFile = PsiUtilCore.getPsiFile(project, file)
            val icon = psiFile.presentation?.getIcon(false)

            super.append(psiFile.name)
            super.setIcon(icon)

            isOpaque = false
            isIconOpaque = false
            isTransparentIconBackground = true
        }

        private fun customizeCellRendererForSuggestion(suggestion: CoReviewService.SuggestionInformation) {
            super.append(suggestion.suggestion.title)
            SeverityIconRenderer.Severity.getSeverity(suggestion.suggestion.severity).icon.let { super.setIcon(it) }
            isOpaque = false
        }
    }
}