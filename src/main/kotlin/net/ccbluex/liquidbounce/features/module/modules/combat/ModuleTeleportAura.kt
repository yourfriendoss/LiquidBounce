package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.event.repeatable
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.aiming.RotationManager
import net.ccbluex.liquidbounce.utils.client.MC_1_8
import net.ccbluex.liquidbounce.utils.client.protocolVersion
import net.ccbluex.liquidbounce.utils.client.timer
import net.ccbluex.liquidbounce.utils.combat.TargetTracker
import net.ccbluex.liquidbounce.utils.entity.eyesPos
import net.ccbluex.liquidbounce.utils.entity.squaredBoxedDistanceTo
import net.ccbluex.liquidbounce.utils.path.findPath
import net.minecraft.entity.Entity
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.Hand

object ModuleTeleportAura : Module("TeleportAura", Category.COMBAT) {

    private val range by float("Range", 100f, 8f..100f)
    private val timer by float("Timer", 1f, 0.1f..2f)
    private val steps by float("Steps", 4f, 2f..10f)
    private val swing by boolean("Swing", true)

    private val targetTracker = tree(TargetTracker())

    private var resetTimer = false

    override fun disable() {
        mc.timer.timerSpeed = 1F
        resetTimer = false
    }

    val repeatable = repeatable {
        if (player.isSpectator || player.isDead) {
            return@repeatable
        }

        if (resetTimer) {
            mc.timer.timerSpeed = 1f
            resetTimer = false
        }

        val rangeSquared = range * range

        for (enemy in targetTracker.enemies()) {
            if (enemy.squaredBoxedDistanceTo(player) > rangeSquared) {
                continue
            }

            val (rotation, _) = RotationManager.raytraceBox(
                player.eyesPos,
                enemy.boundingBox,
                range = range.toDouble(),
                wallsRange = range.toDouble()
            ) ?: continue

            findPath(enemy.x, enemy.y + 1.0, enemy.z, steps.toDouble()).forEach { pos ->
                network.sendPacket(
                    PlayerMoveC2SPacket.Full(pos.x, pos.y, pos.z, rotation.yaw, rotation.pitch, false)
                )
            }

            mc.timer.timerSpeed = timer
            resetTimer = true
            attack(enemy)
        }
    }


    fun attack(entity: Entity) {
        EventManager.callEvent(AttackEvent(entity))

        // Swing before attacking (on 1.8)
        if (swing && protocolVersion == MC_1_8) {
            player.swingHand(Hand.MAIN_HAND)
        } else {
            network.sendPacket(HandSwingC2SPacket(Hand.MAIN_HAND))
        }

        network.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, player.isSneaking))

        // Swing after attacking (on 1.9+)
        if (swing && protocolVersion != MC_1_8) {
            player.swingHand(Hand.MAIN_HAND)
        } else {
            network.sendPacket(HandSwingC2SPacket(Hand.MAIN_HAND))
        }
    }

}
