package de.speedboat.plugins.coreview.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import javax.swing.JProgressBar

class ReviewToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val coReviewToolWindow = CoReviewToolWindow(project)
        val content = ContentFactory.getInstance().createContent(coReviewToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class CoReviewToolWindow(private val project: Project) {
        fun getContent() = JBPanel<JBPanel<*>>().apply {
            add(JProgressBar().apply {
                isIndeterminate = true
            })
        }
    }
}
