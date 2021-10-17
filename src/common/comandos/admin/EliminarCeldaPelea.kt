package common.comandos.admin

import common.CryptManager
import common.SQLManager
import common.SocketManager
import objects.Personaje
import java.io.PrintWriter

object EliminarCeldaPelea {
    fun eliminarceldapelea(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        var cell = -1
        try {
            cell = infos[2].toInt()
        } catch (e: Exception) {
        }
        if (cell < 0 || personaje._curCarte.getCase(cell) == null) {
            cell = personaje._curCell.id
        }
        val places: String = personaje._curCarte._placesStr
        val p = places.split("\\|".toRegex()).toTypedArray()
        val newPlaces = StringBuilder()
        var team0 = ""
        var team1 = ""
        try {
            team0 = p[0]
        } catch (e: Exception) {
        }
        try {
            team1 = p[1]
        } catch (e: Exception) {
        }

            var a = 0
            while (a <= team0.length - 2) {
                val c = p[0].substring(a, a + 2)
                if (cell == CryptManager.cellCode_To_ID(c)) {
                    a += 2
                    continue
                }
                newPlaces.append(c)
                a += 2
            }

        newPlaces.append("|")

            var aa = 0
            while (aa <= team1.length - 2) {
                val c = p[1].substring(aa, aa + 2)
                if (cell == CryptManager.cellCode_To_ID(c)) {
                    aa += 2
                    continue
                }
                newPlaces.append(c)
                aa += 2

        }
        personaje._curCarte.setPlaces(newPlaces.toString())
        if (!SQLManager.SAVE_MAP_DATA(personaje._curCarte)) return
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Las celdas se han modificado a ($newPlaces)")
        return
    }
}