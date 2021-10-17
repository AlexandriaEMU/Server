package common.comandos.admin

import common.SQLManager
import common.SocketManager
import objects.Personaje
import java.io.PrintWriter

object GrupoMaximoMobs {
    fun grupoMaximoMobs(msg: String, imprimir: PrintWriter, personaje: Personaje) {
        val infos = msg.split(" ".toRegex(), 4).toTypedArray()
        var id: Byte = -1
        try {
            id = infos[1].toByte()
        } catch (e: Exception) {
        }
        if (id.toInt() == -1) {
            val str = "Valor invalido"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        var mess = "El número de grupos se ha fijado"
        personaje._curCarte.setMaxGroup(id)
        val ok = SQLManager.SAVE_MAP_DATA(personaje._curCarte)
        if (ok) mess += " y se ha guardado en la base de datos"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
    }
}