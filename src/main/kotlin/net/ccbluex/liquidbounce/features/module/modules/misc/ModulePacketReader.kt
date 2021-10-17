package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.chat
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket

object ModulePacketReader : Module("PacketReader", Category.MISC) {

    val p = handler<PacketEvent> { event ->
        chat(event.packet.toString())
    }
}
