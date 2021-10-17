package common.comandos.admin

import common.Constants
import common.Main
import common.SocketManager
import objects.Personaje
import java.io.PrintWriter

object Online {
    fun online(imprimir: PrintWriter){
        var mess = """
            ==========
            Lista de jugadores en online:
            """.trimIndent()
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
        val diff = Main.gameServer!!.clients.size - 30
        for (b in 0..29) {
            if (b == Main.gameServer!!.clients.size) break
            val GT = Main.gameServer!!.clients[b]
            val P = GT.perso ?: continue
            mess = P._name + "(" + P._GUID + ") "
            mess += when (P._classe) {
                Constants.CLASS_FECA -> "Fec"
                Constants.CLASS_OSAMODAS -> "Osa"
                Constants.CLASS_ENUTROF -> "Enu"
                Constants.CLASS_SRAM -> "Sra"
                Constants.CLASS_XELOR -> "Xel"
                Constants.CLASS_ECAFLIP -> "Feca"
                Constants.CLASS_ENIRIPSA -> "Eni"
                Constants.CLASS_IOP -> "Yop"
                Constants.CLASS_CRA -> "Ocra"
                Constants.CLASS_SADIDA -> "Sad"
                Constants.CLASS_SACRIEUR -> "Sac"
                Constants.CLASS_PANDAWA -> "Pan"
                else -> "Unk"
            }
            mess += " "
            mess += (if (P._sexe == 0) "M" else "F") + " "
            mess += P._lvl.toString() + " "
            mess += P._curCarte._id.toString() + "(" + P._curCarte.x + "/" + P._curCarte.y + ") "
            mess += if (P._fight == null) "" else "Pelea "
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
        }
        if (diff > 0) {
            mess = "Y $diff otros personajes"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
        }
        mess = "==========\n"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)

    }

}