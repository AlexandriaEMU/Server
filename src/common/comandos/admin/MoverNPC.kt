package common.comandos.admin

import common.SQLManager
import common.SocketManager
import objects.NPC_tmpl.NPC
import objects.Personaje
import java.io.PrintWriter

object MoverNPC {
    fun movernpc(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        var id = 0
        try {
            id = infos[1].toInt()
        } catch (e: Exception) {
        }
        val npc: NPC = personaje._curCarte.getNPC(id)
        if (id == 0 || npc == null) {
            val str = "ID del NPC invalida"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        val exC = npc._cellID
        //on l'efface de la map
        //on l'efface de la map
        SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(personaje._curCarte, id)
        //on change sa position/orientation
        //on change sa position/orientation
        npc.setCellID(personaje._curCell.id)
        npc.setOrientation(personaje._orientation as Byte)
        //on envoie la modif
        //on envoie la modif
        SocketManager.GAME_SEND_ADD_NPC_TO_MAP(personaje._curCarte, npc)
        var str = "El NPC se ha movido con exito"
        if (personaje._orientation == 0 || personaje._orientation == 2 || personaje._orientation == 4 || personaje._orientation == 6) str += " pero se ha vuelto invisible (orientacion invalida)."
        if (SQLManager.DELETE_NPC_ON_MAP(personaje._curCarte._id.toInt(), exC)
            && SQLManager.ADD_NPC_ON_MAP(
                personaje._curCarte._id.toInt(),
                npc._template._id,
                personaje._curCell.id,
                personaje._orientation)) SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(
            imprimir,
            str
        ) else SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Error al momento de guardar la nueva posicion")
    }
}