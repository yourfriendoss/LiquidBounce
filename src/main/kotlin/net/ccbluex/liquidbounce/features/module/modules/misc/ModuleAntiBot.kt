@file:Suppress("ReplaceSizeCheckWithIsNotEmpty")

package net.ccbluex.liquidbounce.features.module.modules.misc

import com.mojang.authlib.GameProfile
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.notification
import net.ccbluex.liquidbounce.utils.entity.ping
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.*

object ModuleAntiBot : Module("AntiBot", Category.MISC) {

    var pName: String? = null

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
                            notification("AntiBot", "Removed ${entry.profile.name}", NotificationEvent.Severity.INFO)
                            continue
                        }

                        pName = entry.profile.name
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

    val repeatable = repeatable {
        if (mc.world == null || mc.player == null || pName == null) {
            return@repeatable
        }

        for (entity in world.entities) {
            if (entity is PlayerEntity && entity.entityName == pName) {
                if (!isArmored(entity) || entity.ping < 2) {
                    pName = null
                }

                if (pName != null) {
                    world.removeEntity(entity.id, Entity.RemovalReason.DISCARDED)
                    entity.entityWorld.getEntityById(entity.id)!!.setRemoved(Entity.RemovalReason.DISCARDED)
                    entity.entityWorld.getEntityById(entity.id)!!.remove(Entity.RemovalReason.DISCARDED)
                    entity.entityWorld.disconnect()
                    notification("AntiBot", "Removed $pName", NotificationEvent.Severity.INFO)
                    pName = null
                }
                break
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
}
