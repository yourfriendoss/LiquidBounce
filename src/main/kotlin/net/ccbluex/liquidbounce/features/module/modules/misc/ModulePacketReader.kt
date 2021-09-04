package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.TransferOrigin
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.chat

object ModulePacketReader : Module("PacketReader", Category.MISC) {

    val p = handler<PacketEvent> { event ->
        if (event.origin == TransferOrigin.RECEIVE) {
            chat(event.packet.toString())
        }
    }
}
