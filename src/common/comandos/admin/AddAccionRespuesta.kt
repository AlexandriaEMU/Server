package common.comandos.admin

import common.SQLManager
import common.SocketManager
import common.World
import objects.Action
import java.io.PrintWriter

object AddAccionRespuesta {
    fun addAccionRespuesta(msg: String, imprimir: PrintWriter) {
        val infos = msg.split(" ".toRegex(), 4).toTypedArray()
        var id = -30
        var repID = 0
        val args = infos[3]
        try {
            repID = infos[1].toInt()
            id = infos[2].toInt()
        } catch (e: Exception) {
        }
        val rep = World.getNPCreponse(repID)
        if (id == -30 || rep == null) {
            val str = "Al menos uno de los valores no es válido"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        var mess = "Se ha añadido la acción"
        rep.addAction(Action(id, args, ""))
        val ok = SQLManager.ADD_REPONSEACTION(repID, id, args)
        if (ok) mess += " y se añade a la base de datos"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
    }
}