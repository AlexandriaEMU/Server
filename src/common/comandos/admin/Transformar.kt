package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object Transformar {
    fun transformar(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        var morphID = -1
        try {
            morphID = infos[1].toInt()
        } catch (e: Exception) {
        }
        if (morphID == -1) {
            val str = "ID transformacion invalida"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        var target: Personaje? = personaje
        if (infos.size > 2) //Si un nom de perso est sp�cifi�
        {
            target = World.getPersoByName(infos[2])
            if (target == null) {
                val str = "El personaje no esta disponible"
                SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                return
            }
        }
        target!!._gfxID = morphID
        SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(target._curCarte, target._GUID)
        SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(target._curCarte, target)
        val str = "El personaje se ha transformado"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
    }
}