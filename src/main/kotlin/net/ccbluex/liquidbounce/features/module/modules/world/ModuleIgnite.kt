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
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.repeatable
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.aiming.RotationManager
import net.ccbluex.liquidbounce.utils.aiming.RotationsConfigurable
import net.ccbluex.liquidbounce.utils.aiming.raytraceBlock
import net.ccbluex.liquidbounce.utils.block.getState
import net.ccbluex.liquidbounce.utils.combat.TargetTracker
import net.ccbluex.liquidbounce.utils.entity.eyesPos
import net.ccbluex.liquidbounce.utils.entity.squaredBoxedDistanceTo
import net.ccbluex.liquidbounce.utils.item.findHotbarSlot
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.HitResult

/**
 * Ignite module
 *
 * Automatically sets targets around you on fire.
 */
object ModuleIgnite : Module("Ignite", Category.WORLD) {

    val delay by int("Delay", 20, 0..40)
    val range by float("Range", 4f, 1f..8f)
    private val wallRange by float("WallRange", 3f, 0f..8f).listen {
        if (it > range) {
            range
        } else {
            it
        }
    }

    // Target
    private val targetTracker = tree(TargetTracker())

    val rotations = tree(RotationsConfigurable())

    val networkTickHandler = repeatable {
        val player = mc.player ?: return@repeatable

        val slot = findHotbarSlot(Items.LAVA_BUCKET) ?: return@repeatable

        for (enemy in targetTracker.enemies()) {
            if (enemy.squaredBoxedDistanceTo(player) > 6.0 * 6.0) {
                continue
            }

            val pos = enemy.blockPos

            if (pos.getState()!!.block == Blocks.LAVA || enemy.isOnFire) {
                continue
            }

            updateTarget(enemy)

            val serverRotation = RotationManager.serverRotation ?: return@repeatable

            val rayTraceResult = raytraceBlock(range.toDouble(), serverRotation, pos, pos.getState()!!) ?: return@repeatable

            if (rayTraceResult.type != HitResult.Type.BLOCK || rayTraceResult.blockPos != pos) {
                return@repeatable
            }

            if (interaction.interactBlock(player, world, Hand.MAIN_HAND, rayTraceResult) == ActionResult.SUCCESS) {
                player.swingHand(Hand.MAIN_HAND)
            }

            if (slot != player.inventory.selectedSlot) {
                player.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(player.inventory.selectedSlot))
            }

            wait(delay)

            return@repeatable
        }

    }

    fun updateTarget(entity: Entity) {
        val (rotation, _) = RotationManager.raytraceUpperBlockSide(
            player.eyesPos,
            range.toDouble(),
            wallRange.toDouble(),
            entity.blockPos
        ) ?: return

        targetTracker.lock(entity)

        RotationManager.aimAt(rotation, configurable = rotations)

        return
    }
}
