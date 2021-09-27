@file:Suppress("ReplaceSizeCheckWithIsNotEmpty")

package net.ccbluex.liquidbounce.features.module.modules.misc

import com.google.gson.JsonParser
import com.mojang.authlib.GameProfile
import net.ccbluex.liquidbounce.event.NotificationEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.repeatable
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.client.notification
import net.ccbluex.liquidbounce.utils.entity.ping
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import java.util.*


object ModuleAntiBot : Module("AntiBot", Category.MISC) {

    private var pName: String? = null

    private var canCancelEvent = false

    private val uuidNameCache = hashMapOf<UUID, String>()

    val packetHandler = handler<PacketEvent> { event ->

        when (val packet = event.packet) {
            is PlayerListS2CPacket -> {
                when (packet.action) {
                    PlayerListS2CPacket.Action.ADD_PLAYER -> {
                        for (entry in packet.entries) {
                            if (entry.profile.name.length < 3) {
                                continue
                            }

                            startChecking(entry.profile.id)

                             if (canCancelEvent) {
                                 event.cancelEvent()
                                 notification(
                                     "AntiBot",
                                     "Removed ${entry.profile.name}",
                                     NotificationEvent.Severity.INFO
                                 )
                                 continue
                             }

                             pName = entry.profile.name
                        }
                    }
                    PlayerListS2CPacket.Action.REMOVE_PLAYER -> {
                        for (entry in packet.entries) {
                            if (entry.profile.name == pName) {
                                pName = null
                            }
                        }
                    }
                    else -> {
                    }
                }
            }
        }
    }

/*    val repeatable = repeatable {
        if (pName == null) {
            return@repeatable
        }

        for (entity in world.entities) {
            if (entity is PlayerEntity && entity.entityName == pName) {
                if (!isArmored(entity) || entity.ping < 2) {
                    pName = null
                }

                if (pName != null) {
                    world.removeEntity(entity.id, Entity.RemovalReason.DISCARDED)
                    notification("AntiBot", "Removed $pName", NotificationEvent.Severity.INFO)
                    pName = null
                }
                break
            }
        }
    }*/

    private fun isADuplicate(name: String): Boolean {
        return network.playerList.count { it.profile.name == name } > 0
    }

    private fun isArmored(entity: PlayerEntity): Boolean {
        for (i in 0..3) {
            return !entity.inventory.getArmorStack(i).isEmpty
        }
        return false
    }

    fun getUsername(uuid: UUID): String? {
        val client = HttpClients.createDefault()
        val request = HttpGet("https://api.mojang.com/user/profiles/${uuid}/names")
        val response = client.execute(request)

        if (response.statusLine.statusCode != 200) {
            return null
        }

        val names = JsonParser().parse(EntityUtils.toString(response.entity)).asJsonArray

        return names.get(names.size() - 1).asJsonObject.get("name").asString
    }


    fun startChecking(uuid: UUID) {
        uuidNameCache.clear()

        network.playerUuids.add(uuid)

        for (id in network.playerUuids) {
            if (id != uuid) {
                uuidNameCache[id] = getUsername(id)!!
            }
        }
        chat (uuidNameCache.toString())

        if (uuidNameCache.contains(uuid) && uuidNameCache.containsValue(getUsername(uuid))) {
            chat("$uuid of ${getUsername(uuid)}")
            for (entity in world.entities) {
                if (entity is PlayerEntity && entity.entityName == getUsername(uuid)) {
                    chat("yes??????")
                    if (isArmored(entity)) {
                        chat("te")
                    }
                }
            }
        }
    }
}
