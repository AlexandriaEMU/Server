package common.comandos.admin

import common.SocketManager
import common.World
import objects.Compte
import objects.Personaje
import java.io.PrintWriter

object Objeto {
    fun objeto(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>, cuenta: Compte){
        if (cuenta._gmLvl < 2) {
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Usted no tiene el nivel de admin requerido")
            return
        }
        var tID = 0
        try {
            tID = infos[1].toInt()
        } catch (e: Exception) {
        }
        if (tID == 0) {
            val mess = "El objeto ID $tID no existe"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
            return
        }
        var qua = 1
        if (infos.size == 3) //Si une quantité est spécifiée
        {
            try {
                qua = infos[2].toInt()
            } catch (e: Exception) {
            }
        }
        var useMax = false
        if (infos.size == 4) //Si un jet est spécifié
        {
            if (infos[3].equals("MAX", ignoreCase = true)) useMax = true
        }
        val t = World.getObjTemplate(tID)
        if (t == null) {
            val mess = "El objeto ID $tID no existe"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
            return
        }
        if (qua < 1) qua = 1
        val obj = t.createNewItem(qua, useMax)
        if (personaje.addObjet(obj, true)) //Si le joueur n'avait pas d'item similaire
            World.addObjet(obj, true)
        var str = "Se ha creado el objeto ID $tID con exito"
        if (useMax) str += ", en su maximas estadisticas"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
        SocketManager.GAME_SEND_Ow_PACKET(personaje)
    }
}