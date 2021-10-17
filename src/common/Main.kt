package common

import game.GameServer
import communication.ComServer
import java.io.BufferedWriter
import java.io.PrintStream
import kotlin.jvm.JvmStatic
import java.io.UnsupportedEncodingException
import java.net.InetAddress
import java.lang.InterruptedException
import java.io.BufferedReader
import java.io.FileReader
import java.util.Calendar
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.lang.Exception
import java.util.ArrayList

object Main {

    /* REALM Server */
	@JvmField
	var MULTI_IP: String? = null
    @JvmField
	var MULTI_BDD_IP: String? = null
    @JvmField
	var REALM_DB_NAME: String? = null
    @JvmField
	var REALM_DB_USER: String? = null
    @JvmField
	var REALM_DB_PASS: String? = null

    /* Game Server */
	@JvmField
	var gameServer: GameServer? = null
    @JvmField
	var AUTH_KEY: String? = null
    private const val CONFIG_FILE = "config.txt"
    @JvmField
	var CONFIG_USE_IP = false
    var IP: String? = "127.0.0.1"
    var CONFIG_IP_LOOPBACK = true
    @JvmField
	var GAMESERVER_IP: String? = null
    @JvmField
	var CONFIG_GAME_PORT = 5555
    @JvmField
	var isInit = false
    @JvmField
	var isRunning = false
    @JvmField
	var isSaving = false
    @JvmField
	var CONFIG_DEBUG = false

    //SQL
	@JvmField
	var DB_HOST: String? = null
    @JvmField
	var DB_USER: String? = null
    @JvmField
	var DB_PASS: String? = null
    @JvmField
	var DB_NAME: String? = null

    //Timer
	@JvmField
	var FLOOD_TIME = (1 * 60000).toLong()
    @JvmField
	var CONFIG_SAVE_TIME = 30 * 60000
    @JvmField
	var CONFIG_LOAD_DELAY = 10 * 60000
    @JvmField
    var CONFIG_MOVER_MONSTRUOS = 10 * 30000
    @JvmField
	var CONFIG_RELOAD_MOB_DELAY = 300 * 60000

    //Message
	@JvmField
	var CONFIG_MOTD = ""
    @JvmField
	var CONFIG_MOTD_COLOR: String? = ""

    //Jugadores maximos
	@JvmField
	var CONFIG_PLAYER_LIMIT = 30

    //Configuration
	@JvmField
	var CONFIG_MAX_PERSOS = 5
    @JvmField
	var CONFIG_START_MAP: Short = 10298
    @JvmField
	var CONFIG_START_CELL = 314
    @JvmField
	var CONFIG_START_LEVEL = 1
    @JvmField
	var CONFIG_START_KAMAS = 0
    var CONFIG_ALLOW_MULTI = false
    @JvmField
	var CONFIG_ALLOW_MULE_PVP = false
    @JvmField
	var CONFIG_AURA_SYSTEM = false
    @JvmField
	var CONFIG_ZAAP = false
    @JvmField
	var CONFIG_CUSTOM_STARTMAP = false
    @JvmField
	var CONFIG_USE_MOBS = false
    @JvmField
	var CONFIG_LVL_PVP = 15

    //Rate Xp/Kamas/Drop/Honneur
	@JvmField
	var RATE_DROP = 1
    @JvmField
	var RATE_KAMAS = 1
    @JvmField
	var RATE_HONOR = 1
    @JvmField
	var RATE_PVM = 1
    var RATE_PVP = 1
    @JvmField
	var RATE_METIER = 1

    //Arene
	@JvmField
	var arenaMap = ArrayList<Int>(8)
    @JvmField
	var CONFIG_ARENA_TIMER = 10 * 60 * 1000 // 10 minutes

    //BDD
	@JvmField
	var CONFIG_DB_COMMIT = 30 * 1000

    //Inactivit?
	@JvmField
	var CONFIG_MAX_IDLE_TIME = 1800000 //En millisecondes

    //HDV
	@JvmField
	var NOTINHDV = ArrayList<Int>()

    //Abonnement
	@JvmField
	var USE_SUBSCRIBE = false

    //Comandos
    @JvmField
    var VER_COMANDOS = false

    //ComServer
	@JvmField
	var comServer: ComServer? = null
    @JvmField
	var COM_PORT = -1
    var com_Try = 0
    @JvmField
	var com_Running = false

