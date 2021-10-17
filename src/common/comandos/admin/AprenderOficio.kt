package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object AprenderOficio {
    fun aprenderoficio(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        var job = -1
        try {
            job = infos[1].toInt()
        } catch (e: Exception) {
        }
        if (job == -1 || World.getMetier(job) == null) {
            val str = "Valor invalido"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        var target: Personaje? = personaje
        if (infos.size > 2) //Si espesifica un personaje
        {
            target = World.getPersoByName(infos[2])
            if (target == null) {
                val str = "El personaje no esta disponible"
                SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                return
            }
        }

        target!!.learnJob(World.getMetier(job))

        val str = "El oficio se ha aprendido"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
    }
}