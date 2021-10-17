package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter

object Nivel {
    fun nivel(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        var count = 0
        try {
            count = infos[1].toInt()
            if (count < 1) count = 1
            if (count > World.getExpLevelSize()) count = World.getExpLevelSize()
            var perso: Personaje? = personaje
            if (infos.size == 3) //Si le nom du perso est spécifié
            {
                val name = infos[2]
                perso = World.getPersoByName(name)
                if (perso == null) perso = personaje
            }
            if (perso!!._lvl < count) {
                while (perso._lvl < count) {
                    perso.levelUp(false, true)
                }
                if (perso.isOnline) {
                    SocketManager.GAME_SEND_SPELL_LIST(perso)
                    SocketManager.GAME_SEND_NEW_LVL_PACKET(perso._compte.gameThread._out, perso._lvl)
                    SocketManager.GAME_SEND_STATS_PACKET(perso)
                }
            }
            val mess = "Usted ha cambiado el nivel del personaje " + perso._name + " a " + count
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
        } catch (e: Exception) {
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Valor incorrecto")
            return
        }
    }
}