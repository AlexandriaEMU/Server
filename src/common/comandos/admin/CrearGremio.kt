package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object CrearGremio {
    fun creargremio(imprimir: PrintWriter, personaje: Personaje,infos:Array<String>){
        var personaje: Personaje? = personaje
        if (infos.size > 1) {
            personaje = World.getPersoByName(infos[1])
        }
        if (personaje == null) {
            val mess = "El personaje no puede crear gremio en este momento"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
            return
        }

        if (!personaje.isOnline) {
            val mess = "El personaje " + personaje._name + " necesita estar conectado"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
            return
        }
        if (personaje._guild != null || personaje.guildMember != null) {
            val mess = "El personaje " + personaje._name + " ya es parte de un gremio"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
            return
        }
        SocketManager.GAME_SEND_gn_PACKET(personaje)
        val mess = personaje._name + ": Abre el panel creacion de gremios"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
    }
}