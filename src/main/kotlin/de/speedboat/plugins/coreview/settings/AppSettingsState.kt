package de.speedboat.plugins.coreview.settings;

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "de.speedboat.plugins.coreview.settings.AppSettingsState",
    storages = [Storage("CoReviewSettings.xml")]
)
class AppSettingsState : PersistentStateComponent<AppSettingsState> {

    companion object {
        fun getInstance(): AppSettingsState =
            ApplicationManager.getApplication().getService(AppSettingsState::class.java)
    }

    var openAPIKey = ""

    override fun getState(): AppSettingsState = this

    override fun loadState(state: AppSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

}