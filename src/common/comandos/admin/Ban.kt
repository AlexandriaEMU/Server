package common.comandos.admin

import common.SQLManager
import common.SocketManager
import common.World
import java.io.PrintWriter

object Ban {
    fun ban(imprimir: PrintWriter, infos:Array<String>){
        val personaje = World.getPersoByName(infos[1])
        if (personaje == null) {
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "El personaje no esta disponible")
            return
        }
        if (personaje._compte == null) {
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Error")
            return
        }
        personaje._compte.isBanned = true
        SQLManager.UPDATE_ACCOUNT_DATA(personaje._compte)
        if (personaje._compte.gameThread != null) personaje._compte.gameThread.kick()
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Usted ha baneado a " + personaje._name)
    }
}