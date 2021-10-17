package common

import common.Main.addToMjLog
import common.SocketManager.GAME_SEND_ADD_NPC_TO_MAP
import common.SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET
import common.SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP
import common.SocketManager.GAME_SEND_Im_PACKET_TO_ALL
import common.SocketManager.GAME_SEND_MESSAGE_TO_ALL
import common.SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET
import common.SocketManager.send
import common.comandos.admin.ActualizarMobs.actualizarmobs
import common.comandos.admin.AddAccionRespuesta.addAccionRespuesta
import common.comandos.admin.AgregarCeldaPelea.agregarceldapelea
import common.comandos.admin.AgregarPreguntaInicial.addPreguntaInicial
import common.comandos.admin.AgregarRespuesta.agregarespuesta
import common.comandos.admin.AgregarSet.agregarset
import common.comandos.admin.Alineacion.alineacion
import common.comandos.admin.AlternarAgresion.alternaragresiones
import common.comandos.admin.Anuncio.anuncio
import common.comandos.admin.AprenderHechizo.aprenderhechizo
import common.comandos.admin.AprenderOficio.aprenderoficio
import common.comandos.admin.Ban.ban
import common.comandos.admin.Capital.capital
import common.comandos.admin.CrearGremio.creargremio
import common.comandos.admin.Demorph.demorph
import common.comandos.admin.Desban.desban
import common.comandos.admin.EliminarCeldaPelea.eliminarceldapelea
import common.comandos.admin.Expulsar.expulsar
import common.comandos.admin.GrupoMaximoMobs.grupoMaximoMobs
import common.comandos.admin.Guardar.guardar
import common.comandos.admin.Hablar.hablar
import common.comandos.admin.HacerAccion.haceraccion
import common.comandos.admin.Honor.honor
import common.comandos.admin.Informacion.info
import common.comandos.admin.Ir.ir
import common.comandos.admin.IrMapa.irmapa
import common.comandos.admin.Kamas.kamas
import common.comandos.admin.MapaInformacion.mapainfo
import common.comandos.admin.Mover.mover
import common.comandos.admin.MoverNPC.movernpc
import common.comandos.admin.NAnuncio.nanuncio
import common.comandos.admin.Nivel.nivel
import common.comandos.admin.Objeto.objeto
import common.comandos.admin.Online.online
import common.comandos.admin.PdvPorc.pdvporcentaje
import common.comandos.admin.PuntosHechizos.puntoshechizo
import common.comandos.admin.Salir.salir
import common.comandos.admin.Silenciar.silenciar
import common.comandos.admin.SpawnFijo.spawnFijo
import common.comandos.admin.Tamaño.tamaño
import common.comandos.admin.Titulo.titulo
import common.comandos.admin.Traer.traer
import common.comandos.admin.Transformar.transformar
import common.comandos.admin.VerPosPelea.pospelea
import common.comandos.admin.VerRespuestas.verrespuestas
import common.comandos.admin.XpOficios.xpoficio
import objects.*
import objects.Mapa.MountPark
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.PrintWriter
import java.util.*
import javax.swing.Timer
import kotlin.math.pow
import kotlin.system.exitProcess

object Comandos {


    //Guardado
    var TimerStart = false
    var timer: Timer? = null


    private fun createTimer(time: Int): Timer {
        val action: ActionListener = object : ActionListener {
            var Time = time
            override fun actionPerformed(event: ActionEvent) {
                Time -= 1
                if (Time == 1) {
                    GAME_SEND_Im_PACKET_TO_ALL("115;$Time minuto")
                } else {
                    GAME_SEND_Im_PACKET_TO_ALL("115;$Time minutos")
                }
                if (Time <= 0) {
                    for (perso in World.getOnlinePersos()) {
                        perso._compte.gameThread.kick()
                    }
                    exitProcess(0)
                }
            }
        }
        // Génération du repeat toutes les minutes.
        return Timer(60000, action) //60000
    }

