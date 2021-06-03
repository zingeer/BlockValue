package com.zingeer.blockvalue.world

import org.bukkit.Location
import org.bukkit.block.Block

data class BlockValueChunk(
    val x: Int,
    val z: Int,
    var blocks: HashSet<BlockValue> = hashSetOf(),
) {

    fun getBlockValue(location: Location): BlockValue? {
        blocks.forEach { if (it.location == location) return it }
        return null
    }

    fun getBlockValue(block: Block): BlockValue? = getBlockValue(block.location)

    fun setBlockValue(location: Location, value: Any?) {
        blocks.forEach { if (it.location == location) it.value = value }
        blocks.add(BlockValue(location, value))
    }

    fun setBlockValue(block: Block, value: Any?) = setBlockValue(block.location, value)
}