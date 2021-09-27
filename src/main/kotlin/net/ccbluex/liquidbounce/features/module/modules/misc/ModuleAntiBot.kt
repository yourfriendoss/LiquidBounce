@file:Suppress("ReplaceSizeCheckWithIsNotEmpty")

package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.NotificationEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.notification
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket


object ModuleAntiBot : Module("AntiBot", Category.MISC) {

    private var canCancelEvent = false

    val packetHandler = handler<PacketEvent> { event ->

        when (val packet = event.packet) {
            is PlayerListS2CPacket -> {
                when (packet.action) {
                    PlayerListS2CPacket.Action.ADD_PLAYER -> {
                        for (entry in packet.entries) {
                            if (entry.profile.name.length < 3) {
                                continue
                            }

                            if (isADuplicate(entry.profile.name)) {
                                canCancelEvent = true
                                continue
                            }

                            for (i in 0..3) {
                                if (world.getPlayerByUuid(entry.profile.id)!!.inventory.getArmorStack(i).isEmpty || entry.latency < 2) {
                                    break
                                } else {
                                    canCancelEvent = true
                                    break
                                }
                            }

                            if (canCancelEvent) {
                                event.cancelEvent()
                                notification(
                                    "AntiBot",
                                    "Removed ${entry.profile.name}",
                                    NotificationEvent.Severity.INFO
                                )
                            }
                        }
                    }
                    /*PlayerListS2CPacket.Action.REMOVE_PLAYER -> {
                        for (entry in packet.entries) {
                            if (entry.profile.name == pName) {
                                pName = null
                            }
                        }
                    }
                    else -> {
                    }*/
                    else -> { }
                }
            }
        }
    }

/*    val repeatable = repeatable {
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
    }*/

    fun isADuplicate(name: String): Boolean {
        return network.playerList.count { it.profile.name == name } > 0
    }

    /*private fun isArmored(entity: PlayerEntity): Boolean {
        for (i in 0..3) {
            return !entity.inventory.getArmorStack(i).isEmpty
        }
        return false
    }*/

    /*   fun getUsername(uuid: UUID): String? {
           val client = HttpClients.createDefault()
           val request = HttpGet("https://api.mojang.com/user/profiles/${uuid}/names")
           val response = client.execute(request)

           if (response.statusLine.statusCode != 200) {
               return null
           }

           val names = JsonParser().parse(EntityUtils.toString(response.entity)).asJsonArray

           return names.get(names.size() - 1).asJsonObject.get("name").asString
       }*/

}
