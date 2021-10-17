package common

import common.CryptManager.toUtf
import game.GameServer
import objects.*
import objects.Fight.Fighter
import objects.Guild.GuildMember
import objects.Mapa.MountPark
import objects.Metier.StatsMetier
import objects.Monstre.MobGroup
import objects.NPC_tmpl.NPC
import java.io.PrintWriter

object SocketManager {
    fun send(p: Personaje?, packet: String) {
        var packet = packet
        if (p == null || p._compte == null) return
        if (p._compte.gameThread == null) return
        val out = p._compte.gameThread._out
        if (out != null && packet != "" && packet != "" + 0x00.toChar()) {
            packet = toUtf(packet)
            out.print(packet + 0x00.toChar())
            out.flush()
        }
    }

    private fun send(out: PrintWriter?, packet: String) {
        var packet = packet
        if (out != null && packet != "" && packet != "" + 0x00.toChar()) {
            packet = toUtf(packet)
            out.print(packet + 0x00.toChar())
            out.flush()
        }
    }

    @JvmStatic
    fun GAME_SEND_Af_PACKET(
        out: PrintWriter?, position: Int, totalAbo: Int, totalNonAbo: Int, subscribe: String?,
        queueID: Int
    ) {
        val packet = StringBuilder()
        packet.append("Af").append(position).append("|").append(totalAbo).append("|").append(totalNonAbo).append("|")
            .append(subscribe).append("|").append(queueID)
        send(out, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_HELLOGAME_PACKET(out: PrintWriter?) {
        val packet = "HG"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ATTRIBUTE_FAILED(out: PrintWriter?) {
        val packet = "ATE"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ATTRIBUTE_SUCCESS(out: PrintWriter?) {
        val packet = "ATK0"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_AV0(out: PrintWriter?) {
        val packet = "AV0"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    fun GAME_SEND_HIDE_GENERATE_NAME(out: PrintWriter?) {
        val packet = "APE2"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_PERSO_LIST(out: PrintWriter?, persos: Map<Int?, Personaje>, subscriber: Int) {
        val packet = StringBuilder()
        packet.append("ALK").append(subscriber * 60).append("000").append("|").append(persos.size)
        for ((_, value) in persos) {
            packet.append(value.parseALK())
        }
        send(out, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_NAME_ALREADY_EXIST(out: PrintWriter?) {
        val packet = "AAEa"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_CREATE_PERSO_FULL(out: PrintWriter?) {
        val packet = "AAEf"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_CREATE_OK(out: PrintWriter?) {
        val packet = "AAK"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_DELETE_PERSO_FAILED(out: PrintWriter?) {
        val packet = "ADE"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_CREATE_FAILED(out: PrintWriter?) {
        val packet = "AAEF"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_PERSO_SELECTION_FAILED(out: PrintWriter?) {
        val packet = "ASE"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_STATS_PACKET(perso: Personaje) {
        val packet = perso.asPacket
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_Rx_PACKET(out: Personaje) {
        val packet = "Rx" + out.mountXpGive
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_Rn_PACKET(out: Personaje?, name: String) {
        val packet = "Rn$name"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_Re_PACKET(out: Personaje?, sign: String, DD: Dragodinde) {
        var packet = "Re$sign"
        if (sign == "+") packet += DD.parse()
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ASK(out: PrintWriter?, perso: Personaje) {
        val packet = StringBuilder()
        packet.append("ASK|").append(perso._GUID).append("|").append(perso._name).append("|")
        packet.append(perso._lvl).append("|").append(perso._classe).append("|").append(perso._sexe)
        packet.append("|").append(perso._gfxID).append("|")
            .append(if (perso._color1 == -1) "-1" else Integer.toHexString(perso._color1))
        packet.append("|").append(if (perso._color2 == -1) "-1" else Integer.toHexString(perso._color2)).append("|")
        packet.append(if (perso._color3 == -1) "-1" else Integer.toHexString(perso._color3)).append("|")
        packet.append(perso.parseItemToASK())
        send(out, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ALIGNEMENT(out: PrintWriter?, alliID: Int) {
        val packet = "ZS$alliID"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ADD_CANAL(out: PrintWriter?, chans: String) {
        val packet = "cC+$chans"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ZONE_ALLIGN_STATUT(out: PrintWriter?) {
        val packet = "al|" + World.getSousZoneStateString()
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    fun GAME_SEND_SEESPELL_OPTION(out: PrintWriter?, spells: Boolean) {
        val packet = "SLo" + if (spells) "+" else "-"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_RESTRICTIONS(out: PrintWriter?, args: String) {
        val packet = "AR$args"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_Ow_PACKET(perso: Personaje) {
        val packet = "Ow" + perso.podUsed + "|" + perso.maxPod
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_OT_PACKET(out: PrintWriter?, id: Int) {
        var packet = "OT"
        if (id > 0) packet += id
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_SEE_FRIEND_CONNEXION(out: PrintWriter?, see: Boolean) {
        val packet = "FO" + if (see) "+" else "-"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GAME_CREATE(out: PrintWriter?, _name: String) {
        val packet = "GCK|1|$_name"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_SERVER_HOUR(out: PrintWriter?) {
        val packet = GameServer.getServerTime()
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_SERVER_DATE(out: PrintWriter?) {
        val packet = GameServer.getServerDate()
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_MAPDATA(out: PrintWriter?, id: Int, date: String, key: String) {
        val packet = "GDM|$id|$date|$key"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GDK_PACKET(out: PrintWriter?) {
        val packet = "GDK"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_MAP_MOBS_GMS_PACKETS(out: PrintWriter?, carte: Mapa) {
        val packet = carte.mobGroupGMsPackets
        if (packet == "") return
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_MAP_OBJECTS_GDS_PACKETS(out: PrintWriter?, carte: Mapa) {
        val packet = carte.objectsGDsPackets
        if (packet == "") return
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_MAP_NPCS_GMS_PACKETS(out: PrintWriter?, carte: Mapa) {
        val packet = carte.npcsGMsPackets
        if (packet == "" && packet.length < 4) return
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_MAP_PERCO_GMS_PACKETS(out: PrintWriter?, carte: Mapa?) {
        val packet = Percepteur.parseGM(carte)
        if (packet.length < 5) return
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    fun GAME_SEND_MAP_GMS_PACKETS(out: PrintWriter?, carte: Mapa) {
        val packet = carte.gMsPackets
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ERASE_ON_MAP_TO_MAP(map: Mapa, guid: Int) {
        val packet = "GM|-$guid"
        for (z in map.persos) {
            if (z._compte.gameThread == null) continue
            send(z._compte.gameThread._out, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map " + map._id + ": Send>>" + packet)
    }

    fun GAME_SEND_ERASE_ON_MAP_TO_FIGHT(f: Fight, guid: Int) {
        val packet = "GM|-$guid"
        for (z in f.getFighters(1).indices) {
            if (f.getFighters(1)[z].personnage._compte.gameThread == null) continue
            send(f.getFighters(1)[z].personnage._compte.gameThread._out, packet)
        }
        for (z in f.getFighters(2).indices) {
            if (f.getFighters(2)[z].personnage._compte.gameThread == null) continue
            send(f.getFighters(2)[z].personnage._compte.gameThread._out, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fighter ID " + f._id + ": Send>>" + packet)
    }

    @JvmStatic
    fun GAME_SEND_ON_FIGHTER_KICK(f: Fight, guid: Int, team: Int) {
        val packet = "GM|-$guid"
        for (F in f.getFighters(team)) {
            if (F.personnage == null || F.personnage._compte.gameThread == null || F.personnage._GUID == guid) continue
            send(F.personnage._compte.gameThread._out, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fighter ID " + f._id + ": Send>>" + packet)
    }

    @JvmStatic
    fun GAME_SEND_ALTER_FIGHTER_MOUNT(fight: Fight, fighter: Fighter, guid: Int, team: Int, otherteam: Int) {
        val packet = StringBuilder()
        packet.append("GM|-").append(guid).append(0x00.toChar()).append(fighter.getGmPacket('~'))
        for (F in fight.getFighters(team)) {
            if (F.personnage == null || F.personnage._compte.gameThread == null || !F.personnage.isOnline) continue
            send(F.personnage._compte.gameThread._out, packet.toString())
        }
        if (otherteam > -1) {
            for (F in fight.getFighters(otherteam)) {
                if (F.personnage == null || F.personnage._compte.gameThread == null || !F.personnage.isOnline) continue
                send(F.personnage._compte.gameThread._out, packet.toString())
            }
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight ID " + fight._id + ": Send>>" + packet)
    }

    @JvmStatic
    fun GAME_SEND_ADD_PLAYER_TO_MAP(map: Mapa, perso: Personaje) {
        val packet = "GM|+" + perso.parseToGM()
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map " + map._id + ": Send>>" + packet)
    }

    @JvmStatic
    fun GAME_SEND_DUEL_Y_AWAY(out: PrintWriter?, guid: Int) {
        val packet = "GA;903;$guid;o"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_DUEL_E_AWAY(out: PrintWriter?, guid: Int) {
        val packet = "GA;903;$guid;z"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_MAP_NEW_DUEL_TO_MAP(map: Mapa, guid: Int, guid2: Int) {
        val packet = "GA;900;$guid;$guid2"
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map " + map._id + ": Send>>" + packet)
    }

    @JvmStatic
    fun GAME_SEND_CANCEL_DUEL_TO_MAP(map: Mapa, guid: Int, guid2: Int) {
        val packet = "GA;902;$guid;$guid2"
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_MAP_START_DUEL_TO_MAP(map: Mapa, guid: Int, guid2: Int) {
        val packet = "GA;901;$guid;$guid2"
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_MAP_FIGHT_COUNT(out: PrintWriter?, map: Mapa) {
        val packet = "fC" + map.nbrFight
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(
        fight: Fight,
        teams: Int,
        state: Int,
        cancelBtn: Int,
        duel: Int,
        spec: Int,
        time: Int,
        type: Int
    ) {
        val packet = StringBuilder()
        packet.append("GJK").append(state).append("|").append(cancelBtn).append("|").append(duel).append("|")
            .append(spec).append("|").append(time).append("|").append(type)
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            send(f.personnage, packet.toString())
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(fight: Fight, teams: Int, places: String, team: Int) {
        val packet = "GP$places|$team"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isOnline) continue
            send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(map: Mapa) {
        val packet = "fC" + map.nbrFight
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(
        map: Mapa,
        arg1: Int,
        guid1: Int,
        guid2: Int,
        cell1: Int,
        str1: String?,
        cell2: Int,
        str2: String?
    ) {
        val packet = StringBuilder()
        packet.append("Gc+").append(guid1).append(";").append(arg1).append("|").append(guid1).append(";").append(cell1)
            .append(";").append(str1).append("|").append(guid2).append(";").append(cell2).append(";").append(str2)
        for (z in map.persos) send(z, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GAME_ADDFLAG_PACKET_TO_PLAYER(
        p: Personaje?,
        map: Mapa?,
        arg1: Int,
        guid1: Int,
        guid2: Int,
        cell1: Int,
        str1: String?,
        cell2: Int,
        str2: String?
    ) {
        val packet = StringBuilder()
        packet.append("Gc+").append(guid1).append(";").append(arg1).append("|").append(guid1).append(";").append(cell1)
            .append(";").append(str1).append("|").append(guid2).append(";").append(cell2).append(";").append(str2)
        send(p, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GAME_REMFLAG_PACKET_TO_MAP(map: Mapa, guid: Int) {
        val packet = "Gc-$guid"
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(map: Mapa, teamID: Int, perso: Fighter) {
        val packet = StringBuilder()
        packet.append("Gt").append(teamID).append("|+").append(perso.guid).append(";").append(perso.packetsName)
            .append(";").append(perso._lvl)
        for (z in map.persos) send(z, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(p: Personaje?, map: Mapa?, teamID: Int, perso: Fighter) {
        val packet = StringBuilder()
        packet.append("Gt").append(teamID).append("|+").append(perso.guid).append(";").append(perso.packetsName)
            .append(";").append(perso._lvl)
        send(p, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    fun GAME_SEND_REMOVE_IN_TEAM_PACKET_TO_MAP(map: Mapa, teamID: Int, perso: Fighter) {
        val packet = StringBuilder()
        packet.append("Gt").append(teamID).append("|-").append(perso.guid).append(";").append(perso.packetsName)
            .append(";").append(perso._lvl)
        for (z in map.persos) send(z, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_MAP_MOBS_GMS_PACKETS_TO_MAP(map: Mapa) {
        val packet = map.mobGroupGMsPackets // Un par un comme sa lors du respawn :)
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_MAP_MOBS_GM_PACKET(map: Mapa, current_Mobs: MobGroup) {
        var packet = "GM|"
        packet += current_Mobs.parseGM() // Un par un comme sa lors du respawn :)
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_MAP_GMS_PACKETS(map: Mapa, _perso: Personaje?) {
        val packet = map.gMsPackets
        send(_perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ON_EQUIP_ITEM(map: Mapa, _perso: Personaje) {
        val packet = _perso.parseToOa()
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ON_EQUIP_ITEM_FIGHT(_perso: Personaje, f: Fighter, F: Fight) {
        val packet = _perso.parseToOa()
        for (z in F.getFighters(f.team2)) {
            if (z.personnage == null) continue
            send(z.personnage, packet)
        }
        for (z in F.getFighters(f.otherTeam)) {
            if (z.personnage == null) continue
            send(z.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_FIGHT_CHANGE_PLACE_PACKET_TO_FIGHT(fight: Fight, teams: Int, map: Mapa?, guid: Int, cell: Int) {
        val packet = "GIC|$guid;$cell;1"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isOnline) continue
            send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(map: Mapa, s: Char, option: Char, guid: Int) {
        val packet = "Go$s$option$guid"
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_FIGHT_PLAYER_READY_TO_FIGHT(fight: Fight, teams: Int, guid: Int, b: Boolean) {
        val packet = "GR" + (if (b) "1" else "0") + guid
        if (fight._state != 2) return
        for (f in fight.getFighters(teams)) {
            if (f.personnage == null || !f.personnage.isOnline) continue
            if (f.hasLeft()) continue
            send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GJK_PACKET(
        out: Personaje?,
        state: Int,
        cancelBtn: Int,
        duel: Int,
        spec: Int,
        time: Int,
        unknown: Int
    ) {
        val packet = StringBuilder()
        packet.append("GJK").append(state).append("|").append(cancelBtn).append("|").append(duel).append("|")
            .append(spec).append("|").append(time).append("|").append(unknown)
        send(out, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_FIGHT_PLACES_PACKET(out: PrintWriter?, places: String, team: Int) {
        val packet = "GP$places|$team"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_Im_PACKET_TO_ALL(str: String) {
        val packet = "Im$str"
        for (perso in World.getOnlinePersos()) send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_Im_PACKET(out: Personaje?, str: String) {
        val packet = "Im$str"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ILS_PACKET(out: Personaje?, i: Int) {
        val packet = "ILS$i"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ILF_PACKET(P: Personaje?, i: Int) {
        val packet = "ILF$i"
        send(P, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_Im_PACKET_TO_MAP(map: Mapa, id: String) {
        val packet = "Im$id"
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_eUK_PACKET_TO_MAP(map: Mapa, guid: Int, emote: Int) {
        val packet = "eUK$guid|$emote"
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_Im_PACKET_TO_FIGHT(fight: Fight, teams: Int, id: String) {
        val packet = "Im$id"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isOnline) continue
            send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_MESSAGE(out: Personaje?, mess: String, color: String) {
        val packet = "cs<font color='#$color'>$mess</font>"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_MESSAGE_TO_MAP(map: Mapa, mess: String, color: String) {
        val packet = "cs<font color='#$color'>$mess</font>"
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GA903_ERROR_PACKET(out: PrintWriter?, c: Char, guid: Int) {
        val packet = "GA;903;$guid;$c"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GIC_PACKETS_TO_FIGHT(fight: Fight, teams: Int) {
        val packet = StringBuilder()
        packet.append("GIC|")
        for (p in fight.getFighters(3)) {
            if (p._fightCell == null) continue
            packet.append(p.guid).append(";").append(p._fightCell.id).append(";1|")
        }
        for (perso in fight.getFighters(teams)) {
            if (perso.hasLeft()) continue
            if (perso.personnage == null || !perso.personnage.isOnline) continue
            send(perso.personnage, packet.toString())
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GIC_PACKET_TO_FIGHT(fight: Fight, teams: Int, f: Fighter) {
        val packet = StringBuilder()
        packet.append("GIC|").append(f.guid).append(";").append(f._fightCell.id).append(";1|")
        for (perso in fight.getFighters(teams)) {
            if (perso.hasLeft()) continue
            if (perso.personnage == null || !perso.personnage.isOnline) continue
            send(perso.personnage, packet.toString())
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GS_PACKET_TO_FIGHT(fight: Fight, teams: Int) {
        val packet = "GS"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            f.initBuffStats()
            if (f.personnage == null || !f.personnage.isOnline) continue
            send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GS_PACKET(out: Personaje?) {
        val packet = "GS"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GTL_PACKET_TO_FIGHT(fight: Fight, teams: Int) {
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isOnline) continue
            send(f.personnage, fight.gtl)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight : Send>>" + fight.gtl)
    }

    @JvmStatic
    fun GAME_SEND_GTL_PACKET(out: Personaje?, fight: Fight) {
        val packet = fight.gtl
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GTM_PACKET_TO_FIGHT(fight: Fight, teams: Int) {
        val packet = StringBuilder()
        packet.append("GTM")
        for (f in fight.getFighters(3)) {
            packet.append("|").append(f.guid).append(";")
            if (f.isDead) {
                packet.append("1")
                continue
            } else packet.append("0;").append(f.pdv).append(";").append(f.pa).append(";").append(f.pm).append(";")
            packet.append(if (f.isHide) "-1" else f._fightCell.id).append(";") //On envoie pas la cell d'un invisible :p
            packet.append(";") //??
            packet.append(f.pdvmax)
        }
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isOnline) continue
            send(f.personnage, packet.toString())
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GAMETURNSTART_PACKET_TO_FIGHT(fight: Fight, teams: Int, guid: Int, time: Int) {
        val packet = "GTS$guid|$time"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isOnline) continue
            send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GAMETURNSTART_PACKET(P: Personaje?, guid: Int, time: Int) {
        val packet = "GTS$guid|$time"
        send(P, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GV_PACKET(P: Personaje?) {
        val packet = "GV"
        send(P, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_PONG(out: PrintWriter?) {
        val packet = "pong"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_QPONG(out: PrintWriter?) {
        val packet = "qpong"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GAS_PACKET_TO_FIGHT(fight: Fight, teams: Int, guid: Int) {
        val packet = "GAS$guid"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isOnline) continue
            send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight : Send>>$packet")
    }

    fun GAME_SEND_GA_PACKET_TO_FIGHT(fight: Fight, teams: Int, actionID: Int, s1: String, s2: String) {
        var packet = "GA;$actionID;$s1"
        if (s2 != "") packet += ";$s2"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isOnline) continue
            send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight(" + fight.getFighters(teams).size + ") : Send>>" + packet)
    }

    @JvmStatic
    fun GAME_SEND_GA_PACKET(out: PrintWriter?, actionID: String, s0: String, s1: String, s2: String) {
        var packet = "GA$actionID;$s0"
        if (s1 != "") packet += ";$s1"
        if (s2 != "") packet += ";$s2"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GA_PACKET_TO_FIGHT(fight: Fight, teams: Int, gameActionID: Int, s1: String, s2: String, s3: String) {
        val packet = "GA$gameActionID;$s1;$s2;$s3"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isOnline) continue
            send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GAMEACTION_TO_FIGHT(fight: Fight, teams: Int, packet: String) {
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isOnline) continue
            send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GAF_PACKET_TO_FIGHT(fight: Fight, teams: Int, i1: Int, guid: Int) {
        val packet = "GAF$i1|$guid"
        for (f in fight.getFighters(teams)) {
            if (f.personnage == null || !f.personnage.isOnline) continue
            send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight : Send>>$packet")
    }

    fun GAME_SEND_BN(out: Personaje?) {
        val packet = "BN"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_BN(out: PrintWriter?) {
        val packet = "BN"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GAMETURNSTOP_PACKET_TO_FIGHT(fight: Fight, teams: Int, guid: Int) {
        val packet = "GTF$guid"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isOnline) continue
            send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GTR_PACKET_TO_FIGHT(fight: Fight, teams: Int, guid: Int) {
        val packet = "GTR$guid"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isOnline) continue
            send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EMOTICONE_TO_MAP(map: Mapa, guid: Int, id: Int) {
        val packet = "cS$guid|$id"
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_SPELL_UPGRADE_FAILED(_out: PrintWriter?) {
        val packet = "SUE"
        send(_out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_SPELL_UPGRADE_SUCCED(_out: PrintWriter?, spellID: Int, level: Int) {
        val packet = "SUK$spellID~$level"
        send(_out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_SPELL_LIST(perso: Personaje) {
        val packet = perso.parseSpellList()
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_FIGHT_PLAYER_DIE_TO_FIGHT(fight: Fight, teams: Int, guid: Int) {
        val packet = "GA;103;$guid;$guid"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft() || f.personnage == null) continue
            if (f.personnage.isOnline) send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_FIGHT_GE_PACKET_TO_FIGHT(fight: Fight, teams: Int, win: Int) {
        val packet = fight.GetGE(win)
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft() || f.personnage == null) continue
            if (f.personnage.isOnline) send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight : Send>>$packet")
    }

    fun GAME_SEND_FIGHT_GE_PACKET(out: PrintWriter?, fight: Fight, win: Int) {
        val packet = fight.GetGE(win)
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_FIGHT_GIE_TO_FIGHT(
        fight: Fight,
        teams: Int,
        mType: Int,
        cible: Int,
        value: Int,
        mParam2: String?,
        mParam3: String?,
        mParam4: String?,
        turn: Int,
        spellID: Int
    ) {
        val packet = StringBuilder()
        packet.append("GIE").append(mType).append(";").append(cible).append(";").append(value).append(";")
            .append(mParam2).append(";").append(mParam3).append(";").append(mParam4).append(";").append(turn)
            .append(";").append(spellID)
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft() || f.personnage == null) continue
            if (f.personnage.isOnline) send(f.personnage, packet.toString())
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(fight: Fight, teams: Int, map: Mapa) {
        val packet = map.fightersGMsPackets
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isOnline) continue
            send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_MAP_FIGHT_GMS_PACKETS(fight: Fight?, map: Mapa, _perso: Personaje?) {
        val packet = map.fightersGMsPackets
        send(_perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_FIGHT_PLAYER_JOIN(fight: Fight, teams: Int, _fighter: Fighter) {
        val packet = _fighter.getGmPacket('+')
        for (f in fight.getFighters(teams)) {
            if (f !== _fighter) {
                if (f.personnage == null || !f.personnage.isOnline) continue
                if (f.personnage != null && f.personnage._compte.gameThread != null) send(f.personnage, packet)
            }
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_cMK_PACKET(perso: Personaje?, suffix: String, guid: Int, name: String, msg: String) {
        val packet = "cMK$suffix|$guid|$name|$msg"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_cMK_PACKET_TO_INCARNAM(perso: Personaje?, suffix: String, guid: Int, name: String, msg: String) {
        val packet = "cMK$suffix|$guid|$name|$msg"
        for (incarnamPerso in World.getOnlinePersos()) {
            if (incarnamPerso._lvl > 15) continue
            if (incarnamPerso._curCarte.subArea._area._id == 45) {
                send(incarnamPerso, packet)
            }
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: ALL Incarnam(" + World.getOnlinePersos().size + "): Send>>" + packet)
    }

    @JvmStatic
    fun GAME_SEND_FIGHT_LIST_PACKET(out: PrintWriter?, map: Mapa) {
        val packet = StringBuilder()
        packet.append("fL")
        for ((_, value) in map._fights) {
            if (packet.length > 2) {
                packet.append("|")
            }
            packet.append(value.parseFightInfos())
        }
        send(out, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_cMK_PACKET_TO_MAP(map: Mapa, suffix: String, guid: Int, name: String, msg: String) {
        val packet = "cMK$suffix|$guid|$name|$msg"
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_cMK_PACKET_TO_GUILD(g: Guild, suffix: String, guid: Int, name: String, msg: String) {
        val packet = "cMK$suffix|$guid|$name|$msg"
        for (perso in g.members) {
            if (perso == null || !perso.isOnline) continue
            send(perso, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Guild: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_cMK_PACKET_TO_ALL(suffix: String, guid: Int, name: String, msg: String) {
        val packet = "cMK$suffix|$guid|$name|$msg"
        for (perso in World.getOnlinePersos()) send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: ALL(" + World.getOnlinePersos().size + "): Send>>" + packet)
    }

    @JvmStatic
    fun GAME_SEND_cMK_PACKET_TO_ALIGN(suffix: String, guid: Int, name: String, msg: String, _perso: Personaje) {
        val packet = "cMK$suffix|$guid|$name|$msg"
        for (perso in World.getOnlinePersos()) {
            if (perso._align == _perso._align) {
                send(perso, packet)
            }
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: ALL(" + World.getOnlinePersos().size + "): Send>>" + packet)
    }

    @JvmStatic
    fun GAME_SEND_cMK_PACKET_TO_ADMIN(suffix: String, guid: Int, name: String, msg: String) {
        val packet = "cMK$suffix|$guid|$name|$msg"
        for (perso in World.getOnlinePersos()) if (perso.isOnline) if (perso._compte != null) if (perso._compte._gmLvl > 0) send(
            perso,
            packet
        )
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: ALL(" + World.getOnlinePersos().size + "): Send>>" + packet)
    }

    @JvmStatic
    fun GAME_SEND_cMK_PACKET_TO_FIGHT(fight: Fight, teams: Int, suffix: String, guid: Int, name: String, msg: String) {
        val packet = "cMK$suffix|$guid|$name|$msg"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isOnline) continue
            send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GDZ_PACKET_TO_FIGHT(fight: Fight, teams: Int, suffix: String, cell: Int, size: Int, unk: Int) {
        val packet = "GDZ$suffix$cell;$size;$unk"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isOnline) continue
            send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GDC_PACKET_TO_FIGHT(fight: Fight, teams: Int, cell: Int) {
        val packet = "GDC$cell"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isOnline) continue
            send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GA2_PACKET(out: PrintWriter?, guid: Int) {
        val packet = "GA;2;$guid;"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_CHAT_ERROR_PACKET(out: PrintWriter?, name: String) {
        val packet = "cMEf$name"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_eD_PACKET_TO_MAP(map: Mapa, guid: Int, dir: Int) {
        val packet = "eD$guid|$dir"
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    fun GAME_SEND_ECK_PACKET(out: Personaje?, type: Int, str: String) {
        var packet = "ECK$type"
        if (str != "") packet += "|$str"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ECK_PACKET(out: PrintWriter?, type: Int, str: String) {
        var packet = "ECK$type"
        if (str != "") packet += "|$str"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ITEM_VENDOR_LIST_PACKET(out: PrintWriter?, npc: NPC) {
        val packet = "EL" + npc._template.itemVendorList
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ITEM_LIST_PACKET_PERCEPTEUR(out: PrintWriter?, perco: Percepteur) {
        val packet = "EL" + perco.itemPercepteurList
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ITEM_LIST_PACKET_SELLER(p: Personaje, out: Personaje?) {
        val packet = "EL" + p.parseStoreItemsList()
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EV_PACKET(out: PrintWriter?) {
        val packet = "EV"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_DCK_PACKET(out: PrintWriter?, id: Int) {
        val packet = "DCK$id"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_QUESTION_PACKET(out: PrintWriter?, str: String) {
        val packet = "DQ$str"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_END_DIALOG_PACKET(out: PrintWriter?) {
        val packet = "DV"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_CONSOLE_MESSAGE_PACKET(out: PrintWriter?, mess: String) {
        val packet = "BAT2$mess"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_BUY_ERROR_PACKET(out: PrintWriter?) {
        val packet = "EBE"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_SELL_ERROR_PACKET(out: PrintWriter?) {
        val packet = "ESE"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_BUY_OK_PACKET(out: PrintWriter?) {
        val packet = "EBK"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_OBJECT_QUANTITY_PACKET(out: Personaje?, obj: Objet) {
        val packet = "OQ" + obj.guid + "|" + obj.quantity
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_OAKO_PACKET(out: Personaje?, obj: Objet) {
        val packet = "OAKO" + obj.parseItem()
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ESK_PACKET(out: Personaje?) {
        val packet = "ESK"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_REMOVE_ITEM_PACKET(out: Personaje?, guid: Int) {
        val packet = "OR$guid"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_DELETE_OBJECT_FAILED_PACKET(out: PrintWriter?) {
        val packet = "OdE"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_OBJET_MOVE_PACKET(out: Personaje?, obj: Objet) {
        var packet = "OM" + obj.guid + "|"
        if (obj.position != Constants.ITEM_POS_NO_EQUIPED) packet += obj.position
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EMOTICONE_TO_FIGHT(fight: Fight, teams: Int, guid: Int, id: Int) {
        val packet = "cS$guid|$id"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isOnline) continue
            send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_OAEL_PACKET(out: PrintWriter?) {
        val packet = "OAEL"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_NEW_LVL_PACKET(out: PrintWriter?, lvl: Int) {
        val packet = "AN$lvl"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_MESSAGE_TO_ALL(msg: String, color: String) {
        val packet = "cs<font color='#$color'>$msg</font>"
        for (P in World.getOnlinePersos()) {
            send(P, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: ALL: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EXCHANGE_REQUEST_OK(out: PrintWriter?, guid: Int, guidT: Int, msgID: Int) {
        val packet = "ERK$guid|$guidT|$msgID"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EXCHANGE_REQUEST_ERROR(out: PrintWriter?, c: Char) {
        val packet = "ERE$c"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EXCHANGE_CONFIRM_OK(out: PrintWriter?, type: Int) {
        val packet = "ECK$type"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EXCHANGE_MOVE_OK(out: Personaje?, type: Char, signe: String, s1: String) {
        var packet = "EMK$type$signe"
        if (s1 != "") packet += s1
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EXCHANGE_OTHER_MOVE_OK(out: PrintWriter?, type: Char, signe: String, s1: String) {
        var packet = "EmK$type$signe"
        if (s1 != "") packet += s1
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EXCHANGE_OK(out: PrintWriter?, ok: Boolean, guid: Int) {
        val packet = "EK" + (if (ok) "1" else "0") + guid
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EXCHANGE_VALID(out: PrintWriter?, c: Char) {
        val packet = "EV$c"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GROUP_INVITATION_ERROR(out: PrintWriter?, s: String) {
        val packet = "PIE$s"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GROUP_INVITATION(out: PrintWriter?, n1: String, n2: String) {
        val packet = "PIK$n1|$n2"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GROUP_CREATE(out: PrintWriter?, g: Personaje.Group) {
        val packet = "PCK" + g.chief._name
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Groupe: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_PL_PACKET(out: PrintWriter?, g: Personaje.Group) {
        val packet = "PL" + g.chief._GUID
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Groupe: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_PR_PACKET(out: Personaje?) {
        val packet = "PR"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_PV_PACKET(out: PrintWriter?, s: String) {
        val packet = "PV$s"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ALL_PM_ADD_PACKET(out: PrintWriter?, g: Personaje.Group) {
        val packet = StringBuilder()
        packet.append("PM+")
        var first = true
        for (p in g.persos) {
            if (!first) packet.append("|")
            packet.append(p.parseToPM())
            first = false
        }
        send(out, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_PM_ADD_PACKET_TO_GROUP(g: Personaje.Group, p: Personaje) {
        val packet = "PM+" + p.parseToPM()
        for (P in g.persos) send(P, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Groupe: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_PM_MOD_PACKET_TO_GROUP(g: Personaje.Group, p: Personaje) {
        val packet = "PM~" + p.parseToPM()
        for (P in g.persos) send(P, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Groupe: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_PM_DEL_PACKET_TO_GROUP(g: Personaje.Group, guid: Int) {
        val packet = "PM-$guid"
        for (P in g.persos) send(P, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Groupe: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_cMK_PACKET_TO_GROUP(g: Personaje.Group, s: String, guid: Int, name: String, msg: String) {
        val packet = "cMK$s|$guid|$name|$msg|"
        for (P in g.persos) send(P, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Groupe: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_FIGHT_DETAILS(out: PrintWriter?, fight: Fight?) {
        if (fight == null) return
        val packet = StringBuilder()
        packet.append("fD").append(fight._id).append("|")
        for (f in fight.getFighters(1)) packet.append(f.packetsName).append("~").append(f._lvl).append(";")
        packet.append("|")
        for (f in fight.getFighters(2)) packet.append(f.packetsName).append("~").append(f._lvl).append(";")
        send(out, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_IQ_PACKET(perso: Personaje?, guid: Int, qua: Int) {
        val packet = "IQ$guid|$qua"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_JN_PACKET(perso: Personaje?, jobID: Int, lvl: Int) {
        val packet = "JN$jobID|$lvl"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GDF_PACKET_TO_MAP(map: Mapa, cell: Mapa.Case) {
        val cellID = cell.id
        val `object` = cell.getObject()
        val packet = "GDF|" + cellID + ";" + `object`.state + ";" + if (`object`.isInteractive) "1" else "0"
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GA_PACKET_TO_MAP(map: Mapa, gameActionID: String, actionID: Int, s1: String, s2: String) {
        var packet = "GA$gameActionID;$actionID;$s1"
        if (s2 != "") packet += ";$s2"
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    fun GAME_SEND_EL_BANK_PACKET(perso: Personaje) {
        val packet = "EL" + perso.parseBankPacket()
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EL_TRUNK_PACKET(perso: Personaje?, t: Trunk) {
        val packet = "EL" + t.parseToTrunkPacket()
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_JX_PACKET(perso: Personaje?, SMs: ArrayList<StatsMetier>) {
        val packet = StringBuilder()
        packet.append("JX")
        for (sm in SMs) {
            packet.append("|").append(sm.template.id).append(";").append(sm._lvl).append(";")
                .append(sm.getXpString(";")).append(";")
        }
        send(perso, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    fun GAME_SEND_JO_PACKET(perso: Personaje?, SMs: ArrayList<StatsMetier>) {
        for (sm in SMs) {
            val packet = "JO" + sm.id + "|" + sm.optBinValue + "|" + sm._slotsPublic
            send(perso, packet)
            if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
        }
    }

    @JvmStatic
    fun GAME_SEND_JO_PACKET(perso: Personaje?, sm: StatsMetier) {
        val packet = "JO" + sm.id + "|" + sm.optBinValue + "|" + sm._slotsPublic
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_JS_PACKET(perso: Personaje?, SMs: ArrayList<StatsMetier>) {
        val packet = StringBuilder("JS")
        for (sm in SMs) {
            packet.append(sm.parseJS())
        }
        send(perso, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EsK_PACKET(perso: Personaje?, str: String) {
        val packet = "EsK$str"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_FIGHT_SHOW_CASE(PWs: ArrayList<PrintWriter?>, guid: Int, cellID: Int) {
        val packet = "Gf$guid|$cellID"
        for (PW in PWs) {
            send(PW, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_Ea_PACKET(perso: Personaje?, str: String) {
        val packet = "Ea$str"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EA_PACKET(perso: Personaje?, str: String) {
        val packet = "EA$str"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_Ec_PACKET(perso: Personaje?, str: String) {
        val packet = "Ec$str"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_Em_PACKET(perso: Personaje?, str: String) {
        val packet = "Em$str"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_IO_PACKET_TO_MAP(map: Mapa, guid: Int, str: String) {
        val packet = "IO$guid|$str"
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_FRIENDLIST_PACKET(perso: Personaje) {
        val packet = "FL" + perso._compte.parseFriendList()
        send(perso, packet)
        if (perso.wife != 0) {
            val packet2 = "FS" + perso._wife_friendlist
            send(perso, packet2)
            if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet2")
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_FRIEND_ONLINE(logando: Personaje, amigo: Personaje?) {
        val packet =
            "Im0143;" + logando._compte._pseudo + " (<b><a href='asfunction:onHref,ShowPlayerPopupMenu," + logando._name + "'>" + logando._name + "</a></b>)"
        send(amigo, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_FA_PACKET(perso: Personaje?, str: String) {
        val packet = "FA$str"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_FD_PACKET(perso: Personaje?, str: String) {
        val packet = "FD$str"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_Rp_PACKET(perso: Personaje?, MP: MountPark?) {
        val packet = StringBuilder()
        if (MP == null) return
        packet.append("Rp").append(MP._owner).append(";").append(MP._price).append(";").append(MP._size).append(";")
            .append(MP.objectNumb).append(";")
        val G = MP._guild
        //Si une guilde est definie
        if (G != null) {
            packet.append(G._name).append(";").append(G._emblem)
        } else {
            packet.append(";")
        }
        send(perso, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_OS_PACKET(perso: Personaje, pano: Int) {
        val packet = StringBuilder()
        packet.append("OS")
        val num = perso.getNumbEquipedItemOfPanoplie(pano)
        if (num <= 0) packet.append("-").append(pano) else {
            packet.append("+").append(pano).append("|")
            val IS = World.getItemSet(pano)
            if (IS != null) {
                val items = StringBuilder()
                //Pour chaque objet de la pano
                for (OT in IS.itemTemplates) {
                    //Si le joueur l'a équipé
                    if (perso.hasEquiped(OT.id)) {
                        //On l'ajoute au packet
                        if (items.length > 0) items.append(";")
                        items.append(OT.id)
                    }
                }
                packet.append(items.toString()).append("|").append(IS.getBonusStatByItemNumb(num).parseToItemSetStats())
            }
        }
        send(perso, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_MOUNT_DESCRIPTION_PACKET(perso: Personaje?, DD: Dragodinde) {
        val packet = "Rd" + DD.parse()
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_Rr_PACKET(perso: Personaje?, str: String) {
        val packet = "Rr$str"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ALTER_GM_PACKET(map: Mapa, perso: Personaje) {
        val packet = "GM|~" + perso.parseToGM()
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_Ee_PACKET(perso: Personaje?, c: Char, s: String) {
        val packet = "Ee$c$s"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_cC_PACKET(perso: Personaje?, c: Char, s: String) {
        val packet = "cC$c$s"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ADD_NPC_TO_MAP(map: Mapa, npc: NPC) {
        val packet = "GM|" + npc.parseGM()
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ADD_PERCO_TO_MAP(map: Mapa) {
        val packet = "GM|" + Percepteur.parseGM(map)
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GDO_PACKET_TO_MAP(map: Mapa, c: Char, cell: Int, itm: Int, i: Int) {
        val packet = "GDO$c$cell;$itm;$i"
        for (z in map.persos) send(z, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GDO_PACKET(p: Personaje?, c: Char, cell: Int, itm: Int, i: Int) {
        val packet = "GDO$c$cell;$itm;$i"
        send(p, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ZC_PACKET(p: Personaje?, a: Int) {
        val packet = "ZC$a"
        send(p, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GIP_PACKET(p: Personaje?, a: Int) {
        val packet = "GIP$a"
        send(p, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_gn_PACKET(p: Personaje?) {
        val packet = "gn"
        send(p, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_gC_PACKET(p: Personaje?, s: String) {
        val packet = "gC$s"
        send(p, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_gV_PACKET(p: Personaje?) {
        val packet = "gV"
        send(p, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_gIM_PACKET(p: Personaje?, g: Guild, c: Char) {
        var packet = "gIM$c"
        when (c) {
            '+' -> packet += g.parseMembersToGM()
        }
        send(p, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_gIB_PACKET(p: Personaje?, infos: String) {
        val packet = "gIB$infos"
        send(p, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_gIH_PACKET(p: Personaje?, infos: String) {
        val packet = "gIH$infos"
        send(p, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_gS_PACKET(p: Personaje?, gm: GuildMember) {
        val packet = StringBuilder()
        packet.append("gS").append(gm.guild._name).append("|").append(gm.guild._emblem.replace(',', '|')).append("|")
            .append(gm.parseRights())
        send(p, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_gJ_PACKET(p: Personaje?, str: String) {
        val packet = "gJ$str"
        send(p, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_gK_PACKET(p: Personaje?, str: String) {
        val packet = "gK$str"
        send(p, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_gIG_PACKET(p: Personaje?, g: Guild) {
        val xpMin = World.getExpLevel(g._lvl).guilde
        val xpMax: Long
        xpMax = if (World.getExpLevel(g._lvl + 1) == null) {
            -1
        } else {
            World.getExpLevel(g._lvl + 1).guilde
        }
        val packet = StringBuilder()
        packet.append("gIG").append(if (g.size > 9) 1 else 0).append("|").append(g._lvl).append("|").append(xpMin)
            .append("|").append(g._xp).append("|").append(xpMax)
        send(p, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun MESSAGE_BOX(out: PrintWriter?, args: String) {
        val packet = "M$args"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_WC_PACKET(perso: Personaje) {
        val packet = "WC" + perso.parseZaapList()
        send(perso._compte.gameThread._out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_WV_PACKET(out: Personaje?) {
        val packet = "WV"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ZAAPI_PACKET(perso: Personaje, list: String) {
        val packet = "Wc" + perso._curCarte._id + "|" + list
        send(perso, packet)
        GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_CLOSE_ZAAPI_PACKET(out: Personaje?) {
        val packet = "Wv"
        send(out, packet)
        GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_WUE_PACKET(out: Personaje?) {
        val packet = "WUE"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EMOTE_LIST(perso: Personaje?, s: String, s1: String) {
        val packet = "eL$s|$s1"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    fun GAME_SEND_NO_EMOTE(out: Personaje?) {
        val packet = "eUE"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    fun REALM_SEND_TOO_MANY_PLAYER_ERROR(out: PrintWriter?) {
        val packet = "AlEw"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun REALM_SEND_REQUIRED_APK(out: PrintWriter?) {
        val chars = "abcdefghijklmnopqrstuvwxyz" // Tu supprimes les lettres dont tu ne veux pas
        val pass = StringBuilder()
        for (x in 0..4) {
            val i = Math.floor(Math.random() * 26).toInt() // Si tu supprimes des lettres tu diminues ce nb
            pass.append(chars[i])
        }
        println(pass)
        val packet = "APK$pass"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ADD_ENEMY(out: Personaje?, str: String) {
        val packet = "iAK$str"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_iAEA_PACKET(out: Personaje?) {
        val packet = "iAEA."
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ENEMY_LIST(perso: Personaje) {
        val packet = "iL" + perso._compte.parseEnemyList()
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_iD_COMMANDE(perso: Personaje?, str: String) {
        val packet = "iD$str"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_BWK(perso: Personaje?, str: String) {
        val packet = "BWK$str"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_KODE(perso: Personaje?, str: String) {
        val packet = "K$str"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_hOUSE(perso: Personaje?, str: String) {
        val packet = "h$str"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_FORGETSPELL_INTERFACE(sign: Char, perso: Personaje?) {
        val packet = "SF$sign"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_R_PACKET(perso: Personaje?, str: String) {
        val packet = "R$str"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_gIF_PACKET(perso: Personaje?, str: String) {
        val packet = "gIF$str"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_gITM_PACKET(perso: Personaje?, str: String) {
        val packet = "gITM$str"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_gITp_PACKET(perso: Personaje?, str: String) {
        val packet = "gITp$str"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_gITP_PACKET(perso: Personaje?, str: String) {
        val packet = "gITP$str"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_IH_PACKET(perso: Personaje?, str: String) {
        val packet = "IH$str"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_FLAG_PACKET(perso: Personaje?, cible: Personaje) {
        val packet = "IC" + cible._curCarte.x + "|" + cible._curCarte.y
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_DELETE_FLAG_PACKET(perso: Personaje?) {
        val packet = "IC|"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_gT_PACKET(perso: Personaje?, str: String) {
        val packet = "gT$str"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_PERCO_INFOS_PACKET(perso: Personaje?, perco: Percepteur, car: String?) {
        val str = StringBuilder()
        str.append("gA").append(car).append(perco._N1).append(",").append(perco._N2).append("|")
        str.append("-1").append("|")
        str.append(World.getCarte(perco._mapID).x).append("|").append(World.getCarte(perco._mapID).y)
        send(perso, str.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$str")
    }

    @JvmStatic
    fun GAME_SEND_GUILDHOUSE_PACKET(perso: Personaje?) {
        val packet = "gUT"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GUILDENCLO_PACKET(perso: Personaje?) {
        val packet = "gUF"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    /**HDV */
    @JvmStatic
    fun GAME_SEND_HDVITEM_SELLING(perso: Personaje) {
        val packet = StringBuilder("EL")
        val entries =
            perso._compte.getHdvItems(Math.abs(perso._isTradingWith)) //Récupère un tableau de tout les items que le personnage à en vente dans l'HDV où il est
        var isFirst = true
        for (curEntry in entries) {
            if (curEntry == null) break
            if (!isFirst) packet.append("|")
            packet.append(curEntry.parseToEL())
            isFirst = false
        }
        send(perso, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    fun GAME_SEND_EHm_PACKET(out: Personaje?, sign: String, str: String) {
        val packet = "EHm$sign$str"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EHM_PACKET(out: Personaje?, sign: String, str: String) {
        val packet = "EHM$sign$str"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EHP_PACKET(
        out: Personaje?,
        templateID: Int,
        price: Int
    ) //Packet d'envoie du prix moyen du template (En réponse a un packet EHP)
    {
        val packet = "EHP$templateID|$price"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EHL_PACKET(
        out: Personaje?,
        categ: Int,
        templates: String
    ) //Packet de listage des templates dans une catégorie (En réponse au packet EHT)
    {
        val packet = "EHL$categ|$templates"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EHl_PACKET(out: Personaje?, items: String) //Packet de listage des objets en vente
    {
        val packet = "EHl$items"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    /**HDV */
    @JvmStatic
    fun GAME_SEND_WEDDING(c: Mapa?, action: Int, homme: Int, femme: Int, parlant: Int) {
        val packet = "GA;$action;$homme;$homme,$femme,$parlant"
        val Homme = World.getPersonnage(homme)
        send(Homme, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_PF(perso: Personaje?, str: String) {
        val packet = "PF$str"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_MERCHANT_LIST(P: Personaje, mapID: Short) {
        val packet = StringBuilder()
        packet.append("GM|~")
        if (World.getSeller(P._curCarte._id) == null) return
        for (pID in World.getSeller(P._curCarte._id)) {
            if (!World.getPersonnage(pID!!).isOnline && World.getPersonnage(pID).is_showSeller) {
                packet.append(World.getPersonnage(pID).parseToMerchant()).append("|")
            }
        }
        if (packet.length < 5) return
        send(P, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(perso: Personaje?, item: Objet) {
        val packet = StringBuilder()
        packet.append("OCO").append(item.parseItem())
        send(perso, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_WELCOME(perso: Personaje?) {
        val packet = StringBuilder()
        packet.append("TB")
        send(perso, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_TAXE(perso: Personaje?, price: Long) {
        val packet = StringBuilder()
        packet.append("Eq1|1|").append(price)
        send(perso, packet.toString())
    }

    @JvmStatic
    fun GAME_SEND_INFO_HIGHLIGHT_PACKET(perso: Personaje?, args: String) {
        val packet = "IH$args"
        send(perso, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_CHALLENGE_FIGHT(fight: Fight, team: Int, str: String?) {
        val packet = StringBuilder()
        packet.append("Gd").append(str)
        for (fighter in fight.getFighters(team)) {
            if (fighter.hasLeft()) continue
            if (fighter.personnage == null || !fighter.personnage.isOnline) continue
            send(fighter.personnage, packet.toString())
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_CHALLENGE_PERSO(p: Personaje?, str: String?) {
        val packet = StringBuilder()
        packet.append("Gd").append(str)
        send(p, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_Im_PACKET_TO_CHALLENGE(fight: Fight, challenge: Int, str: String?) {
        val packet = StringBuilder()
        packet.append("Im").append(str)
        for (fighter in fight.getFighters(challenge)) {
            if (fighter.hasLeft()) continue
            if (fighter.personnage == null || !fighter.personnage.isOnline) continue
            send(fighter.personnage, packet.toString())
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_SUBSCRIBE_MESSAGE(p: Personaje?, str: String?) {
        val packet = StringBuilder()
        packet.append("BP").append(str)
        send(p, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    fun GAME_SEND_CRAFT_PUBLIC_MODE(p: Personaje, C: Char, JobID: String?) {
        val packet = StringBuilder()
        packet.append("EW").append(C).append(p._GUID).append("|").append(JobID)
        send(p, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_CRAFT_PUBLIC_MODE(p: Personaje?) {
        val packet = StringBuilder()
        packet.append("EW+")
        send(p, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_Ej_PACKET(p: Personaje?, S: Char, jobID: Int) {
        val packet = StringBuilder()
        packet.append("Ej").append(S).append(jobID)
        send(p, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EJ_PACKET(p: Personaje?, str: String?) {
        val packet = StringBuilder()
        packet.append("EJ").append(str)
        send(p, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ATTRIBUTE_GIFT_SUCCESS(out: PrintWriter?) {
        val packet = StringBuilder()
        packet.append("AGK")
        send(out, packet.toString())
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GIFT(out: PrintWriter?, str: String) {
        val packet = "Ag1|$str"
        send(out, packet)
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GA_CLEAR_PACKET_TO_FIGHT(fight: Fight, teams: Int) {
        val packet = "GA;0"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft() || f.personnage == null || !f.personnage.isOnline) continue
            send(f.personnage, packet)
        }
        if (Main.CONFIG_DEBUG) GameServer.addToSockLog("Game: Fight: Send>>$packet")
    }
}