package common.comandos.admin

import common.SocketManager
import objects.Mapa
import objects.Percepteur
import objects.Personaje
import java.io.PrintWriter

object MapaInformacion {
    fun mapainfo(imprimir: PrintWriter, personaje: Personaje) {
        var mensaje = """
             ==========
             Lista de los NPC en el mapa: (ORDEN: ID, TemplateID, CellID, InitQuestionID)
             """.trimIndent()
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mensaje)
        val map: Mapa = personaje.get_curCarte()
        for ((id,npc) in map._npcs) {
            mensaje =
                id.toString() + " " + npc._template._id + " " + npc._cellID + " " + npc._template._initQuestionID
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mensaje)
        }
        mensaje = "Lista de los grupos de monstruos en el mapa: (ORDEN: ID, CellID, Alignement, Size)"
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mensaje)
        for ((id,mobGroup) in map.mobGroups) {
            mensaje = id.toString() + " " + mobGroup.cellID + " " + mobGroup.alignement + " " + mobGroup.size
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mensaje)
        }
        val p = Percepteur.GetPercoByMapID(map._id)
        if (p != null) {
            mensaje = "Recaudador en el mapa: (ORDEN: ID, CellID, GuildID)"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mensaje)
            mensaje = p.guid.toString() + " " + p._cellID + " " + p._guildID
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mensaje)
        }
        mensaje = "=========="
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mensaje)
    }
}