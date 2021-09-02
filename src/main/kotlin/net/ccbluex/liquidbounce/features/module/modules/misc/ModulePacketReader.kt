package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.repeatable
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.chat
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket

object ModulePacketReader : Module("PacketReader", Category.MISC) {

    val repeatable = repeatable {
        val a = player.boundingBox.minY + (player.boundingBox.maxY - player.boundingBox.minY) * 0.5
        val b = player.boundingBox.maxY * 0.5
        chat("Shit one $a Shit second $b")
    }
}
