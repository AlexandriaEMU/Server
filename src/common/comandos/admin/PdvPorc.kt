package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object PdvPorc {
    fun pdvporcentaje(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        var count = 0
        try {
            count = infos[1].toInt()
            if (count < 0) count = 0
            if (count > 100) count = 100
            var perso: Personaje? = personaje
            if (infos.size == 3) //Si le nom du perso est spécifié
            {
                val name: String = infos[2]
                perso = World.getPersoByName(name)
                if (perso == null) perso = personaje
            }
            val newPDV = perso!!._PDVMAX * count / 100
            perso._PDV = newPDV
            if (perso.isOnline) SocketManager.GAME_SEND_STATS_PACKET(perso)
            val mess = "Usted cambio el porcentaje PDV de  " + perso._name + " a " + count
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
        } catch (e: Exception) {
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Valor incorrecto")
            return
        }
    }
}