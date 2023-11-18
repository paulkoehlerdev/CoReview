package de.speedboat.plugins.coreview.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import de.speedboat.plugins.coreview.Bundle

@Service(Service.Level.PROJECT)
class MyProjectService(project: Project) {
    fun getRandomNumber() = (1..100).random()
}
