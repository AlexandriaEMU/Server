package common

import com.mysql.jdbc.PreparedStatement
import common.Main.addToShopLog
import common.Main.closeServers
import common.SocketManager.GAME_SEND_MESSAGE
import common.SocketManager.GAME_SEND_STATS_PACKET
import common.World.*
import game.GameServer
import objects.*
import objects.Compte.EnemyList
import objects.Compte.FriendList
import objects.Guild.GuildMember
import objects.Mapa.MountPark
import objects.NPC_tmpl.NPC_question
import objects.NPC_tmpl.NPC_reponse
import objects.Objet.ObjTemplate
import objects.Others.Bank
import objects.Sort.SortStats
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

object SQLManager {

    lateinit var gameCon: Connection
    lateinit var realmCon: Connection
    private var timerCommit: Timer? = null
    private var needCommit = false

    @JvmStatic
    val nextPersonnageGuid: Int
        get() {
            try {
                val RS = executeQuery("SELECT guid FROM personnages ORDER BY guid DESC LIMIT 1;", Main.DB_NAME)
                if (!RS!!.first()) return 1
                var guid = RS.getInt("guid")
                guid++
                closeResultSet(RS)
                return guid
            } catch (e: SQLException) {
                GameServer.addToLog("SQL ERROR: " + e.message)
                e.printStackTrace()
                closeServers()
            }
            return 0
        }

    @JvmStatic
    val nextObjetID: Int
        get() {
            try {
                val RS = executeQuery("SELECT MAX(guid) AS max FROM items;", Main.DB_NAME)
                var guid = 1
                val found = RS!!.first()
                if (found) guid = RS.getInt("max")
                closeResultSet(RS)
                return guid
            } catch (e: SQLException) {
                GameServer.addToLog("SQL ERROR: " + e.message)
                e.printStackTrace()
                closeServers()
            }
            return 0
        }

    @Synchronized
    @Throws(SQLException::class)
    fun executeQuery(query: String?, DBNAME: String?): ResultSet? {
        if (!Main.isInit) return null
        val DB: Connection?
        DB = if (DBNAME == Main.DB_NAME) gameCon else realmCon
        val stat = DB.createStatement()
        val RS = stat.executeQuery(query)
        stat.queryTimeout = 300
        return RS
    }

    @Synchronized
    @Throws(SQLException::class)
    fun newTransact(baseQuery: String?, dbCon: Connection?): PreparedStatement {
        val toReturn = dbCon!!.prepareStatement(baseQuery) as PreparedStatement
        needCommit = true
        return toReturn
    }

    @JvmStatic
    @Synchronized
    fun commitTransacts() {
        try {
            if (gameCon.isClosed || realmCon.isClosed) {
                closeCons()
                setUpConnexion()
            }
            gameCon.commit()
            realmCon.commit()
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR:" + e.message)
            e.printStackTrace()
            commitTransacts()
        }
    }

    @Synchronized
    fun closeCons() {
        try {
            commitTransacts()
            gameCon.close()
            realmCon.close()
        } catch (e: Exception) {
            println("Erreur a la fermeture des connexions SQL:" + e.message)
            e.printStackTrace()
        }
    }

    fun setUpConnexion(): Boolean {
        return try {
            gameCon = DriverManager.getConnection(
                "jdbc:mysql://" + Main.DB_HOST + "/" + Main.DB_NAME,
                Main.DB_USER,
                Main.DB_PASS
            )
            gameCon.autoCommit = false
            realmCon = DriverManager.getConnection(
                "jdbc:mysql://" + Main.MULTI_BDD_IP + "/" + Main.REALM_DB_NAME,
                Main.REALM_DB_USER,
                Main.REALM_DB_PASS
            )
            realmCon.autoCommit = false
            if (!gameCon.isValid(1000) || !realmCon.isValid(1000)) {
                GameServer.addToLog("SQLError : Connexion a la BD invalide!")
                return false
            }
            needCommit = false
            TIMER(true)
            true
        } catch (e: SQLException) {
            println("SQL ERROR: " + e.message)
            e.printStackTrace()
            false
        }
    }

