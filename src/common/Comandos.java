package common;

import game.GameServer.SaveThread;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

import javax.swing.Timer;

import common.comandos.admin.*;

import objects.Action;
import objects.Compte;
import objects.Hdv;
import objects.HdvEntry;
import objects.NPC_tmpl;
import objects.Objet;
import objects.Personaje;
import objects.Mapa.MountPark;
import objects.NPC_tmpl.NPC;
import objects.NPC_tmpl.NPC_reponse;
import objects.Objet.ObjTemplate;
import objects.PetsEntry;


public class Comandos {
	Compte cuenta;
	Personaje personaje;
	PrintWriter imprimir;

	//Guardado
	private boolean _TimerStart = false;
	Timer _timer;
	
	private Timer createTimer(final int time) {
	    ActionListener action = new ActionListener () {
	    	int Time = time;
	        public void actionPerformed (ActionEvent event) {
	        	Time = Time-1;
	        	if(Time == 1) {
	        		SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;"+Time+" minuto");
	        	}else {
		        	SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;"+Time+" minutos");
	        	}
	        	if(Time <= 0) {
	        		for(Personaje perso : World.getOnlinePersos()) {
	        			perso.get_compte().getGameThread().kick();
	        		}
	    			System.exit(0);
	        	}
	        }
	      };
	    // Génération du repeat toutes les minutes.
	    return new Timer (60000, action);//60000
	}
	
	public Comandos(Personaje personaje) {
		this.cuenta = personaje.get_compte();
		this.personaje = personaje;
		this.imprimir = cuenta.getGameThread().get_out();
	}
	
	public void comandosdeconsola(String paquete) {
		
		if(cuenta.get_gmLvl() < 1) {
			cuenta.getGameThread().kick();
			return;
		}
		
		String msg = paquete.substring(2);
		String[] infos = msg.split(" ");
		if(infos.length == 0)return;
		String comando = infos[0];
		
		if(Main.canLog) {
			Main.addToMjLog(cuenta.get_curIP()+": "+ cuenta.get_name()+" "+ personaje.get_name()+"=>"+msg);
		}
		
		if(cuenta.get_gmLvl() == 1) {
			comandoslvl1(comando, infos, msg);
		}else

		if(cuenta.get_gmLvl() == 2) {
			comandoslvl2(comando, infos, msg);
		}
		else

		if(cuenta.get_gmLvl() == 3) {
			comandoslvl3(comando, infos, msg);
		}
		else

		if(cuenta.get_gmLvl() >= 4) {
			commandGmFour(comando, infos, msg);
		}
	}
	