    fun comandosdeconsola(paquete: String, personaje: Personaje) {
        val cuenta: Compte = personaje._compte
        val imprimir = cuenta.gameThread._out
        if (cuenta._gmLvl < 1) {
            cuenta.gameThread.kick()
            return
        }
        val msg = paquete.substring(2)
        val infos = msg.split(" ".toRegex()).toTypedArray()
        if (infos.isEmpty()) return
        val comando = infos[0]
        if (Main.canLog) {
            addToMjLog(cuenta._curIP + ": " + cuenta._name + " " + personaje._name + "=>" + msg)
        }
        when {
            cuenta._gmLvl == 1 -> comandoslvl1(comando, infos, msg, personaje, cuenta, imprimir)
            cuenta._gmLvl == 2 -> comandoslvl2(comando, infos, msg, personaje, cuenta, imprimir)
            cuenta._gmLvl == 3 -> comandoslvl3(comando, infos, msg, personaje, cuenta, imprimir)
            cuenta._gmLvl >= 4 -> comandoslvl4(comando, infos, msg, personaje, cuenta, imprimir)
        }
    }

    fun comandoslvl1(
        command: String,
        infos: Array<String>,
        msg: String,
        personaje: Personaje,
        cuenta: Compte,
        imprimir: PrintWriter
    ) {
        if (cuenta._gmLvl < 1) {
            cuenta.gameThread.kick()
            return
        }

        //Comando INFORMACION - Info del server
        when (command.uppercase(Locale.getDefault())) {
            "INFORMACION" -> {
                info(imprimir)
                return
            }  //Comando ACTUALIZARMOBS - Actualiza los mobs del server
            "ACTUALIZARMOBS" -> {
                actualizarmobs(imprimir, personaje)
                return
            }
            //Comando INFOMAPA - Lanza la informacion del mapa(NPC, MOBS, Recaudadores)
            "INFOMAPA" -> {
                mapainfo(imprimir, personaje)
            }
            "ONLINE" -> {
                online(imprimir)
            }
            "POSPELEA" -> {
                pospelea(imprimir, personaje)
            }
            "CREARGREMIO" -> {
                creargremio(imprimir, personaje, infos)
            }
            "ALTERNARAGRESION" -> {
                alternaragresiones(imprimir, personaje, infos)
            }
            "ANUNCIO" -> {
                anuncio(msg)
            }
            "DEMORPH" -> {
                demorph(imprimir, personaje, infos)
            }
            "IR" -> {
                ir(imprimir, personaje, infos)
            }
            "TRAER" -> {
                traer(imprimir, personaje, infos)
            }
            "NANUNCIO" -> {
                nanuncio(msg, personaje)
            }
            "MOVER" -> {
                mover(imprimir, personaje, infos)
            }
            "IRMAPA" -> {
                irmapa(imprimir, personaje, infos)
            }
            "HACERACCION" -> {
                haceraccion(imprimir, personaje, infos)
            }
            else -> {
                val mess = "Comando incorrecto"
                GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
            }
        }
    }

