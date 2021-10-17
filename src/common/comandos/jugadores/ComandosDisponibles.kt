package common.comandos.jugadores

import common.Main
import common.SocketManager
import objects.Personaje

object ComandosDisponibles {
    fun comandosdisponibles(personaje: Personaje){
        Main.CONFIG_MOTD_COLOR?.let {
            SocketManager.GAME_SEND_MESSAGE(
                personaje, """
         Comandos disponibles: 
         .start
         .infos
         .save
         """.trimIndent(), it
            )
        }
    }
}