	public void comandoslvl1(String command, String[] infos, String msg) {

		if(cuenta.get_gmLvl() < 1) {
			cuenta.getGameThread().kick();
			return;
		}

		//Comando INFORMACION - Info del server
		if(command.equalsIgnoreCase("INFORMACION")) {
			Informacion.INSTANCE.info(imprimir);
			return;
		}else

		//Comando ACTUALIZARMOBS - Actualiza los mobs del server
		if(command.equalsIgnoreCase("ACTUALIZARMOBS")) {
			ActualizarMobs.INSTANCE.actualizarmobs(imprimir,personaje);
			return;

		//Comando INFOMAPA - Lanza la informacion del mapa(NPC, MOBS, Recaudadores)
		}if(command.equalsIgnoreCase("INFOMAPA")) {
			MapaInformacion.INSTANCE.mapainfo(imprimir,personaje);
		}else

		//Comando ONLINE - Muestra los jugadores online en tiempo real
		if(command.equalsIgnoreCase("ONLINE")) {
			Online.INSTANCE.online(imprimir);
		}else

		//Comando POSPELEA - Muestra los pos de las peleas en tiempo real
		if(command.equalsIgnoreCase("POSPELEA")) {
			VerPosPelea.INSTANCE.pospelea(imprimir,personaje);
		}else

		//Comando CREARGREMIO - Crear un gremio a gusto
		if(command.equalsIgnoreCase("CREARGREMIO")) {
			CrearGremio.INSTANCE.creargremio(imprimir,personaje,infos);
		}else

		//Comando ALTERNARAGRESION - Activa y desactiva las agresiones sobre un personaje
		if(command.equalsIgnoreCase("ALTERNARAGRESION")) {
			AlternarAgresion.INSTANCE.alternaragresiones(imprimir,personaje,infos);
		}else

		//Comando ANUNCIO - Genera un anuncio para todo el server
		if(command.equalsIgnoreCase("ANUNCIO")) {
			Anuncio.INSTANCE.anuncio(msg);
		}else

		//Comando DEMORPH - Quita la transformacion de un personaje
		if(command.equalsIgnoreCase("DEMORPH")) {
			Demorph.INSTANCE.demorph(imprimir,personaje,infos);
		} else

		//Comando IR - Te lleva donde el personaje seleccionado este
		if(command.equalsIgnoreCase("IR")) {
			Ir.INSTANCE.ir(imprimir,personaje,infos);
		}else

		//Comando TRAER - Trae el personaje hacia donde estes
		if(command.equalsIgnoreCase("TRAER")) {
			Traer.INSTANCE.traer(imprimir,personaje,infos);
		}else

		//Comando NANUNCIO - Crea un anuncio para todo el server con tu nombre por delante
		if(command.equalsIgnoreCase("NANUNCIO")) {
			NAnuncio.INSTANCE.nanuncio(msg,personaje);
		}else

		//Comando MOVER - Teletransportate a la ID del mapa que quieras
		if(command.equalsIgnoreCase("MOVER")) {
			Mover.INSTANCE.mover(imprimir,personaje,infos);
		}else

		//Comando IRMAPA - Teletransporta mediante coordenadas
		if(command.equalsIgnoreCase("IRMAPA")) {
		IrMapa.INSTANCE.irmapa(imprimir,personaje,infos);
		}else

		//Comando HACERACCION - Genera un accion desde la consola de admin (NAME TYPE ARGS COND)
		if(command.equalsIgnoreCase("HACERACCION")) {
			HacerAccion.INSTANCE.haceraccion(imprimir, personaje, infos);
		}else {
			String mess = "Comando incorrecto";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,mess);
		}
	}
	
	public void comandoslvl2(String command, String[] infos, String msg) {
		if(cuenta.get_gmLvl() < 2) {
			cuenta.getGameThread().kick();
			return;
		}

		//Comando SILENCIAR - Silencia al personaje por el tiempo que quieras
		if(command.equalsIgnoreCase("SILENCIAR")) {
			Silenciar.INSTANCE.silenciar(imprimir, personaje, infos);
			return;
		}else

		//Comando HABLAR - Quita el silenciado a un personaje
		if(command.equalsIgnoreCase("HABLAR")) {
			Hablar.INSTANCE.hablar(imprimir, personaje, infos);
		}else

		//Comando EXPULSAR - Saca a un personaje del servidor
		if(command.equalsIgnoreCase("EXPULSAR")) {
			Expulsar.INSTANCE.expulsar(imprimir, personaje, infos);
		}else

		//Comando PUNTOSHECHIZO - Agrega los puntos de hechizo a ti mismo o a un personaj espesifico
		if(command.equalsIgnoreCase("PUNTOSHECHIZO")) {
			PuntosHechizos.INSTANCE.puntoshechizo(imprimir, personaje, infos);
		}else

		//Comando APRENDERHECHIZO - Aprende cualquier hechizo en tu personaje o uno espesifico
		if(command.equalsIgnoreCase("APRENDERHECHIZO")) {
			AprenderHechizo.INSTANCE.aprenderhechizo(imprimir, personaje, infos);
		}else

		//Comando ALINEACION - Cambia la alineacion de un personaje
		if(command.equalsIgnoreCase("ALINEACION")) {
			Alineacion.INSTANCE.alineacion(imprimir, personaje, infos);
		}else

		//Comando AGREGARESPUESTA - Agrega una respuesta a una pregunta de un NPC
		if(command.equalsIgnoreCase("AGREGARESPUESTA")) {
			AgregarRespuesta.INSTANCE.agregarespuesta(imprimir, infos);
			return;
		}else

		//Comando VERRESPUESTAS - Ver las respuestas de una pregunta de un NPC
		if(command.equalsIgnoreCase("SHOWREPONSES")) {
			VerRespuestas.INSTANCE.verrespuestas(imprimir, infos);
			return;
		}else

		//Comando HONOR - Agrega honor a un personaje
		if(command.equalsIgnoreCase("HONOR")) {
			Honor.INSTANCE.honor(imprimir, personaje, infos);
		}else

		//Comando XPOFICIO - Agrega experiencia a los oficios
		if(command.equalsIgnoreCase("XPOFICIO")) {
			XpOficios.INSTANCE.xpoficio(imprimir, personaje, infos);
		}else

		//Comando APRENDEROFICIO - Aprende el oficio que quieras
		if(command.equalsIgnoreCase("APRENDEROFICIO")) {
			AprenderOficio.INSTANCE.aprenderoficio(imprimir, personaje, infos);
		}else

		//Comando CAPITAL - Agrega puntos de capital al personaje
		if(command.equalsIgnoreCase("CAPITAL")) {
			Capital.INSTANCE.capital(imprimir, personaje, infos);
		}

		//Comando TAMAÑO - Cambia el tamaño de un personaje
		if(command.equalsIgnoreCase("TAMAÑO")) {
			Tamaño.INSTANCE.tamaño(imprimir, personaje, infos);
		}else

		//Comando TRANSFORMAR - Cambia la forma de un personaje
		if(command.equalsIgnoreCase("TRANSFORMAR")) {
			Transformar.INSTANCE.transformar(imprimir, personaje, infos);
		}

		//Comando MOVERNPC - Mueve un NPC de un lugar a otro
		if(command.equalsIgnoreCase("MOVERNPC")) {
			MoverNPC.INSTANCE.movernpc(imprimir, personaje, infos);
		}else

		//Comando AGREGARSET - Agrega el grupo de item que forman un set por ID
		if(command.equalsIgnoreCase("AGREGARSET")) {
			AgregarSet.INSTANCE.agregarset(imprimir, personaje, infos);
		}else

		//Comando NIVEL - Agrega la cantidad de nivel que quieras a un personaje
		if(command.equalsIgnoreCase("NIVEL")) {
			Nivel.INSTANCE.nivel(imprimir, personaje, infos);
        }else

		//Comando PDVPORC - Aumenta el porcentaje de los puntos de vida
		if(command.equalsIgnoreCase("PDVPORC")) {
			PdvPorc.INSTANCE.pdvporcentaje(imprimir, personaje, infos);
        }else

		//Comando KAMAS - Aumenta las kamas de un personaje
		if(command.equalsIgnoreCase("KAMAS")) {
			Kamas.INSTANCE.kamas(imprimir, personaje, infos);
		}else

		//Comando OBJETO - Agregar cualquier item u objeto a un personaje
		if(command.equalsIgnoreCase("OBJETO")) {
			Objeto.INSTANCE.objeto(imprimir, personaje, infos, cuenta);
		}else

		//Comando TITULO - Cambia el titulo de cualquier personaje
		if (command.equalsIgnoreCase("TITULO")) {
			Titulo.INSTANCE.titulo(imprimir, personaje, infos);
		}else {
			this.comandoslvl1(command, infos, msg);
		}
	}
	
	public void comandoslvl3(String command, String[] infos, String msg) {
		if(cuenta.get_gmLvl() < 3) {
			cuenta.getGameThread().kick();
			return;
		}

		//Comando SALIR - Cierra el servidor
		if(command.equalsIgnoreCase("SALIR")) {
			Salir.INSTANCE.salir();
		}else

		//Comando GUARDAR - Guarda todo el servidor completo
		if(command.equalsIgnoreCase("GUARDAR") && !Main.isSaving) {
			Guardar.INSTANCE.guardar(imprimir);
		}else

		//Comando ELIMINARCELDAPELEA - Elimina la celda de la posicion de pelea
		if(command.equalsIgnoreCase("ELIMINARCELDAPELEA")) {
			EliminarCeldaPelea.INSTANCE.eliminarceldapelea(imprimir, personaje, infos);
		}else

		//Comando BAN - Banea a un personaje
		if(command.equalsIgnoreCase("BAN")) {
			Ban.INSTANCE.ban(imprimir, infos);
		}else

		//Comando DESBAN - Elimina el baneo de un personaje
		if(command.equalsIgnoreCase("DESBAN")) {
			Desban.INSTANCE.desban(imprimir, infos);
		}else

		//Comando AGREGARCELDAPELEA - Agrega una celda para posicion de pelea
		if(command.equalsIgnoreCase("AGREGARCELDAPELEA")) {
			AgregarCeldaPelea.INSTANCE.agregarceldapelea(imprimir, personaje, infos);
		}else

		//Comando GRUPOMAXIMO - Modificar el maximo grupo de mobs del mapa
		if(command.equalsIgnoreCase("SETMAXGROUP")) {
			infos = msg.split(" ",4);
			byte id = -1;
			try
			{
				id = Byte.parseByte(infos[1]);
			}catch(Exception e){}
            if(id == -1)
			{
				String str = "Valeur invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
				return;
			}
			String mess = "Le nombre de groupe a ete fixe";
			personaje.get_curCarte().setMaxGroup(id);
			boolean ok = SQLManager.SAVE_MAP_DATA(personaje.get_curCarte());
			if(ok)mess += " et a ete sauvegarder a la BDD";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,mess);
		}else
		if(command.equalsIgnoreCase("ADDREPONSEACTION"))
		{
			infos = msg.split(" ",4);
			int id = -30;
			int repID = 0;
			String args = infos[3];
			try
			{
				repID = Integer.parseInt(infos[1]);
				id = Integer.parseInt(infos[2]);
			}catch(Exception e){}
            NPC_reponse rep = World.getNPCreponse(repID);
			if(id == -30 || rep == null)
			{
				String str = "Au moins une des valeur est invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
				return;
			}
			String mess = "L'action a ete ajoute";
			
			rep.addAction(new Action(id,args,""));
			boolean ok = SQLManager.ADD_REPONSEACTION(repID,id,args);
			if(ok)mess += " et ajoute a la BDD";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,mess);
		}else
		if(command.equalsIgnoreCase("SETINITQUESTION"))
		{
			infos = msg.split(" ",4);
			int id = -30;
			int q = 0;
			try
			{
				q = Integer.parseInt(infos[2]);
				id = Integer.parseInt(infos[1]);
			}catch(Exception e){}
            if(id == -30)
			{
				String str = "NpcID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
				return;
			}
			String mess = "L'action a ete ajoute";
			NPC_tmpl npc = World.getNPCTemplate(id);
			
			npc.setInitQuestion(q);
			boolean ok = SQLManager.UPDATE_INITQUESTION(id,q);
			if(ok)mess += " et ajoute a la BDD";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,mess);
		}else
		if(command.equalsIgnoreCase("ADDENDFIGHTACTION"))
		{
			infos = msg.split(" ",4);
			int id = -30;
			int type = 0;
			String args = infos[3];
			String cond = infos[4];
			try
			{
				type = Integer.parseInt(infos[1]);
				id = Integer.parseInt(infos[2]);
				
			}catch(Exception e){}
            if(id == -30)
			{
				String str = "Au moins une des valeur est invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
				return;
			}
			String mess = "L'action a ete ajoute";
			personaje.get_curCarte().addEndFightAction(type, new Action(id,args,cond));
			boolean ok = SQLManager.ADD_ENDFIGHTACTION(personaje.get_curCarte().get_id(),type,id,args,cond);
			if(ok)mess += " et ajoute a la BDD";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,mess);
			return;
		}else
		if(command.equalsIgnoreCase("SPAWNFIX"))
		{
			String groupData = infos[1];

			personaje.get_curCarte().addStaticGroup(personaje.get_curCell().getID(), groupData);
			String str = "Le grouppe a ete fixe";
			//Sauvegarde DB de la modif
			if(SQLManager.SAVE_NEW_FIXGROUP(personaje.get_curCarte().get_id(), personaje.get_curCell().getID(), groupData))
				str += " et a ete sauvegarde dans la BDD";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
			return;
		}else
		if(command.equalsIgnoreCase("ADDNPC"))
		{
			int id = 0;
			try
			{
				id = Integer.parseInt(infos[1]);
			}catch(Exception e){}
            if(id == 0 || World.getNPCTemplate(id) == null)
			{
				String str = "NpcID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
				return;
			}
			NPC npc = personaje.get_curCarte().addNpc(id, personaje.get_curCell().getID(), personaje.get_orientation());
			SocketManager.GAME_SEND_ADD_NPC_TO_MAP(personaje.get_curCarte(), npc);
			String str = "Le PNJ a ete ajoute";
			if(personaje.get_orientation() == 0
					|| personaje.get_orientation() == 2
					|| personaje.get_orientation() == 4
					|| personaje.get_orientation() == 6)
						str += " mais est invisible (orientation diagonale invalide).";
			
			if(SQLManager.ADD_NPC_ON_MAP(personaje.get_curCarte().get_id(), id, personaje.get_curCell().getID(), personaje.get_orientation()))
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
			else
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,"Erreur au moment de sauvegarder la position");
		}else
		if(command.equalsIgnoreCase("DELNPC"))
		{
			int id = 0;
			try
			{
				id = Integer.parseInt(infos[1]);
			}catch(Exception e){}
            NPC npc = personaje.get_curCarte().getNPC(id);
			if(id == 0 || npc == null)
			{
				String str = "Npc GUID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
				return;
			}
			int exC = npc.get_cellID();
			//on l'efface de la map
			SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(personaje.get_curCarte(), id);
			personaje.get_curCarte().removeNpcOrMobGroup(id);
			
			String str = "Le PNJ a ete supprime";
			if(SQLManager.DELETE_NPC_ON_MAP(personaje.get_curCarte().get_id(),exC))
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
			else
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,"Erreur au moment de sauvegarder la position");
		}else
		if(command.equalsIgnoreCase("DELTRIGGER"))
		{
			int cellID = -1;
			try
			{
				cellID = Integer.parseInt(infos[1]);
			}catch(Exception e){}
            if(cellID == -1 || personaje.get_curCarte().getCase(cellID) == null)
			{
				String str = "CellID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
				return;
			}
			
			personaje.get_curCarte().getCase(cellID).clearOnCellAction();
			boolean success = SQLManager.REMOVE_TRIGGER(personaje.get_curCarte().get_id(),cellID);
			String str = "";
			if(success)	str = "Le trigger a ete retire";
			else 		str = "Le trigger n'a pas ete retire";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
		}else
		if(command.equalsIgnoreCase("ADDTRIGGER"))
		{
			int actionID = -1;
			String args = "",cond = "";
			try
			{
				actionID = Integer.parseInt(infos[1]);
				args = infos[2];
				cond = infos[3];
			}catch(Exception e){}
            if(args.equals("") || actionID <= -3)
			{
				String str = "Valeur invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
				return;
			}
			
			personaje.get_curCell().addOnCellStopAction(actionID,args, cond);
			boolean success = SQLManager.SAVE_TRIGGER(personaje.get_curCarte().get_id(), personaje.get_curCell().getID(),actionID,1,args,cond);
			String str = "";
			if(success)	str = "Le trigger a ete ajoute";
			else 		str = "Le trigger n'a pas ete ajoute";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
		}else
		if(command.equalsIgnoreCase("DELNPCITEM"))
		{
			if(cuenta.get_gmLvl() <3)return;
			int npcGUID = 0;
			int itmID = -1;
			try
			{
				npcGUID = Integer.parseInt(infos[1]);
				itmID = Integer.parseInt(infos[2]);
			}catch(Exception e){}
            NPC_tmpl npc =  personaje.get_curCarte().getNPC(npcGUID).get_template();
			if(npcGUID == 0 || itmID == -1 || npc == null)
			{
				String str = "NpcGUID ou itmID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
				return;
			}
			
			
			String str = "";
			if(npc.delItemVendor(itmID))str = "L'objet a ete retire";
			else str = "L'objet n'a pas ete retire";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
		}else
		if(command.equalsIgnoreCase("ADDNPCITEM"))
		{
			if(cuenta.get_gmLvl() <3)return;
			int npcGUID = 0;
			int itmID = -1;
			try
			{
				npcGUID = Integer.parseInt(infos[1]);
				itmID = Integer.parseInt(infos[2]);
			}catch(Exception e){}
            NPC_tmpl npc =  personaje.get_curCarte().getNPC(npcGUID).get_template();
			ObjTemplate item =  World.getObjTemplate(itmID);
			if(npcGUID == 0 || itmID == -1 || npc == null || item == null)
			{
				String str = "NpcGUID ou itmID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
				return;
			}
			
			
			String str = "";
			if(npc.addItemVendor(item))str = "L'objet a ete rajoute";
			else str = "L'objet n'a pas ete rajoute";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
		}else
		if(command.equalsIgnoreCase("ADDMOUNTPARK"))
		{
			int size = -1;
			int owner = -2;
			int price = -1;
			try
			{
				size = Integer.parseInt(infos[1]);
				owner = Integer.parseInt(infos[2]);
				price = Integer.parseInt(infos[3]);
				if(price > 20000000)price = 20000000;
				if(price <0)price = 0;
			}catch(Exception e){}
            if(size == -1 || owner == -2 || price == -1 || personaje.get_curCarte().getMountPark() != null)
			{
				String str = "Infos invalides ou map deja config.";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
				return;
			}
			MountPark MP = new MountPark(owner, personaje.get_curCarte(), personaje.get_curCell().getID(), size, "", -1, price);
			personaje.get_curCarte().setMountPark(MP);
			SQLManager.SAVE_MOUNTPARK(MP);
			String str = "L'enclos a ete config. avec succes";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
		}else 
		if (command.equalsIgnoreCase("SHUTDOWN"))
		{
			int time = 30, OffOn = 0;
			try
			{
				OffOn = Integer.parseInt(infos[1]);
				time = Integer.parseInt(infos[2]);
			}catch(Exception e){}

            if(OffOn == 1 && _TimerStart)// demande de démarer le reboot
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Un shutdown est deja programmer.");
			}else if(OffOn == 1 && !_TimerStart)
			{
				_timer = createTimer(time);
				_timer.start();
				_TimerStart = true;
				String timeMSG = "minutes";
				if(time <= 1)
				{
					timeMSG = "minute";
				}
				SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;"+time+" "+timeMSG);
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Shutdown lance.");
			}else if(OffOn == 0 && _TimerStart)
			{
				_timer.stop();
				_TimerStart = false;
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Shutdown arrete.");
			}else if(OffOn == 0 && !_TimerStart)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Aucun shutdown n'est lance.");
			}
		}else
		{
			this.comandoslvl2(command, infos, msg);
		}
	}
	
	public void commandGmFour(String command, String[] infos, String msg)
	{
		if(cuenta.get_gmLvl() < 4)
		{
			cuenta.getGameThread().kick();
			return;
		}
		
		if(command.equalsIgnoreCase("SETADMIN"))
		{
			int gmLvl = -100;
			try
			{
				gmLvl = Integer.parseInt(infos[1]);
			}catch(Exception e){}
            if(gmLvl == -100)
			{
				String str = "Valeur incorrecte";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
				return;
			}
			Personaje target = personaje;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = World.getPersoByName(infos[2]);
				if(target == null)
				{
					String str = "Le personnage n'a pas ete trouve";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
					return;
				}
			}
			target.get_compte().setGmLvl(gmLvl);
			SQLManager.UPDATE_ACCOUNT_DATA(target.get_compte());
			String str = "Le niveau GM du joueur a ete modifie";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
		}else
		if(command.equalsIgnoreCase("LOCK"))
		{
			byte LockValue = 1;//Accessible
			try
			{
				LockValue = Byte.parseByte(infos[1]);
			}catch(Exception e){}

            if(LockValue > 2) LockValue = 2;
			if(LockValue < 0) LockValue = 0;
			
			char c = 0;
			if(LockValue == 0) c = 'D';
			if(LockValue == 1) c = 'O';
			if(LockValue == 2) c = 'S';
			Main.comServer.sendChangeState(c);
			
			if(LockValue == 1)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Serveur accessible.");
			}else if(LockValue == 0)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Serveur inaccessible.");
			}else if(LockValue == 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Serveur en sauvegarde.");
			}
		}else
		if(command.equalsIgnoreCase("BLOCK"))
		{
			byte GmAccess = 0;
			byte KickPlayer = 0;
			try
			{
				GmAccess = Byte.parseByte(infos[1]);
				KickPlayer = Byte.parseByte(infos[2]);
			}catch(Exception e){}

            Main.comServer.lockGMlevel(GmAccess);
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Serveur bloque au GmLevel : "+GmAccess);
			if(KickPlayer > 0)
			{
				for(Personaje z : World.getOnlinePersos())
				{
					if(z.get_compte().get_gmLvl() < GmAccess)
						z.get_compte().getGameThread().kick();
				}
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Les joueurs de GmLevel inferieur a "+GmAccess+" ont ete kicks.");
			}
		}else
		if(command.equalsIgnoreCase("BANIP"))
		{
			Personaje P = null;
			try
			{
				P = World.getPersoByName(infos[1]);
			}catch(Exception e){}
            if(P == null || !P.isOnline())
			{
				String str = "Le personnage n'a pas ete trouve.";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,str);
				return;
			}
			
			Main.comServer.addBanIP(P.get_compte().get_curIP());
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "L'IP a ete banni.");
			if(P.isOnline())
			{
				P.get_compte().getGameThread().kick();
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir, "Le joueur a ete kick.");
			}	
		}else
		if(command.equalsIgnoreCase("FULLHDV"))
		{
			int numb = 1;
			try
			{
				numb = Integer.parseInt(infos[1]);
			}catch(Exception e){}
            fullHdv(numb);
		}else
		if(command.equalsIgnoreCase("RES"))
		{
			int objID = 1;
			try
			{
				objID = Integer.parseInt(infos[1]);
			}catch(Exception e){}
            PetsEntry p = World.get_PetsEntry(objID);
			if(p == null) return;
			p.resurrection();
			SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(personaje, World.getObjet(objID));
		}if(command.equalsIgnoreCase("SENDME"))
		{
			String str = null;
			try
			{
				str = infos[1];
			}catch(Exception e){}
            SocketManager.send(personaje, str);
		}else
		{
			this.comandoslvl3(command, infos, msg);
		}
	}
	
	private void fullHdv(int ofEachTemplate) {
		SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,"Démarrage du remplissage!");
		
		Objet objet = null;
		byte amount = 0;
		int rAmount = 0;
		int hdv = 0;
		
		int lastSend = 0;
		long time1 = System.currentTimeMillis();//TIME
		for (ObjTemplate curTemp : World.getObjTemplates())//Boucler dans les template
		{
			try {
				if(Main.NOTINHDV.contains(curTemp.getID())) continue;
				
				for (int j = 0; j < ofEachTemplate; j++)//Ajouter plusieur fois le template
				{
					if(curTemp.getType() == 85) break;
					
					objet = curTemp.createNewItem(1, false);
					hdv = getHdv(objet.getTemplate().getType());//Obtient la map (liée a l'HDV) correspondant au template de l'item
					
					if(hdv < 0) break;
					
					Hdv curHdv = World.getHdv(hdv);
					
					amount = (byte) Formulas.getRandomValue(1, 3);
					rAmount = (int)(Math.pow(10,amount)/10);
					objet.setQuantity(rAmount);
					HdvEntry toAdd = new HdvEntry(objet.getGuid(), objet, curHdv.get_mapID(), -1,  calculPrice(objet, rAmount), rAmount);//Créer l'entry
					World.addHdvItem(-1, curHdv.get_mapID(), toAdd);//Ajoute l'entry dans le world
					World.addObjet(objet, false);
				}
			}catch (Exception e) {
				continue;
			}
			
			if((System.currentTimeMillis() - time1)/1000 != lastSend
				&& (System.currentTimeMillis() - time1)/1000 % 3 == 0) {
				lastSend = (int) ((System.currentTimeMillis() - time1)/1000);
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,(System.currentTimeMillis() - time1)/1000 + "sec Template: "+curTemp.getID());
			}
		}
		SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(imprimir,"Remplissage fini en "+(System.currentTimeMillis() - time1) + "ms");
		World.saveAll(null);
		SocketManager.GAME_SEND_MESSAGE_TO_ALL("HDV remplis!", Main.CONFIG_MOTD_COLOR);
	}
	
	private int getHdv(int type) {
		int rand = Formulas.getRandomValue(1, 4);
		int map = -1;
		
		switch(type) {
			case 12:
			case 14: 
			case 26: 
			case 43: 
			case 44: 
			case 45: 
			case 66: 
			case 70: 
			case 71: 
			case 86:
				if(rand == 1)
				{
					map = 4271;
				}else
				if(rand == 2)
				{
					map = 4607;
				}else
				{
					map = 7516;
				}
				return map;
			case 1:
			case 9:
				if(rand == 1)
				{
					map = 4216;
				}else
				if(rand == 2)
				{
					map = 4622;
				}else
				{
					map = 7514;
				}
				return map;
			case 18: 
			case 72: 
			case 77: 
			case 90: 
			case 97: 
			case 113: 
			case 116:
				if(rand == 1)
				{
					map = 8759;
				}else
				{
					map = 8753;
				}
				return map;
			case 63:
			case 64:
			case 69:
				if(rand == 1)
				{
					map = 4287;
				}else
				if(rand == 2)
				{
					map = 4595;
				}else
				if(rand == 3)
				{
					map = 7515;
				}else
				{
					map = 7350;
				}
				return map;
			case 33:
			case 42:
				if(rand == 1)
				{
					map = 2221;
				}else
				if(rand == 2)
				{
					map = 4630;
				}else
				{
					map = 7510;
				}
				return map;
			case 84: 
			case 93: 
			case 112: 
			case 114:
				if(rand == 1)
				{
					map = 4232;
				}else
				if(rand == 2)
				{
					map = 4627;
				}else
				{
					map = 12262;
				}
				return map;
			case 38: 
			case 95: 
			case 96: 
			case 98: 
			case 108:
				if(rand == 1)
				{
					map = 4178;
				}else
				if(rand == 2)
				{
					map = 5112;
				}else
				{
					map = 7289;
				}
				return map;
			case 10:
			case 11:
				if(rand == 1)
				{
					map = 4183;
				}else
				if(rand == 2)
				{
					map = 4562;
				}else
				{
					map = 7602;
				}
				return map;
			case 13: 
			case 25: 
			case 73: 
			case 75: 
			case 76:
				if(rand == 1)
				{
					map = 8760;
				}else
				{
					map = 8754;
				}
				return map;
			case 5: 
			case 6: 
			case 7: 
			case 8: 
			case 19: 
			case 20: 
			case 21: 
			case 22:
				if(rand == 1)
				{
					map = 4098;
				}else
				if(rand == 2)
				{
					map = 5317;
				}else
				{
					map = 7511;
				}
				return map;
			case 39: 
			case 40: 
			case 50: 
			case 51: 
			case 88:
				if(rand == 1)
				{
					map = 4179;
				}else
				if(rand == 2)
				{
					map = 5311;
				}else
				{
					map = 7443;
				}
				return map;
			case 87:
				if(rand == 1)
				{
					map = 6159;
				}else
				{
					map = 6167;
				}
				return map;
			case 34:
			case 52:
			case 60:
				if(rand == 1)
				{
					map = 4299;
				}else
				if(rand == 2)
				{
					map = 4629;
				}else
				{
					map = 7397;
				}
				return map;
			case 41:
			case 49:
			case 62:
				if(rand == 1)
				{
					map = 4247;
				}else
				if(rand == 2)
				{
					map = 4615;
				}else
				if(rand == 3)
				{
					map = 7501;
				}else
				{
					map = 7348;
				}
				return map;
			case 15: 
			case 35: 
			case 36: 
			case 46: 
			case 47: 
			case 48: 
			case 53: 
			case 54: 
			case 55: 
			case 56: 
			case 57: 
			case 58: 
			case 59: 
			case 65: 
			case 68: 
			case 103: 
			case 104: 
			case 105: 
			case 106: 
			case 107: 
			case 109: 
			case 110: 
			case 111:
				if(rand == 1)
				{
					map = 4262;
				}else
				if(rand == 2)
				{
					map = 4646;
				}else
				{
					map = 7413;
				}
				return map;
			case 78:
				if(rand == 1)
				{
					map = 8757;
				}else
				{
					map = 8756;
				}
				return map;
			case 2:
			case 3:
			case 4:
				if(rand == 1)
				{
					map = 4174;
				}else
				if(rand == 2) {
					map = 4618;
				}else {
					map = 7512;
				}
				return map;
			case 16:
			case 17:
			case 81:
				if(rand == 1) {
					map = 4172;
				}else
				if(rand == 2) {
					map = 4588;
				}else {
					map = 7513;
				}
				return map;
			case 83:
				if(rand == 1) {
					map = 10129;
				}else {
					map = 8482;
				}
				return map;
			case 82:
				return 8039;
			default:
				return -1;
		}
	}
	
	private int calculPrice(Objet obj, int amount) {
		int stats = 0;
		
		for(int curStat : obj.getStats().getMap().values()) {
			stats += curStat;
		}
		if(stats > 0)
			return (int) (((Math.cbrt(stats) * Math.pow(obj.getTemplate().getLevel(), 2)) * 10 + Formulas.getRandomValue(1, obj.getTemplate().getLevel()*100)) * amount);
		else
			return (int) ((Math.pow(obj.getTemplate().getLevel(),2) * 10 + Formulas.getRandomValue(1, obj.getTemplate().getLevel()*100))*amount);
	}
}