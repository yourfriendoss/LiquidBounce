@file:Suppress("ReplaceSizeCheckWithIsNotEmpty")

package net.ccbluex.liquidbounce.features.module.modules.misc

import com.mojang.authlib.GameProfile
import net.ccbluex.liquidbounce.event.NotificationEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.client.notification
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket


object ModuleAntiBot : Module("AntiBot", Category.MISC) {

    val packetHandler = handler<PacketEvent> { event ->

        when (val packet = event.packet) {
            is PlayerListS2CPacket -> {
                when (packet.action) {
                    PlayerListS2CPacket.Action.ADD_PLAYER -> {
                        for (entry in packet.entries) {
                            if (entry.profile.name.length < 3) {
                                continue
                            }

                            if (world.getPlayerByUuid(entry.profile.id)!!.inventory.getArmorStack(1).isEmpty) {
                                chat("empty lol")
                            } else {
                                chat("noetmepy")
                            }

                            if (isADuplicate(entry.profile)) {
                                event.cancelEvent()
                                notification(
                                    "AntiBot",
                                    "Removed ${entry.profile.name}",
                                    NotificationEvent.Severity.INFO
                                )
                                chat("duplicate, whatn andiot")
                            }
                        }
                    }
                    else -> { }
                }
            }
        }
    }

    private fun isADuplicate(profile: GameProfile): Boolean {
        return network.playerList.count { it.profile.name == profile.name } > 0
    }

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
