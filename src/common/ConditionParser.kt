package common

import com.singularsys.jep.Jep
import com.singularsys.jep.JepException
import game.GameServer
import objects.Personaje
import java.util.*

object ConditionParser {
    @JvmStatic
    fun validConditions(perso: Personaje, req: String?): Boolean {
        var req = req
        if (req == null || req == "") return true
        if (req.contains("BI")) return false
        val jep = Jep()
        req = req.replace("&", "&&").replace("=", "==").replace("|", "||").replace("!", "!=").replace("~", "==")
        if (req.contains("PO")) req = havePO(req, perso)
        if (req.contains("PN")) req = canPN(req, perso)
        if (req.contains("PJ")) req = canPJ(req, perso)
        //TODO : Gérer Pj
        try {
            //Stats stuff compris
            jep.addVariable("CI", perso.totalStats.getEffect(Constants.STATS_ADD_INTE).toDouble())
            jep.addVariable("CV", perso.totalStats.getEffect(Constants.STATS_ADD_VITA).toDouble())
            jep.addVariable("CA", perso.totalStats.getEffect(Constants.STATS_ADD_AGIL).toDouble())
            jep.addVariable("CW", perso.totalStats.getEffect(Constants.STATS_ADD_SAGE).toDouble())
            jep.addVariable("CC", perso.totalStats.getEffect(Constants.STATS_ADD_CHAN).toDouble())
            jep.addVariable("CS", perso.totalStats.getEffect(Constants.STATS_ADD_FORC).toDouble())
            //Stats de bases
            jep.addVariable("Ci", perso._baseStats.getEffect(Constants.STATS_ADD_INTE).toDouble())
            jep.addVariable("Cs", perso._baseStats.getEffect(Constants.STATS_ADD_FORC).toDouble())
            jep.addVariable("Cv", perso._baseStats.getEffect(Constants.STATS_ADD_VITA).toDouble())
            jep.addVariable("Ca", perso._baseStats.getEffect(Constants.STATS_ADD_AGIL).toDouble())
            jep.addVariable("Cw", perso._baseStats.getEffect(Constants.STATS_ADD_SAGE).toDouble())
            jep.addVariable("Cc", perso._baseStats.getEffect(Constants.STATS_ADD_CHAN).toDouble())
            //Autre
            jep.addVariable("Ps", perso._align.toDouble())
            jep.addVariable("Pa", perso.aLvl.toDouble())
            jep.addVariable("PP", perso.grade.toDouble())
            jep.addVariable("PL", perso._lvl.toDouble())
            jep.addVariable("PK", perso._kamas.toDouble())
            jep.addVariable("PG", perso._classe.toDouble())
            jep.addVariable("PS", perso._sexe.toDouble())
            jep.addVariable("PZ", 1.0) //Abonnement TODO
            jep.addVariable("PX", perso._compte._gmLvl.toDouble())
            jep.addVariable("PW", perso.maxPod.toDouble())
            jep.addVariable("PB", perso._curCarte.subArea._id.toDouble())
            jep.addVariable("PR", (if (perso.wife > 0) 1 else 0).toDouble())
            jep.addVariable("SI", perso._curCarte._id.toDouble())
            //Les pierres d'ames sont lancables uniquement par le lanceur.
            jep.addVariable("MiS", perso._GUID.toDouble())
            jep.parse(req)
            val result = jep.evaluate()
            var ok = false
            if (result != null) ok = java.lang.Boolean.parseBoolean(result.toString())
            return ok
        } catch (e: JepException) {
            println("An error occurred: " + e.message)
        }
        return true
    }

