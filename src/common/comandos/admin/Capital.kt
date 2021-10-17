package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object Capital {
    fun capital(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        var pts = -1
        try {
            pts = infos[1].toInt()
        } catch (e: Exception) {
        }
        if (pts == -1) {
            val str = "Valor invalido"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        var target: Personaje? = personaje
        if (infos.size > 2) //Si un nom de perso est spécifié
        {
            target = World.getPersoByName(infos[2])
            if (target == null) {
                val str = "El personaje no esta disponible"
                SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                return
            }
        }
        target!!.addCapital(pts)
        SocketManager.GAME_SEND_STATS_PACKET(target)
        val str = "El capital del personaje se ha modificado con exito"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
    }
}