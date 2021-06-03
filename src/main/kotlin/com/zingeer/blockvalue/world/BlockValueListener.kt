package com.zingeer.blockvalue.world

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.event.world.WorldSaveEvent
import org.bukkit.event.world.WorldUnloadEvent

object BlockValueListener : Listener {

    @EventHandler
    fun loadChunkEvent(event: ChunkLoadEvent) {
        val world = event.world
        val chunk = event.chunk

        BlockValueWorld[world].loadChunk(chunk)
    }

    @EventHandler
    fun unloadChunkEvent(event: ChunkUnloadEvent) {
        if (event.isSaveChunk) {
            val world = event.world
            val chunk = event.chunk

            BlockValueWorld[world].unloadChunk(chunk.x shr 5, chunk.z shr 5)
        }
    }

    @EventHandler
    fun saveWorldEvent(event: WorldSaveEvent) {
        val activeWorld = BlockValueWorld[event.world]

        activeWorld.getLoadedChunks().forEach {
            activeWorld.saveChunk(it)
        }
    }

    @EventHandler
    fun worldUnloadEvent(event: WorldUnloadEvent) {
        BlockValueWorld[event.world].unloadWorld()
    }
}