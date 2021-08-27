@file:Suppress("ReplaceSizeCheckWithIsNotEmpty")

package net.ccbluex.liquidbounce.features.module.modules.misc

import com.mojang.authlib.GameProfile
import net.ccbluex.liquidbounce.event.NotificationEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.StateUpdateEvent
import net.ccbluex.liquidbounce.utils.client.notification
import net.ccbluex.liquidbounce.utils.entity.ping
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket

object ModuleAntiBot : Module("AntiBot", Category.MISC) {

    private var pName: String? = null

    val packetHandler = handler<PacketEvent> { event ->
        if (mc.world == null || mc.player == null) {
            return@handler
        }

        val packet = event.packet

        if (packet is PlayerListS2CPacket) {
            when (packet.action) {
                PlayerListS2CPacket.Action.ADD_PLAYER -> {
                    for (entry in packet.entries) {
                        if (entry.profile.name.length < 3) {
                            continue
                        }

                        if (isADuplicate(entry.profile)) {
                            event.cancelEvent()
                            notification(
                                "AntiBot",
                                "Removed ${entry.profile.name}",
                                NotificationEvent.Severity.INFO
                            )
                        } else {
                            pName = entry.profile.name
                        }
                    }
                }
                PlayerListS2CPacket.Action.REMOVE_PLAYER -> {
                    for (entry in packet.entries) {
                        if (entry.profile.name == pName) {
                            pName = null
                        }
                    }
                }
                else -> {
                }
            }
        }
    }

    val repeatable = handler<StateUpdateEvent> {
        if (mc.world == null || mc.player == null || pName == null) {
            return@handler
        }

        for (entity in world.entities) {
            if (entity is PlayerEntity && entity.entityName == pName) {
                if (!isArmored(entity) || entity.ping < 2) {
                    pName = null
                }

                if (pName != null) {
                    world.removeEntity(entity.id, Entity.RemovalReason.DISCARDED)
                    notification("AntiBot", "Removed $pName", NotificationEvent.Severity.INFO)
                    pName = null
                }
                break
            }
        }
    }

    private fun isADuplicate(profile: GameProfile): Boolean {
        return network.playerList.count { it.profile.name.equals(profile.name, true) } > 0
    }

    private fun isArmored(entity: PlayerEntity): Boolean {
        for (i in 0..3) {
            return !entity.inventory.getArmorStack(i).isEmpty
        }
        return false
    }
}
