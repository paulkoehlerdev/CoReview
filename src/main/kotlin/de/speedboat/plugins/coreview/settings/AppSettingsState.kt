package de.speedboat.plugins.coreview.settings;

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import de.speedboat.plugins.coreview.services.OpenAIService

@State(name = "de.speedboat.plugins.coreview.settings.AppSettingsState", storages = [Storage("CoReviewSettings.xml")])
class AppSettingsState : PersistentStateComponent<AppSettingsState> {

    var experienceLevel: OpenAIService.DeveloperExperience = OpenAIService.DeveloperExperience.INTERMEDIATE

    companion object {
        fun getInstance(): AppSettingsState = ApplicationManager.getApplication().getService(AppSettingsState::class.java)
    }

    override fun getState(): AppSettingsState = this

    override fun loadState(state: AppSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }
}