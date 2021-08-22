package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.HealthUpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object ModuleKeepAlive : Module("KeepAlive", Category.PLAYER) {

    val healthUpdateHandler = handler<HealthUpdateEvent> { event ->
    }
}
