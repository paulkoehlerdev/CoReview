package de.speedboat.plugins.coreview.toolWindow

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import de.speedboat.plugins.coreview.actions.SuggestionInlayComponentsFactory
import de.speedboat.plugins.coreview.editor.SuggestionInlaysManager
import javax.swing.JButton


class ReviewToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(project, toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(private val project: Project, toolWindow: ToolWindow) {
        fun getContent() = JBPanel<JBPanel<*>>().apply {
            add(JButton("Show Inlay").apply {
                addActionListener {
                    val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return@addActionListener
                    val manager = SuggestionInlaysManager.from(editor)

                    manager.insertAfter(0, SuggestionInlayComponentsFactory.createSuggestionInlayComponent())
                }
            })
        }
    }
}
