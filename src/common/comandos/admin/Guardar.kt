package common.comandos.admin

import common.SocketManager
import game.GameServer.SaveThread
import java.io.PrintWriter

object Guardar {
    fun guardar(imprimir: PrintWriter){
        val t = Thread(SaveThread())
        t.start()
        val mess = "Se ha lanzado el guardado general"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
        return
    }
}