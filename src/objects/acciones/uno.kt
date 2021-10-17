package objects.acciones

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object uno {
    fun uno(imprimir: PrintWriter, personaje: Personaje, argumento: String){
        imprimir = personaje._compte.gameThread._out
        if (argumento.equals("DV", ignoreCase = true)) {
            SocketManager.GAME_SEND_END_DIALOG_PACKET(imprimir)
            personaje._isTalkingWith = 0
        } else {
            var qID = -1
            try {
                qID = argumento.toInt()
            } catch (e: NumberFormatException) {
            }
            val quest = World.getNPCQuestion(qID)
            if (quest == null) {
                SocketManager.GAME_SEND_END_DIALOG_PACKET(imprimir)
                personaje._isTalkingWith = 0
                return
            }
            SocketManager.GAME_SEND_QUESTION_PACKET(imprimir, quest.parseToDQPacket(personaje))
        }
    }
}