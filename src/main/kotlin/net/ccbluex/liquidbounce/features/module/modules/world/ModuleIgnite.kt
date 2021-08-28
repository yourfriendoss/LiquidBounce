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
import net.ccbluex.liquidbounce.utils.combat.TargetTracker
import net.ccbluex.liquidbounce.utils.entity.eyesPos
import net.ccbluex.liquidbounce.utils.entity.squaredBoxedDistanceTo
import net.ccbluex.liquidbounce.utils.item.findHotbarSlot
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext

/**
 * BlockTrap module
 *
 * Automatically traps players around you.
 */
object ModuleIgnite : Module("BlockTrap", Category.WORLD) {

    var delay by int("Delay", 20, 0..40)

    // Target
    private val targetTracker = tree(TargetTracker())

    // Rotations
    private val rotations = tree(RotationsConfigurable())

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

            val (rotation, _) = RotationManager.raytraceBlock(
                player.eyesPos,
                enemy.blockPos,
                enemy.blockStateAtPos,
                range = 6.0,
                wallsRange = 0.0
            ) ?: continue

            val raytraceResult = mc.world?.raycast(
                RaycastContext(
                    player.eyesPos,
                    Vec3d.of(enemy.blockPos).add(0.5, 0.5, 0.5),
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    player
                )
            ) ?: return@repeatable

            if (raytraceResult.type != HitResult.Type.BLOCK) {
                continue
            }

            if (slot != player.inventory.selectedSlot) {
                network.sendPacket(UpdateSelectedSlotC2SPacket(slot))
            }

            RotationManager.aimAt(rotation, configurable = rotations)

            if (interaction.interactBlock(player, world, Hand.MAIN_HAND, raytraceResult) == ActionResult.SUCCESS) {
                player.swingHand(Hand.MAIN_HAND)
            }

            if (slot != player.inventory.selectedSlot) {
                network.sendPacket(UpdateSelectedSlotC2SPacket(player.inventory.selectedSlot))
            }

            wait(delay)

            return@repeatable
        }

    }
}
