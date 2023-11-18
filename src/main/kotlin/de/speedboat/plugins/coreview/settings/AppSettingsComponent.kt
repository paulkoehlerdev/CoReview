package de.speedboat.plugins.coreview.settings

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class AppSettingsComponent {

    private val settingsPanel: JPanel
    private val openAPITextField: JBTextField = JBTextField()

    init {
        settingsPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("OpenAI key: "), openAPITextField, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    val panel: JPanel
        get() = settingsPanel
    val preferredFocusedComponent: JComponent
        get() = openAPITextField

    var openAPIText: String
        get() = openAPITextField.getText()
        set(newText) {
            openAPITextField.setText(newText)
        }
}