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
import net.ccbluex.liquidbounce.utils.block.getCenterDistanceSquared
import net.ccbluex.liquidbounce.utils.block.getState
import net.ccbluex.liquidbounce.utils.block.searchBlocksInRadius
import net.ccbluex.liquidbounce.utils.combat.TargetTracker
import net.ccbluex.liquidbounce.utils.entity.eyesPos
import net.ccbluex.liquidbounce.utils.entity.getNearestPoint
import net.ccbluex.liquidbounce.utils.entity.squaredBoxedDistanceTo
import net.ccbluex.liquidbounce.utils.item.findHotbarSlot
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext

/**
 * Ignite module
 *
 * Automatically sets targets around you on fire.
 */
object ModuleIgnite : Module("Ignite", Category.WORLD) {

    var delay by int("Delay", 20, 0..40)

    // Target
    private val targetTracker = tree(TargetTracker())

    val rotations = tree(RotationsConfigurable())

    private var currentBlock: BlockPos? = null

    val networkTickHandler = repeatable {
        val player = mc.player ?: return@repeatable

        val slot = findHotbarSlot(Items.FLINT_AND_STEEL) ?: return@repeatable

        for (enemy in targetTracker.enemies()) {
            if (enemy.squaredBoxedDistanceTo(player) > 6.0 * 6.0) {
                continue
            }

            if (enemy.isOnFire) {
                continue
            }

            updateTarget(enemy)

            val curr = currentBlock ?: return@repeatable
            val serverRotation = RotationManager.serverRotation ?: return@repeatable

            val rayTraceResult = raytraceBlock(
                6.0,
                serverRotation,
                curr,
                curr.getState() ?: return@repeatable
            )

            if (rayTraceResult?.type != HitResult.Type.BLOCK || rayTraceResult.blockPos != curr) {
                return@repeatable
            }

            if (slot != player.inventory.selectedSlot) {
                network.sendPacket(UpdateSelectedSlotC2SPacket(slot))
            }

            if (interaction.interactBlock(
                    player,
                    world,
                    Hand.MAIN_HAND,
                    rayTraceResult
                ) == ActionResult.SUCCESS
            ) {
                    player.swingHand(Hand.MAIN_HAND)
            }

            if (slot != player.inventory.selectedSlot) {
                network.sendPacket(UpdateSelectedSlotC2SPacket(player.inventory.selectedSlot))
            }

            wait(delay)
            return@repeatable
        }

    }

    private fun updateTarget(entity: net.minecraft.entity.Entity) {
        currentBlock = null

        val radius = 6.0f + 1
        val radiusSquared = radius * radius
        val eyesPos = player.eyesPos

        val blockToProcess = searchBlocksInRadius(radius) { _, state ->
            !state.isAir && getNearestPoint(
                eyesPos,
                Box(entity.blockPos, entity.blockPos.add(1, 1, 1))
            ).squaredDistanceTo(eyesPos) <= radiusSquared
        }.minByOrNull { it.first.getCenterDistanceSquared() } ?: return

        val (pos, state) = blockToProcess

        val rt = RotationManager.raytraceBlock(
            player.eyesPos,
            pos,
            state,
            range = 6.0,
            wallsRange = 0.0
        )

        // We got a free angle at the block? Cool.
        if (rt != null) {
            val (rotation, _) = rt
            RotationManager.aimAt(rotation, configurable = rotations)
            currentBlock = pos
            return
        }

        val raytraceResult = world.raycast(
            RaycastContext(
                player.eyesPos,
                Vec3d.of(pos).add(0.5, 0.5, 0.5),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                player
            )
        ) ?: return

        // Failsafe. Should not trigger
        if (raytraceResult.type != HitResult.Type.BLOCK) return

        currentBlock = raytraceResult.blockPos
    }
}
