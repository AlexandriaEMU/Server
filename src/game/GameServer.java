package game;

import java.io.IOException;
import java.net.ServerSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import objects.Mapa;
import objects.Compte;
import objects.Personaje;

import common.*;

public class GameServer implements Runnable{

	private ServerSocket _SS;
	private Thread _t;
	private ArrayList<GameThread> _clients = new ArrayList<>();
	private ArrayList<Compte> _waitings = new ArrayList<>();

    private int _saveTimer = 0, _loadActionTimer = 0, _reloadMobTimer = 0, _movermonstruos = 0;
	
	private long _startTime;
	private int _maxPlayer = 0;

	public GameServer(String Ip)
	{
		try {

            Timer _actionTimer = new Timer();
			_actionTimer.schedule(new TimerTask()
			{
				public void run()
				{
					_saveTimer++;
					_loadActionTimer++;
					_reloadMobTimer++;

					//Guardado automatico del server
					if(_saveTimer == (Main.CONFIG_SAVE_TIME/60000)) {
						if(!Main.isSaving) {
							Thread t = new Thread(new SaveThread());
							t.start();
						}
						_saveTimer = 0;
					}

					//v0.00.1 - Agregamos movimiento de monstruos
					//Movimiento de monstruos, recaudadores y npc
					if(_movermonstruos == (Main.CONFIG_MOVER_MONSTRUOS/30000)) {
						for (Mapa mapa : World.getCartes()) {
							mapa.movimientodemonstruosenmapa();
						}
						GameServer.addToLog("Se movieron los monstruos");
						_movermonstruos = 0;
					}

					//Acciones en tiempo real
					if(_loadActionTimer == (Main.CONFIG_LOAD_DELAY/60000)) {
						SQLManager.LOAD_ACTION();
						GameServer.addToLog("Las acciones en tiempo real se han realizado");
						_loadActionTimer = 0;
					}

					//Actualizacion de moobs
					if(_reloadMobTimer == (Main.CONFIG_RELOAD_MOB_DELAY/60000)) {
						World.RefreshAllMob();
						GameServer.addToLog("La actualizacion de mobs ha finalizado");
						_reloadMobTimer = 0;
					}

					for(Personaje perso : World.getOnlinePersos()) {
						if (perso.getLastPacketTime() + Main.CONFIG_MAX_IDLE_TIME < System.currentTimeMillis()) {
							
							if(perso != null && perso.get_compte().getGameThread() != null && perso.isOnline()) {
								GameServer.addToLog("Kick por inactividad del jugador: "+perso.get_name());
								SocketManager.MESSAGE_BOX(perso.get_compte().getGameThread().get_out(),"01|"); 
								perso.get_compte().getGameThread().kick();
							}
						}
					}
					World.MoveMobsOnMaps();
				}
			}, 60000,60000);

			_SS = new ServerSocket(Main.CONFIG_GAME_PORT);
			if(Main.CONFIG_USE_IP)
				Main.GAMESERVER_IP = CryptManager.CryptIP(Ip)+CryptManager.CryptPort(Main.CONFIG_GAME_PORT);
			_startTime = System.currentTimeMillis();
			_t = new Thread(this);
			_t.start();
		} catch (IOException e) {
			addToLog("IOException: "+e.getMessage());
			Main.closeServers();
		}
	}
	
	public static class SaveThread implements Runnable {
		public void run() {
			if(Main.isSaving == false) {
				SocketManager.GAME_SEND_Im_PACKET_TO_ALL("1164");
				World.saveAll(null);
				SocketManager.GAME_SEND_Im_PACKET_TO_ALL("1165");
			}
		}
	}
	
	public ArrayList<GameThread> getClients() {
		return _clients;
	}

	public long getStartTime()
	{
		return _startTime;
	}
	
	public int getMaxPlayer()
	{
		return _maxPlayer;
	}
	
	public int getPlayerNumber()
	{
		return _clients.size();
	}
	public void run()
	{	
		while(Main.isRunning)//bloque sur _SS.accept()
		{
			try
			{
				_clients.add(new GameThread(_SS.accept()));
				if(_clients.size() > _maxPlayer)_maxPlayer = _clients.size();
			}catch(IOException e)
			{
				addToLog("IOException: "+e.getMessage());
				try
				{
					if(!_SS.isClosed())_SS.close();
					Main.closeServers();
				}
				catch(IOException e1){}
			}
		}
	}
	
	public void kickAll()
	{
		try {
			_SS.close();
		} catch (IOException e) {}
		ArrayList<GameThread> c = new ArrayList<>();
		c.addAll(_clients);
		for(GameThread GT : c)
		{
			try
			{
				GT.kick();
			}catch(Exception e){}
		}
	}
	
	public synchronized static void addToLog(String str)
	{
		System.out.println(str);
		if(Main.canLog)
		{
			try {
				String date = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+Calendar.getInstance().get(+Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND);
				Main.Log_Game.write(date+": "+str);
				Main.Log_Game.newLine();
				Main.Log_Game.flush();
			} catch (IOException e) {e.printStackTrace();}//ne devrait pas avoir lieu
		}
	}
	
	public synchronized static void addToSockLog(String str)
	{
		if(Main.CONFIG_DEBUG)System.out.println(str);
		if(Main.canLog)
		{
			try {
				String date = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+Calendar.getInstance().get(+Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND);
				Main.Log_GameSock.write(date+": "+str);
				Main.Log_GameSock.newLine();
				Main.Log_GameSock.flush();
			} catch (IOException e) {}//ne devrait pas avoir lieu
		}
	}
	
	public void delClient(GameThread gameThread)
	{
		_clients.remove(gameThread);
		if(_clients.size() > _maxPlayer)_maxPlayer = _clients.size();
	}
	
	public synchronized Compte getWaitingCompte(int guid)
	{
		for (int i = 0; i < _waitings.size(); i++)
		{
			if(_waitings.get(i).get_GUID() == guid)
				return _waitings.get(i);
		}
		return null;
	}
	
	public synchronized void delWaitingCompte(Compte _compte)
	{
		_waitings.remove(_compte);
	}
	
	public synchronized void addWaitingCompte(Compte _compte)
	{
		_waitings.add(_compte);
	}
	
	public static String getServerTime()
	{
		Date actDate = new Date();
		return "BT"+(actDate.getTime()+3600000);
	}
	public static String getServerDate()
	{
		Date actDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("dd");
		StringBuilder jour = new StringBuilder(Integer.parseInt(dateFormat.format(actDate)) + "");
		while(jour.length() <2)
		{
			jour.insert(0, "0");
		}
		dateFormat = new SimpleDateFormat("MM");
		StringBuilder mois = new StringBuilder((Integer.parseInt(dateFormat.format(actDate)) - 1) + "");
		while(mois.length() <2)
		{
			mois.insert(0, "0");
		}
		dateFormat = new SimpleDateFormat("yyyy");
		String annee = (Integer.parseInt(dateFormat.format(actDate))-1370)+"";
		return "BD"+annee+"|"+mois+"|"+jour;
	}

	public Thread getThread()
	{
		return _t;
	}
}
