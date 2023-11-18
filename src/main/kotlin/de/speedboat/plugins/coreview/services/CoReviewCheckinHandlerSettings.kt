package de.speedboat.plugins.coreview.services

import com.intellij.openapi.components.*

@Service(Service.Level.PROJECT)
@State(name = "CoReviewCheckinHandlerSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class CoReviewCheckinHandlerSettings :
    SimplePersistentStateComponent<CoReviewCheckinHandlerState>(CoReviewCheckinHandlerState())
