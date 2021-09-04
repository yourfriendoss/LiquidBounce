@file:Suppress("ReplaceSizeCheckWithIsNotEmpty")

package net.ccbluex.liquidbounce.features.module.modules.misc

import com.mojang.authlib.GameProfile
import net.ccbluex.liquidbounce.event.NotificationEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.notification
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket

object ModuleAntiBot : Module("AntiBot", Category.MISC) {

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

                        if (isADuplicate(entry.profile) || (armor(entry.profile) || entry.latency > 1)) {
                            event.cancelEvent()
                            notification("AntiBot", "Removed ${entry.profile.name}", NotificationEvent.Severity.INFO)
                        }
                    }
                }
                else -> {
                }
            }
        }
    }

    /* val repeatable = repeatable {
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
                     notification("AntiBot", "Removed $pName", NotificationEvent.Severity.INFO)
                     pName = null
                 }
                 break
             }
         }*/

    private fun isADuplicate(profile: GameProfile): Boolean {
        return network.playerList.count { it.profile.name == profile.name } > 0
    }

    private fun armor(entry: GameProfile) : Boolean {
        network.playerList.filter { it.profile.name == entry.name }.forEach { entity ->
            if (entity is PlayerEntity) {
                return isArmored(entity)
            }
        }
        return false
    }

    private fun isArmored(entity: PlayerEntity): Boolean {
        for (i in 0..3) {
            return !entity.inventory.getArmorStack(i).isEmpty
        }
        return false
    }
}
