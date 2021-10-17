package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object XpOficios {
    fun xpoficio(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        var job = -1
        var xp = -1
        try {
            job = infos[1].toInt()
            xp = infos[2].toInt()
        } catch (e: Exception) {
        }
        if (job == -1 || xp < 0) {
            val str = "Valor invalido"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        var target: Personaje? = personaje
        if (infos.size > 3) //Si se espesifica un personaje
        {
            target = World.getPersoByName(infos[3])
            if (target == null) {
                val str = "El personaje no esta disponible"
                SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                return
            }
        }
        val SM = target!!.getMetierByID(job)
        if (SM == null) {
            val str = "El jugador no tiene el oficio espesificado"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }

        SM.addXp(target, xp.toLong())

        val str = "Se ha agregado experiencia al oficio"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
    }
}