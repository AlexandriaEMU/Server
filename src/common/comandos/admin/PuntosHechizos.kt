package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object PuntosHechizos {
    fun puntoshechizo(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
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
        if (infos.size > 2) //Si se espesifica nombre de personaje
        {
            target = World.getPersoByName(infos[2])
            if (target == null) {
                val str = "El personaje no esta disponible"
                SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                return
            }
        }
        target!!.addSpellPoint(pts)
        SocketManager.GAME_SEND_STATS_PACKET(target)
        val str = "Los puntos de hechizo se han modificado"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
    }
}