    fun comandoslvl2(
        command: String,
        infos: Array<String>,
        msg: String,
        personaje: Personaje,
        cuenta: Compte,
        imprimir: PrintWriter
    ) {
        if (cuenta._gmLvl < 2) {
            cuenta.gameThread.kick()
            return
        }

        //Comando SILENCIAR - Silencia al personaje por el tiempo que quieras
        when (command.uppercase(Locale.getDefault())) {
            "SILENCIAR" -> {
                silenciar(imprimir, personaje, infos)
                return
            }  //Comando HABLAR - Quita el silenciado a un personaje
            "HABLAR" -> {
                hablar(imprimir, personaje, infos)
            }  //Comando EXPULSAR - Saca a un personaje del servidor
            "EXPULSAR" -> {
                expulsar(imprimir, personaje, infos)
            }  //Comando PUNTOSHECHIZO - Agrega los puntos de hechizo a ti mismo o a un personaj espesifico
            "PUNTOSHECHIZO" -> {
                puntoshechizo(imprimir, personaje, infos)
            }  //Comando APRENDERHECHIZO - Aprende cualquier hechizo en tu personaje o uno espesifico
            "APRENDERHECHIZO" -> {
                aprenderhechizo(imprimir, personaje, infos)
            }  //Comando ALINEACION - Cambia la alineacion de un personaje
            "ALINEACION" -> {
                alineacion(imprimir, personaje, infos)
            }  //Comando AGREGARESPUESTA - Agrega una respuesta a una pregunta de un NPC
            "AGREGARESPUESTA" -> {
                agregarespuesta(imprimir, infos)
                return
            }  //Comando VERRESPUESTAS - Ver las respuestas de una pregunta de un NPC
            "SHOWREPONSES" -> {
                verrespuestas(imprimir, infos)
                return
            }  //Comando HONOR - Agrega honor a un personaje
            "HONOR" -> {
                honor(imprimir, personaje, infos)
            }  //Comando XPOFICIO - Agrega experiencia a los oficios
            "XPOFICIO" -> {
                xpoficio(imprimir, personaje, infos)
            }  //Comando APRENDEROFICIO - Aprende el oficio que quieras
            "APRENDEROFICIO" -> {
                aprenderoficio(imprimir, personaje, infos)
            }  //Comando CAPITAL - Agrega puntos de capital al personaje
            "CAPITAL" -> {
                capital(imprimir, personaje, infos)
            }
            //Comando TAMAÑO - Cambia el tamaño de un personaje
            "TAMAÑO" -> {
                tamaño(imprimir, personaje, infos)
            }
            //Comando TRANSFORMAR - Cambia la forma de un personaje
            "TRANSFORMAR" -> transformar(imprimir, personaje, infos)
            "MOVERNPC" -> {
                movernpc(imprimir, personaje, infos)
            }  //Comando AGREGARSET - Agrega el grupo de item que forman un set por ID
            "AGREGARSET" -> {
                agregarset(imprimir, personaje, infos)
            }  //Comando NIVEL - Agrega la cantidad de nivel que quieras a un personaje
            "NIVEL" -> {
                nivel(imprimir, personaje, infos)
            }  //Comando PDVPORC - Aumenta el porcentaje de los puntos de vida
            "PDVPORC" -> {
                pdvporcentaje(imprimir, personaje, infos)
            }  //Comando KAMAS - Aumenta las kamas de un personaje
            "KAMAS" -> {
                kamas(imprimir, personaje, infos)
            }  //Comando OBJETO - Agregar cualquier item u objeto a un personaje
            "OBJETO" -> {
                objeto(imprimir, personaje, infos, cuenta)
            }  //Comando TITULO - Cambia el titulo de cualquier personaje
            "TITULO" -> {
                titulo(imprimir, personaje, infos)
            }
            else -> {
                comandoslvl1(command, infos, msg, personaje, cuenta, imprimir)
            }
        }


        //Comando MOVERNPC - Mueve un NPC de un lugar a otro
    }

