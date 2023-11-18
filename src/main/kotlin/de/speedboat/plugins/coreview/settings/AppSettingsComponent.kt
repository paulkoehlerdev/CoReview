package de.speedboat.plugins.coreview.settings

import com.intellij.credentialStore.getTrimmedChars
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class AppSettingsComponent {
    private val settingsPanel: JPanel
    private val openAiApiKeyPasswordField: JBTextField = JBTextField()

    init {
        settingsPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("OpenAI API Key: "), openAiApiKeyPasswordField, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    val panel: JPanel get() = settingsPanel
    val preferredFocusedComponent: JComponent get() = openAiApiKeyPasswordField

    var openAiApiKey: String
        get() {
            return openAiApiKeyPasswordField.text
        }
        set(value) {
            openAiApiKeyPasswordField.text = value
        }
}
