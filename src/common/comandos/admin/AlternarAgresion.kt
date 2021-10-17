package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object AlternarAgresion {
    fun alternaragresiones(imprimir: PrintWriter, personaje: Personaje,infos:Array<String>){
        var perso: Personaje? = personaje

        var name: String? = null
        try {
            name = infos[1]
        } catch (e: Exception) {
        }

        perso = World.getPersoByName(name)

        if (perso == null) {
            val mess = "El personaje no esta disponible."
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
            return
        }

        perso.set_canAggro(!perso.canAggro())
        var mess = perso._name
        mess += if (perso.canAggro()) " ahora puede ser agredido" else " ya no puede ser agredido"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)

        if (!perso.isOnline) {
            mess = "(El personaje " + perso._name + " no esta conectado)"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
        }
    }
}