package common.comandos.admin

import common.Main
import common.SocketManager
import objects.Personaje

object NAnuncio {
    fun nanuncio(msg: String,personaje:Personaje){
        val infos = msg.split(" ".toRegex(), 2).toTypedArray()
        val prefix = "[" + personaje._name + "]"
        Main.CONFIG_MOTD_COLOR?.let { SocketManager.GAME_SEND_MESSAGE_TO_ALL(prefix + infos[1], it) }
    }
}