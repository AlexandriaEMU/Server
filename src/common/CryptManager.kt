package common

import objects.Mapa
import java.util.*

object CryptManager {
    @JvmStatic
    fun CryptIP(IP: String): String {
        val Splitted = IP.split("\\.".toRegex()).toTypedArray()
        val Encrypted = StringBuilder()
        var Count = 0
        var i = 0
        while (i < 50) {
            var o = 0
            while (o < 50) {
                if (i and 15 shl 4 or o and 15 == Splitted[Count].toInt()) {
                    val A = (i + 48).toChar()
                    val B = (o + 48).toChar()
                    Encrypted.append(Character.toString(A)).append(Character.toString(B))
                    i = 0
                    o = 0
                    Count++
                    if (Count == 4) return Encrypted.toString()
                }
                o++
            }
            i++
        }
        return "DD"
    }

    @JvmStatic
    fun CryptPort(config_game_port: Int): String {
        val HASH = charArrayOf(
            'a',
            'b',
            'c',
            'd',
            'e',
            'f',
            'g',
            'h',
            'i',
            'j',
            'k',
            'l',
            'm',
            'n',
            'o',
            'p',
            'q',
            'r',
            's',
            't',
            'u',
            'v',
            'w',
            'x',
            'y',
            'z',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F',
            'G',
            'H',
            'I',
            'J',
            'K',
            'L',
            'M',
            'N',
            'O',
            'P',
            'Q',
            'R',
            'S',
            'T',
            'U',
            'V',
            'W',
            'X',
            'Y',
            'Z',
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            '-',
            '_'
        )
        var P = config_game_port
        val nbr64 = StringBuilder()
        for (a in 2 downTo 0) {
            nbr64.append(HASH[(P / Math.pow(64.0, a.toDouble())).toInt()])
            P = (P % Math.pow(64.0, a.toDouble()).toInt())
        }
        return nbr64.toString()
    }

    @JvmStatic
    fun cellID_To_Code(cellID: Int): String {
        val HASH = charArrayOf(
            'a',
            'b',
            'c',
            'd',
            'e',
            'f',
            'g',
            'h',
            'i',
            'j',
            'k',
            'l',
            'm',
            'n',
            'o',
            'p',
            'q',
            'r',
            's',
            't',
            'u',
            'v',
            'w',
            'x',
            'y',
            'z',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F',
            'G',
            'H',
            'I',
            'J',
            'K',
            'L',
            'M',
            'N',
            'O',
            'P',
            'Q',
            'R',
            'S',
            'T',
            'U',
            'V',
            'W',
            'X',
            'Y',
            'Z',
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            '-',
            '_'
        )
        val char1 = cellID / 64
        val char2 = cellID % 64
        return HASH[char1].toString() + "" + HASH[char2]
    }

    @JvmStatic
    fun cellCode_To_ID(cellCode: String): Int {
        val HASH = charArrayOf(
            'a',
            'b',
            'c',
            'd',
            'e',
            'f',
            'g',
            'h',
            'i',
            'j',
            'k',
            'l',
            'm',
            'n',
            'o',
            'p',
            'q',
            'r',
            's',
            't',
            'u',
            'v',
            'w',
            'x',
            'y',
            'z',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F',
            'G',
            'H',
            'I',
            'J',
            'K',
            'L',
            'M',
            'N',
            'O',
            'P',
            'Q',
            'R',
            'S',
            'T',
            'U',
            'V',
            'W',
            'X',
            'Y',
            'Z',
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            '-',
            '_'
        )
        val char1 = cellCode[0]
        val char2 = cellCode[1]
        var code1 = 0
        var code2 = 0
        var a = 0
        while (a < HASH.size) {
            if (HASH[a] == char1) {
                code1 = a * 64
            }
            if (HASH[a] == char2) {
                code2 = a
            }
            a++
        }
        return code1 + code2
    }

