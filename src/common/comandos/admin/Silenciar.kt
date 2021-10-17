package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object Silenciar {
    fun silenciar(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        var perso: Personaje? = personaje
        var name: String? = null
        try {
            name = infos[1]
        } catch (e: Exception) {
        }
        var time = 0
        try {
            time = infos[2].toInt()
        } catch (e: Exception) {
        }

        perso = World.getPersoByName(name)
        if (perso == null || time < 0) {
            val mess = "El personaje no existe"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
            return
        }
        var mess = "Usted silencio a " + perso._name + " por " + time + " segundos"
        if (perso._compte == null) {
            mess = "(El personaje " + perso._name + " no esta conectado)"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
            return
        }
        perso._compte.mute(true, time)
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)

        if (!perso.isOnline) {
            mess = "(El personaje " + perso._name + " no esta conectado)"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
        } else {
            SocketManager.GAME_SEND_Im_PACKET(perso, "1124;$time")
        }
    }
}