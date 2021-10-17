package common.comandos.admin

import common.SocketManager
import common.World
import objects.Personaje
import java.io.PrintWriter
import kotlin.math.abs

object Kamas {
    fun kamas(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        var count = 0
        count = try {
            infos[1].toInt()
        } catch (e: Exception) {
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Valor incorrecto")
            return
        }
        if (count == 0) return

        var perso: Personaje? = personaje
        if (infos.size == 3) //Si le nom du perso est spécifié
        {
            val name: String = infos[2]
            perso = World.getPersoByName(name)
            if (perso == null) perso = personaje
        }
        val curKamas = perso!!._kamas
        var newKamas = curKamas + count
        if (newKamas < 0) newKamas = 0
        if (newKamas > 1000000000) newKamas = 1000000000
        perso._kamas = newKamas
        if (perso.isOnline) SocketManager.GAME_SEND_STATS_PACKET(perso)
        var mess = "Usted ha "
        mess += (if (count < 0) "retirado" else "agregado") + " "
        mess += abs(count).toString() + " kamas a " + perso._name
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
    }
}