    private fun closeResultSet(RS: ResultSet?) {
        try {
            RS!!.statement.close()
            RS.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    private fun closePreparedStatement(p: PreparedStatement?) {
        try {
            p!!.clearParameters()
            p.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun UPDATE_ACCOUNT_DATA(acc: Compte) {
        try {
            val baseQuery = "UPDATE accounts SET " +
                    "`level` = ?," +
                    "`banned` = ?," +
                    "`curIP` = ?," +
                    "`logged` = ?" +
                    " WHERE `guid` = ?;"
            val p = newTransact(baseQuery, realmCon)
            p.setInt(1, acc._gmLvl)
            p.setInt(2, if (acc.isBanned) 1 else 0)
            p.setString(3, acc._curIP)
            p.setInt(4, if (acc.isOnline) 1 else 0)
            p.setInt(5, acc._GUID)
            p.executeUpdate()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun UPDATE_LASTCONNECTION_INFO(compte: Compte) {
        val baseQuery = "UPDATE accounts SET " +
                "`lastIP` = ?," +
                "`lastConnectionDate` = ?" +
                " WHERE `guid` = ?;"
        try {
            val p = newTransact(baseQuery, realmCon)
            p.setString(1, compte._curIP)
            p.setString(2, compte.lastConnectionDate)
            p.setInt(3, compte._GUID)
            p.executeUpdate()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun UPDATE_ACCOUNT_SUBSCRIBE(guid: Int, SubScribe: Int) {
        val baseQuery = "UPDATE accounts SET " +
                "`subscription` = ?" +
                " WHERE `guid` = ?;"
        try {
            val p = newTransact(baseQuery, realmCon)
            p.setInt(1, SubScribe)
            p.setInt(2, guid)
            p.executeUpdate()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun LOAD_ACCOUNTS_DATA() {
        try {
            val RS = executeQuery("SELECT * FROM account_data", Main.DB_NAME)
            while (RS!!.next()) {
                World.AddFriendList(RS.getInt("guid"), FriendList(RS.getString("friends")))
                World.AddEnemyList(RS.getInt("guid"), EnemyList(RS.getString("enemys")))
                val bk = Bank(RS.getInt("guid"), RS.getInt("bankKamas"), RS.getString("bankObj"))
                World.AddBank(bk)
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun LOAD_CRAFTS() {
        try {
            val RS = executeQuery("SELECT * from crafts;", Main.DB_NAME)
            while (RS!!.next()) {
                val m = ArrayList<Couple<Int, Int>>()
                var cont = true
                for (str in RS.getString("craft").split(";".toRegex()).toTypedArray()) {
                    try {
                        val tID = str.split("\\*".toRegex()).toTypedArray()[0].toInt()
                        val qua = str.split("\\*".toRegex()).toTypedArray()[1].toInt()
                        m.add(Couple(tID, qua))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        cont = false
                    }
                }
                //s'il y a eu une erreur de parsing, on ignore cette recette
                if (!cont) continue
                World.addCraft(
                    RS.getInt("id"),
                    m
                )
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun LOAD_GUILDS() {
        try {
            val RS = executeQuery("SELECT * from guilds;", Main.DB_NAME)
            while (RS!!.next()) {
                World.addGuild(
                    Guild(
                        RS.getInt("id"),
                        RS.getString("name"),
                        RS.getString("emblem"),
                        RS.getInt("lvl"),
                        RS.getLong("xp"),
                        RS.getInt("capital"),
                        RS.getInt("nbrmax"),
                        RS.getString("sorts"),
                        RS.getString("stats")
                    ), false
                )
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    //v0.00.0 - Eliminado de columnas inecesarias
    @JvmStatic
    fun LOAD_GUILD_MEMBERS() {
        try {
            val RS = executeQuery("SELECT * FROM guild_members;", Main.DB_NAME)
            while (RS!!.next()) {
                val G = World.getGuild(RS.getInt("guild")) ?: continue
                G.addMember(
                    RS.getInt("guid"),
                    RS.getInt("rank"),
                    RS.getByte("pxp"),
                    RS.getLong("xpdone"),
                    RS.getInt("rights"),
                    RS.getDate("lastConnection").toString().replace("-".toRegex(), "~")
                )
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun LOAD_MOUNTS() {
        try {
            val RS = executeQuery("SELECT * FROM mounts_data;", Main.DB_NAME)
            while (RS!!.next()) {
                World.addDragodinde(
                    Dragodinde(
                        RS.getInt("id"),
                        RS.getInt("color"),
                        RS.getInt("sexe"),
                        RS.getInt("amour"),
                        RS.getInt("endurance"),
                        RS.getInt("level"),
                        RS.getLong("xp"),
                        RS.getString("name"),
                        RS.getInt("fatigue"),
                        RS.getInt("energie"),
                        RS.getInt("reproductions"),
                        RS.getInt("maturite"),
                        RS.getInt("serenite"),
                        RS.getString("items"),
                        RS.getString("ancetres")
                    )
                )
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun LOAD_DROPS(): Int {
        var i = 0
        try {
            val RS = executeQuery("SELECT * from drops;", Main.DB_NAME)
            while (RS!!.next()) {
                val MT = World.getMonstre(RS.getInt("mob"))
                MT.addDrop(
                    Drop(
                        RS.getInt("item"),
                        RS.getInt("seuil"),
                        RS.getFloat("taux"),
                        RS.getInt("max")
                    )
                )
                i++
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
        return i
    }

    @JvmStatic
    fun LOAD_ITEMSETS() {
        try {
            val RS = executeQuery("SELECT * from itemsets;", Main.DB_NAME)
            while (RS!!.next()) {
                World.addItemSet(
                    ItemSet(
                        RS.getInt("id"),
                        RS.getString("items"),
                        RS.getString("bonus")
                    )
                )
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun LOAD_IOTEMPLATE() {
        try {
            val RS = executeQuery("SELECT * from interactive_objects_data;", Main.DB_NAME)
            while (RS!!.next()) {
                World.addIOTemplate(
                    IOTemplate(
                        RS.getInt("id"),
                        RS.getInt("respawn"),
                        RS.getInt("duration"),
                        RS.getInt("unknow"),
                        RS.getInt("walkable") == 1
                    )
                )
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun LOAD_MOUNTPARKS(): Int {
        var nbr = 0
        try {
            val RS = executeQuery("SELECT * from mountpark_data;", Main.DB_NAME)
            while (RS!!.next()) {
                val map = World.getCarte(RS.getShort("mapid")) ?: continue
                World.addMountPark(
                    MountPark(
                        RS.getInt("owner"),
                        map,
                        RS.getInt("cellid"),
                        RS.getInt("size"),
                        RS.getString("data"),
                        RS.getInt("guild"),
                        RS.getInt("price")
                    )
                )
                nbr++
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
            nbr = 0
        }
        return nbr
    }

    @JvmStatic
    fun LOAD_JOBS() {
        try {
            val RS = executeQuery("SELECT * from jobs_data;", Main.DB_NAME)
            while (RS!!.next()) {
                World.addJob(
                    Metier(
                        RS.getInt("id"),
                        RS.getString("tools"),
                        RS.getString("crafts")
                    )
                )
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun LOAD_AREA() {
        try {
            val RS = executeQuery("SELECT * from area_data;", Main.DB_NAME)
            while (RS!!.next()) {
                val A = World.Area(
                    RS.getInt("id"),
                    RS.getInt("superarea"),
                    RS.getString("name")
                )
                World.addArea(A)
                //on ajoute la zone au continent
                A._superArea.addArea(A)
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun LOAD_SUBAREA() {
        try {
            val RS = executeQuery("SELECT * from subarea_data;", Main.DB_NAME)
            while (RS!!.next()) {
                val SA = SubArea(
                    RS.getInt("id"),
                    RS.getInt("area"),
                    RS.getInt("alignement"),
                    RS.getString("name"),
                    RS.getInt("subscribeNeed") == 1
                )
                World.addSubArea(SA)
                //on ajoute la sous zone a la zone
                if (SA._area != null) SA._area.addSubArea(SA)
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun LOAD_NPCS(): Int {
        var nbr = 0
        try {
            val RS = executeQuery("SELECT * from npcs;", Main.DB_NAME)
            while (RS!!.next()) {
                val map = World.getCarte(RS.getShort("mapid")) ?: continue
                map.addNpc(RS.getInt("npcid"), RS.getInt("cellid"), RS.getInt("orientation"))
                nbr++
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
            nbr = 0
        }
        return nbr
    }

    @JvmStatic
    fun LOAD_PERCEPTEURS(): Int {
        var nbr = 0
        try {
            val RS = executeQuery("SELECT * from percepteurs;", Main.DB_NAME)
            while (RS!!.next()) {
                val map = World.getCarte(RS.getShort("mapid")) ?: continue
                World.addPerco(
                    Percepteur(
                        RS.getInt("guid"),
                        RS.getShort("mapid"),
                        RS.getInt("cellid"),
                        RS.getByte("orientation"),
                        RS.getInt("guild_id"),
                        RS.getShort("N1"),
                        RS.getShort("N2"),
                        RS.getString("objets"),
                        RS.getLong("kamas"),
                        RS.getLong("xp")
                    )
                )
                nbr++
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
            nbr = 0
        }
        return nbr
    }

    @JvmStatic
    fun LOAD_HOUSES(): Int {
        var nbr = 0
        try {
            val RS = executeQuery("SELECT * from houses;", Main.DB_NAME)
            while (RS!!.next()) {
                val map = World.getCarte(RS.getShort("map_id")) ?: continue
                World.addHouse(
                    House(
                        RS.getInt("id"),
                        RS.getShort("map_id"),
                        RS.getInt("cell_id"),
                        RS.getInt("owner_id"),
                        RS.getString("owner_pseudo"),
                        RS.getInt("sale"),
                        RS.getInt("guild_id"),
                        RS.getInt("access"),
                        RS.getString("key"),
                        RS.getInt("guild_rights"),
                        RS.getInt("mapid"),
                        RS.getInt("caseid")
                    )
                )
                nbr++
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
            nbr = 0
        }
        return nbr
    }

    @JvmStatic
    fun LOAD_PERSO_BY_ACCOUNT(accID: Int) {
        try {
            val RS = executeQuery("SELECT * FROM personnages WHERE account = '$accID';", Main.DB_NAME)
            while (RS!!.next()) {
                val stats = TreeMap<Int, Int>()
                stats[Constants.STATS_ADD_VITA] = RS.getInt("vitalite")
                stats[Constants.STATS_ADD_FORC] = RS.getInt("force")
                stats[Constants.STATS_ADD_SAGE] = RS.getInt("sagesse")
                stats[Constants.STATS_ADD_INTE] = RS.getInt("intelligence")
                stats[Constants.STATS_ADD_CHAN] = RS.getInt("chance")
                stats[Constants.STATS_ADD_AGIL] = RS.getInt("agilite")
                val perso = Personaje(
                    RS.getInt("guid"),
                    RS.getString("name"),
                    RS.getInt("sexe"),
                    RS.getInt("class"),
                    RS.getInt("color1"),
                    RS.getInt("color2"),
                    RS.getInt("color3"),
                    RS.getLong("kamas"),
                    RS.getInt("spellboost"),
                    RS.getInt("capital"),
                    RS.getInt("energy"),
                    RS.getInt("level"),
                    RS.getLong("xp"),
                    RS.getInt("size"),
                    RS.getInt("gfx"),
                    RS.getByte("alignement"),
                    RS.getInt("account"),
                    stats,
                    RS.getByte("seeFriend"),
                    RS.getByte("seeAlign"),
                    RS.getByte("seeSeller"),
                    RS.getString("canaux"),
                    RS.getShort("map"),
                    RS.getInt("cell"),
                    RS.getString("objets"),
                    RS.getString("storeObjets"),
                    RS.getInt("pdvper"),
                    RS.getString("spells"),
                    RS.getString("savepos"),
                    RS.getString("jobs"),
                    RS.getInt("mountxpgive"),
                    RS.getInt("mount"),
                    RS.getInt("honor"),
                    RS.getInt("deshonor"),
                    RS.getInt("alvl"),
                    RS.getString("zaaps"),
                    RS.getByte("title"),
                    RS.getInt("wife")
                )
                //V�rifications pr�-connexion
                perso.VerifAndChangeItemPlace()
                World.addPersonnage(perso)
                val guildId = isPersoInGuild(RS.getInt("guid"))
                if (guildId >= 0) {
                    perso.guildMember = World.getGuild(guildId).getMember(RS.getInt("guid"))
                }
                if (World.getCompte(accID) != null) World.getCompte(accID).addPerso(perso)
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
            closeServers()
        }
    }

    fun LOAD_PERSO(persoID: Int) {
        try {
            val RS = executeQuery("SELECT * FROM personnages WHERE guid = '$persoID';", Main.DB_NAME)
            var accID: Int
            while (RS!!.next()) {
                val stats = TreeMap<Int, Int>()
                stats[Constants.STATS_ADD_VITA] = RS.getInt("vitalite")
                stats[Constants.STATS_ADD_FORC] = RS.getInt("force")
                stats[Constants.STATS_ADD_SAGE] = RS.getInt("sagesse")
                stats[Constants.STATS_ADD_INTE] = RS.getInt("intelligence")
                stats[Constants.STATS_ADD_CHAN] = RS.getInt("chance")
                stats[Constants.STATS_ADD_AGIL] = RS.getInt("agilite")
                accID = RS.getInt("account")
                val perso = Personaje(
                    RS.getInt("guid"),
                    RS.getString("name"),
                    RS.getInt("sexe"),
                    RS.getInt("class"),
                    RS.getInt("color1"),
                    RS.getInt("color2"),
                    RS.getInt("color3"),
                    RS.getLong("kamas"),
                    RS.getInt("spellboost"),
                    RS.getInt("capital"),
                    RS.getInt("energy"),
                    RS.getInt("level"),
                    RS.getLong("xp"),
                    RS.getInt("size"),
                    RS.getInt("gfx"),
                    RS.getByte("alignement"),
                    accID,
                    stats,
                    RS.getByte("seeFriend"),
                    RS.getByte("seeAlign"),
                    RS.getByte("seeSeller"),
                    RS.getString("canaux"),
                    RS.getShort("map"),
                    RS.getInt("cell"),
                    RS.getString("objets"),
                    RS.getString("storeObjets"),
                    RS.getInt("pdvper"),
                    RS.getString("spells"),
                    RS.getString("savepos"),
                    RS.getString("jobs"),
                    RS.getInt("mountxpgive"),
                    RS.getInt("mount"),
                    RS.getInt("honor"),
                    RS.getInt("deshonor"),
                    RS.getInt("alvl"),
                    RS.getString("zaaps"),
                    RS.getByte("title"),
                    RS.getInt("wife")
                )
                //V�rifications pr�-connexion
                perso.VerifAndChangeItemPlace()
                World.addPersonnage(perso)
                val guildId = isPersoInGuild(RS.getInt("guid"))
                if (guildId >= 0) {
                    perso.guildMember = World.getGuild(guildId).getMember(RS.getInt("guid"))
                }
                if (World.getCompte(accID) != null) World.getCompte(accID).addPerso(perso)
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
            closeServers()
        }
    }

    @JvmStatic
    fun LOAD_PERSOS() {
        try {
            val RS = executeQuery("SELECT * FROM personnages;", Main.DB_NAME)
            while (RS!!.next()) {
                val stats = TreeMap<Int, Int>()
                stats[Constants.STATS_ADD_VITA] = RS.getInt("vitalite")
                stats[Constants.STATS_ADD_FORC] = RS.getInt("force")
                stats[Constants.STATS_ADD_SAGE] = RS.getInt("sagesse")
                stats[Constants.STATS_ADD_INTE] = RS.getInt("intelligence")
                stats[Constants.STATS_ADD_CHAN] = RS.getInt("chance")
                stats[Constants.STATS_ADD_AGIL] = RS.getInt("agilite")
                val perso = Personaje(
                    RS.getInt("guid"),
                    RS.getString("name"),
                    RS.getInt("sexe"),
                    RS.getInt("class"),
                    RS.getInt("color1"),
                    RS.getInt("color2"),
                    RS.getInt("color3"),
                    RS.getLong("kamas"),
                    RS.getInt("spellboost"),
                    RS.getInt("capital"),
                    RS.getInt("energy"),
                    RS.getInt("level"),
                    RS.getLong("xp"),
                    RS.getInt("size"),
                    RS.getInt("gfx"),
                    RS.getByte("alignement"),
                    RS.getInt("account"),
                    stats,
                    RS.getByte("seeFriend"),
                    RS.getByte("seeAlign"),
                    RS.getByte("seeSeller"),
                    RS.getString("canaux"),
                    RS.getShort("map"),
                    RS.getInt("cell"),
                    RS.getString("objets"),
                    RS.getString("storeObjets"),
                    RS.getInt("pdvper"),
                    RS.getString("spells"),
                    RS.getString("savepos"),
                    RS.getString("jobs"),
                    RS.getInt("mountxpgive"),
                    RS.getInt("mount"),
                    RS.getInt("honor"),
                    RS.getInt("deshonor"),
                    RS.getInt("alvl"),
                    RS.getString("zaaps"),
                    RS.getByte("title"),
                    RS.getInt("wife")
                )
                //V�rifications pr�-connexion
                perso.VerifAndChangeItemPlace()
                World.addPersonnage(perso)
                if (World.getCompte(RS.getInt("account")) != null) World.getCompte(RS.getInt("account")).addPerso(perso)
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
            closeServers()
        }
    }

    @JvmStatic
    fun DELETE_PERSO_IN_BDD(perso: Personaje): Boolean {
        val guid = perso._GUID
        var baseQuery = "DELETE FROM personnages WHERE guid = ?;"
        return try {
            var p = newTransact(baseQuery, gameCon)
            p.setInt(1, guid)
            p.execute()
            if (perso.getItemsIDSplitByChar(",") != "") {
                baseQuery = "DELETE FROM items WHERE guid IN (?);"
                p = newTransact(baseQuery, gameCon)
                p.setString(1, perso.getItemsIDSplitByChar(","))
                p.execute()
            }
            if (perso.getStoreItemsIDSplitByChar(",") != "") {
                baseQuery = "DELETE FROM items WHERE guid IN (?);"
                p = newTransact(baseQuery, gameCon)
                p.setString(1, perso.getStoreItemsIDSplitByChar(","))
                p.execute()
            }
            if (perso.mount != null) {
                baseQuery = "DELETE FROM mounts_data WHERE id = ?"
                p = newTransact(baseQuery, gameCon)
                p.setInt(1, perso.mount._id)
                p.execute()
                World.delDragoByID(perso.mount._id)
            }
            closePreparedStatement(p)
            true
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
            GameServer.addToLog("Game: Supression du personnage echouee")
            false
        }
    }

    @JvmStatic
    fun ADD_PERSO_IN_BDD(perso: Personaje): Boolean {
        val baseQuery =
            "INSERT INTO personnages( `guid` , `name` , `sexe` , `class` , `color1` , `color2` , `color3` , `kamas` , `spellboost` , `capital` , `energy` , `level` , `xp` , `size` , `gfx` , `account`,`cell`,`map`,`spells`,`objets`, `storeObjets`)" +
                    " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'', '');"
        return try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, perso._GUID)
            p.setString(2, perso._name)
            p.setInt(3, perso._sexe)
            p.setInt(4, perso._classe)
            p.setInt(5, perso._color1)
            p.setInt(6, perso._color2)
            p.setInt(7, perso._color3)
            p.setLong(8, perso._kamas)
            p.setInt(9, perso._spellPts)
            p.setInt(10, perso._capital)
            p.setInt(11, perso._energy)
            p.setInt(12, perso._lvl)
            p.setLong(13, perso._curExp)
            p.setInt(14, perso._size)
            p.setInt(15, perso._gfxID)
            p.setInt(16, perso.accID)
            p.setInt(17, perso._curCell.id)
            p.setInt(18, perso._curCarte._id.toInt())
            p.setString(19, perso.parseSpellToDB())
            p.execute()
            closePreparedStatement(p)
            true
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
            GameServer.addToLog("Game: Creation du personnage echouee")
            false
        }
    }

    @JvmStatic
    fun LOAD_EXP() {
        try {
            val RS = executeQuery("SELECT * from experience;", Main.DB_NAME)
            while (RS!!.next()) World.addExpLevel(
                RS.getInt("lvl"),
                ExpLevel(RS.getLong("perso"), RS.getInt("metier"), RS.getInt("dinde"), RS.getInt("pvp"))
            )
            closeResultSet(RS)
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            System.exit(1)
        }
    }

    @JvmStatic
    fun LOAD_TRIGGERS(): Int {
        try {
            var nbr = 0
            val RS = executeQuery("SELECT * FROM `scripted_cells`", Main.DB_NAME)
            while (RS!!.next()) {
                if (World.getCarte(RS.getShort("MapID")) == null) continue
                if (World.getCarte(RS.getShort("MapID")).getCase(RS.getInt("CellID")) == null) continue
                when (RS.getInt("EventID")) {
                    1 -> World.getCarte(RS.getShort("MapID")).getCase(RS.getInt("CellID")).addOnCellStopAction(
                        RS.getInt("ActionID"), RS.getString("ActionsArgs"), RS.getString("Conditions")
                    )
                    else -> GameServer.addToLog("Action Event " + RS.getInt("EventID") + " non implante")
                }
                nbr++
            }
            closeResultSet(RS)
            return nbr
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            System.exit(1)
        }
        return 0
    }

    @JvmStatic
    fun LOAD_MAPS() {
        try {
            var RS: ResultSet?
            RS = executeQuery("SELECT  * from maps LIMIT " + Constants.DEBUG_MAP_LIMIT + ";", Main.DB_NAME)
            while (RS!!.next()) {
                World.addCarte(
                    Mapa(
                        RS.getShort("id"),
                        RS.getString("date"),
                        RS.getByte("width"),
                        RS.getByte("heigth"),
                        RS.getString("key"),
                        RS.getString("places"),
                        RS.getString("mapData"),
                        RS.getString("cells"),
                        RS.getString("monsters"),
                        RS.getString("mappos"),
                        RS.getByte("numgroup"),
                        RS.getByte("groupmaxsize")
                    )
                )
            }
            closeResultSet(RS)
            RS = executeQuery("SELECT  * from mobgroups_fix;", Main.DB_NAME)
            while (RS!!.next()) {
                val c = World.getCarte(RS.getShort("mapid")) ?: continue
                if (c.getCase(RS.getInt("cellid")) == null) continue
                c.addStaticGroup(RS.getInt("cellid"), RS.getString("groupData"))
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            System.exit(1)
        }
    }

    @JvmStatic
    fun SAVE_PERSONNAGE(_perso: Personaje, saveAll: Boolean) {
        var baseQuery = "UPDATE `personnages` SET " +
                "`kamas`= ?," +
                "`spellboost`= ?," +
                "`capital`= ?," +
                "`energy`= ?," +
                "`level`= ?," +
                "`xp`= ?," +
                "`size` = ?," +
                "`gfx`= ?," +
                "`alignement`= ?," +
                "`honor`= ?," +
                "`deshonor`= ?," +
                "`alvl`= ?," +
                "`vitalite`= ?," +
                "`force`= ?," +
                "`sagesse`= ?," +
                "`intelligence`= ?," +
                "`chance`= ?," +
                "`agilite`= ?," +
                "`seeSpell`= ?," +
                "`seeFriend`= ?," +
                "`seeAlign`= ?," +
                "`seeSeller`= ?," +
                "`canaux`= ?," +
                "`map`= ?," +
                "`cell`= ?," +
                "`pdvper`= ?," +
                "`spells`= ?," +
                "`objets`= ?," +
                "`storeObjets`= ?," +
                "`savepos`= ?," +
                "`zaaps`= ?," +
                "`jobs`= ?," +
                "`mountxpgive`= ?," +
                "`mount`= ?," +
                "`title`= ?," +
                "`wife`= ?" +
                " WHERE `personnages`.`guid` = ? LIMIT 1 ;"
        var p: PreparedStatement? = null
        try {
            p = newTransact(baseQuery, gameCon)
            p.setLong(1, _perso._kamas)
            p.setInt(2, _perso._spellPts)
            p.setInt(3, _perso._capital)
            p.setInt(4, _perso._energy)
            p.setInt(5, _perso._lvl)
            p.setLong(6, _perso._curExp)
            p.setInt(7, _perso._size)
            p.setInt(8, _perso._gfxID)
            p.setInt(9, _perso._align.toInt())
            p.setInt(10, _perso._honor)
            p.setInt(11, _perso.deshonor)
            p.setInt(12, _perso.aLvl)
            p.setInt(13, _perso._baseStats.getEffect(Constants.STATS_ADD_VITA))
            p.setInt(14, _perso._baseStats.getEffect(Constants.STATS_ADD_FORC))
            p.setInt(15, _perso._baseStats.getEffect(Constants.STATS_ADD_SAGE))
            p.setInt(16, _perso._baseStats.getEffect(Constants.STATS_ADD_INTE))
            p.setInt(17, _perso._baseStats.getEffect(Constants.STATS_ADD_CHAN))
            p.setInt(18, _perso._baseStats.getEffect(Constants.STATS_ADD_AGIL))
            p.setInt(19, if (_perso.is_showSpells) 1 else 0)
            p.setInt(20, if (_perso.is_showFriendConnection) 1 else 0)
            p.setInt(21, if (_perso.is_showWings) 1 else 0)
            p.setInt(22, if (_perso.is_showSeller) 1 else 0)
            p.setString(23, _perso._canaux)
            p.setInt(24, _perso._curCarte._id.toInt())
            p.setInt(25, _perso._curCell.id)
            p.setInt(26, _perso._pdvper)
            p.setString(27, _perso.parseSpellToDB())
            p.setString(28, _perso.parseObjetsToDB())
            p.setString(29, _perso.parseStoreItemstoBD())
            p.setString(30, _perso._savePos)
            p.setString(31, _perso.parseZaaps())
            p.setString(32, _perso.parseJobData())
            p.setInt(33, _perso.mountXpGive)
            p.setInt(34, if (_perso.mount != null) _perso.mount._id else -1)
            p.setByte(35, _perso._title)
            p.setInt(36, _perso.wife)
            p.setInt(37, _perso._GUID)
            p.executeUpdate()
            if (_perso.guildMember != null) UPDATE_GUILDMEMBER(_perso.guildMember)
            if (_perso.mount != null) UPDATE_MOUNT_INFOS(_perso.mount)
            GameServer.addToLog("Personnage " + _perso._name + " sauvegarde")
        } catch (e: Exception) {
            println("Game: SQL ERROR: " + e.message)
            println("Requete: $baseQuery")
            println("Le personnage n'a pas ete sauvegarde")
            System.exit(1)
        }
        if (saveAll) {
            baseQuery = "UPDATE `items` SET qua = ?, pos= ?, stats = ?" +
                    " WHERE guid = ?;"
            try {
                p = newTransact(baseQuery, gameCon)
            } catch (e1: SQLException) {
                e1.printStackTrace()
            }
            for (idStr in _perso.getItemsIDSplitByChar(":").split(":".toRegex()).toTypedArray()) {
                try {
                    val guid = idStr.toInt()
                    val obj = World.getObjet(guid) ?: continue
                    p!!.setInt(1, obj.quantity)
                    p.setInt(2, obj.position)
                    p.setString(3, obj.parseStatsString())
                    p.setInt(4, idStr.toInt())
                    p.execute()
                } catch (e: Exception) {
                    continue
                }
            }
            if (_perso._compte == null) return
            for (idStr in _perso._compte.getBankItemsIDSplitByChar(":").split(":".toRegex()).toTypedArray())  //Banque
            {
                try {
                    val guid = idStr.toInt()
                    val obj = World.getObjet(guid) ?: continue
                    p!!.setInt(1, obj.quantity)
                    p.setInt(2, obj.position)
                    p.setString(3, obj.parseStatsString())
                    p.setInt(4, idStr.toInt())
                    p.execute()
                } catch (e: Exception) {
                    continue
                }
            }
            UPDATE_BANK(_perso._compte.bank)
            UPDATE_FL_AND_EL(
                _perso._compte._GUID,
                _perso._compte.GetFriends().parseFriends(),
                _perso._compte.GetEnemys().parseEnemys()
            )
        }
        closePreparedStatement(p)
    }

    @JvmStatic
    fun LOAD_SORTS() {
        try {
            val RS = executeQuery("SELECT  * from sorts;", Main.DB_NAME)
            while (RS!!.next()) {
                val id = RS.getInt("id")
                val sort = Sort(id, RS.getInt("sprite"), RS.getString("spriteInfos"), RS.getString("effectTarget"))
                val l1 = parseSortStats(id, 1, RS.getString("lvl1"))
                val l2 = parseSortStats(id, 2, RS.getString("lvl2"))
                val l3 = parseSortStats(id, 3, RS.getString("lvl3"))
                val l4 = parseSortStats(id, 4, RS.getString("lvl4"))
                var l5: SortStats? = null
                if (!RS.getString("lvl5").equals("-1", ignoreCase = true)) l5 =
                    parseSortStats(id, 5, RS.getString("lvl5"))
                var l6: SortStats? = null
                if (!RS.getString("lvl6").equals("-1", ignoreCase = true)) l6 =
                    parseSortStats(id, 6, RS.getString("lvl6"))
                sort.addSortStats(1, l1)
                sort.addSortStats(2, l2)
                sort.addSortStats(3, l3)
                sort.addSortStats(4, l4)
                sort.addSortStats(5, l5)
                sort.addSortStats(6, l6)
                World.addSort(sort)
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            System.exit(1)
        }
    }

    @JvmStatic
    fun LOAD_OBJ_TEMPLATE() {
        try {
            val RS = executeQuery("SELECT  * from item_template;", Main.DB_NAME)
            while (RS!!.next()) {
                World.addObjTemplate(
                    ObjTemplate(
                        RS.getInt("id"),
                        RS.getString("statsTemplate"),
                        RS.getString("name"),
                        RS.getInt("type"),
                        RS.getInt("level"),
                        RS.getInt("pod"),
                        RS.getInt("prix"),
                        RS.getInt("panoplie"),
                        RS.getString("condition"),
                        RS.getString("armesInfos")
                    )
                )
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            System.exit(1)
        }
    }

    private fun parseSortStats(id: Int, lvl: Int, str: String): SortStats? {
        return try {
            var stats: SortStats? = null
            val stat = str.split(",".toRegex()).toTypedArray()
            val effets = stat[0]
            val CCeffets = stat[1]
            var PACOST = 6
            try {
                PACOST = stat[2].trim { it <= ' ' }.toInt()
            } catch (e: NumberFormatException) {
            }
            val POm = stat[3].trim { it <= ' ' }.toInt()
            val POM = stat[4].trim { it <= ' ' }.toInt()
            val TCC = stat[5].trim { it <= ' ' }.toInt()
            val TEC = stat[6].trim { it <= ' ' }.toInt()
            val line = stat[7].trim { it <= ' ' }.equals("true", ignoreCase = true)
            val LDV = stat[8].trim { it <= ' ' }.equals("true", ignoreCase = true)
            val emptyCell = stat[9].trim { it <= ' ' }.equals("true", ignoreCase = true)
            val MODPO = stat[10].trim { it <= ' ' }.equals("true", ignoreCase = true)
            //int unk = Integer.parseInt(stat[11]);//All 0
            val MaxByTurn = stat[12].trim { it <= ' ' }.toInt()
            val MaxByTarget = stat[13].trim { it <= ' ' }.toInt()
            val CoolDown = stat[14].trim { it <= ' ' }.toInt()
            val type = stat[15].trim { it <= ' ' }
            val level = stat[stat.size - 2].trim { it <= ' ' }.toInt()
            val endTurn = stat[19].trim { it <= ' ' }.equals("true", ignoreCase = true)
            stats = SortStats(
                id,
                lvl,
                PACOST,
                POm,
                POM,
                TCC,
                TEC,
                line,
                LDV,
                emptyCell,
                MODPO,
                MaxByTurn,
                MaxByTarget,
                CoolDown,
                level,
                endTurn,
                effets,
                CCeffets,
                type
            )
            stats
        } catch (e: Exception) {
            e.printStackTrace()
            var nbr = 0
            println("[DEBUG]Sort $id lvl $lvl")
            for (z in str.split(",".toRegex()).toTypedArray()) {
                println("[DEBUG]$nbr $z")
                nbr++
            }
            System.exit(1)
            null
        }
    }

    @JvmStatic
    fun LOAD_MOB_TEMPLATE() {
        try {
            val RS = executeQuery("SELECT * FROM monsters;", Main.DB_NAME)
            while (RS!!.next()) {
                val id = RS.getInt("id")
                val gfxID = RS.getInt("gfxID")
                val align = RS.getInt("align")
                val colors = RS.getString("colors")
                val grades = RS.getString("grades")
                val spells = RS.getString("spells")
                val stats = RS.getString("stats")
                val pdvs = RS.getString("pdvs")
                val pts = RS.getString("points")
                val inits = RS.getString("inits")
                val mK = RS.getInt("minKamas")
                val MK = RS.getInt("maxKamas")
                val IAType = RS.getInt("AI_Type")
                val xp = RS.getString("exps")
                var capturable: Boolean
                capturable = RS.getInt("capturable") == 1
                World.addMobTemplate(
                    id,
                    Monstre(
                        id,
                        gfxID,
                        align,
                        colors,
                        grades,
                        spells,
                        stats,
                        pdvs,
                        pts,
                        inits,
                        mK,
                        MK,
                        xp,
                        IAType,
                        capturable
                    )
                )
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            System.exit(1)
        }
    }

    @JvmStatic
    fun LOAD_NPC_TEMPLATE() {
        try {
            val RS = executeQuery("SELECT * FROM npc_template;", Main.DB_NAME)
            while (RS!!.next()) {
                val id = RS.getInt("id")
                val bonusValue = RS.getInt("bonusValue")
                val gfxID = RS.getInt("gfxID")
                val scaleX = RS.getInt("scaleX")
                val scaleY = RS.getInt("scaleY")
                val sex = RS.getInt("sex")
                val color1 = RS.getInt("color1")
                val color2 = RS.getInt("color2")
                val color3 = RS.getInt("color3")
                val access = RS.getString("accessories")
                val extraClip = RS.getInt("extraClip")
                val customArtWork = RS.getInt("customArtWork")
                val initQId = RS.getInt("initQuestion")
                val ventes = RS.getString("ventes")
                World.addNpcTemplate(
                    NPC_tmpl(
                        id,
                        bonusValue,
                        gfxID,
                        scaleX,
                        scaleY,
                        sex,
                        color1,
                        color2,
                        color3,
                        access,
                        extraClip,
                        customArtWork,
                        initQId,
                        ventes
                    )
                )
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            System.exit(1)
        }
    }

    @JvmStatic
    fun SAVE_NEW_ITEM(item: Objet) {
        try {
            val baseQuery = "REPLACE INTO `items` VALUES(?,?,?,?,?);"
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, item.guid)
            p.setInt(2, item.template.id)
            p.setInt(3, item.quantity)
            p.setInt(4, item.position)
            p.setString(5, item.parseStatsString())
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun SAVE_NEW_FIXGROUP(mapID: Int, cellID: Int, groupData: String?): Boolean {
        try {
            val baseQuery = "REPLACE INTO `mobgroups_fix` VALUES(?,?,?)"
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, mapID)
            p.setInt(2, cellID)
            p.setString(3, groupData)
            p.execute()
            closePreparedStatement(p)
            return true
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return false
    }

    @JvmStatic
    fun LOAD_NPC_QUESTIONS() {
        try {
            val RS = executeQuery("SELECT * FROM npc_questions;", Main.DB_NAME)
            while (RS!!.next()) {
                World.addNPCQuestion(
                    NPC_question(
                        RS.getInt("ID"),
                        RS.getString("responses"),
                        RS.getString("params"),
                        RS.getString("cond"),
                        RS.getInt("ifFalse")
                    )
                )
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            System.exit(1)
        }
    }

    @JvmStatic
    fun LOAD_NPC_ANSWERS() {
        try {
            val RS = executeQuery("SELECT * FROM npc_reponses_actions;", Main.DB_NAME)
            while (RS!!.next()) {
                val id = RS.getInt("ID")
                val type = RS.getInt("type")
                val args = RS.getString("args")
                if (World.getNPCreponse(id) == null) World.addNPCreponse(NPC_reponse(id))
                World.getNPCreponse(id).addAction(Action(type, args, ""))
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            System.exit(1)
        }
    }

    @JvmStatic
    fun LOAD_ENDFIGHT_ACTIONS(): Int {
        var nbr = 0
        try {
            val RS = executeQuery("SELECT * FROM endfight_action;", Main.DB_NAME)
            while (RS!!.next()) {
                val map = World.getCarte(RS.getShort("map")) ?: continue
                map.addEndFightAction(
                    RS.getInt("fighttype"),
                    Action(RS.getInt("action"), RS.getString("args"), RS.getString("cond"))
                )
                nbr++
            }
            closeResultSet(RS)
            return nbr
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            System.exit(1)
        }
        return nbr
    }

    @JvmStatic
    fun LOAD_ITEM_ACTIONS(): Int {
        var nbr = 0
        try {
            val RS = executeQuery("SELECT * FROM use_item_actions;", Main.DB_NAME)
            while (RS!!.next()) {
                val id = RS.getInt("template")
                val type = RS.getInt("type")
                val args = RS.getString("args")
                if (World.getObjTemplate(id) == null) continue
                World.getObjTemplate(id).addAction(Action(type, args, ""))
                nbr++
            }
            closeResultSet(RS)
            return nbr
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            System.exit(1)
        }
        return nbr
    }

    fun LOAD_ITEMS(ids: String) {
        val req = "SELECT * FROM items WHERE guid IN ($ids);"
        try {
            val RS = executeQuery(req, Main.DB_NAME)
            while (RS!!.next()) {
                val guid = RS.getInt("guid")
                val tempID = RS.getInt("template")
                val qua = RS.getInt("qua")
                val pos = RS.getInt("pos")
                val stats = RS.getString("stats")
                World.addObjet(
                    World.newObjet(
                        guid,
                        tempID,
                        qua,
                        pos,
                        stats
                    ),
                    false
                )
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            println("Game: SQL ERROR: " + e.message)
            println("Requete: \n$req")
            System.exit(1)
        }
    }

    @JvmStatic
    fun DELETE_ITEM(guid: Int) {
        val baseQuery = "DELETE FROM items WHERE guid = ?;"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, guid)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
    fun SAVE_ITEM(item: Objet) {
        val baseQuery = "REPLACE INTO `items` VALUES (?,?,?,?,?);"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, item.guid)
            p.setInt(2, item.template.id)
            p.setInt(3, item.quantity)
            p.setInt(4, item.position)
            p.setString(5, item.parseStatsString())
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
    fun CREATE_MOUNT(DD: Dragodinde) {
        val baseQuery = "REPLACE INTO `mounts_data`(`id`,`color`,`sexe`,`name`,`xp`,`level`," +
                "`endurance`,`amour`,`maturite`,`serenite`,`reproductions`,`fatigue`,`items`," +
                "`ancetres`,`energie`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, DD._id)
            p.setInt(2, DD._color)
            p.setInt(3, DD._sexe)
            p.setString(4, DD._nom)
            p.setLong(5, DD._exp)
            p.setInt(6, DD._level)
            p.setInt(7, DD._endurance)
            p.setInt(8, DD._amour)
            p.setInt(9, DD._maturite)
            p.setInt(10, DD._serenite)
            p.setInt(11, DD._reprod)
            p.setInt(12, DD._fatigue)
            p.setString(13, DD.itemsId)
            p.setString(14, DD._ancetres)
            p.setInt(15, DD._energie)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
    fun REMOVE_MOUNT(DID: Int) {
        val baseQuery = "DELETE FROM `mounts_data` WHERE `id` = ?;"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, DID)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
    fun UPDATE_MOUNT_INFOS(DD: Dragodinde) {
        val baseQuery = "UPDATE mounts_data SET " +
                "`name` = ?," +
                "`xp` = ?," +
                "`level` = ?," +
                "`endurance` = ?," +
                "`amour` = ?," +
                "`maturite` = ?," +
                "`serenite` = ?," +
                "`reproductions` = ?," +
                "`fatigue` = ?," +
                "`energie` = ?," +
                "`ancetres` = ?," +
                "`items` = ?" +
                " WHERE `id` = ?;"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setString(1, DD._nom)
            p.setLong(2, DD._exp)
            p.setInt(3, DD._level)
            p.setInt(4, DD._endurance)
            p.setInt(5, DD._amour)
            p.setInt(6, DD._maturite)
            p.setInt(7, DD._serenite)
            p.setInt(8, DD._reprod)
            p.setInt(9, DD._fatigue)
            p.setInt(10, DD._energie)
            p.setString(11, DD._ancetres)
            p.setString(12, DD.itemsId)
            p.setInt(13, DD._id)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            GameServer.addToLog("Query: $baseQuery")
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun SAVE_MOUNTPARK(MP: MountPark) {
        val baseQuery =
            "REPLACE INTO `mountpark_data`( `mapid` , `cellid`, `size` , `owner` , `guild` , `price` , `data` )" +
                    " VALUES (?,?,?,?,?,?,?);"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, MP._map._id.toInt())
            p.setInt(2, MP._cellid)
            p.setInt(3, MP._size)
            p.setInt(4, MP._owner)
            p.setInt(5, if (MP._guild == null) -1 else MP._guild._id)
            p.setInt(6, MP._price)
            p.setString(7, MP.parseDBData())
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
    fun UPDATE_MOUNTPARK(MP: MountPark) {
        val baseQuery = "UPDATE `mountpark_data` SET " +
                "`data` = ?" +
                " WHERE mapid = ?;"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setString(1, MP.parseDBData())
            p.setShort(2, MP._map._id)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
    }

    fun SAVE_TRIGGER(mapID1: Int, cellID1: Int, action: Int, event: Int, args: String?, cond: String?): Boolean {
        val baseQuery = "REPLACE INTO `scripted_cells`" +
                " VALUES (?,?,?,?,?,?);"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, mapID1)
            p.setInt(2, cellID1)
            p.setInt(3, action)
            p.setInt(4, event)
            p.setString(5, args)
            p.setString(6, cond)
            p.execute()
            closePreparedStatement(p)
            return true
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
        return false
    }

    fun REMOVE_TRIGGER(mapID: Int, cellID: Int): Boolean {
        val baseQuery = "DELETE FROM `scripted_cells` WHERE " +
                "`MapID` = ? AND " +
                "`CellID` = ?;"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, mapID)
            p.setInt(2, cellID)
            p.execute()
            closePreparedStatement(p)
            return true
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
        return false
    }

    fun SAVE_MAP_DATA(map: Mapa): Boolean {
        val baseQuery = "UPDATE `maps` SET " +
                "`places` = ?, " +
                "`numgroup` = ? " +
                "WHERE id = ?;"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setString(1, map._placesStr)
            p.setInt(2, map.maxGroupNumb)
            p.setInt(3, map._id.toInt())
            p.executeUpdate()
            closePreparedStatement(p)
            return true
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
        return false
    }

    fun DELETE_NPC_ON_MAP(m: Int, c: Int): Boolean {
        val baseQuery = "DELETE FROM npcs WHERE mapid = ? AND cellid = ?;"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, m)
            p.setInt(2, c)
            p.execute()
            closePreparedStatement(p)
            return true
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
        return false
    }

    @JvmStatic
    fun DELETE_PERCO(id: Int): Boolean {
        val baseQuery = "DELETE FROM percepteurs WHERE guid = ?;"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, id)
            p.execute()
            closePreparedStatement(p)
            return true
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
        return false
    }

    fun ADD_NPC_ON_MAP(m: Int, id: Int, c: Int, o: Int): Boolean {
        val baseQuery = "INSERT INTO `npcs`" +
                " VALUES (?,?,?,?);"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, m)
            p.setInt(2, id)
            p.setInt(3, c)
            p.setInt(4, o)
            p.execute()
            closePreparedStatement(p)
            return true
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
        return false
    }

    @JvmStatic
    fun ADD_PERCO_ON_MAP(guid: Int, mapid: Int, guildID: Int, cellid: Int, o: Int, N1: Short, N2: Short): Boolean {
        val baseQuery = "INSERT INTO `percepteurs`" +
                " VALUES (?,?,?,?,?,?,?,?,?,?);"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, guid)
            p.setInt(2, mapid)
            p.setInt(3, cellid)
            p.setInt(4, o)
            p.setInt(5, guildID)
            p.setShort(6, N1)
            p.setShort(7, N2)
            p.setString(8, "")
            p.setLong(9, 0)
            p.setLong(10, 0)
            p.execute()
            closePreparedStatement(p)
            return true
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
        return false
    }

    @JvmStatic
    fun UPDATE_PERCO(P: Percepteur) {
        val baseQuery = "UPDATE `percepteurs` SET " +
                "`objets` = ?," +
                "`kamas` = ?," +
                "`xp` = ?" +
                " WHERE guid = ?;"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setString(1, P.parseItemPercepteur())
            p.setLong(2, P.kamas)
            p.setLong(3, P.xp)
            p.setInt(4, P.guid)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
    }

    fun ADD_ENDFIGHTACTION(mapID: Int, type: Int, Aid: Int, args: String?, cond: String?): Boolean {
        if (!DEL_ENDFIGHTACTION(mapID, type, Aid)) return false
        val baseQuery = "INSERT INTO `endfight_action` " +
                "VALUES (?,?,?,?,?);"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, mapID)
            p.setInt(2, type)
            p.setInt(3, Aid)
            p.setString(4, args)
            p.setString(5, cond)
            p.execute()
            closePreparedStatement(p)
            return true
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
        return false
    }

    fun DEL_ENDFIGHTACTION(mapID: Int, type: Int, aid: Int): Boolean {
        val baseQuery = "DELETE FROM `endfight_action` " +
                "WHERE map = ? AND " +
                "fighttype = ? AND " +
                "action = ?;"
        return try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, mapID)
            p.setInt(2, type)
            p.setInt(3, aid)
            p.execute()
            closePreparedStatement(p)
            true
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
            false
        }
    }

    @JvmStatic
    fun SAVE_NEWGUILD(g: Guild) {
        val baseQuery = "INSERT INTO `guilds` " +
                "VALUES (?,?,?,1,0,0,0,?,?);"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, g._id)
            p.setString(2, g._name)
            p.setString(3, g._emblem)
            p.setString(4, "462;0|461;0|460;0|459;0|458;0|457;0|456;0|455;0|454;0|453;0|452;0|451;0|")
            p.setString(5, "176;100|158;1000|124;100|")
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
    fun DEL_GUILD(id: Int) {
        val baseQuery = "DELETE FROM `guilds` " +
                "WHERE `id` = ?;"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, id)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
    fun DEL_ALL_GUILDMEMBER(guildid: Int) {
        val baseQuery = "DELETE FROM `guild_members` " +
                "WHERE `guild` = ?;"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, guildid)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
    fun DEL_GUILDMEMBER(id: Int) {
        val baseQuery = "DELETE FROM `guild_members` " +
                "WHERE `guid` = ?;"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, id)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
    fun UPDATE_GUILD(g: Guild) {
        val baseQuery = "UPDATE `guilds` SET " +
                "`lvl` = ?," +
                "`xp` = ?," +
                "`capital` = ?," +
                "`nbrmax` = ?," +
                "`sorts` = ?," +
                "`stats` = ?" +
                " WHERE id = ?;"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, g._lvl)
            p.setLong(2, g._xp)
            p.setInt(3, g._Capital)
            p.setInt(4, g._nbrPerco)
            p.setString(5, g.compileSpell())
            p.setString(6, g.compileStats())
            p.setInt(7, g._id)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
    }

    //v0.00.0 - Modificamos la carga de columnas inecesarias
    @JvmStatic
    fun UPDATE_GUILDMEMBER(gm: GuildMember) {
        val baseQuery = "REPLACE INTO `guild_members` " +
                "VALUES(?,?,?,?,?,?,?);"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, gm.guid)
            p.setInt(2, gm.guild._id)
            p.setInt(3, gm.rank)
            p.setLong(4, gm.xpGave)
            p.setInt(5, gm.pXpGive)
            p.setInt(6, gm.rights)
            p.setString(7, gm.lastCo)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
    }

    fun isPersoInGuild(guid: Int): Int {
        var guildId = -1
        try {
            val GuildQuery = executeQuery("SELECT guild FROM `guild_members` WHERE guid=$guid;", Main.DB_NAME)
            val found = GuildQuery!!.first()
            if (found) guildId = GuildQuery.getInt("guild")
            closeResultSet(GuildQuery)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
        return guildId
    }

    @JvmStatic
    fun isPersoInGuild(name: String): IntArray {
        var guildId = -1
        var guid = -1
        try {
            val GuildQuery = executeQuery(
                "SELECT guild,guid FROM `guild_members` WHERE name='$name';",
                Main.DB_NAME
            )
            val found = GuildQuery!!.first()
            if (found) {
                guildId = GuildQuery.getInt("guild")
                guid = GuildQuery.getInt("guid")
            }
            closeResultSet(GuildQuery)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
        return intArrayOf(guid, guildId)
    }

    fun ADD_REPONSEACTION(repID: Int, type: Int, args: String?): Boolean {
        var baseQuery = "DELETE FROM `npc_reponses_actions` " +
                "WHERE `ID` = ? AND " +
                "`type` = ?;"
        var p: PreparedStatement
        try {
            p = newTransact(baseQuery, gameCon)
            p.setInt(1, repID)
            p.setInt(2, type)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
        baseQuery = "INSERT INTO `npc_reponses_actions` " +
                "VALUES (?,?,?);"
        try {
            p = newTransact(baseQuery, gameCon)
            p.setInt(1, repID)
            p.setInt(2, type)
            p.setString(3, args)
            p.execute()
            closePreparedStatement(p)
            return true
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
        return false
    }

    fun UPDATE_INITQUESTION(id: Int, q: Int): Boolean {
        val baseQuery = "UPDATE `npc_template` SET " +
                "`initQuestion` = ? " +
                "WHERE `id` = ?;"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, q)
            p.setInt(2, id)
            p.execute()
            closePreparedStatement(p)
            return true
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
        return false
    }

    fun UPDATE_NPCREPONSES(id: Int, reps: String?): Boolean {
        val baseQuery = "UPDATE `npc_questions` SET " +
                "`responses` = ? " +
                "WHERE `ID` = ?;"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setString(1, reps)
            p.setInt(2, id)
            p.execute()
            closePreparedStatement(p)
            return true
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
        return false
    }

    @JvmStatic
    fun LOAD_ACTION() {
        /*Variables repr�sentant les champs de la base*/
        var perso: Personaje?
        var action: Int
        var nombre: Int
        var id: Int
        addToShopLog("Lancement de l'application des Lives Actions ...")
        var sortie: String
        val couleur = "DF0101" //La couleur du message envoyer a l'utilisateur (couleur en code HTML)
        var t: ObjTemplate?
        var obj: Objet?
        var p: PreparedStatement
        /*FIN*/try {
            val RS = executeQuery("SELECT * from live_action;", Main.DB_NAME)
            while (RS!!.next()) {
                perso = World.getPersonnage(RS.getInt("PlayerID"))
                if (perso == null) {
                    addToShopLog("Personnage " + RS.getInt("PlayerID") + " non trouve, personnage non charge ?")
                    continue
                }
                if (!perso.isOnline) {
                    addToShopLog("Personnage " + RS.getInt("PlayerID") + " hors ligne")
                    continue
                }
                if (perso._compte == null) {
                    addToShopLog("Le Personnage " + RS.getInt("PlayerID") + " n'est attribue a aucun compte charge")
                    continue
                }
                if (perso._compte.gameThread == null) {
                    addToShopLog("Le Personnage " + RS.getInt("PlayerID") + " n'a pas thread associe, le personnage est il hors ligne ?")
                    continue
                }
                if (perso._fight != null) continue  // Perso en combat  @ Nami-Doc
                action = RS.getInt("Action")
                nombre = RS.getInt("Nombre")
                id = RS.getInt("ID")
                sortie = "+"
                when (action) {
                    1 -> {
                        if (perso._lvl == World.getExpLevelSize()) continue
                        var n = nombre
                        while (n > 1) {
                            perso.levelUp(false, true)
                            n--
                        }
                        perso.levelUp(true, true)
                        sortie += "$nombre Niveau(x)"
                    }
                    2 -> {
                        if (perso._lvl == World.getExpLevelSize()) continue
                        perso.addXp(nombre.toLong())
                        sortie += "$nombre Xp"
                    }
                    3 -> {
                        perso.addKamas(nombre.toLong())
                        sortie += "$nombre Kamas"
                    }
                    4 -> {
                        perso.addCapital(nombre)
                        sortie += "$nombre Point(s) de capital"
                    }
                    5 -> {
                        perso.addSpellPoint(nombre)
                        sortie += "$nombre Point(s) de sort"
                    }
                    20 -> {
                        t = World.getObjTemplate(nombre)
                        if (t == null) continue
                        obj = t.createNewItem(
                            1,
                            false
                        ) //Si mis � "true" l'objet � des jets max. Sinon ce sont des jets al�atoire
                        if (obj == null) continue
                        if (perso.addObjet(obj, true)) //Si le joueur n'avait pas d'item similaire
                            World.addObjet(obj, true)
                        GameServer.addToSockLog("Objet " + nombre + " ajouter a " + perso._name + " avec des stats aleatoire")
                        GAME_SEND_MESSAGE(
                            perso,
                            "L'objet \"" + t.name + "\" viens d'etre ajouter a votre personnage",
                            couleur
                        )
                    }
                    21 -> {
                        t = World.getObjTemplate(nombre)
                        if (t == null) continue
                        obj = t.createNewItem(
                            1,
                            true
                        ) //Si mis � "true" l'objet � des jets max. Sinon ce sont des jets al�atoire
                        if (obj == null) continue
                        if (perso.addObjet(obj, true)) //Si le joueur n'avait pas d'item similaire
                            World.addObjet(obj, true)
                        GameServer.addToSockLog("Objet " + nombre + " ajoute a " + perso._name + " avec des stats MAX")
                        GAME_SEND_MESSAGE(
                            perso,
                            "L'objet \"" + t.name + "\" avec des stats maximum, viens d'etre ajoute a votre personnage",
                            couleur
                        )
                    }
                    118 -> {
                        perso._baseStats.addOneStat(action, nombre)
                        GAME_SEND_STATS_PACKET(perso)
                        sortie += "$nombre force"
                    }
                    119 -> {
                        perso._baseStats.addOneStat(action, nombre)
                        GAME_SEND_STATS_PACKET(perso)
                        sortie += "$nombre agilite"
                    }
                    123 -> {
                        perso._baseStats.addOneStat(action, nombre)
                        GAME_SEND_STATS_PACKET(perso)
                        sortie += "$nombre chance"
                    }
                    124 -> {
                        perso._baseStats.addOneStat(action, nombre)
                        GAME_SEND_STATS_PACKET(perso)
                        sortie += "$nombre sagesse"
                    }
                    125 -> {
                        perso._baseStats.addOneStat(action, nombre)
                        GAME_SEND_STATS_PACKET(perso)
                        sortie += "$nombre vita"
                    }
                    126 -> {
                        val statID = action
                        perso._baseStats.addOneStat(statID, nombre)
                        GAME_SEND_STATS_PACKET(perso)
                        sortie += "$nombre intelligence"
                    }
                }
                GAME_SEND_STATS_PACKET(perso)
                if (action < 20 || action > 100) GAME_SEND_MESSAGE(
                    perso,
                    "$sortie a votre personnage",
                    couleur
                ) //Si l'action n'est pas un ajout d'objet on envoye un message a l'utilisateur
                addToShopLog(
                    "(Commande " + id + ")Action " + action + " Nombre: " + nombre + " appliquee sur le personnage " + RS.getInt(
                        "PlayerID"
                    ) + "(" + perso._name + ")"
                )
                try {
                    val query = "DELETE FROM live_action WHERE ID=$id;"
                    p = newTransact(query, gameCon)
                    p.execute()
                    closePreparedStatement(p)
                    addToShopLog("Commande $id supprimee.")
                } catch (e: SQLException) {
                    GameServer.addToLog("SQL ERROR: " + e.message)
                    addToShopLog("Error Delete From: " + e.message)
                    e.printStackTrace()
                }
                SAVE_PERSONNAGE(perso, true)
            }
            closeResultSet(RS)
        } catch (e: Exception) {
            GameServer.addToLog("ERROR: " + e.message)
            addToShopLog("Error: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun LOAD_ITEMS() {
        try {
            val RS = executeQuery("SELECT * FROM items;", Main.DB_NAME)
            while (RS!!.next()) {
                val guid = RS.getInt("guid")
                val tempID = RS.getInt("template")
                val qua = RS.getInt("qua")
                val pos = RS.getInt("pos")
                val stats = RS.getString("stats")
                World.addObjet(Objet(guid, tempID, qua, pos, stats), false)
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            System.exit(1)
        }
    }

    @JvmStatic
    fun TIMER(start: Boolean) {
        if (start) {
            timerCommit = Timer()
            timerCommit?.schedule(object : TimerTask() {
                override fun run() {
                    if (!needCommit) return
                    commitTransacts()
                    needCommit = false
                }
            }, Main.CONFIG_DB_COMMIT.toLong())
        } else timerCommit!!.cancel()
    }

    @JvmStatic
    fun persoExist(name: String): Boolean {
        var exist = false
        try {
            val RS = executeQuery("SELECT COUNT(*) AS exist FROM personnages WHERE name LIKE '$name';", Main.DB_NAME)
            RS!!.next()
            val nb = RS.getInt("exist")
            if (nb > 0) {
                exist = true
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
        return exist
    }

    @JvmStatic
    fun HOUSE_BUY(P: Personaje, h: House) {
        var p: PreparedStatement
        var query =
            "UPDATE `houses` SET `sale`='0', `owner_id`=?, `owner_pseudo`=?, `guild_id`='0', `access`='0', `key`='-', `guild_rights`='0' WHERE `id`=?;"
        try {
            p = newTransact(query, gameCon)
            p.setInt(1, P.accID)
            p.setString(2, P._compte._pseudo)
            p.setInt(3, h._id)
            p.execute()
            closePreparedStatement(p)
            h._sale = 0
            h._owner_id = P.accID
            h._owner_pseudo = P._compte._pseudo
            h._guild_id = 0
            h._access = 0
            h._key = "-"
            h._guild_rights = 0
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $query")
        }
        val trunks = Trunk.getTrunksByHouse(h)
        for (trunk in trunks) {
            trunk._owner_id = P.accID
            trunk._key = "-"
        }
        query = "UPDATE `coffres` SET `owner_id`=?, `key`='-' WHERE `id_house`=?;"
        try {
            p = newTransact(query, gameCon)
            p.setInt(1, P.accID)
            p.setInt(2, h._id)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $query")
        }
    }

    @JvmStatic
    fun HOUSE_SELL(h: House, price: Int) {
        h._sale = price
        val p: PreparedStatement
        val query = "UPDATE `houses` SET `sale`=? WHERE `id`=?;"
        try {
            p = newTransact(query, gameCon)
            p.setInt(1, price)
            p.setInt(2, h._id)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $query")
        }
    }

    @JvmStatic
    fun HOUSE_CODE(P: Personaje, h: House, packet: String?) {
        val p: PreparedStatement
        val query = "UPDATE `houses` SET `key`=? WHERE `id`=? AND owner_id=?;"
        try {
            p = newTransact(query, gameCon)
            p.setString(1, packet)
            p.setInt(2, h._id)
            p.setInt(3, P.accID)
            p.execute()
            closePreparedStatement(p)
            h._key = packet
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $query")
        }
    }

    @JvmStatic
    fun HOUSE_GUILD(h: House, GuildID: Int, GuildRights: Int) {
        val p: PreparedStatement
        val query = "UPDATE `houses` SET `guild_id`=?, `guild_rights`=? WHERE `id`=?;"
        try {
            p = newTransact(query, gameCon)
            p.setInt(1, GuildID)
            p.setInt(2, GuildRights)
            p.setInt(3, h._id)
            p.execute()
            closePreparedStatement(p)
            h._guild_id = GuildID
            h._guild_rights = GuildRights
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $query")
        }
    }

    @JvmStatic
    fun HOUSE_GUILD_REMOVE(GuildID: Int) {
        val p: PreparedStatement
        val query = "UPDATE `houses` SET `guild_rights`='0', `guild_id`='0' WHERE `guild_id`=?;"
        try {
            p = newTransact(query, gameCon)
            p.setInt(1, GuildID)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $query")
        }
    }

    @JvmStatic
    fun UPDATE_HOUSE(h: House) {
        val baseQuery = "UPDATE `houses` SET " +
                "`owner_id` = ?," +
                "`owner_pseudo` = ?," +
                "`sale` = ?," +
                "`guild_id` = ?," +
                "`access` = ?," +
                "`key` = ?," +
                "`guild_rights` = ?" +
                " WHERE id = ?;"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, h._owner_id)
            p.setString(2, h._owner_pseudo)
            p.setInt(3, h._sale)
            p.setInt(4, h._guild_id)
            p.setInt(5, h._access)
            p.setString(6, h._key)
            p.setInt(7, h._guild_rights)
            p.setInt(8, h._id)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
    fun GetNewIDPercepteur(): Int {
        var i = -50 //Pour �viter les conflits avec touts autre NPC
        try {
            val query = "SELECT `guid` FROM `percepteurs` ORDER BY `guid` ASC LIMIT 0 , 1;"
            val RS = executeQuery(query, Main.DB_NAME)
            while (RS!!.next()) {
                i = RS.getInt("guid") - 1
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
        return i
    }

    @JvmStatic
    fun LOAD_ZAAPIS(): Int {
        var i = 0
        val Bonta = StringBuilder()
        val Brak = StringBuilder()
        val Neutre = StringBuilder()
        try {
            val RS = executeQuery("SELECT mapid, align from zaapi;", Main.DB_NAME)
            while (RS!!.next()) {
                if (RS.getInt("align") == Constants.ALIGNEMENT_BONTARIEN) {
                    Bonta.append(RS.getString("mapid"))
                    if (!RS.isLast) Bonta.append(",")
                } else if (RS.getInt("align") == Constants.ALIGNEMENT_BRAKMARIEN) {
                    Brak.append(RS.getString("mapid"))
                    if (!RS.isLast) Brak.append(",")
                } else {
                    Neutre.append(RS.getString("mapid"))
                    if (!RS.isLast) Neutre.append(",")
                }
                i++
            }
            Constants.ZAAPI[Constants.ALIGNEMENT_BONTARIEN] = Bonta.toString()
            Constants.ZAAPI[Constants.ALIGNEMENT_BRAKMARIEN] = Brak.toString()
            Constants.ZAAPI[Constants.ALIGNEMENT_NEUTRE] = Neutre.toString()
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
        return i
    }

    @JvmStatic
    fun LOAD_ZAAPS(): Int {
        var i = 0
        try {
            val RS = executeQuery("SELECT mapID, cellID from zaaps;", Main.DB_NAME)
            while (RS!!.next()) {
                Constants.ZAAPS[RS.getInt("mapID")] = RS.getInt("cellID")
                i++
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
        }
        return i
    }

    @JvmStatic
    fun LOAD_HDVS(): Int {
        var i = 0
        try {
            val RS = executeQuery("SELECT * FROM `hdvs` ORDER BY map ASC", Main.DB_NAME)
            while (RS!!.next()) {
                World.addHdv(
                    Hdv(
                        RS.getInt("map"),
                        RS.getString("categories"),
                        RS.getFloat("sellTaxe"),
                        RS.getShort("sellTime").toInt(),
                        RS.getShort("accountItem").toInt(),
                        RS.getShort("lvlMax").toInt()
                    )
                )
                i++
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
        }
        return i
    }

    @JvmStatic
    fun LOAD_HDVS_ITEMS(): Int {
        var i = 0
        try {
            val RS = executeQuery("SELECT * FROM `hdvs_items`", Main.DB_NAME)
            while (RS!!.next()) {
                val tempHdv = World.getHdv(RS.getInt("hdvmapid")) ?: continue
                if (World.getObjet(RS.getInt("itemid")) == null) continue
                World.addHdvItem(
                    RS.getInt("ownerGuid"), RS.getInt("hdvmapid"),
                    HdvEntry(
                        RS.getInt("itemid"),
                        World.getObjet(RS.getInt("itemid")),
                        RS.getInt("hdvmapid"),
                        RS.getInt("ownerGuid"),
                        RS.getInt("price"),
                        RS.getByte("count").toInt()
                    )
                )
                i++
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
        }
        return i
    }

    @JvmStatic
    fun SAVE_HDVS_ITEMS(liste: ArrayList<HdvEntry>) {
        var queries: PreparedStatement? = null
        try {
            val emptyQuery = "TRUNCATE TABLE `hdvs_items`"
            val emptyTable = newTransact(emptyQuery, gameCon)
            emptyTable.execute()
            closePreparedStatement(emptyTable)
            val baseQuery = "INSERT INTO `hdvs_items` " +
                    "(`itemid`,`hdvmapid`,`ownerGuid`,`price`,`count`,`sellDate`) " +
                    "VALUES(?,?,?,?,?,?);"
            queries = newTransact(baseQuery, gameCon)
            for (curEntry in liste) {
                if (curEntry._ownerGuid == -1) continue
                queries.setInt(1, curEntry._ObjetID)
                queries.setInt(2, curEntry._HdvMapID)
                queries.setInt(3, curEntry._ownerGuid)
                queries.setInt(4, curEntry._price)
                queries.setInt(5, curEntry._qua)
                queries.setString(6, "")
                queries.execute()
            }
            closePreparedStatement(queries)
            //SAVE_HDV_AVGPRICE();
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun LOAD_ANIMATIONS() {
        try {
            val RS = executeQuery("SELECT * from animations;", Main.DB_NAME)
            while (RS!!.next()) {
                World.addAnimation(
                    Animations(
                        RS.getInt("guid"),
                        RS.getInt("id"),
                        RS.getString("nom"),
                        RS.getInt("area"),
                        RS.getInt("action"),
                        RS.getInt("size")
                    )
                )
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun LOAD_TRUNK(): Int {
        var nbr = 0
        try {
            val RS = executeQuery("SELECT * from coffres;", Main.DB_NAME)
            while (RS!!.next()) {
                World.addTrunk(
                    Trunk(
                        RS.getInt("id"),
                        RS.getInt("id_house"),
                        RS.getShort("mapid"),
                        RS.getInt("cellid"),
                        RS.getString("object"),
                        RS.getInt("kamas").toLong(),
                        RS.getString("key"),
                        RS.getInt("owner_id")
                    )
                )
                nbr++
            }
            closeResultSet(RS)
        } catch (e: SQLException) {
            GameServer.addToLog("SQL ERROR: " + e.message)
            e.printStackTrace()
            nbr = 0
        }
        return nbr
    }

    @JvmStatic
    fun TRUNK_CODE(P: Personaje, t: Trunk, packet: String?) {
        val p: PreparedStatement
        val query = "UPDATE `coffres` SET `key`=? WHERE `id`=? AND owner_id=?;"
        try {
            p = newTransact(query, gameCon)
            p.setString(1, packet)
            p.setInt(2, t._id)
            p.setInt(3, P.accID)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $query")
        }
    }

    @JvmStatic
    fun UPDATE_TRUNK(t: Trunk) {
        val p: PreparedStatement
        val query = "UPDATE `coffres` SET `kamas`=?, `object`=? WHERE `id`=?"
        try {
            p = newTransact(query, gameCon)
            p.setLong(1, t._kamas)
            p.setString(2, t.parseTrunkObjetsToDB())
            p.setInt(3, t._id)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $query")
        }
    }

    @JvmStatic
    fun ADD_ACCOUNT_DATA(guid: Int) {
        try {
            val bquery =
                "INSERT INTO account_data(`guid`, `friends`, `enemys`, `bankObj`, `bankKamas`) VALUES (?,?,?,?,?);"
            val p = newTransact(bquery, gameCon)
            p.setInt(1, guid)
            p.setString(2, "")
            p.setString(3, "")
            p.setString(4, "")
            p.setInt(5, 0)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun UPDATE_BANK(bk: Bank) {
        try {
            val bquery = "UPDATE `account_data` SET `bankObj`=?, `bankKamas`=? WHERE `guid`=?;"
            val p = newTransact(bquery, gameCon)
            p.setString(1, bk.parseBankItems())
            p.setLong(2, bk.bankKamas)
            p.setInt(3, bk.guid)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun UPDATE_FL_AND_EL(guid: Int, FL: String?, EL: String?) {
        try {
            val bquery = "UPDATE `account_data` SET `friends`=?, `enemys`=? WHERE `guid`=?;"
            val p = newTransact(bquery, gameCon)
            p.setString(1, FL)
            p.setString(2, EL)
            p.setInt(3, guid)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun LOAD_PETS(): Int {
        var i = 0
        return try {
            val RS = executeQuery("SELECT * from pets;", Main.DB_NAME)
            while (RS!!.next()) {
                i++
                World.addPets(
                    Pets(
                        RS.getInt("TemplateID"),
                        RS.getInt("Type"),
                        RS.getString("Gap"),
                        RS.getString("StatsUp"),
                        RS.getInt("Max"),
                        RS.getInt("Gain"),
                        RS.getInt("DeadTemplate")
                    )
                )
            }
            closeResultSet(RS)
            i
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
            i
        }
    }

    @JvmStatic
    fun LOAD_PETS_ENTRY(): Int {
        var i = 0
        return try {
            val RS = executeQuery("SELECT * from pets_data;", Main.DB_NAME)
            while (RS!!.next()) {
                i++
                World.addPetsEntry(
                    PetsEntry(
                        RS.getInt("id"),
                        RS.getLong("LastEatDate"),
                        RS.getInt("quaEat"),
                        RS.getInt("pdv"),
                        RS.getInt("Corpulence"),
                        RS.getInt("isEPO") == 1
                    )
                )
            }
            closeResultSet(RS)
            i
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
            i
        }
    }

    @JvmStatic
    fun ADD_PETS_DATA(id: Int, LastEatDate: Long) {
        try {
            val bquery =
                "INSERT INTO pets_data(`id`, `LastEatDate`, `quaEat`, `pdv`, `Corpulence`, `isEPO`) VALUES (?,?,?,?,?,?);"
            val p = newTransact(bquery, gameCon)
            p.setInt(1, id)
            p.setLong(2, LastEatDate)
            p.setInt(3, 0)
            p.setInt(4, 10)
            p.setInt(5, 0)
            p.setInt(6, 0)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun UPDATE_PETS_DATA(pets: PetsEntry) {
        val p: PreparedStatement
        val query =
            "UPDATE `pets_data` SET `LastEatDate`=?, `quaEat`=?, `pdv`=?, `Corpulence`=?, `isEPO`=? WHERE `id`=?"
        try {
            p = newTransact(query, gameCon)
            p.setLong(1, pets._LastEatDate)
            p.setInt(2, pets._quaEat)
            p.setInt(3, pets._PDV)
            p.setInt(4, pets._Corpulence)
            p.setInt(5, if (pets._isEupeoh == true) 1 else 0)
            p.setInt(6, pets._ObjectID)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $query")
        }
    }

    @JvmStatic
    fun REMOVE_PETS_DATA(id: Int) {
        val baseQuery = "DELETE FROM pets_data WHERE id = ?;"
        try {
            val p = newTransact(baseQuery, gameCon)
            p.setInt(1, id)
            p.execute()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            GameServer.addToLog("Game: Query: $baseQuery")
        }
    }

    @JvmStatic
    fun LOAD_CHALLENGES(): Int {
        var i = 0
        return try {
            val RS = executeQuery("SELECT * from challenge;", Main.DB_NAME)
            while (RS!!.next()) {
                val chal = StringBuilder()
                chal.append(RS.getInt("id")).append(",")
                chal.append(RS.getInt("gainXP")).append(",")
                chal.append(RS.getInt("gainDrop")).append(",")
                chal.append(RS.getInt("gainParMob")).append(",")
                chal.append(RS.getInt("conditions"))
                World.addChallenge(chal.toString())
                i++
            }
            closeResultSet(RS)
            i
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
            i
        }
    }

    @JvmStatic
    fun LOAD_GIFTS(): Int {
        var i = 0
        return try {
            val RS = executeQuery("SELECT * from gift;", Main.DB_NAME)
            while (RS!!.next()) {
                World.addGift(
                    Gift(
                        RS.getInt("giftId"),
                        RS.getString("title"),
                        RS.getString("description"),
                        RS.getString("pictureUrl"),
                        RS.getString("items")
                    )
                )
                i++
            }
            closeResultSet(RS)
            i
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
            i
        }
    }

    @JvmStatic
    fun DELETE_GIFT_BY_ACCOUNT(guid: Int) {
        val baseQuery = "UPDATE accounts SET `giftID`=? WHERE `guid`=?"
        try {
            val p = newTransact(baseQuery, realmCon)
            p.setString(1, "")
            p.setInt(2, guid)
            p.executeUpdate()
            closePreparedStatement(p)
        } catch (e: SQLException) {
            GameServer.addToLog("Game: SQL ERROR: " + e.message)
            e.printStackTrace()
        }
    }
}