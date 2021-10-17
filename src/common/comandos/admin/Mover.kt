package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object Mover {
    fun mover(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        var mapID: Short = -1
        var cellID = -1
        try {
            mapID = infos[1].toShort()
            cellID = infos[2].toInt()
        } catch (e: Exception) {
        }
        if (mapID.toInt() == -1 || cellID == -1 || World.getCarte(mapID) == null) {
            val str = "MapID o CelID invalidas"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        if (World.getCarte(mapID).getCase(cellID) == null) {
            val str = "MapID o CelID invalidas"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        var target: Personaje? = personaje
        if (infos.size > 3) //Si un nom de perso est spécifié
        {
            target = World.getPersoByName(infos.get(3))
            if (target == null || target._fight != null) {
                val str = "El personaje que quieres teletransportar esta en combate"
                SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                return
            }
        }
        target!!.teleport(mapID, cellID)
        val str = "El jugador ha sido teletransportado"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
    }
}