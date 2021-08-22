package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.HealthUpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object ModuleKeepAlive : Module("KeepAlive", Category.PLAYER) {

    val health by float("health", 0.5f, 1.0f..0.0f)
    var oneTime = false

    val healthUpdateHandler = handler<HealthUpdateEvent> { event ->
        if (event.health <= health) {
            if (oneTime) {
                return@handler
            }

            player.sendChatMessage("/heal")
            oneTime = true
        } else {
            if (oneTime) {
                oneTime = false
            }
        }
    }
}
