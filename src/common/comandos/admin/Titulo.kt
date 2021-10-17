package common.comandos.admin

import common.SQLManager
import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object Titulo {
    fun titulo(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        var target: Personaje? = null
        var tituloid: Byte = 0
        try {
            target = World.getPersoByName(infos[1])
            tituloid = infos[2].toByte()
        } catch (e: Exception) {
        }

        if (target == null) {
            val str = "El personaje no esta disponible"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }

        target._title = tituloid
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Titulo agregado con exito.")
        SQLManager.SAVE_PERSONNAGE(target, false)
        if (target._fight == null) SocketManager.GAME_SEND_ALTER_GM_PACKET(target._curCarte, target)
    }
}