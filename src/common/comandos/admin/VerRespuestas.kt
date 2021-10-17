package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object VerRespuestas {
    fun verrespuestas(imprimir: PrintWriter, infos:Array<String>){
        var id = 0
        try {
            id = infos[1].toInt()
        } catch (e: Exception) {
        }
        val Q = World.getNPCQuestion(id)
        var str = ""
        if (id == 0 || Q == null) {
            str = "ID de la pregunta invalida"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        str = "Lista de respuesta para la pregunta " + id + ": " + Q.reponses
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
    }
}