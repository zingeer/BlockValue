package com.github.zingeer.blockvalue.world

import com.github.zingeer.blockvalue.BlockValueFactory
import com.github.zingeer.blockvalue.BlockValueManager
import com.github.zingeer.blockvalue.utils.Compression
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.slf4j.LoggerFactory
import java.io.File


class BlockValueWorld(
    val worldName: String,
) {

    val world: World
        get() = Bukkit.getWorld(worldName)!!

    private var LOADED_CHUNKS = HashMap<Pair<Int, Int>, BlockValueChunk>()

    fun getLoadedChunks() = mutableListOf<BlockValueChunk>().apply { addAll(LOADED_CHUNKS.values) }

    @OptIn(DelicateCoroutinesApi::class)
    fun saveChunk(chunk: BlockValueChunk) {
        LoggerFactory.getLogger("BlockValue").info("PreSave chunk - ${chunk.x} ${chunk.z}")
        val fileDir = File(Bukkit.getWorldContainer(), "${world.name}/block_value/chunks")
        val file = File(fileDir, "с.${chunk.x}.${chunk.z}.bv")

        if (!fileDir.exists()) fileDir.mkdirs()
        if (!file.exists()) file.createNewFile()
        if (chunk.blocks.isNullOrEmpty()) {
            file.delete()
            return
        }

        val buf = Unpooled.buffer()

        buf.writeInt(chunk.blocks.size)

        chunk.blocks.forEach { block ->
            val value = block.value ?: return@forEach
            buf.writeDouble(block.location.x)
            buf.writeDouble(block.location.y)
            buf.writeDouble(block.location.z)
            val blockValue = BlockValueFactory.serialize(value)
            val array = blockValue.array()
            buf.writeInt(array.size)
            buf.writeBytes(array)
        }
        LoggerFactory.getLogger("BlockValue").info("Save chunk - ${chunk.x} ${chunk.z}")
        GlobalScope.launch {
            file.writeBytes(Compression.compress(ByteBufUtil.getBytes(buf)))
        }
    }

    fun loadChunk(chunk: Chunk): BlockValueChunk {
        val x = chunk.x shr 5
        val z = chunk.z shr 5
        return LOADED_CHUNKS[x to z] ?: run {
            val fileDir = File(Bukkit.getWorldContainer(), "${world.name}/block_value/chunks")
            val file = File(fileDir, "с.${x}.${z}.bv")

            if (!fileDir.exists()) fileDir.mkdirs()

            val blocks = hashSetOf<BlockValue>()

            if (file.exists()) {
                val buf = Unpooled.buffer()
                buf.writeBytes(Compression.decompress(file.readBytes()))
                val blocksSize = buf.readInt()

                repeat(blocksSize) {
                    val blockX = buf.readDouble()
                    val blockY = buf.readDouble()
                    val blockZ = buf.readDouble()
                    val size = buf.readInt()
                    blocks.add(BlockValue(Location(world, blockX, blockY, blockZ),
                        BlockValueFactory.deserialize(buf.readBytes(size))))
                }
            }
            val blockValueChunk = BlockValueChunk(x, z, blocks)
            if (chunk.isLoaded) {
                LOADED_CHUNKS[z to x] = blockValueChunk
            } else {
                saveChunk(blockValueChunk)
            }
            blockValueChunk
        }
    }

    fun unloadChunk(x: Int, z: Int) {
        val chunk = LOADED_CHUNKS.remove(x shr 5 to (z shr 5)) ?: return
        saveChunk(chunk)
        //this code sosalca
//        val iterator = LOADED_CHUNKS.entries.iterator()
//
//        while(iterator.hasNext()) {
//            val value = iterator.next()
//            if (value.key == x shr 5 to (z shr 5)) {
//                iterator.remove()
//            }
//        }

        if (LOADED_CHUNKS.isEmpty()) unloadWorld()
    }

    fun unloadChunk(chunk: BlockValueChunk) {
        unloadChunk(chunk.x, chunk.z)
    }

    fun unloadWorld() {
        if (LOADED_CHUNKS.isNotEmpty()) {
            LOADED_CHUNKS.forEach {
                unloadChunk(it.value)
            }
        }
        BlockValueManager.WORLDS.removeIf { it == this }
    }

    companion object {
        operator fun get(world: World): BlockValueWorld = get(world.name)

        operator fun get(worldName: String): BlockValueWorld {
            BlockValueManager.WORLDS.forEach {
                if (it.worldName == worldName) return it
            }

            return BlockValueWorld(worldName).apply {
                BlockValueManager.WORLDS.add(this)
            }
        }
    }
}