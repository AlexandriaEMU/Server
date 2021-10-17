package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object Traer {
    fun traer (imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        val target = World.getPersoByName(infos[1])
        if (target == null) {
            val str = "El personaje no existe"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        if (target._fight != null) {
            val str = "El personaje esta en combate"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        var P: Personaje? = personaje
        if (infos.size > 2) //Si un nom de perso est spécifié
        {
            P = World.getPersoByName(infos[2])
            if (P == null) {
                val str = "El personaje no esta disponible"
                SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                return
            }
        }
        if (P!!.isOnline) {
            val mapID = P._curCarte._id
            val cellID = P._curCell.id
            target.teleport(mapID, cellID)
            val str = "El jugado ha sido teletransportado"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
        } else {
            val str = "El jugador no esta online"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
        }
    }
}