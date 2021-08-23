package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object ModuleLiquidInteract : Module("LiquidInteract", Category.WORLD) {
    val a by boolean("a", false)
}
