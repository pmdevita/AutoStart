package net.pdevita.autostart.events

import net.md_5.bungee.api.plugin.Event

const val UP = 0
const val NO_RESPONSE = 1
const val DOWN = 2

class ServerStatusChange(val status: Int) : Event() {
}
