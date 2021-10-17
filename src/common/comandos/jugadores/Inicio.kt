package common.comandos.jugadores

import objects.Personaje

object Inicio {
    fun inicio(personaje: Personaje){
        if (personaje._fight != null) return
        personaje.warpToSavePos()
    }
}