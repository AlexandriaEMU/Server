package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object AgregarSet {
    fun agregarset(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        var tID = 0
        try {
            tID = infos[1].toInt()
        } catch (e: Exception) {
        }
        val `is` = World.getItemSet(tID)
        if (tID == 0 || `is` == null) {
            val mess = "El set con la ID: $tID no existe "
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
            return
        }
        var useMax = false
        if (infos.size == 3) useMax = infos[2] == "MAX" //Si un jet est spécifié


        for (t in `is`.itemTemplates) {
            val obj = t.createNewItem(1, useMax)
            if (personaje.addObjet(obj, true)) //Si le joueur n'avait pas d'item similaire
                World.addObjet(obj, true)
        }
        var str = "Se ha creado el set ID $tID con exito"
        if (useMax) str += " en sus maximas estadisticas"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
    }
}