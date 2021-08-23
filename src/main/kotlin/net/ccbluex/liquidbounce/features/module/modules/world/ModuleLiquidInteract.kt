package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object ModuleLiquidInteract : Module("LiquidInteract", Category.WORLD) {
    val a by boolean("a", false)

    val b by int("b", 10, 0..100)
    val c by int("c", 10, 0..100)
}
