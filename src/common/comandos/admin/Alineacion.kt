package common.comandos.admin

import common.Constants
import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object Alineacion {
    fun alineacion(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        var alineacion: Byte = -1
        try {
            alineacion = infos[1].toByte()
        } catch (e: Exception) {
        }
        if (alineacion < Constants.ALIGNEMENT_NEUTRE || alineacion > Constants.ALIGNEMENT_MERCENAIRE) {
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

        target!!.modifAlignement(alineacion)

        val str = "La alineacion del personaje se ha modificado con exito"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
    }
}