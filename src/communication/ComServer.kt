package communication

import common.Main
import common.Main.closeServers
import common.Main.try_ComServer
import common.World
import objects.Compte
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class ComServer : Runnable {

    private var _in: BufferedReader? = null
    private var _out: PrintWriter? = null

    init {
        try {
            val _s = Socket(Main.MULTI_IP, Main.COM_PORT)
            val _t = Thread(this)
            _t.isDaemon = true
            _t.start()
            _in = BufferedReader(InputStreamReader(_s.getInputStream()))
            _out = PrintWriter(_s.getOutputStream())
        } catch (e: Exception) {
            println("\nComServer : Connection au Realm impossible")
            println(e.message)
            Main.com_Running = false
            try_ComServer()
        }
    }

    override fun run() {
        try {
            var packet = StringBuilder()
            val charCur = CharArray(1)
            Main.com_Running = true
            try {
                _out!!.print("GA" + Main.AUTH_KEY + 0x00.toChar())
                _out!!.flush()
            } catch (e: Exception) {
                try {
                    Thread.sleep(1000)
                    println("ComServer : Erreur d'envoi du GA, renvoi ...")
                    _out!!.print("GA" + Main.AUTH_KEY + 0x00.toChar())
                    _out!!.flush()
                } catch (e1: Exception) {
                    println("ComServer : Erreur d'envoi du GA : " + e1.message)
                    Main.com_Running = false
                    closeServers()
                }
            }
            while (_in!!.read(charCur, 0, 1) != -1 && Main.isRunning) {
                if (charCur[0] != '\u0000' && charCur[0] != '\n' && charCur[0] != '\r') {
                    packet.append(charCur[0])
                } else if (packet.length > 0) {
                    if (Main.CONFIG_DEBUG) println("Exchange: Recv << $packet")
                    parsePacket(packet.toString())
                    packet = StringBuilder()
                }
            }
        } catch (e: IOException) {
            println("\nComServer : Serveur d'echange inlancable")
            println(e.message)
            Main.com_Running = false
            try_ComServer()
        }
    }

    fun sendChangeState(c: Char) {
        _out!!.print("S" + c + 0x00.toChar())
        _out!!.flush()
    }

    fun addBanIP(ip: String) {
        _out!!.print("RA" + ip + 0x00.toChar())
        _out!!.flush()
    }

    fun lockGMlevel(level: Int) {
        _out!!.print("RG" + level + 0x00.toChar())
        _out!!.flush()
    }

    fun sendGetOnline(str: String) {
        _out!!.print("GO" + str + 0x00.toChar())
        _out!!.flush()
    }

    fun parsePacket(packet: String) {
        when (packet[0]) {
            'A' -> when (packet[1]) {
                'W' -> {
                    var acc: Compte? = null
                    println("Ajout d'un compte au GameThread ...")
                    val AD = packet.substring(2).split("\\|".toRegex()).toTypedArray()
                    var guid = -1
                    var gmlvl = -1
                    var subscriberTime = 0
                    var name = ""
                    var pass = ""
                    var nickname = ""
                    var question = ""
                    var response = ""
                    var lastIp = ""
                    var lastConnectionDate = ""
                    var curIp = ""
                    var gifts = ""
                    var isBanned = false
                    try {
                        guid = AD[0].toInt()
                        name = AD[1]
                        pass = AD[2]
                        nickname = AD[3]
                        question = AD[4]
                        response = AD[5]
                        gmlvl = AD[6].toInt()
                        subscriberTime = AD[7].toInt()
                        isBanned = AD[8].toInt() != 0
                        lastIp = AD[9]
                        lastConnectionDate = AD[10]
                        curIp = AD[11]
                        gifts = if (AD.size == 12) "" else AD[12]
                    } catch (e: NumberFormatException) {
                        println("Création du compte échouée : " + e.message)
                    } finally {
                        acc = Compte(
                            guid,
                            name,
                            pass,
                            nickname,
                            question,
                            response,
                            gmlvl,
                            subscriberTime,
                            isBanned,
                            lastIp,
                            lastConnectionDate,
                            curIp,
                            gifts
                        )
                    }
                    if (acc != null && Main.gameServer!!.getWaitingCompte(acc._GUID) == null) {
                        println("Ajout du compte")
                        Main.gameServer!!.addWaitingCompte(acc)
                    } else if (acc != null && Main.gameServer!!.getWaitingCompte(acc._GUID) != null) {
                        println("Supression du compte")
                        Main.gameServer!!.delWaitingCompte(acc)
                        println("Ajout du compte")
                        Main.gameServer!!.addWaitingCompte(acc)
                    }
                    println("Ajout d'un compte au GameThread Termine")
                }
            }
            'L' -> when (packet[1]) {
                'O' -> {
                    val guid = packet.substring(2).toInt()
                    val acc = World.getCompte(guid)
                    println("Verification connexion GameThread ...")
                    if (acc != null) {
                        println("Compte existant, on le kick")
                        if (acc.gameThread != null) acc.gameThread.kick()
                    }
                    println("Verification connexion GameThread Termine")
                }
            }
            'G' -> when (packet[1]) {
                'O' -> {
                    val data: String = Main.CONFIG_PLAYER_LIMIT.toString() + ";" + World.getComptes().size.toString()
                    sendGetOnline(data)
                }
            }
        }
    }
}