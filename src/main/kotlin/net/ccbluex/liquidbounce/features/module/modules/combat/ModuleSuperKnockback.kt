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

package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.config.Choice
import net.ccbluex.liquidbounce.config.ChoiceConfigurable
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.entity.LivingEntity
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket

/**
 * SuperKnockback module
 *
 * Increases knockback dealt to other entities.
 */
object ModuleSuperKnockback : Module("SuperKnockback", Category.COMBAT) {

    val modes = choices("Mode", Packet, arrayOf(Packet, WTap))
    val hurtTime by int("HurtTime", 10, 0..10)

    private object Packet : Choice("Packet") {

        override val parent: ChoiceConfigurable
            get() = modes

        val attackHandler = handler<AttackEvent> { event ->
            val enemy = event.enemy

            if (enemy is LivingEntity && enemy.hurtTime <= hurtTime && !ModuleCriticals.wouldCrit()) {
                if (player.isSprinting) {
                    network.sendPacket(ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.STOP_SPRINTING))
                }

                network.sendPacket(ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_SPRINTING))
                network.sendPacket(ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.STOP_SPRINTING))
                network.sendPacket(ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_SPRINTING))

                player.lastSprinting = true
            }
        }
    }

    private object WTap : Choice("WTap") {

        override val parent: ChoiceConfigurable
            get() = modes

        val attackHandler = handler<AttackEvent> { event ->
            val enemy = event.enemy

            if (enemy is LivingEntity && enemy.hurtTime <= hurtTime && !ModuleCriticals.wouldCrit()) {
                if (player.isSprinting) {
                    network.sendPacket(ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.STOP_SPRINTING))
                }

                network.sendPacket(ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_SPRINTING))
                player.lastSprinting = true
            }
        }
    }
}
