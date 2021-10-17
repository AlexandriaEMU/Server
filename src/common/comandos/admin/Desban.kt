package common.comandos.admin

import common.SQLManager
import common.SocketManager
import common.World
import java.io.PrintWriter

object Desban {
    fun desban(imprimir: PrintWriter, infos:Array<String>){
        val personaje = World.getPersoByName(infos[1])
        if (personaje == null) {
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "El personaje no esta disponible")
            return
        }
        if (personaje._compte == null) {
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Error")
            return
        }
        personaje._compte.isBanned = false
        SQLManager.UPDATE_ACCOUNT_DATA(personaje._compte)
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Usted ha desbaneado a " + personaje._name)
    }
}