    fun comandoslvl3(
        comando: String,
        infos: Array<String>,
        msg: String,
        personaje: Personaje,
        cuenta: Compte,
        imprimir: PrintWriter
    ) {
        if (cuenta._gmLvl < 3) {
            cuenta.gameThread.kick()
            return
        }
        val command = comando.uppercase(Locale.getDefault())

        //Comando SALIR - Cierra el servidor
        when (command) {
            "SALIR" -> {
                salir()
            }  //Comando GUARDAR - Guarda todo el servidor completo
            "GUARDAR" -> {
                if (!Main.isSaving) guardar(imprimir)
            }  //Comando ELIMINARCELDAPELEA - Elimina la celda de la posicion de pelea
            "ELIMINARCELDAPELEA" -> {
                eliminarceldapelea(imprimir, personaje, infos)
            }  //Comando BAN - Banea a un personaje
            "BAN" -> {
                ban(imprimir, infos)
            }  //Comando DESBAN - Elimina el baneo de un personaje
            "DESBAN" -> {
                desban(imprimir, infos)
            }  //Comando AGREGARCELDAPELEA - Agrega una celda para posicion de pelea
            "AGREGARCELDAPELEA" -> {
                agregarceldapelea(imprimir, personaje, infos)
            }  //Comando GRUPOMAXIMO - Modificar el maximo grupo de mobs del mapa
            "GRUPOMAXIMOMOBS" -> {
                grupoMaximoMobs(msg, imprimir, personaje)
            }
            "ADDACCIONRESPUESTA" -> {
                addAccionRespuesta(msg, imprimir)
            }
            "ADDPREGUNTAINICIAL" -> {
                addPreguntaInicial(msg, imprimir)
            }
            "ADDENDFIGHTACTION" -> {
                val infos = msg.split(" ".toRegex(), 4).toTypedArray()
                var id = -30
                var type = 0
                val args = infos[3]
                val cond = infos[4]
                try {
                    type = infos[1].toInt()
                    id = infos[2].toInt()
                } catch (e: Exception) {
                }
                if (id == -30) {
                    val str = "Au moins une des valeur est invalide"
                    GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                    return
                }
                var mess = "L'action a ete ajoute"
                personaje._curCarte.addEndFightAction(type, Action(id, args, cond))
                val ok = SQLManager.ADD_ENDFIGHTACTION(
                    personaje._curCarte._id.toInt(),
                    type,
                    id,
                    args,
                    cond
                )
                if (ok) mess += " et ajoute a la BDD"
                GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, mess)
                return
            }
            "SPAWNFIX" -> {
                spawnFijo(infos, personaje, imprimir)
                return
            }
            "ADDNPC" -> {
                var id = 0
                try {
                    id = infos[1].toInt()
                } catch (e: Exception) {
                }
                if (id == 0 || World.getNPCTemplate(id) == null) {
                    val str = "NpcID invalide"
                    GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                    return
                }
                val npc =
                    personaje._curCarte.addNpc(id, personaje._curCell.id, personaje._orientation)
                GAME_SEND_ADD_NPC_TO_MAP(personaje._curCarte, npc)
                var str = "Le PNJ a ete ajoute"
                if (personaje._orientation == 0 || personaje._orientation == 2 || personaje._orientation == 4 || personaje._orientation == 6) str += " mais est invisible (orientation diagonale invalide)."
                if (SQLManager.ADD_NPC_ON_MAP(
                        personaje._curCarte._id.toInt(),
                        id,
                        personaje._curCell.id,
                        personaje._orientation
                    )
                ) GAME_SEND_CONSOLE_MESSAGE_PACKET(
                    imprimir,
                    str
                ) else GAME_SEND_CONSOLE_MESSAGE_PACKET(
                    imprimir,
                    "Erreur au moment de sauvegarder la position"
                )
            }
            "DELNPC" -> {
                var id = 0
                try {
                    id = infos[1].toInt()
                } catch (e: Exception) {
                }
                val npc = personaje._curCarte.getNPC(id)
                if (id == 0 || npc == null) {
                    val str = "Npc GUID invalide"
                    GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                    return
                }
                val exC = npc._cellID
                //on l'efface de la map
                GAME_SEND_ERASE_ON_MAP_TO_MAP(personaje._curCarte, id)
                personaje._curCarte.removeNpcOrMobGroup(id)
                val str = "Le PNJ a ete supprime"
                if (SQLManager.DELETE_NPC_ON_MAP(
                        personaje._curCarte._id.toInt(),
                        exC
                    )
                ) GAME_SEND_CONSOLE_MESSAGE_PACKET(
                    imprimir,
                    str
                ) else GAME_SEND_CONSOLE_MESSAGE_PACKET(
                    imprimir,
                    "Erreur au moment de sauvegarder la position"
                )
            }
            "DELTRIGGER" -> {
                var cellID = -1
                try {
                    cellID = infos[1].toInt()
                } catch (e: Exception) {
                }
                if (cellID == -1 || personaje._curCarte.getCase(cellID) == null) {
                    val str = "CellID invalide"
                    GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                    return
                }
                personaje._curCarte.getCase(cellID).clearOnCellAction()
                val success = SQLManager.REMOVE_TRIGGER(personaje._curCarte._id.toInt(), cellID)
                var str = ""
                str = if (success) "Le trigger a ete retire" else "Le trigger n'a pas ete retire"
                GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            }
            "ADDTRIGGER" -> {
                var actionID = -1
                var args = ""
                var cond = ""
                try {
                    actionID = infos[1].toInt()
                    args = infos[2]
                    cond = infos[3]
                } catch (e: Exception) {
                }
                if (args == "" || actionID <= -3) {
                    val str = "Valeur invalide"
                    GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                    return
                }
                personaje._curCell.addOnCellStopAction(actionID, args, cond)
                val success = SQLManager.SAVE_TRIGGER(
                    personaje._curCarte._id.toInt(),
                    personaje._curCell.id,
                    actionID,
                    1,
                    args,
                    cond
                )
                var str = ""
                str = if (success) "Le trigger a ete ajoute" else "Le trigger n'a pas ete ajoute"
                GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            }
            "DELNPCITEM" -> {
                if (cuenta._gmLvl < 3) return
                var npcGUID = 0
                var itmID = -1
                try {
                    npcGUID = infos[1].toInt()
                    itmID = infos[2].toInt()
                } catch (e: Exception) {
                }
                val npc = personaje._curCarte.getNPC(npcGUID)._template
                if (npcGUID == 0 || itmID == -1 || npc == null) {
                    val str = "NpcGUID ou itmID invalide"
                    GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                    return
                }
                var str = ""
                str =
                    if (npc.delItemVendor(itmID)) "L'objet a ete retire" else "L'objet n'a pas ete retire"
                GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            }
            "ADDNPCITEM" -> {
                if (cuenta._gmLvl < 3) return
                var npcGUID = 0
                var itmID = -1
                try {
                    npcGUID = infos[1].toInt()
                    itmID = infos[2].toInt()
                } catch (e: Exception) {
                }
                val npc = personaje._curCarte.getNPC(npcGUID)._template
                val item = World.getObjTemplate(itmID)
                if (npcGUID == 0 || itmID == -1 || npc == null || item == null) {
                    val str = "NpcGUID ou itmID invalide"
                    GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                    return
                }
                var str = ""
                str =
                    if (npc.addItemVendor(item)) "L'objet a ete rajoute" else "L'objet n'a pas ete rajoute"
                GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            }
            "ADDMOUNTPARK" -> {
                var size = -1
                var owner = -2
                var price = -1
                try {
                    size = infos[1].toInt()
                    owner = infos[2].toInt()
                    price = infos[3].toInt()
                    if (price > 20000000) price = 20000000
                    if (price < 0) price = 0
                } catch (e: Exception) {
                }
                if (size == -1 || owner == -2 || price == -1 || personaje._curCarte.mountPark != null) {
                    val str = "Infos invalides ou map deja config."
                    GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                    return
                }
                val MP = MountPark(
                    owner,
                    personaje._curCarte,
                    personaje._curCell.id,
                    size,
                    "",
                    -1,
                    price
                )
                personaje._curCarte.mountPark = MP
                SQLManager.SAVE_MOUNTPARK(MP)
                val str = "L'enclos a ete config. avec succes"
                GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            }
            "SHUTDOWN" -> {
                var time = 30
                var OffOn = 0
                try {
                    OffOn = infos[1].toInt()
                    time = infos[2].toInt()
                } catch (e: Exception) {
                }
                if (OffOn == 1 && TimerStart) // demande de démarer le reboot
                {
                    GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Un shutdown est deja programmer.")
                } else if (OffOn == 1 && !TimerStart) {
                    timer = createTimer(time)
                    timer!!.start()
                    TimerStart = true
                    var timeMSG = "minutes"
                    if (time <= 1) {
                        timeMSG = "minute"
                    }
                    GAME_SEND_Im_PACKET_TO_ALL("115;$time $timeMSG")
                    GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Shutdown lance.")
                } else if (OffOn == 0 && TimerStart) {
                    timer!!.stop()
                    TimerStart = false
                    GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Shutdown arrete.")
                } else if (OffOn == 0 && !TimerStart) {
                    GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Aucun shutdown n'est lance.")
                }
            }
            else -> {
                comandoslvl2(command, infos, msg, personaje, cuenta, imprimir)
            }
        }
    }


    fun comandoslvl4(
        command: String,
        infos: Array<String>,
        msg: String,
        personaje: Personaje,
        cuenta: Compte,
        imprimir: PrintWriter
    ) {
        if (cuenta._gmLvl < 4) {
            cuenta.gameThread.kick()
            return
        }
        when (command.uppercase(Locale.getDefault())) {
            "SETADMIN" -> {
                var gmLvl = -100
                try {
                    gmLvl = infos[1].toInt()
                } catch (e: Exception) {
                }
                if (gmLvl == -100) {
                    val str = "Valeur incorrecte"
                    GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                    return
                }
                var target: Personaje? = personaje
                if (infos.size > 2) //Si un nom de perso est spécifié
                {
                    target = World.getPersoByName(infos[2])
                    if (target == null) {
                        val str = "Le personnage n'a pas ete trouve"
                        GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                        return
                    }
                }
                target!!._compte.setGmLvl(gmLvl)
                SQLManager.UPDATE_ACCOUNT_DATA(target._compte)
                val str = "Le niveau GM du joueur a ete modifie"
                GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
            }
            "LOCK" -> {
                var LockValue: Byte = 1 //Accessible
                try {
                    LockValue = infos[1].toByte()
                } catch (e: Exception) {
                }
                if (LockValue > 2) LockValue = 2
                if (LockValue < 0) LockValue = 0
                var c = 0.toChar()
                if (LockValue.toInt() == 0) c = 'D'
                if (LockValue.toInt() == 1) c = 'O'
                if (LockValue.toInt() == 2) c = 'S'
                Main.comServer!!.sendChangeState(c)
                if (LockValue.toInt() == 1) {
                    GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Serveur accessible.")
                } else if (LockValue.toInt() == 0) {
                    GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Serveur inaccessible.")
                } else if (LockValue.toInt() == 2) {
                    GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Serveur en sauvegarde.")
                }
            }
            "BLOCK" -> {
                var GmAccess: Byte = 0
                var KickPlayer: Byte = 0
                try {
                    GmAccess = infos[1].toByte()
                    KickPlayer = infos[2].toByte()
                } catch (e: Exception) {
                }
                Main.comServer!!.lockGMlevel(GmAccess.toInt())
                GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Serveur bloque au GmLevel : $GmAccess")
                if (KickPlayer > 0) {
                    for (z in World.getOnlinePersos()) {
                        if (z._compte._gmLvl < GmAccess) z._compte.gameThread.kick()
                    }
                    GAME_SEND_CONSOLE_MESSAGE_PACKET(
                        imprimir,
                        "Les joueurs de GmLevel inferieur a $GmAccess ont ete kicks."
                    )
                }
            }
            "BANIP" -> {
                var P: Personaje? = null
                try {
                    P = World.getPersoByName(infos[1])
                } catch (e: Exception) {
                }
                if (P == null || !P.isOnline) {
                    val str = "Le personnage n'a pas ete trouve."
                    GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, str)
                    return
                }
                Main.comServer!!.addBanIP(P._compte._curIP)
                GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "L'IP a ete banni.")
                if (P.isOnline) {
                    P._compte.gameThread.kick()
                    GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Le joueur a ete kick.")
                }
            }
            "FULLHDV" -> {
                var numb = 1
                try {
                    numb = infos[1].toInt()
                } catch (e: Exception) {
                }
                fullHdv(numb, imprimir)
            }
            "RES" -> {
                var objID = 1
                try {
                    objID = infos[1].toInt()
                } catch (e: Exception) {
                }
                val p = World.get_PetsEntry(objID) ?: return
                p.resurrection()
                GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(personaje, World.getObjet(objID))
            }
            "SENDME" -> {
                var str: String? = null
                try {
                    str = infos[1]
                } catch (e: Exception) {
                }
                send(personaje, str!!)
            }
            else -> {
                comandoslvl3(command, infos, msg, personaje, cuenta, imprimir)
            }
        }
    }

    private fun fullHdv(ofEachTemplate: Int, imprimir: PrintWriter) {
        GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Démarrage du remplissage!")
        var objet: Objet? = null
        var amount: Byte = 0
        var rAmount = 0
        var hdv = 0
        var lastSend = 0
        val time1 = System.currentTimeMillis() //TIME
        for (curTemp in World.getObjTemplates())  //Boucler dans les template
        {
            try {
                if (Main.NOTINHDV.contains(curTemp.id)) continue
                for (j in 0 until ofEachTemplate)  //Ajouter plusieur fois le template
                {
                    if (curTemp.type == 85) break
                    objet = curTemp.createNewItem(1, false)
                    hdv =
                        getHdv(objet.template.type) //Obtient la map (liée a l'HDV) correspondant au template de l'item
                    if (hdv < 0) break
                    val curHdv = World.getHdv(hdv)
                    amount = Formulas.getRandomValue(1, 3).toByte()
                    rAmount = (10.0.pow(amount.toDouble()) / 10).toInt()
                    objet.quantity = rAmount
                    val toAdd = HdvEntry(
                        objet.guid,
                        objet,
                        curHdv._mapID,
                        -1,
                        calculPrice(objet, rAmount),
                        rAmount
                    ) //Créer l'entry
                    World.addHdvItem(-1, curHdv._mapID, toAdd) //Ajoute l'entry dans le world
                    World.addObjet(objet, false)
                }
            } catch (e: Exception) {
                continue
            }
            if ((System.currentTimeMillis() - time1) / 1000 != lastSend.toLong() && (System.currentTimeMillis() - time1) / 1000 % 3 == 0L) {
                lastSend = ((System.currentTimeMillis() - time1) / 1000).toInt()
                GAME_SEND_CONSOLE_MESSAGE_PACKET(
                    imprimir,
                    ((System.currentTimeMillis() - time1) / 1000).toString() + "sec Template: " + curTemp.id
                )
            }
        }
        GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Remplissage fini en " + (System.currentTimeMillis() - time1) + "ms")
        World.saveAll(null)
        GAME_SEND_MESSAGE_TO_ALL("HDV remplis!", Main.CONFIG_MOTD_COLOR!!)
    }

    private fun getHdv(type: Int): Int {
        val rand = Formulas.getRandomValue(1, 4)
        var map = -1
        return when (type) {
            12, 14, 26, 43, 44, 45, 66, 70, 71, 86 -> {
                map = when (rand) {
                    1 -> {
                        4271
                    }
                    2 -> {
                        4607
                    }
                    else -> {
                        7516
                    }
                }
                map
            }
            1, 9 -> {
                map = when (rand) {
                    1 -> {
                        4216
                    }
                    2 -> {
                        4622
                    }
                    else -> {
                        7514
                    }
                }
                map
            }
            18, 72, 77, 90, 97, 113, 116 -> {
                map = if (rand == 1) {
                    8759
                } else {
                    8753
                }
                map
            }
            63, 64, 69 -> {
                map = when (rand) {
                    1 -> {
                        4287
                    }
                    2 -> {
                        4595
                    }
                    3 -> {
                        7515
                    }
                    else -> {
                        7350
                    }
                }
                map
            }
            33, 42 -> {
                map = when (rand) {
                    1 -> {
                        2221
                    }
                    2 -> {
                        4630
                    }
                    else -> {
                        7510
                    }
                }
                map
            }
            84, 93, 112, 114 -> {
                map = when (rand) {
                    1 -> {
                        4232
                    }
                    2 -> {
                        4627
                    }
                    else -> {
                        12262
                    }
                }
                map
            }
            38, 95, 96, 98, 108 -> {
                map = when (rand) {
                    1 -> {
                        4178
                    }
                    2 -> {
                        5112
                    }
                    else -> {
                        7289
                    }
                }
                map
            }
            10, 11 -> {
                map = when (rand) {
                    1 -> {
                        4183
                    }
                    2 -> {
                        4562
                    }
                    else -> {
                        7602
                    }
                }
                map
            }
            13, 25, 73, 75, 76 -> {
                map = if (rand == 1) {
                    8760
                } else {
                    8754
                }
                map
            }
            5, 6, 7, 8, 19, 20, 21, 22 -> {
                map = when (rand) {
                    1 -> {
                        4098
                    }
                    2 -> {
                        5317
                    }
                    else -> {
                        7511
                    }
                }
                map
            }
            39, 40, 50, 51, 88 -> {
                map = when (rand) {
                    1 -> {
                        4179
                    }
                    2 -> {
                        5311
                    }
                    else -> {
                        7443
                    }
                }
                map
            }
            87 -> {
                map = if (rand == 1) {
                    6159
                } else {
                    6167
                }
                map
            }
            34, 52, 60 -> {
                map = when (rand) {
                    1 -> {
                        4299
                    }
                    2 -> {
                        4629
                    }
                    else -> {
                        7397
                    }
                }
                map
            }
            41, 49, 62 -> {
                map = when (rand) {
                    1 -> {
                        4247
                    }
                    2 -> {
                        4615
                    }
                    3 -> {
                        7501
                    }
                    else -> {
                        7348
                    }
                }
                map
            }
            15, 35, 36, 46, 47, 48, 53, 54, 55, 56, 57, 58, 59, 65, 68, 103, 104, 105, 106, 107, 109, 110, 111 -> {
                map = when (rand) {
                    1 -> {
                        4262
                    }
                    2 -> {
                        4646
                    }
                    else -> {
                        7413
                    }
                }
                map
            }
            78 -> {
                map = if (rand == 1) {
                    8757
                } else {
                    8756
                }
                map
            }
            2, 3, 4 -> {
                map = when (rand) {
                    1 -> {
                        4174
                    }
                    2 -> {
                        4618
                    }
                    else -> {
                        7512
                    }
                }
                map
            }
            16, 17, 81 -> {
                map = when (rand) {
                    1 -> {
                        4172
                    }
                    2 -> {
                        4588
                    }
                    else -> {
                        7513
                    }
                }
                map
            }
            83 -> {
                map = if (rand == 1) {
                    10129
                } else {
                    8482
                }
                map
            }
            82 -> 8039
            else -> -1
        }
    }

    private fun calculPrice(obj: Objet?, amount: Int): Int {
        var stats = 0
        for (curStat in obj!!.stats.map.values) {
            stats += curStat
        }
        return if (stats > 0) ((Math.cbrt(stats.toDouble()) * obj.template.level.toDouble()
            .pow(2.0) * 10 + Formulas.getRandomValue(
            1,
            obj.template.level * 100
        )) * amount).toInt() else ((obj.template.level.toDouble()
            .pow(2.0) * 10 + Formulas.getRandomValue(1, obj.template.level * 100)) * amount).toInt()
    }
}