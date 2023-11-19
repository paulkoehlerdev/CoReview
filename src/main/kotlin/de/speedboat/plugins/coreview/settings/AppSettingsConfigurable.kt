package de.speedboat.plugins.coreview.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import de.speedboat.plugins.coreview.services.OpenAIService
import javax.swing.JComponent

class AppSettingsConfigurable : Configurable {
    private var settingsComponent: AppSettingsComponent? = null

    override fun getDisplayName(): String {
        return "CoReview"
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return settingsComponent!!.preferredFocusedComponent;
    }

    override fun createComponent(): JComponent {
        settingsComponent = AppSettingsComponent()
        return settingsComponent!!.panel
    }

    override fun isModified(): Boolean {
        return settingsComponent!!.openAiApiKey != AppSettingsSecrets.getSecret(AppSecrets.OPEN_AI_API_KEY)
                || settingsComponent!!.experienceLevel != AppSettingsState.getInstance().experienceLevel
    }

    override fun apply() {
        AppSettingsSecrets.setSecret(AppSecrets.OPEN_AI_API_KEY, settingsComponent!!.openAiApiKey)
        AppSettingsState.getInstance().experienceLevel = settingsComponent!!.experienceLevel

        service<OpenAIService>().initializeOpenAI()
    }

    override fun reset() {
        settingsComponent!!.openAiApiKey = AppSettingsSecrets.getSecret(AppSecrets.OPEN_AI_API_KEY) ?: ""
        settingsComponent!!.experienceLevel = AppSettingsState.getInstance().experienceLevel
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }
}