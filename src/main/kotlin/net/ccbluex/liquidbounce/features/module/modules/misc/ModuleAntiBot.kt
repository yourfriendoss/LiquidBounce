@file:Suppress("ReplaceSizeCheckWithIsNotEmpty")

package net.ccbluex.liquidbounce.features.module.modules.misc

import com.mojang.authlib.GameProfile
import net.ccbluex.liquidbounce.event.NotificationEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.repeatable
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.notification
import net.ccbluex.liquidbounce.utils.entity.ping
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients

object ModuleAntiBot : Module("AntiBot", Category.MISC) {

    private var pName: String? = null

    val packetHandler = handler<PacketEvent> { event ->
        when (val packet = event.packet) {
            is PlayerListS2CPacket -> {
                when (packet.action) {
                    PlayerListS2CPacket.Action.ADD_PLAYER -> {
                        for (entry in packet.entries) {
                            if (entry.profile.name.length < 3 || entry.latency < 2) {
                                continue
                            }

                            if (isADuplicate(entry.profile)) {
                                event.cancelEvent()
                                notification(
                                    "AntiBot",
                                    "Removed ${entry.profile.name}",
                                    NotificationEvent.Severity.INFO
                                )
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
    }

    val repeatable = repeatable {
        if (pName == null) {
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

    private fun doesSiteAcceptProfile(profile: GameProfile): Boolean {
        val client = HttpClients.createDefault()
        val request = HttpGet("https://api.mojang.com/user/profiles/${profile.id}/names")
        val response = client.execute(request)

        return response.statusLine.statusCode == 200
    }
}
