package de.speedboat.plugins.coreview.settings

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import de.speedboat.plugins.coreview.services.OpenAIService
import javax.swing.JComponent
import javax.swing.JPanel

class AppSettingsComponent {
    private val settingsPanel: JPanel
    private val openAiApiKeyPasswordField: JBTextField = JBTextField()
    private val experienceLevelComboBox: ComboBox<OpenAIService.DeveloperExperience> = ComboBox(OpenAIService.DeveloperExperience.values())

    init {
        settingsPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(JBLabel("OpenAI API Key: "), openAiApiKeyPasswordField, 1, false)
                .addSeparator()
                .addLabeledComponent(JBLabel("Experience Level: "), experienceLevelComboBox, 1, false)
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

    var experienceLevel: OpenAIService.DeveloperExperience
        get() {
            return experienceLevelComboBox.item
        }
        set(value) {
            experienceLevelComboBox.item = value
        }
}
