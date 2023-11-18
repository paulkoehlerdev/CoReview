package de.speedboat.plugins.coreview.editor

import com.intellij.collaboration.ui.codereview.CodeReviewChatItemUIUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.editor.impl.EditorEmbeddedComponentManager
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.ui.JBUI
import de.speedboat.plugins.coreview.services.CoReviewService
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.util.concurrent.ConcurrentHashMap
import javax.swing.JComponent
import javax.swing.ScrollPaneConstants
import kotlin.math.max
import kotlin.math.min

/**
 * Copied from com.intellij.util.ui.codereview.diff.EditorComponentInlaysManager
 * @author Colin Fleming
 */
class SuggestionInlaysManager(val editor: EditorImpl) : Disposable {

    private val managedInlays = ConcurrentHashMap<ComponentWrapper, Disposable>()
    private val editorWidthWatcher = EditorTextWidthWatcher()

    init {
        editor.scrollPane.viewport.addComponentListener(editorWidthWatcher)
        Disposer.register(this, Disposable {
            editor.scrollPane.viewport.removeComponentListener(editorWidthWatcher)
        })

        EditorUtil.disposeWithEditor(editor, this)
    }

    fun managedInlays(): Int {
        return managedInlays.size
    }

    @RequiresEdt
    fun insertAfter(lineIndex: Int, component: JComponent, suggestion: CoReviewService.SuggestionInformation): Disposable? {
        if (Disposer.isDisposed(this)) return null

        val wrappedComponent = ComponentWrapper(component)
        val gutterRenderer = SeverityIconRenderer(suggestion)
        val offset = editor.document.getLineEndOffset(lineIndex)

        return EditorEmbeddedComponentManager.getInstance()
            .addComponent(
                editor, wrappedComponent,
                EditorEmbeddedComponentManager.Properties(
                    EditorEmbeddedComponentManager.ResizePolicy.none(),
                    { gutterRenderer },
                    true,
                    false,
                    0,
                    offset
                )
            )?.also {
                managedInlays[wrappedComponent] = it
                Disposer.register(it, Disposable { managedInlays.remove(wrappedComponent) })
            }
    }

    private inner class ComponentWrapper(private val component: JComponent) : JBScrollPane(component) {
        init {
            isOpaque = false
            viewport.isOpaque = false

            border = JBUI.Borders.empty()
            viewportBorder = JBUI.Borders.empty()

            horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
            verticalScrollBar.preferredSize = Dimension(0, 0)
            setViewportView(component)

            component.addComponentListener(object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent) =
                    dispatchEvent(ComponentEvent(component, ComponentEvent.COMPONENT_RESIZED))
            })
        }

        override fun getPreferredSize(): Dimension {
            return Dimension(editorWidthWatcher.editorTextWidth, component.preferredSize.height)
        }
    }

    fun clear() {
        managedInlays.values.forEach(Disposer::dispose)
        managedInlays.clear()
    }

    override fun dispose() {
        managedInlays.values.forEach(Disposer::dispose)
    }

    private inner class EditorTextWidthWatcher : ComponentAdapter() {

        var editorTextWidth: Int = 0

        private val verticalScrollbarFlipped: Boolean

        init {
            val scrollbarFlip = editor.scrollPane.getClientProperty(JBScrollPane.Flip::class.java)
            verticalScrollbarFlipped =
                scrollbarFlip == JBScrollPane.Flip.HORIZONTAL || scrollbarFlip == JBScrollPane.Flip.BOTH
        }

        override fun componentResized(e: ComponentEvent) = updateWidthForAllInlays()
        override fun componentHidden(e: ComponentEvent) = updateWidthForAllInlays()
        override fun componentShown(e: ComponentEvent) = updateWidthForAllInlays()

        private fun updateWidthForAllInlays() {
            val newWidth = calcWidth()
            if (editorTextWidth == newWidth) return
            editorTextWidth = newWidth

            managedInlays.keys.forEach {
                it.dispatchEvent(ComponentEvent(it, ComponentEvent.COMPONENT_RESIZED))
                it.invalidate()
            }
        }

        private fun calcWidth(): Int {
            val visibleEditorTextWidth =
                editor.scrollPane.viewport.width - getVerticalScrollbarWidth() - getGutterTextGap()
            return min(max(visibleEditorTextWidth, 0), CodeReviewChatItemUIUtil.TEXT_CONTENT_WIDTH + 52)
        }

        private fun getVerticalScrollbarWidth(): Int {
            val width = editor.scrollPane.verticalScrollBar.width
            return if (!verticalScrollbarFlipped) width * 2 else width
        }

        private fun getGutterTextGap(): Int {
            return if (verticalScrollbarFlipped) {
                val gutter = (editor as EditorEx).gutterComponentEx
                gutter.width - gutter.whitespaceSeparatorOffset
            } else 0
        }
    }

    companion object {
        val INLAYS_KEY: Key<SuggestionInlaysManager> = Key.create("SuggestionInlaysManager")

        fun from(editor: EditorImpl): SuggestionInlaysManager {
            return synchronized(editor) {
                val manager = editor.getUserData(INLAYS_KEY)
                if (manager == null) {
                    val newManager = SuggestionInlaysManager(editor)
                    editor.putUserData(INLAYS_KEY, newManager)
                    newManager
                } else manager
            }
        }
    }
}