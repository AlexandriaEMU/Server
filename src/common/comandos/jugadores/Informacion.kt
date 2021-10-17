package common.comandos.jugadores

import common.Main
import common.SocketManager
import objects.Personaje

object Informacion {
    fun info(personaje: Personaje){
        var online = System.currentTimeMillis() - Main.gameServer!!.startTime
        val dias = (online / (1000 * 3600 * 24)).toInt()
        online %= (1000 * 3600 * 24).toLong()
        val horas = (online / (1000 * 3600)).toInt()
        online %= (1000 * 3600).toLong()
        val minutos = (online / (1000 * 60)).toInt()
        online %= (1000 * 60).toLong()
        val segundos = (online / 1000).toInt()

        val mensaje = """
            ====================================
            AlexandriaEMU - http://rltech.click
            ====================================
            Tiempo en linea: ${dias}D ${horas}H ${minutos}M ${segundos}S
            Jugadores en linea: ${Main.gameServer!!.playerNumber}
            Maximos conectados: ${Main.gameServer!!.maxPlayer}
            ====================================
            """.trimIndent()
        Main.CONFIG_MOTD_COLOR?.let { SocketManager.GAME_SEND_MESSAGE(personaje, mensaje, it) }
    }
}