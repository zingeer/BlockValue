package com.zingeer.blockvalue

import org.bukkit.plugin.java.JavaPlugin

class Plugin : JavaPlugin() {

    override fun onEnable() {
        BlockValueManager.enable(this)
    }

    override fun onDisable() {
        BlockValueManager.disable(this)
    }

}