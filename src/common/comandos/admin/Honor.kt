package common.comandos.admin

import common.Constants
import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object Honor {
    fun honor(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        var honor = 0
        try {
            honor = infos[1].toInt()
        } catch (e: Exception) {
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
        var str = "Usted le ha dado " + honor + " honor a " + target!!._name
        if (target._align.toInt() == Constants.ALIGNEMENT_NEUTRE) {
            str = "El jugador es NEUTRAL"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        if (honor < 0) target.remHonor(honor) else target.addHonor(honor)
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
    }
}