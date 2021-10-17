package common.comandos.admin

import common.SQLManager
import common.SocketManager
import objects.Personaje
import java.io.PrintWriter

object SpawnFijo {
    fun spawnFijo(infos: Array<String>, personaje: Personaje, imprimir: PrintWriter) {
        val groupData = infos[1]
        personaje._curCarte.addStaticGroup(personaje._curCell.id, groupData)

        //Sauvegarde DB de la modif
        val str = "El grupo fijo se ha agregado y" + if (SQLManager.SAVE_NEW_FIXGROUP(
                personaje._curCarte._id.toInt(),
                personaje._curCell.id,
                groupData
            )
        ) " se ha guardado en la base de datos" else " ha fallado en guardarse en la base de datos"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
    }
}