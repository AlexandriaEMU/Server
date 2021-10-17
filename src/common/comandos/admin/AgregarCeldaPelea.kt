package common.comandos.admin

import common.CryptManager
import common.SQLManager
import common.SocketManager
import objects.Personaje
import java.io.PrintWriter

object AgregarCeldaPelea {
    fun agregarceldapelea(imprimir: PrintWriter, personaje: Personaje, infos:Array<String>){
        var team = -1
        var cell = -1
        try {
            team = infos[1].toInt()
            cell = infos[2].toInt()
        } catch (e: Exception) {
        }
        if (team < 0 || team > 1) {
            val str = "Equipo o ID de celda incorrecta"
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            return
        }
        if (cell < 0 || personaje._curCarte.getCase(cell) == null || !personaje._curCarte.getCase(cell)
                .isWalkable(true)
        ) {
            cell = personaje._curCell.id
        }
        val places: String = personaje._curCarte._placesStr
        val p = places.split("\\|".toRegex()).toTypedArray()
        var already = false
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

        //Si case déjà utilisée
        println(
            """
        0 => $team0
        1 =>$team1
        Cell: ${CryptManager.cellID_To_Code(cell)}
        """.trimIndent())

            var a = 0
            while (a <= team0.length - 2) {
                if (cell == CryptManager.cellCode_To_ID(team0.substring(a, a + 2))) already = true
                a += 2
            }


            var aa: Int = 0
            while (aa <= team1.length - 2) {
                if (cell == CryptManager.cellCode_To_ID(team1.substring(aa, aa + 2))) already = true
                aa += 2
            }

        if (already) {
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "La celda ya esta asignada a una posicion de pelea")
            return
        }
        if (team == 0) team0 += CryptManager.cellID_To_Code(cell) else if (team == 1) team1 += CryptManager.cellID_To_Code(
            cell
        )

        val newPlaces = "$team0|$team1"

        personaje._curCarte.setPlaces(newPlaces)
        if (!SQLManager.SAVE_MAP_DATA(personaje._curCarte)) return
        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Las celdas posicion de pelea se han modificado ($newPlaces)")
    }
}