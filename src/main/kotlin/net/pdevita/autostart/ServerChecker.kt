package net.pdevita.autostart

import com.github.shynixn.mccoroutine.bungeecord.launch
import kotlinx.coroutines.delay
import net.md_5.bungee.api.Callback
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.ServerPing
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.Server
import net.pdevita.autostart.events.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Logger
import kotlin.math.log
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.math.ceil
import kotlin.reflect.typeOf


class ServerChecker(val main: AutoStart, val logger: Logger, val proxy: ProxyServer) {
    var serverUp = false
    private var checking = AtomicBoolean(false) // Prevents more than one check going on at once
    private var timesToCheck = ceil(main.configuration.getDouble("delay") / 5).toInt()

    suspend fun turnOn() {
        // Run only one check at a time
        if (!this.checking.getAndSet(true)) {
            main.logger.info("Detecting if server is on...")
            // Only check if we know the server isn't on
            if (!serverUp) {
                // Dispatch a ping for every known server
                // This creates a callback chain for each

                for (server in ProxyServer.getInstance().servers) {
                    main.logger.info("Pinging server ${server.value}")
                    server.value.ping { serverPing, _ ->
                        main.launch {
                            callbackHandler(serverPing, server.value, timesToCheck)
                        }
                    }
                }
            }
        } else {
            main.logger.info("Waiting for current server on check to complete...")
        }
    }

    fun turnOffCheck() {
        if (proxy.onlineCount <= 1) {
            this.serverUp = false
            proxy.pluginManager.callEvent(ServerStatusChange(DOWN))
        }
    }

    suspend fun callbackHandler(ping: ServerPing?, server: ServerInfo, times: Int) {
        main.logger.info("callback")
        val status = ping != null
        if (status) {   // If the server is up, dispatch the event and stop checking
            logger.info("Server up!")
            proxy.pluginManager.callEvent(ServerStatusChange(UP))
            this.checking.set(false)
        } else {    // If it is down
            if (times == timesToCheck) {   // First callback returned, server is indeed down
                logger.info("Launching server...")
                val process = ProcessBuilder(main.configuration.getString("command").split(" "))
                        .directory(File(main.configuration.getString("directory")))
                        .redirectInput(ProcessBuilder.Redirect.INHERIT)
                        .redirectError(ProcessBuilder.Redirect.INHERIT).start()
            }
            if (times > 0) { // If we have more tries left
                logger.info("Server down, checking in 5")
                logger.info(times.toString())
                delay(5000)
                server.ping { serverPing, _ ->
                    main.launch {
                        callbackHandler(serverPing, server, times - 1)
                    }
                }
            } else {    // We are out of retries and the server is down
                logger.info("Server timed out")
                proxy.pluginManager.callEvent(ServerStatusChange(NO_RESPONSE))
                this.checking.set(false)
            }
        }
        // TODO: Not safe, could cause problems with multiple servers
        this.serverUp = status
    }
}
