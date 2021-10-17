package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object Ir {
    fun ir(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        val P = World.getPersoByName(infos[1])
        if (P == null) {
            val str = "El personaje no existe"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        val mapID = P._curCarte._id
        val cellID = P._curCell.id

        var target: Personaje? = personaje
        if (infos.size > 2) //Si un nom de perso est spécifié
        {
            target = World.getPersoByName(infos[2])
            if (target == null) {
                val str = "El personaje no esta disponible"
                SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                return
            }
            if (target._fight != null) {
                val str = "El personaje esta en combate"
                SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                return
            }
        }
        target!!.teleport(mapID, cellID)
        val str = "Has sido teletransportado hacia el personaje"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
    }
}