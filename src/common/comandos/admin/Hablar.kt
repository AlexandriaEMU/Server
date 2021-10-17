package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object Hablar {
    fun hablar (imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        var perso: Personaje? = personaje

        var name: String? = null
        try {
            name = infos[1]
        } catch (e: Exception) {
        }

        perso = World.getPersoByName(name)
        if (perso == null) {
            val mess = "El personaje no existe"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
            return
        }

        perso._compte.mute(false, 0)
        var mess = "Usted deja que " + perso._name + " vuelva a hablar"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)

        if (!perso.isOnline) {
            mess = "(El personaje " + perso._name + " no esta conectado)"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
        }
    }
}