package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object AprenderHechizo {
    fun aprenderhechizo(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        var spell = -1
        try {
            spell = infos[1].toInt()
        } catch (e: Exception) {
        }
        if (spell == -1) {
            val str = "Valor invalido"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        var target: Personaje? = personaje
        if (infos.size > 2) //Si se espesifica un personaje
        {
            target = World.getPersoByName(infos[2])
            if (target == null) {
                val str = "El personaje no esta disponible"
                SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                return
            }
        }

        target!!.learnSpell(spell, 1, true, true)

        val str = "El hechizo se ha aprendido con exito"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
    }
}