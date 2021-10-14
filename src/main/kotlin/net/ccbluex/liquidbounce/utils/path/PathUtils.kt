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

package net.ccbluex.liquidbounce.utils.path

import net.ccbluex.liquidbounce.utils.client.mc
import net.minecraft.util.math.Vec3d
import kotlin.math.ceil
import kotlin.math.sqrt

fun findPath(tpX: Double, tpY: Double, tpZ: Double, offset: Double): ArrayList<Vec3d> {
    val positions = ArrayList<Vec3d>()
    val steps = ceil(getDistance(mc.player!!.x, mc.player!!.y, mc.player!!.z, tpX, tpY, tpZ) / offset).toInt()

    val diffX = tpX - mc.player!!.x
    val diffY = tpY - mc.player!!.y
    val diffZ = tpZ - mc.player!!.z

    for (step in 1 until steps) {
        positions.add(Vec3d(mc.player!!.x + (diffX * step) / steps, mc.player!!.y + (diffY * step) / steps, mc.player!!.z + (diffZ * step) / steps))
    }
    return positions
}

fun getDistance(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Double {
    val diffX = x1 - x2
    val diffY = y1 - y2
    val diffZ = z1 - z2
    return sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ)
}
