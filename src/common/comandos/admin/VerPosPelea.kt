package common.comandos.admin

import common.CryptManager
import common.SocketManager
import objects.Personaje
import java.io.PrintWriter

object VerPosPelea {
    fun pospelea(imprimir: PrintWriter, personaje: Personaje) {
        var mensaje = StringBuilder("Lista celdas de inicio [teamID][cellID]:")
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mensaje.toString())
        val places: String = personaje._curCarte._placesStr
        if (places.indexOf('|') == -1 || places.length < 2) {
            mensaje = StringBuilder("Las celdas de pelea no estan definidas")
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mensaje.toString())
            return
        }
        var team0 = ""
        var team1 = ""
        val p = places.split("\\|".toRegex()).toTypedArray()
        try {
            team0 = p[0]
        } catch (e: Exception) {
        }
        try {
            team1 = p[1]
        } catch (e: Exception) {
        }
        mensaje = StringBuilder("Team 0:\n")

        var a = 0
        while (a <= team0.length - 2) {
            val code = team0.substring(a, a + 2)
            mensaje.append(CryptManager.cellCode_To_ID(code))
            a += 2
        }

        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mensaje.toString())
        mensaje = StringBuilder("Team 1:\n")

        a = 0
        while (a <= team1.length - 2) {
            val code = team1.substring(a, a + 2)
            mensaje.append(CryptManager.cellCode_To_ID(code)).append(" , ")
            a += 2
        }

        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mensaje.toString())
    }
}