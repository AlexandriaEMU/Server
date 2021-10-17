package objects.acciones

import common.SQLManager
import common.SocketManager
import objects.Personaje

object menosuno {
    fun menosuno(personaje: Personaje){
        //Sauvagarde du perso et des item avant.
        SQLManager.SAVE_PERSONNAGE(personaje, true)
        if (personaje.deshonor >= 1) {
            SocketManager.GAME_SEND_Im_PACKET(personaje, "183")
            return
        }
        val cost: Int = personaje._compte.bankCost
        if (cost > 0) {
            val playerKamas: Long = personaje._kamas
            val kamasRemaining = playerKamas - cost
            val bankKamas: Long = personaje._compte.GetBankKamas()
            val totalKamas = bankKamas + playerKamas
            if (kamasRemaining < 0) //Si le joueur n'a pas assez de kamas SUR LUI pour ouvrir la banque
            {
                if (bankKamas >= cost) {
                    personaje._compte.setBankKamas(bankKamas - cost) //On modifie les kamas de la banque
                } else if (totalKamas >= cost) {
                    personaje._kamas = 0 //On puise l'entièreter des kamas du joueurs. Ankalike ?
                    personaje._compte.setBankKamas(totalKamas - cost) //On modifie les kamas de la banque
                    SocketManager.GAME_SEND_STATS_PACKET(personaje)
                    SocketManager.GAME_SEND_Im_PACKET(personaje, "020;$playerKamas")
                } else {
                    SocketManager.MESSAGE_BOX(personaje._compte.gameThread._out, "110|$cost")
                    return
                }
            } else  //Si le joueur a les kamas sur lui on lui retire directement
            {
                personaje._kamas = kamasRemaining
                SocketManager.GAME_SEND_STATS_PACKET(personaje)
                SocketManager.GAME_SEND_Im_PACKET(personaje, "020;$cost")
            }
        }
        SocketManager.GAME_SEND_ECK_PACKET(personaje._compte.gameThread._out, 5, "")
        SocketManager.GAME_SEND_EL_BANK_PACKET(personaje)
        personaje.is_away = true
        personaje.isInBank = true
    }
}