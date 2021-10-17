package common.comandos.admin

import common.SQLManager
import common.SocketManager
import common.World
import java.io.PrintWriter

object AgregarRespuesta {
    fun agregarespuesta(imprimir: PrintWriter, infos:Array<String>){
        if (infos.size < 3) {
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Marque uno o varios argumentos")
            return
        }
        var id = 0
        try {
            id = infos.get(1).toInt()
        } catch (e: Exception) {
        }
        val reps: String = infos.get(2)
        val Q = World.getNPCQuestion(id)
        var str = ""
        if (id == 0 || Q == null) {
            str = "ID de la pregunta invalida"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        Q.reponses = reps
        val a = SQLManager.UPDATE_NPCREPONSES(id, reps)
        str = "Lista de respuestas para la pregunta " + id + ": " + Q.reponses
        if (a) str += "(Guardado en la BDD)"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
    }
}