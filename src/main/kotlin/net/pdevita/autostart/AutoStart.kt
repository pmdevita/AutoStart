package net.pdevita.autostart

import net.md_5.bungee.api.connection.Server
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.pdevita.autostart.events.Events
import java.io.File
import java.nio.file.Files
import net.md_5.bungee.config.YamlConfiguration
import java.io.IOException


class AutoStart : Plugin {
    lateinit var checker: ServerChecker
    lateinit var configuration: Configuration

    constructor(): super()

    override fun onEnable() {
        saveDefaultConfig()
        configuration = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(File(dataFolder, "config.yml"))

        proxy.pluginManager.registerListener(this, Events(this, logger, proxy.scheduler, proxy))
        this.checker = ServerChecker(this, logger, proxy)
    }

    private fun saveDefaultConfig() {
        if (!dataFolder.exists())
            dataFolder.mkdir()

        val file = File(dataFolder, "config.yml")

        if (!file.exists()) {
            try {
                getResourceAsStream("config.yml").use { `in` -> Files.copy(`in`, file.toPath()) }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

}


