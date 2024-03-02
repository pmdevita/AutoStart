package net.pdevita.autostart.events

import com.github.shynixn.mccoroutine.bungeecord.launch
import net.md_5.bungee.api.Callback
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.ServerPing
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.Server
import net.md_5.bungee.api.event.*
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.scheduler.TaskScheduler
import net.md_5.bungee.event.EventHandler
import net.pdevita.autostart.AutoStart
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import kotlin.collections.HashMap
import net.md_5.bungee.api.event.ProxyPingEvent



class Events(var main: AutoStart, var logger: Logger, var scheduler: TaskScheduler, val proxy: ProxyServer) : Listener {
    private var players = HashSet<LoginEvent>()

    @EventHandler
    fun onLoginEvent(event: LoginEvent) {
        logger.info("Player connected, is server up? ${this.main.checker.serverUp.toString()}")
        // Only if the server is down do we act
        if (!this.main.checker.serverUp) {
            event.registerIntent(this.main) // Block the event from proceeding
            players.add(event)
            logger.info("LoginEvent")
            main.launch {
                main.checker.turnOn()
            }
        }
    }

//    @EventHandler
//    suspend fun onLoginEvent(event: LoginEvent) {
//        logger.info("Player connected, is server up? ${this.main.checker.serverUp.toString()}")
//        // Only if the server is down do we act
//        if (!this.main.checker.serverUp) {
//            players.add(event)
//            logger.info("LoginEvent")
//            this.main.checker.turnOn()
//        }
//    }

    @EventHandler
    fun onPlayerDisconnect(event: PlayerDisconnectEvent) {
        this.main.checker.turnOffCheck()
    }

    @EventHandler
    fun onServerStatusChange(event: ServerStatusChange) {
        // The check has completed. Dispatch everyone
        for (player in players) {
            player.completeIntent(this.main)
        }
        players.clear()
    }

    @EventHandler
    fun onProxyPing(event: ProxyPingEvent) {
        // Experiment to change MOTD displayed on server browser, doesn't work
        if (main.checker.serverUp) {
            event.response.descriptionComponent = TextComponent("ptrharmonic server!\nServer is on!")
        } else {
            event.response.descriptionComponent = TextComponent("ptrharmonic server!\nServer is off, connect to turn on!")
        }
    }

}