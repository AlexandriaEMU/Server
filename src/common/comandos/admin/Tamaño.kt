package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object Tamaño {
    fun tamaño(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        var size = -1
        try {
            size = infos[1].toInt()
        } catch (e: Exception) {
        }
        if (size == -1) {
            val str = "Tamaño invalido"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        var target: Personaje? = personaje
        if (infos.size > 2) //Si un nom de perso est spécifié
        {
            target = World.getPersoByName(infos[2])
            if (target == null) {
                val str = "El personaje no esta disponible"
                SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                return
            }
        }
        target!!._size = size
        SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(target._curCarte, target._GUID)
        SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(target._curCarte, target)
        val str = "El temaño del personaje se ha modificado con exito"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
    }
}