package common.comandos.admin

import common.SQLManager
import common.SocketManager
import common.World
import java.io.PrintWriter

object AgregarPreguntaInicial {
    fun addPreguntaInicial(msg: String, imprimir: PrintWriter) {
        val infos = msg.split(" ".toRegex(), 4).toTypedArray()
        var id = -30
        var q = 0
        try {
            q = infos[2].toInt()
            id = infos[1].toInt()
        } catch (e: Exception) {
        }
        if (id == -30) {
            val str = "NpcID invalide"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        var mess = "L'action a ete ajoute"
        val npc = World.getNPCTemplate(id)
        npc.setInitQuestion(q)
        val ok = SQLManager.UPDATE_INITQUESTION(id, q)
        if (ok) mess += " et ajoute a la BDD"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
    }
}