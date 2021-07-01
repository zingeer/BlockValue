package com.github.zingeer.blockvalue

import com.github.rqbik.bukkt.extensions.event.register
import com.github.rqbik.bukkt.extensions.event.unregister
import com.github.zingeer.blockvalue.world.BlockValueListener
import com.github.zingeer.blockvalue.world.BlockValueWorld
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

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
        BlockValueFactory.registry(object : BlockValueConverter<ItemStack> {
            override fun serialize(src: ItemStack): ByteBuf {
                return Unpooled.buffer().writeBytes(src.serializeAsBytes())
            }

            override fun deserialize(byteBuf: ByteBuf): ItemStack {
                return ItemStack.deserializeBytes(byteBuf.array())
            }
        }, ItemStack::class.java)
    }

    var Location.value: Any?
        get() = BlockValueWorld[this.world].loadChunk(chunk).getBlockValue(this)?.value
        set(value) {
            BlockValueWorld[this.world].loadChunk(chunk).setBlockValue(this, value)
        }

}