    //Logs
	@JvmField
	var Log_GameSock: BufferedWriter? = null
    @JvmField
	var Log_Game: BufferedWriter? = null
    var Log_MJ: BufferedWriter? = null
    var Log_Shop: BufferedWriter? = null
    var Log_Errors: PrintStream? = null
    @JvmField
	var canLog = false
    @JvmStatic
    fun main(args: Array<String>) {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                closeServers()
            }
        }
        )
        try {
            System.setOut(PrintStream(System.out, true, "IBM850"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        println("==============================================================")
        println("AlexandriaEMU - SERVER")
        println("Por Player-xD, basado en AncestraR v54 - Gracias DIABU")
        println("==============================================================\n")
        print("Cargando la configuracion: ")
        loadConfiguration()
        isInit = true
        println("OK")
        print("Conectando a la base de datos: ")
        if (SQLManager.setUpConnexion()) println("OK") else {
            println("Conexion invalida")
            System.exit(0)
        }
        println("Utiliza monstruos: " + CONFIG_USE_MOBS)
        println("Creando el mundo:\n")
        val startTime = System.currentTimeMillis()
        World.createWorld()
        val endTime = System.currentTimeMillis()
        val differenceTime = (endTime - startTime) / 1000
        println("TODO CARGADO EN: $differenceTime s")
        isRunning = true
        println("\n")
        print("Creando el servidor de juego con el puerto " + CONFIG_GAME_PORT)
        var Ip: String? = ""
        try {
            Ip = InetAddress.getLocalHost().hostAddress
        } catch (e: Exception) {
            println(e.message)
            try {
                Thread.sleep(10000)
            } catch (e1: InterruptedException) {
            }
            System.exit(1)
        }
        Ip = IP
        gameServer = GameServer(Ip)
        println(": OK")
        if (CONFIG_USE_IP) println("IP del server " + IP + " encriptada " + GAMESERVER_IP)
        print("Creacion del COM PORT en el puerto " + COM_PORT)
        comServer = ComServer()
        println(": OK")
        println("\nAtento a nuevas conexiones")
    }

    private fun loadConfiguration() {
        var log = false
        try {
            val config = BufferedReader(FileReader(CONFIG_FILE))
            var line = config.readLine()
            while (config.readLine().also { line = it } != null) {
                if (line.split("=".toRegex()).toTypedArray().size == 1) continue
                val param = line.split("=".toRegex()).toTypedArray()[0].trim { it <= ' ' }
                var value = line.split("=".toRegex()).toTypedArray()[1].trim { it <= ' ' }
                if (param.equals("MULTI_IP", ignoreCase = true)) {
                    MULTI_IP = value
                } else if (param.equals("MULTI_BDD_IP", ignoreCase = true)) {
                    MULTI_BDD_IP = value
                } else if (param.equals("REALM_DB_USER", ignoreCase = true)) {
                    REALM_DB_USER = value
                } else if (param.equals("REALM_DB_PASS", ignoreCase = true)) {
                    if (value == null) value = ""
                    REALM_DB_PASS = value
                } else if (param.equals("REALM_DB_NAME", ignoreCase = true)) {
                    REALM_DB_NAME = value
                } else if (param.equals("DEBUG", ignoreCase = true)) {
                    if (value.equals("true", ignoreCase = true)) {
                        CONFIG_DEBUG = true
                    }
                } else if (param.equals("LOG", ignoreCase = true)) {
                    if (value.equals("true", ignoreCase = true)) {
                        log = true
                    }
                } else if (param.equals("USE_IP", ignoreCase = true)) {
                    if (value.equals("true", ignoreCase = true)) {
                        CONFIG_USE_IP = true
                    }
                } else if (param.equals("HOST_IP", ignoreCase = true)) {
                    IP = value
                } else if (param.equals("LOCALIP_LOOPBACK", ignoreCase = true)) {
                    if (value.equals("true", ignoreCase = true)) {
                        CONFIG_IP_LOOPBACK = true
                    }
                } else if (param.equals("AUTH_KEY", ignoreCase = true)) {
                    AUTH_KEY = value
                } else if (param.equals("GAME_PORT", ignoreCase = true)) {
                    CONFIG_GAME_PORT = value.toInt()
                } else if (param.equals("COM_PORT", ignoreCase = true)) {
                    COM_PORT = value.toInt()
                } else if (param.equals("MOTD", ignoreCase = true)) {
                    CONFIG_MOTD = line.split("=".toRegex(), 2).toTypedArray()[1]
                } else if (param.equals("MOTD_COLOR", ignoreCase = true)) {
                    CONFIG_MOTD_COLOR = value
                } else if (param.equals("PLAYER_LIMIT", ignoreCase = true)) {
                    CONFIG_PLAYER_LIMIT = value.toInt()
                } else if (param.equals("LOAD_ACTION_DELAY", ignoreCase = true)) {
                    CONFIG_LOAD_DELAY = value.toInt() * 60000
                } else if (param.equals("SAVE_TIME", ignoreCase = true)) {
                    CONFIG_MOVER_MONSTRUOS = value.toInt() * 30000
                } else if (param.equals("MOVER_MONSTRUOS", ignoreCase = true)) {
                    CONFIG_SAVE_TIME = value.toInt() * 60000
                } else if (param.equals("DB_HOST", ignoreCase = true)) {
                    DB_HOST = value
                } else if (param.equals("DB_USER", ignoreCase = true)) {
                    DB_USER = value
                } else if (param.equals("DB_PASS", ignoreCase = true)) {
                    if (value == null) value = ""
                    DB_PASS = value
                } else if (param.equals("DB_NAME", ignoreCase = true)) {
                    DB_NAME = value
                } else if (param.equals("XP_PVP", ignoreCase = true)) {
                    RATE_PVP = value.toInt()
                } else if (param.equals("XP_METIER", ignoreCase = true)) {
                    RATE_METIER = value.toInt()
                } else if (param.equals("XP_PVM", ignoreCase = true)) {
                    RATE_PVM = value.toInt()
                } else if (param.equals("DROP", ignoreCase = true)) {
                    RATE_DROP = value.toInt()
                } else if (param.equals("KAMAS", ignoreCase = true)) {
                    RATE_KAMAS = value.toInt()
                } else if (param.equals("HONOR", ignoreCase = true)) {
                    RATE_HONOR = value.toInt()
                } else if (param.equals("ALLOW_MULTI_ACCOUNT", ignoreCase = true)) {
                    CONFIG_ALLOW_MULTI = value.equals("true", ignoreCase = true)
                } else if (param.equals("MAX_PERSO_PAR_COMPTE", ignoreCase = true)) {
                    CONFIG_MAX_PERSOS = value.toInt()
                } else if (param.equals("USE_MOBS", ignoreCase = true)) {
                    CONFIG_USE_MOBS = value.equals("true", ignoreCase = true)
                } else if (param.equals("USE_CUSTOM_START", ignoreCase = true)) {
                    if (value.equals("true", ignoreCase = true)) {
                        CONFIG_CUSTOM_STARTMAP = true
                    }
                } else if (param.equals("START_MAP", ignoreCase = true)) {
                    CONFIG_START_MAP = value.toShort()
                } else if (param.equals("START_CELL", ignoreCase = true)) {
                    CONFIG_START_CELL = value.toInt()
                } else if (param.equals("START_LEVEL", ignoreCase = true)) {
                    CONFIG_START_LEVEL = value.toInt()
                    if (CONFIG_START_LEVEL < 1) CONFIG_START_LEVEL = 1
                    if (CONFIG_START_LEVEL > 200) CONFIG_START_LEVEL = 200
                } else if (param.equals("START_KAMAS", ignoreCase = true)) {
                    CONFIG_START_KAMAS = value.toInt()
                    if (CONFIG_START_KAMAS < 0) CONFIG_START_KAMAS = 0
                    if (CONFIG_START_KAMAS > 1000000000) CONFIG_START_KAMAS = 1000000000
                } else if (param.equals("ZAAP", ignoreCase = true)) {
                    if (value.equals("true", ignoreCase = true)) {
                        CONFIG_ZAAP = true
                    }
                } else if (param.equals("LVL_PVP", ignoreCase = true)) {
                    CONFIG_LVL_PVP = value.toInt()
                } else if (param.equals("ALLOW_MULE_PVP", ignoreCase = true)) {
                    CONFIG_ALLOW_MULE_PVP = value.equals("true", ignoreCase = true)
                } else if (param.equals("AURA_SYSTEM", ignoreCase = true)) {
                    CONFIG_AURA_SYSTEM = value.equals("true", ignoreCase = true)
                } else if (param.equals("MAX_IDLE_TIME", ignoreCase = true)) {
                    CONFIG_MAX_IDLE_TIME = value.toInt() * 60000
                } else if (param.equals("NOT_IN_HDV", ignoreCase = true)) {
                    for (curID in value.split(",".toRegex()).toTypedArray()) {
                        NOTINHDV.add(curID.toInt())
                    }
                } else if (param.equals("ARENA_MAP", ignoreCase = true)) {
                    for (curID in value.split(",".toRegex()).toTypedArray()) {
                        arenaMap.add(curID.toInt())
                    }
                } else if (param.equals("ARENA_TIMER", ignoreCase = true)) {
                    CONFIG_ARENA_TIMER = value.toInt() * 60000
                } else if (param.equals("USE_SUBSCRIBE", ignoreCase = true)) {
                    USE_SUBSCRIBE = value.equals("true", ignoreCase = true)
                }
            }
            if (MULTI_IP == null || MULTI_BDD_IP == null || REALM_DB_NAME == null || REALM_DB_USER == null || REALM_DB_PASS == null || AUTH_KEY == null || COM_PORT == -1 || DB_NAME == null || DB_HOST == null || DB_PASS == null || DB_USER == null) {
                throw Exception()
            }
        } catch (e: Exception) {
            println(e.message)
            println("Fichero de configuracion inexistente")
            println("Cerrando el servidor")
            System.exit(1)
        }
        if (CONFIG_DEBUG) Constants.DEBUG_MAP_LIMIT = 20000
        try {
            val date =
                Calendar.getInstance()[Calendar.DAY_OF_MONTH].toString() + "-" + (Calendar.getInstance()[Calendar.MONTH] + 1) + "-" + Calendar.getInstance()[Calendar.YEAR]
            if (log) {
                if (!File("Game_logs").exists()) {
                    File("Game_logs").mkdir()
                }
                if (!File("Error_logs").exists()) {
                    File("Error_logs").mkdir()
                }
                if (!File("Shop_logs").exists()) {
                    File("Shop_logs").mkdir()
                }
                if (!File("Gms_logs").exists()) {
                    File("Gms_logs").mkdir()
                }
                Log_Game = BufferedWriter(FileWriter("Game_logs/$date.txt", true))
                Log_GameSock = BufferedWriter(FileWriter("Game_logs/" + date + "_packets.txt", true))
                Log_Shop = BufferedWriter(FileWriter("Shop_logs/$date.txt", true))
                Log_MJ = BufferedWriter(FileWriter("Gms_logs/" + date + "_GM.txt", true))
                Log_Errors = PrintStream(File("Error_logs/" + date + "_error.txt"))
                Log_Errors!!.append("Abriendo el servidor\n")
                Log_Errors!!.flush()
                System.setErr(Log_Errors)
                canLog = true
                val str = "Abriendo el servidor\n"
                Log_GameSock!!.write(str)
                Log_Game!!.write(str)
                Log_MJ!!.write(str)
                Log_Shop!!.write(str)
                Log_GameSock!!.flush()
                Log_Game!!.flush()
                Log_MJ!!.flush()
                Log_Shop!!.flush()
            }
        } catch (e: IOException) {
            println("No se pudieron crear los logs")
            println(e.message)
            System.exit(1)
        }
    }

    @JvmStatic
	fun closeServers() {
        println("Cerrando el servidor")
        if (isRunning) {
            isRunning = false
            try {
                gameServer!!.kickAll()
            } catch (e: Exception) {
                println(e.message)
            }
            World.saveAll(null)
            SQLManager.closeCons()
        }
        println("Cerrando servidor: CERRADO")
        isRunning = false
    }

    @JvmStatic
	fun addToMjLog(str: String) {
        if (!canLog) return
        val date =
            Calendar.getInstance()[Calendar.HOUR_OF_DAY].toString() + ":" + Calendar.getInstance()[+Calendar.MINUTE] + ":" + Calendar.getInstance()[Calendar.SECOND]
        try {
            Log_MJ!!.write("[$date]$str")
            Log_MJ!!.newLine()
            Log_MJ!!.flush()
        } catch (e: IOException) {
        }
    }

    @JvmStatic
	fun addToShopLog(str: String) {
        if (!canLog) return
        val date =
            Calendar.getInstance()[Calendar.HOUR_OF_DAY].toString() + ":" + Calendar.getInstance()[+Calendar.MINUTE] + ":" + Calendar.getInstance()[Calendar.SECOND]
        try {
            Log_Shop!!.write("[$date]$str")
            Log_Shop!!.newLine()
            Log_Shop!!.flush()
        } catch (e: IOException) {
        }
    }

    @JvmStatic
	fun try_ComServer() {
        if (com_Try == 0 && isRunning) {
            try {
                print("Creation d'une nouvelle connexion avec le Realm (ComServer) ... ")
                com_Try = 1
                while (com_Running == false && isRunning) {
                    comServer = ComServer()
                    Thread.sleep(10000)
                }
                println("ComServer de nouveau operationnel !")
                com_Try = 0
            } catch (e: InterruptedException) {
            }
        }
    }
}