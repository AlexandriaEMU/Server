package common.comandos.admin

import common.Main
import common.SocketManager
import java.io.PrintWriter

object Informacion {

    fun info(imprimir: PrintWriter) {
        assert(Main.gameServer != null)
        var tiempo: Long = System.currentTimeMillis() - Main.gameServer?.startTime!!
        val dias = (tiempo / (1000 * 3600 * 24)).toInt()
        tiempo %= (1000 * 3600 * 24).toLong()
        val horas = (tiempo / (1000 * 3600)).toInt()
        tiempo %= (1000 * 3600).toLong()
        val minutos = (tiempo / (1000 * 60)).toInt()
        tiempo %= (1000 * 60).toLong()
        val segundos = (tiempo / 1000).toInt()
        val mensaje = """
             ======================================
             AlexandriaEMU - http://rltech.click
             ======================================
             Tiempo en linea: ${dias}D ${horas}H ${minutos}M ${segundos}S
             Jugadores en linea: ${Main.gameServer?.playerNumber}
             Maximos conectados: ${Main.gameServer?.maxPlayer}
             ======================================
             """.trimIndent()
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mensaje)
    }
}