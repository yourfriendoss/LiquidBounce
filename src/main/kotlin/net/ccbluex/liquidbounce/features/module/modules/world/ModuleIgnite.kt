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
import net.ccbluex.liquidbounce.features.module.modules.world.ModuleScaffold.updateTarget
import net.ccbluex.liquidbounce.utils.aiming.RotationManager
import net.ccbluex.liquidbounce.utils.aiming.RotationsConfigurable
import net.ccbluex.liquidbounce.utils.aiming.raycast
import net.ccbluex.liquidbounce.utils.block.getState
import net.ccbluex.liquidbounce.utils.combat.TargetTracker
import net.ccbluex.liquidbounce.utils.entity.squaredBoxedDistanceTo
import net.ccbluex.liquidbounce.utils.item.findHotbarSlot
import net.minecraft.block.CobwebBlock
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.HitResult

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

        val slot = findHotbarSlot(arrayListOf(Items.LAVA_BUCKET, Items.FLINT_AND_STEEL, Items.COBWEB)) ?: return@repeatable

        for (enemy in targetTracker.enemies()) {
            if (enemy.squaredBoxedDistanceTo(player) > 6.0 * 6.0) {
                continue
            }

            val pos = enemy.blockPos

            val state = pos.getState()

            if (enemy.isOnFire || state?.block is CobwebBlock) {
                continue
            }

            val currentTarget = updateTarget(pos, true) ?: continue

            val rotation = currentTarget.rotation.fixedSensitivity() ?: continue
            val rayTraceResult = raycast(4.5, rotation) ?: return@repeatable

            if (rayTraceResult.type != HitResult.Type.BLOCK) {
                continue
            }

            RotationManager.aimAt(rotation, configurable = rotations)

            if (slot != player.inventory.selectedSlot) {
                network.sendPacket(UpdateSelectedSlotC2SPacket(slot))
            }

            if (interaction.interactBlock(player, world, Hand.MAIN_HAND, rayTraceResult) == ActionResult.SUCCESS) {
                if (player.inventory.getStack(slot).item == Items.LAVA_BUCKET) {
                    network.sendPacket(PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, rayTraceResult))
                    player.swingHand(Hand.MAIN_HAND)
                }
                player.swingHand(Hand.MAIN_HAND)
            }

            if (slot != player.inventory.selectedSlot) {
                network.sendPacket(UpdateSelectedSlotC2SPacket(player.inventory.selectedSlot))
            }

            break
        }

    }
}
