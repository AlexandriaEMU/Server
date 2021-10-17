package common.comandos.admin

import common.Main
import common.SocketManager

object Anuncio {
    fun anuncio(mensaje: String){
        val infos = mensaje.split(" ".toRegex(), 2).toTypedArray()
        SocketManager.GAME_SEND_MESSAGE_TO_ALL(infos[1], Main.CONFIG_MOTD_COLOR)
    }
}