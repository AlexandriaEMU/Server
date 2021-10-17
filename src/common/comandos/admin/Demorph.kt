package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object Demorph {
    fun demorph(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        var objetivo: Personaje? = personaje
        if (infos.size > 1)//Si un nom de perso est spécifié
        {
            objetivo = World.getPersoByName(infos[1])
            if (objetivo == null) {
                val str = "El personaje no esta disponible"
                SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                return
            }
        }
        val morphID = objetivo!!._classe * 10 + objetivo._sexe
        objetivo._gfxID = morphID
        SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(objetivo._curCarte, objetivo._GUID)
        SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(objetivo._curCarte, objetivo)
        val str = "El jugador ha sido transformado"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
    }
}