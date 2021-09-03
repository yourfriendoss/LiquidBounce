package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.repeatable
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.chat

object ModulePacketReader : Module("PacketReader", Category.MISC) {

    val repeatable = repeatable {
        val a = arrayListOf(player.boundingBox.minZ, player.boundingBox.minY, player.boundingBox.minZ, player.boundingBox.maxX, player.boundingBox.maxY, player.boundingBox.maxZ)
        chat(a.toString())
    }
}
