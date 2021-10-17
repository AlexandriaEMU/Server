package common.comandos.admin

import common.SocketManager
import objects.Personaje
import java.io.PrintWriter

object ActualizarMobs {


    fun actualizarmobs(imprimir: PrintWriter,personaje: Personaje) {
        personaje._curCarte.refreshSpawns()
        val mensaje = "Mobs actualizados con exito."
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mensaje)
    }
}