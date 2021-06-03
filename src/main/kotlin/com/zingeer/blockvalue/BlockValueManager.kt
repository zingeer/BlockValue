package com.zingeer.blockvalue

import com.github.rqbik.bukkt.extensions.event.register
import com.github.rqbik.bukkt.extensions.event.unregister
import com.zingeer.blockvalue.world.BlockValueListener
import com.zingeer.blockvalue.world.BlockValueWorld
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import java.nio.charset.Charset

object BlockValueManager {

    internal val WORLDS = HashSet<BlockValueWorld>()

    fun enable(plugin: Plugin) {
        BlockValueListener.register(plugin)
        loadDefaultConverter()
    }

    fun disable(plugin: Plugin) {
        BlockValueListener.unregister()
        WORLDS.forEach { blockValueWorld ->
            blockValueWorld.getLoadedChunks().forEach { blockValueChunk ->
                blockValueWorld.unloadChunk(blockValueChunk)
            }
        }
    }

    private fun loadDefaultConverter() {
        BlockValueFactory.registry(object : BlockValueConverter<Int> {
            override fun serialize(src: Int): ByteBuf {
                return Unpooled.buffer().writeInt(src)
            }

            override fun deserialize(byteBuf: ByteBuf): Int {
                return byteBuf.readInt()
            }
        }, Integer::class.java)
            .registry(object : BlockValueConverter<String> {
                override fun serialize(src: String): ByteBuf {
                    val string = src.encodeToByteArray()
                    return Unpooled.buffer().writeBytes(string)
                }

                override fun deserialize(byteBuf: ByteBuf): String {
                    return String(ByteBufUtil.getBytes(byteBuf), Charset.forName("UTF-8"))
                }
            }, String::class.java)
            .registry(object : BlockValueConverter<Double> {
                override fun serialize(src: Double): ByteBuf {
                    return Unpooled.buffer().writeDouble(src)
                }

                override fun deserialize(byteBuf: ByteBuf): Double {
                    return byteBuf.readDouble()
                }
            }, Double::class.java)
    }

    var Location.value: Any?
        get() = BlockValueWorld[this.world].loadChunk(chunk).getBlockValue(this)?.value
        set(value) {
            BlockValueWorld[this.world].loadChunk(chunk).setBlockValue(this, value)
        }

}