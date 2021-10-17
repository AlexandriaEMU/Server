package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object IrMapa {
    fun irmapa (imprimir: PrintWriter, personaje: Personaje, infos:Array<String>) {
        var mapX = 0
        var mapY = 0
        var cellID = 0
        var contID = 0 //Par défaut Amakna

        try {
            mapX = infos[1].toInt()
            mapY = infos[2].toInt()
            cellID = infos[3].toInt()
            contID = infos[4].toInt()
        } catch (e: Exception) {
        }
        val map = World.getCarteByPosAndCont(mapX, mapY, contID)
        if (map == null) {
            val str = "Coordenadas o continente invalido"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        if (map.getCase(cellID) == null) {
            val str = "CelID invalida"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        var target: Personaje? = personaje
        if (infos.size > 5) //Si un nom de perso est spécifié
        {
            target = World.getPersoByName(infos.get(5))
            if (target == null || target._fight != null) {
                val str = "El personaje a teletransportar esta en combate"
                SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                return
            }
            if (target._fight != null) {
                val str = "El personaje esta en combate"
                SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                return
            }
        }
        target?.teleport(map._id, cellID) // Por si las moscas xD...
        val str = "El jugador ha sido teletransportado"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
    }
}