    @JvmStatic
    fun getIntByHashedValue(c: Char): Int {
        val HASH = charArrayOf(
            'a',
            'b',
            'c',
            'd',
            'e',
            'f',
            'g',
            'h',
            'i',
            'j',
            'k',
            'l',
            'm',
            'n',
            'o',
            'p',
            'q',
            'r',
            's',
            't',
            'u',
            'v',
            'w',
            'x',
            'y',
            'z',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F',
            'G',
            'H',
            'I',
            'J',
            'K',
            'L',
            'M',
            'N',
            'O',
            'P',
            'Q',
            'R',
            'S',
            'T',
            'U',
            'V',
            'W',
            'X',
            'Y',
            'Z',
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            '-',
            '_'
        )
        for (a in HASH.indices) {
            if (HASH[a] == c) {
                return a
            }
        }
        return -1
    }

    @JvmStatic
    fun getHashedValueByInt(c: Int): Char {
        val HASH = charArrayOf(
            'a',
            'b',
            'c',
            'd',
            'e',
            'f',
            'g',
            'h',
            'i',
            'j',
            'k',
            'l',
            'm',
            'n',
            'o',
            'p',
            'q',
            'r',
            's',
            't',
            'u',
            'v',
            'w',
            'x',
            'y',
            'z',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F',
            'G',
            'H',
            'I',
            'J',
            'K',
            'L',
            'M',
            'N',
            'O',
            'P',
            'Q',
            'R',
            'S',
            'T',
            'U',
            'V',
            'W',
            'X',
            'Y',
            'Z',
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            '-',
            '_'
        )
        return HASH[c]
    }

    @JvmStatic
    fun parseStartCell(map: Mapa, num: Int): ArrayList<Mapa.Case?>? {
        var list: ArrayList<Mapa.Case?>? = null
        var infos: String? = null
        if (!map._placesStr.equals("-1", ignoreCase = true)) {
            infos = map._placesStr.split("\\|".toRegex()).toTypedArray()[num]
            var a = 0
            list = ArrayList()
            while (a < infos.length) {
                list.add(
                    map.getCase(
                        (getIntByHashedValue(infos[a]) shl 6) + getIntByHashedValue(
                            infos[a + 1]
                        )
                    )
                )
                a = a + 2
            }
        }
        return list
    }

    @JvmStatic
    fun DecompileMapData(map: Mapa?, dData: String): Map<Int, Mapa.Case> {
        val cells: MutableMap<Int, Mapa.Case> = TreeMap()
        var f = 0
        while (f < dData.length) {
            val CellData = dData.substring(f, f + 10)
            val CellInfo: MutableList<Byte> = ArrayList()
            for (i in 0 until CellData.length) CellInfo.add(getIntByHashedValue(CellData[i]).toByte())
            val Type: Int = CellInfo[2].toInt() and 56 shr 3
            val IsSightBlocker = CellInfo[0].toInt() and 1 != 0
            val layerObject2: Int =
                (CellInfo[0].toInt() and 2 shl 12) + (CellInfo[7].toInt() and 1 shl 12) + (CellInfo[8].toInt() shl 6) + CellInfo[9]
            val layerObject2Interactive = CellInfo[7].toInt() and 2 shr 1 != 0
            val obj = if (layerObject2Interactive) layerObject2 else -1
            cells[f / 10] = Mapa.Case(map, f / 10, Type != 0, IsSightBlocker, obj)
            f += 10
        }
        return cells
    }

    //Fonction qui convertis tout les textes ANSI(Unicode) en UTF-8. Les fichiers doivent �tre cod� en ANSI sinon les phrases seront illisible.
    @JvmStatic
    fun toUtf(_in: String): String {
        var _out = ""
        try {
            _out = String(_in.toByteArray(charset("UTF8")))
        } catch (e: Exception) {
            println("Conversion en UTF-8 echoue! : " + e.message)
        }
        return _out
    }

    //Utilis� pour convertir les inputs UTF-8 en String normal.
    @JvmStatic
    fun toUnicode(_in: String): String {
        var _out = ""
        try {
            _out = String(_in.toByteArray(), charset("UTF8"))
        } catch (e: Exception) {
            println("Conversion en UTF-8 echoue! : " + e.message)
        }
        return _out
    }
}