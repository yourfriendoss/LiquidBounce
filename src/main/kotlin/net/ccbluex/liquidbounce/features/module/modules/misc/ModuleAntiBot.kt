@file:Suppress("ReplaceSizeCheckWithIsNotEmpty")

package net.ccbluex.liquidbounce.features.module.modules.misc

import com.mojang.authlib.GameProfile
import net.ccbluex.liquidbounce.event.NotificationEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.repeatable
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.client.notification
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket

object ModuleAntiBot : Module("AntiBot", Category.MISC) {

    private var pName: String? = null

    val packetHandler = handler<PacketEvent> { event ->
        if (event.packet is PlayerListS2CPacket && event.packet.action == PlayerListS2CPacket.Action.ADD_PLAYER) {
            for (entry in event.packet.entries) {
                if (entry.latency < 2 || entry.profile.name.length < 3 || !entry.profile.properties.isEmpty || isTheSamePlayer(
                        entry.profile
                    )
                ) {
                    continue
                }

                if (isADuplicate(entry.profile)) {
                    event.cancelEvent()
                    notification("AntiBot", "Removed dupe ${entry.profile.name}", NotificationEvent.Severity.INFO)
                    continue
                }

                pName = entry.profile.name
            }
        }
    }

    val repeatable = repeatable {
        if (pName == null) {
            return@repeatable
        }


        for (entity in world.entities) {
            if (entity is PlayerEntity && entity.entityName == pName) {
                chat("${entity.age} AGE <<<< and HURTTIME: ${entity.hurtTime}")
            }
        }
    }

    private fun isADuplicate(profile: GameProfile): Boolean {
        return network.playerList.count { it.profile.name == profile.name } > 0
    }

    private fun isArmored(entity: PlayerEntity): Boolean {
        for (i in 0..3) {
            return !entity.inventory.getArmorStack(i).isEmpty
        }
        return false
    }

    private fun isTheSamePlayer(profile: GameProfile): Boolean {
        // Prevents false positives when a player joins a minigame such as Practice
        return network.playerList.count { it.profile.name == profile.name && it.profile.id == profile.id } > 0
    }
}
