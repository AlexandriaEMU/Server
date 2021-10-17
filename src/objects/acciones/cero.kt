package objects.acciones

import objects.Personaje

object cero {
    fun cero(personaje: Personaje, argumento: String){
        try {
            val nuevomapid: Short = argumento.split(",".toRegex(), 2).toTypedArray()[0].toShort()
            val nuevaceldaid: Int = argumento.split(",".toRegex(), 2).toTypedArray()[1].toInt()
            personaje.teleport(nuevomapid, nuevaceldaid)
        } catch (e: Exception) {
            return
        }
    }
}