@file:Suppress("ReplaceSizeCheckWithIsNotEmpty")

package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.NotificationEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.notification
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import java.util.*


object ModuleAntiBot : Module("AntiBot", Category.MISC) {

    val packetHandler = handler<PacketEvent> { event ->
        if (event.packet is PlayerListS2CPacket && event.packet.action == PlayerListS2CPacket.Action.ADD_PLAYER) {
            for (entry in event.packet.entries) {
                if (entry.profile.name.length < 3) {
                    continue
                }

                if (isADuplicate(entry) || (entry.profile.properties.isEmpty && entry.latency > 1 && !doesSiteAcceptName(entry.profile.id))) {
                    event.cancelEvent()
                    notification("AntiBot", "Removed ${entry.profile.name}", NotificationEvent.Severity.INFO)
                }
            }
        }
    }

    private fun isADuplicate(entry: PlayerListS2CPacket.Entry): Boolean {
        return network.playerList.count { it.profile.name == entry.profile.name } > 0
    }


    fun doesSiteAcceptName(uuid: UUID): Boolean {
        val client = HttpClients.createDefault()
        val request = HttpGet("https://api.mojang.com/user/profiles/${uuid}/names")
        val response = client.execute(request)

        return response.statusLine.statusCode == 200
    }
}
