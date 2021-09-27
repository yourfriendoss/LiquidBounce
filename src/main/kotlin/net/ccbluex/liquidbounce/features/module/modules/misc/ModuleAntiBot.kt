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

    val packetHandler = handler<PacketEvent> { event ->
        if (event.packet is PlayerListS2CPacket && event.packet.action == PlayerListS2CPacket.Action.ADD_PLAYER) {
            for (entry in event.packet.entries) {
                if (entry.profile.name.length < 3) {
                    continue
                }

                network.playerUuids.add(entry.profile.id)

                if (isADuplicate(entry) || (entry.profile.properties.isEmpty && entry.latency > 1 && isArmored(entry))) {
                    event.cancelEvent()
                    notification("AntiBot", "Removed ${entry.profile.name}", NotificationEvent.Severity.INFO)
                }
            }
        }
    }

    private fun isADuplicate(entry: PlayerListS2CPacket.Entry): Boolean {
        return network.playerList.count { it.profile.name == entry.profile.name } > 0
    }

    private fun isArmored(entry: PlayerListS2CPacket.Entry): Boolean {
        for (i in 0..3) {
            return !world.getPlayerByUuid(entry.profile.id)!!.inventory.getArmorStack(i).isEmpty
        }
        return false
    }
}
