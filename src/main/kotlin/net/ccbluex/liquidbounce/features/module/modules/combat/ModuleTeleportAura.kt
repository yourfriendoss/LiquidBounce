package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.render.engine.Color4b
import net.ccbluex.liquidbounce.render.engine.RenderEngine
import net.ccbluex.liquidbounce.render.engine.Vec3
import net.ccbluex.liquidbounce.render.engine.memory.PositionColorVertexFormat
import net.ccbluex.liquidbounce.render.engine.memory.putVertex
import net.ccbluex.liquidbounce.render.utils.drawBoxNew
import net.ccbluex.liquidbounce.render.utils.drawBoxOutlineNew
import net.ccbluex.liquidbounce.utils.aiming.Rotation
import net.ccbluex.liquidbounce.utils.aiming.facingEnemy
import net.ccbluex.liquidbounce.utils.client.MC_1_8
import net.ccbluex.liquidbounce.utils.client.protocolVersion
import net.ccbluex.liquidbounce.utils.client.timer
import net.ccbluex.liquidbounce.utils.combat.TargetTracker
import net.ccbluex.liquidbounce.utils.entity.squaredBoxedDistanceTo
import net.ccbluex.liquidbounce.utils.path.findPath
import net.ccbluex.liquidbounce.utils.render.espBoxInstancedOutlineRenderTask
import net.ccbluex.liquidbounce.utils.render.espBoxInstancedRenderTask
import net.minecraft.entity.Entity
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

object ModuleTeleportAura : Module("TeleportHit", Category.COMBAT) {

    private val swing by boolean("Swing", true)
    private val range by float("Range", 100f, 8f..100f)
    private val timer by float("Timer", 1f, 0.1f..2f)
    private val steps by int("Steps", 4, 2..10)

    private val targetTracker = tree(TargetTracker())

    private var positions = ArrayList<Vec3d>()

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

            if (facingEnemy(enemy, range.toDouble(), Rotation(player.yaw, player.pitch)) && mc.options.keyAttack.isPressed) {
                findPath(enemy.x, enemy.y + 1, enemy.z, steps.toDouble()).forEach { pos ->
                    network.sendPacket(
                        PlayerMoveC2SPacket.Full(pos.x, pos.y, pos.z, player.yaw, player.pitch, false)
                    )
                    positions.add(pos)
                }
                mc.timer.timerSpeed = timer
                resetTimer = true
                attack(enemy)
            }
        }
    }

    val box = drawBoxNew(Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0), Color4b.WHITE)

    val boxOutline = drawBoxOutlineNew(Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0), Color4b.WHITE)

    val tickHandler = handler<EngineRenderEvent> {
        val instanceBuffer = PositionColorVertexFormat()
        val instanceBufferOutline = PositionColorVertexFormat()
        for (i in positions) {

            instanceBuffer.initBuffer(positions.size)
            instanceBufferOutline.initBuffer(positions.size)

            instanceBuffer.putVertex { this.position = Vec3(i); this.color = Color4b.WHITE }
            instanceBufferOutline.putVertex { this.position = Vec3(i); this.color = Color4b.WHITE }
        }

        RenderEngine.enqueueForRendering(
        RenderEngine.CAMERA_VIEW_LAYER,
        espBoxInstancedRenderTask(instanceBuffer, box.first, box.second)
        )
        RenderEngine.enqueueForRendering(
        RenderEngine.CAMERA_VIEW_LAYER,
        espBoxInstancedOutlineRenderTask(instanceBufferOutline, boxOutline.first, boxOutline.second)
        )

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
