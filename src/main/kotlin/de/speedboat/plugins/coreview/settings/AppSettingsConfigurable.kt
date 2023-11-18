package de.speedboat.plugins.coreview.settings

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
        val settings: AppSettingsState = AppSettingsState.getInstance()
        return settingsComponent!!.openAPIText != settings.openAPIKey
    }

    override fun apply() {
        val settings: AppSettingsState = AppSettingsState.getInstance()
        settings.openAPIKey = settingsComponent!!.openAPIText
    }

    override fun reset() {
        val settings: AppSettingsState = AppSettingsState.getInstance()
        settingsComponent!!.openAPIText = settings.openAPIKey
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }
}