    fun havePO(cond: String, perso: Personaje): String //On remplace les PO par leurs valeurs si possession de l'item
    {
        var Jump = false
        var ContainsPO = false
        var CutFinalLenght = true
        var copyCond = StringBuilder()
        var finalLength = 0
        if (Main.CONFIG_DEBUG) GameServer.addToLog("Entered Cond : $cond")
        if (cond.contains("&&")) {
            for (cur in cond.split("&&".toRegex()).toTypedArray()) {
                if (cond.contains("==")) {
                    for (cur2 in cur.split("==".toRegex()).toTypedArray()) {
                        if (cur2.contains("PO")) {
                            ContainsPO = true
                            continue
                        }
                        if (Jump) {
                            copyCond.append(cur2)
                            Jump = false
                            continue
                        }
                        if (!cur2.contains("PO") && !ContainsPO) {
                            copyCond.append(cur2).append("==")
                            Jump = true
                            continue
                        }
                        if (cur2.contains("!=")) continue
                        ContainsPO = false
                        if (perso.hasItemTemplate(cur2.toInt(), 1)) {
                            copyCond.append(cur2.toInt()).append("==").append(cur2.toInt())
                        } else {
                            copyCond.append(cur2.toInt()).append("==").append(0)
                        }
                    }
                }
                if (cond.contains("!=")) {
                    for (cur2 in cur.split("!=".toRegex()).toTypedArray()) {
                        if (cur2.contains("PO")) {
                            ContainsPO = true
                            continue
                        }
                        if (Jump) {
                            copyCond.append(cur2)
                            Jump = false
                            continue
                        }
                        if (!cur2.contains("PO") && !ContainsPO) {
                            copyCond.append(cur2).append("!=")
                            Jump = true
                            continue
                        }
                        if (cur2.contains("==")) continue
                        ContainsPO = false
                        if (perso.hasItemTemplate(cur2.toInt(), 1)) {
                            copyCond.append(cur2.toInt()).append("!=").append(cur2.toInt())
                        } else {
                            copyCond.append(cur2.toInt()).append("!=").append(0)
                        }
                    }
                }
                copyCond.append("&&")
            }
        } else if (cond.contains("||")) {
            for (cur in cond.split("\\|\\|".toRegex()).toTypedArray()) {
                if (cond.contains("==")) {
                    for (cur2 in cur.split("==".toRegex()).toTypedArray()) {
                        if (cur2.contains("PO")) {
                            ContainsPO = true
                            continue
                        }
                        if (Jump) {
                            copyCond.append(cur2)
                            Jump = false
                            continue
                        }
                        if (!cur2.contains("PO") && !ContainsPO) {
                            copyCond.append(cur2).append("==")
                            Jump = true
                            continue
                        }
                        if (cur2.contains("!=")) continue
                        ContainsPO = false
                        if (perso.hasItemTemplate(cur2.toInt(), 1)) {
                            copyCond.append(cur2.toInt()).append("==").append(cur2.toInt())
                        } else {
                            copyCond.append(cur2.toInt()).append("==").append(0)
                        }
                    }
                }
                if (cond.contains("!=")) {
                    for (cur2 in cur.split("!=".toRegex()).toTypedArray()) {
                        if (cur2.contains("PO")) {
                            ContainsPO = true
                            continue
                        }
                        if (Jump) {
                            copyCond.append(cur2)
                            Jump = false
                            continue
                        }
                        if (!cur2.contains("PO") && !ContainsPO) {
                            copyCond.append(cur2).append("!=")
                            Jump = true
                            continue
                        }
                        if (cur2.contains("==")) continue
                        ContainsPO = false
                        if (perso.hasItemTemplate(cur2.toInt(), 1)) {
                            copyCond.append(cur2.toInt()).append("!=").append(cur2.toInt())
                        } else {
                            copyCond.append(cur2.toInt()).append("!=").append(0)
                        }
                    }
                }
                copyCond.append("||")
            }
        } else {
            CutFinalLenght = false
            if (cond.contains("==")) {
                for (cur in cond.split("==".toRegex()).toTypedArray()) {
                    if (cur.contains("PO")) {
                        continue
                    }
                    if (cur.contains("!=")) continue
                    if (perso.hasItemTemplate(cur.toInt(), 1)) {
                        copyCond.append(cur.toInt()).append("==").append(cur.toInt())
                    } else {
                        copyCond.append(cur.toInt()).append("==").append(0)
                    }
                }
            }
            if (cond.contains("!=")) {
                for (cur in cond.split("!=".toRegex()).toTypedArray()) {
                    if (cur.contains("PO")) {
                        continue
                    }
                    if (cur.contains("==")) continue
                    if (perso.hasItemTemplate(cur.toInt(), 1)) {
                        copyCond.append(cur.toInt()).append("!=").append(cur.toInt())
                    } else {
                        copyCond.append(cur.toInt()).append("!=").append(0)
                    }
                }
            }
        }
        if (CutFinalLenght) {
            finalLength = copyCond.length - 2 //On retire les deux derniers carractères (|| ou &&)
            copyCond = StringBuilder(copyCond.substring(0, finalLength))
        }
        if (Main.CONFIG_DEBUG) GameServer.addToLog("Returned Cond : $copyCond")
        return copyCond.toString()
    }

    fun canPN(cond: String?, perso: Personaje): String //On remplace le PN par 1 et si le nom correspond == 1 sinon == 0
    {
        val copyCond = StringBuilder()
        for (cur in cond!!.split("==".toRegex()).toTypedArray()) {
            if (cur.contains("PN")) {
                copyCond.append("1==")
                continue
            }
            if (perso._name.lowercase(Locale.getDefault()).compareTo(cur) == 0) {
                copyCond.append("1")
            } else {
                copyCond.append("0")
            }
        }
        return copyCond.toString()
    }

    fun canPJ(
        cond: String?,
        perso: Personaje
    ): String //On remplace le PJ par 1 et si le metier correspond == 1 sinon == 0
    {
        val copyCond = StringBuilder()
        for (cur in cond!!.split("==".toRegex()).toTypedArray()) {
            if (cur.contains("PJ")) {
                copyCond.append("1==")
                continue
            }
            if (perso.getMetierByID(cur.toInt()) != null) {
                copyCond.append("1")
            } else {
                copyCond.append("0")
            }
        }
        return copyCond.toString()
    }
}