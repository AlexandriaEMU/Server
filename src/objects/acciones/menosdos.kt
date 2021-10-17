package objects.acciones

import common.SocketManager
import objects.Personaje

object menosdos {
    fun menosdos(personaje: Personaje){
        if (personaje.is_away) return
        if (personaje._guild != null || personaje.guildMember != null) {
            SocketManager.GAME_SEND_gC_PACKET(personaje, "Ea")
            return
        }
        SocketManager.GAME_SEND_gn_PACKET(personaje)
    }
}