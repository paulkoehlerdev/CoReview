package de.speedboat.plugins.coreview.settings

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

internal class AppSettingsConfigurable : Configurable {
    private var settingsComponent: AppSettingsComponent? = null

    @Nls(capitalization = Nls.Capitalization.Title)
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
    }

    override fun apply() {
        thisLogger().warn(settingsComponent!!.openAiApiKey)
        AppSettingsSecrets.setSecret(AppSecrets.OPEN_AI_API_KEY, settingsComponent!!.openAiApiKey)
    }

    override fun reset() {
        settingsComponent!!.openAiApiKey = AppSettingsSecrets.getSecret(AppSecrets.OPEN_AI_API_KEY) ?: ""
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }
}