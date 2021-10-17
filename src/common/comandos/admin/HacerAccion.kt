package common.comandos.admin

import common.SocketManager
import common.World
import objects.Action
import objects.Personaje
import java.io.PrintWriter

object HacerAccion {
    fun haceraccion (imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){

        //HACERACCION NAME TYPE ARGS COND
        if (infos.size < 4) {
            val mess = "Nombre o argumento incorrecto"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
            return
        }
        var tipo = -100
        var argumento = ""
        var condicion = ""
        var personaje: Personaje? = personaje
        try {
            personaje = World.getPersoByName(infos[1])
            if (personaje == null) personaje = personaje
            tipo = infos[2].toInt()
            argumento = infos[3]
            if (infos.size > 4) condicion = infos[4]
        } catch (e: Exception) {
            val mess = "Agumento del comando incorrecto"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
            return
        }
        Action(tipo, argumento, condicion).apply(personaje, null, -1, -1)
        val mess = "Accion efectuada con exito"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)

    }
}