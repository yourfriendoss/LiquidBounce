/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2016 - 2021 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.config.Choice
import net.ccbluex.liquidbounce.config.ChoiceConfigurable
import net.ccbluex.liquidbounce.config.ToggleableConfigurable
import net.ccbluex.liquidbounce.event.BlockVelocityMultiplierEvent
import net.ccbluex.liquidbounce.event.PlayerMoveEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.getBlock
import net.minecraft.block.IceBlock
import net.minecraft.block.LadderBlock
import net.minecraft.block.VineBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

/**
 * TerrainSpeed module
 *
 * Move faster on specific surfaces.
 */
object ModuleTerrainSpeed : Module("TerrainSpeed", Category.MOVEMENT) {

    /**
     * Fast Climb allows you to climb up ladder-related blocks faster
     */
    private object FastClimb : ToggleableConfigurable(this, "FastClimb", true) {

        private val modes = choices("Mode", Motion, arrayOf(Motion, Clip))

        /**
         * Not server or anti cheat-specific mode.
         * A basic motion fast climb, which should be configurable enough to bypass most anti-cheats.
         */
        private object Motion : Choice("Motion") {

            override val parent: ChoiceConfigurable
                get() = modes

            private val motion by float("Motion", 0.2872F, 0.1f..0.5f)

            val moveHandler = handler<PlayerMoveEvent> {
                if (player.horizontalCollision && player.isClimbing) {
                    it.movement.y = motion.toDouble()
                }
            }

        }

        /**
         * A very vanilla-like fast climb. Not working on anti-cheats.
         */
        private object Clip : Choice("Clip") {

            override val parent: ChoiceConfigurable
                get() = modes

            val moveHandler = handler<PlayerMoveEvent> {

                if (player.isClimbing && mc.options.keyForward.isPressed) {
                    val startPos = player.pos

                    for (y in 1..8) {
                        val block = BlockPos(player.blockPos.add(0, y, 0)).getBlock()

                        if (block is LadderBlock || block is VineBlock) {
                            player.updatePosition(startPos.x, startPos.y.toInt() + y.toDouble(), startPos.z)
                        } else {
                            var x = 0.0
                            var z = 0.0
                            when (player.horizontalFacing) {
                                Direction.NORTH -> z = -1.0
                                Direction.SOUTH -> z = +1.0
                                Direction.WEST -> x = -1.0
                                Direction.EAST -> x = +1.0
                                else -> break
                            }

                            player.updatePosition(startPos.x + x, startPos.y.toInt() + y.toDouble() + 1, startPos.z + z)
                            break
                        }
                    }
                }
            }

        }

    }

    /**
     * Ice Speed allows you to manipulate slide speed.
     */
    object IceSpeed : ToggleableConfigurable(this, "IceSpeed", true) {

       val motion by float("Motion", 1f, 0.2f..3f)

        val blockVelocityMultiplierHandler = handler<BlockVelocityMultiplierEvent> { event ->

            if (event.block is IceBlock) {
                event.multiplier = motion
            }
        }
    }

    init {
        tree(FastClimb)
        tree(IceSpeed)
    }

}
