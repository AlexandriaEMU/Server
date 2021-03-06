package common;

import game.GameServer;
import objects.*;
import objects.Compte.EnemyList;
import objects.Compte.FriendList;
import objects.NPC_tmpl.NPC_question;
import objects.NPC_tmpl.NPC_reponse;
import objects.Objet.ObjTemplate;
import objects.Others.Bank;
import objects.Personaje.Stats;

import java.io.PrintWriter;
import java.util.*;
import java.util.Map.Entry;

public class World {

	private static final Map<Integer, Compte> Comptes = new TreeMap<>();
	private static final Map<String, Integer> ComptebyName = new TreeMap<>();
	private static final Map<Integer, Personaje> Persos = new TreeMap<>();
	private static final Map<String, Integer> PersosbyName = new TreeMap<>();
	private static final Map<Short, Mapa> Cartes = new TreeMap<>();
	private static final Map<Integer, Objet> Objets = new TreeMap<>();
	private static final Map<Integer, ExpLevel> ExpLevels = new TreeMap<>();
	private static final Map<Integer, Sort> Sorts = new TreeMap<>();
	private static final Map<Integer, ObjTemplate> ObjTemplates = new TreeMap<>();
	private static final Map<Integer, Monstre> MobTemplates = new TreeMap<>();
	private static final Map<Integer, NPC_tmpl> NPCTemplates = new TreeMap<>();
	private static final Map<Integer, NPC_question> NPCQuestions = new TreeMap<>();
	private static final Map<Integer, NPC_reponse> NPCReponses = new TreeMap<>();
	private static final Map<Integer, IOTemplate> IOTemplate = new TreeMap<>();
	private static final Map<Integer, Dragodinde> Dragodindes = new TreeMap<>();
	private static final Map<Integer, SuperArea> SuperAreas = new TreeMap<>();
	private static final Map<Integer, Area> Areas = new TreeMap<>();
	private static final Map<Integer, SubArea> SubAreas = new TreeMap<>();
	private static final Map<Integer, Metier> Jobs = new TreeMap<>();
	private static final Map<Integer, ArrayList<Couple<Integer, Integer>>> Crafts = new TreeMap<>();
	private static final Map<Integer, ItemSet> ItemSets = new TreeMap<>();
	private static final Map<Integer, Guild> Guildes = new TreeMap<>();
	private static final Map<Integer, Hdv> Hdvs = new TreeMap<>();//<MapID,Hdv>
	private static final Map<Integer, Map<Integer, ArrayList<HdvEntry>>> HdvsItems = new HashMap<>();//Contient tout les items en ventes des comptes dans le format<compteID,<hdvID,items<>>>
	private static final Map<Integer, Map<Integer, Map<Integer, Map<Integer, Objet>>>> HdvsTypes = new HashMap<>();//Contient tout les items en ventes des comptes dans le format<TypeID,<hdvID,<TemplateID,<ItemID,items<>>>>>
	private static final Map<Integer, Map<Integer, HdvEntry>> HdvsTemplates = new HashMap<>();//Contient tout les items en ventes des comptes dans le format<TemplateID,<ItemID, HdvEntry>>>>
	private static final Map<Integer, Personaje> Married = new TreeMap<>();
	private static final Map<Integer, Animations> Animations = new TreeMap<>();
	private static final Map<Short, Mapa.MountPark> MountPark = new TreeMap<>();
	private static final Map<Integer, Trunk> Trunks = new TreeMap<>();
	private static final Map<Integer, Percepteur> Percepteurs = new TreeMap<>();
	private static final Map<Integer, House> Houses = new TreeMap<>();
	private static final Map<Short, Collection<Integer>> Seller = new TreeMap<>();
	private static final Map<Integer, Bank> Banks = new TreeMap<>();
	private static final Map<Integer, FriendList> Friends = new TreeMap<>();
	private static final Map<Integer, EnemyList> Enemys = new TreeMap<>();
	private static final Map<Integer, Pets> Pets = new TreeMap<>();
	private static final Map<Integer, PetsEntry> PetsEntry = new TreeMap<>();
	private static final StringBuilder Challenges = new StringBuilder();
	private static final Map<Integer, Collection<Integer>> CraftBook = new TreeMap<>();
	private static final Map<Integer, Gift> Gifts = new HashMap<>();

	private static int saveTry = 1;

	private static byte _GmAccess = 0;

	private static int nextObjetID; //Contient le derniere ID utilis? pour cr?e un Objet

	public static void delCarte(Mapa map) {
		Cartes.remove(map.get_id());
	}

	public static void saveAll(Personaje saver) {
		PrintWriter _out = null;
		if (saver != null)
			_out = saver.get_compte().getGameThread().get_out();

		Main.comServer.sendChangeState('S');

		try {
			GameServer.addToLog("Lanzando el guardado del server...");
			Main.isSaving = true;
			SQLManager.commitTransacts();
			SQLManager.TIMER(false);//Arr?te le timer d'enregistrement SQL


			GameServer.addToLog("Guardando los personajes.");
			for (Personaje perso : Persos.values()) {
				if (!perso.isOnline()) continue;
				SQLManager.SAVE_PERSONNAGE(perso, true);//sauvegarde des persos et de leurs items
			}


			GameServer.addToLog("Guardando los bancos...");
			for (Bank bk : Banks.values()) {
				SQLManager.UPDATE_BANK(bk);
			}


			GameServer.addToLog("Guardando los gremios...");
			for (Guild guilde : Guildes.values()) {
				SQLManager.UPDATE_GUILD(guilde);
			}


			GameServer.addToLog("Guardando los recaudadores...");
			for (Percepteur perco : Percepteurs.values()) {
				if (perco.get_inFight() > 0) continue;
				SQLManager.UPDATE_PERCO(perco);
			}


			GameServer.addToLog("Guardando las casas...");
			for (House house : Houses.values()) {
				if (house.get_owner_id() > 0) {
					SQLManager.UPDATE_HOUSE(house);
				}
			}


			GameServer.addToLog("Guardando los cofres...");
			for (Trunk t : Trunks.values()) {
				if (t.get_owner_id() > 0) {
					SQLManager.UPDATE_TRUNK(t);
				}
			}


			GameServer.addToLog("Guardando los cercados...");
			for (Mapa.MountPark mp : MountPark.values()) {
				if (mp.get_owner() > 0 || mp.get_owner() == -1) {
					SQLManager.UPDATE_MOUNTPARK(mp);
				}
			}


			GameServer.addToLog("Guardando las mascotas...");
			for (PetsEntry pets : PetsEntry.values()) {
				SQLManager.UPDATE_PETS_DATA(pets);
			}


			GameServer.addToLog("Guardando los mercadillos...");
			ArrayList<HdvEntry> toSave = new ArrayList<>();
			for (Map<Integer, HdvEntry> curEntry : HdvsTemplates.values()) {
				toSave.addAll(curEntry.values());
			}
			SQLManager.SAVE_HDVS_ITEMS(toSave);


			GameServer.addToLog("GUARDADO COMPLETADO!");

			Main.comServer.sendChangeState('O');

		} catch (ConcurrentModificationException e) {
			if (saveTry < 10) {
				GameServer.addToLog("Nouvelle tentative de sauvegarde");
				if (saver != null && _out != null)
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Erreur. Nouvelle tentative de sauvegarde");
				saveTry++;
				saveAll(saver);
			} else {
				Main.comServer.sendChangeState('O');
				String mess = "Echec de la sauvegarde apres " + saveTry + " tentatives";
				if (saver != null && _out != null)
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
				GameServer.addToLog(mess);
			}

		} catch (Exception e) {
			GameServer.addToLog("Erreur lors de la sauvegarde : " + e.getMessage());
			e.printStackTrace();
		} finally {
			SQLManager.commitTransacts();
			SQLManager.TIMER(true); //Red?marre le timer d'enregistrement SQL
			Main.isSaving = false;
			saveTry = 1;
		}
	}

	public static String get_HdvsTemplate(int TypeID, int HdvID) {
		StringBuilder str = new StringBuilder();
		if (HdvsTypes.get(TypeID) == null) return "";
		if (HdvsTypes.get(TypeID).get(HdvID) == null) return "";
		for (Entry<Integer, Map<Integer, Objet>> i : HdvsTypes.get(TypeID).get(HdvID).entrySet()) {
			str.append(i.getKey()).append(";");
		}
		return (str.toString().length() > 0 ? str.substring(0, str.length() - 1) : "");
	}

	public static ArrayList<String> getRandomChallenge(int nombreChal, String challenges) {
		String MovingChals = ";1;2;8;36;37;39;40;";// Challenges de d???placements incompatibles
		boolean hasMovingChal = false;
		String TargetChals = ";3;4;10;25;31;32;34;35;38;42;";// ceux qui ciblent
		boolean hasTargetChal = false;
		String SpellChals = ";5;6;9;11;19;20;24;41;";// ceux qui obligent ??? caster sp???cialement
		boolean hasSpellChal = false;
		String KillerChals = ";28;29;30;44;45;46;48;";// ceux qui disent qui doit tuer
		boolean hasKillerChal = false;
		String HealChals = ";18;43;";// ceux qui emp???chent de soigner
		boolean hasHealChal = false;

		int compteur = 0, i = 0;
		ArrayList<String> toReturn = new ArrayList<>();
		String chal = "";
		while (compteur < 100 && toReturn.size() < nombreChal) {
			compteur++;
			i = Formulas.getRandomValue(1, challenges.split(";").length);
			chal = challenges.split(";")[i - 1];// challenge au hasard dans la liste

			if (!toReturn.contains(chal))// si le challenge n'y etait pas encore
			{
				if (MovingChals.contains(";" + chal.split(",")[0] + ";"))// s'il appartient a une liste
					if (!hasMovingChal)// et qu'aucun de la liste n'a ete choisi deja
					{
						hasMovingChal = true;
						toReturn.add(chal);
						continue;
					} else continue;
				if (TargetChals.contains(";" + chal.split(",")[0] + ";"))
					if (!hasTargetChal) {
						hasTargetChal = true;
						toReturn.add(chal);
						continue;
					} else continue;
				if (SpellChals.contains(";" + chal.split(",")[0] + ";"))
					if (!hasSpellChal) {
						hasSpellChal = true;
						toReturn.add(chal);
						continue;
					} else continue;
				if (KillerChals.contains(";" + chal.split(",")[0] + ";"))
					if (!hasKillerChal) {
						hasKillerChal = true;
						toReturn.add(chal);
						continue;
					} else continue;
				if (HealChals.contains(";" + chal.split(",")[0] + ";"))
					if (!hasHealChal) {
						hasHealChal = true;
						toReturn.add(chal);
						continue;
					} else continue;
				toReturn.add(chal);
			}
			compteur++;
		}
		return toReturn;
	}

	public static class Drop {
		private final int _itemID;
		private final int _prosp;
		private final float _taux;
		private int _max;

		public Drop(int itm, int p, float t, int m) {
			_itemID = itm;
			_prosp = p;
			_taux = t;
			_max = m;
		}

		public void setMax(int m)
		{
			_max = m;
		}
		public int get_itemID() {
			return _itemID;
		}

		public int getMinProsp() {
			return _prosp;
		}

		public float get_taux() {
			return _taux;
		}

		public int get_max() {
			return _max;
		}
	}

	public static class Couple<L, R> {
		public L first;
		public R second;

		public Couple(L s, R i) {
			this.first = s;
			this.second = i;
		}
	}

	public static class ItemSet {
		private final int _id;
		private final ArrayList<ObjTemplate> _itemTemplates = new ArrayList<>();
		private final ArrayList<Stats> _bonuses = new ArrayList<>();

		public ItemSet(int id, String items, String bonuses) {
			_id = id;
			//parse items String
			for (String str : items.split(",")) {
				try {
					ObjTemplate t = World.getObjTemplate(Integer.parseInt(str.trim()));
					if(t == null)continue;
					_itemTemplates.add(t);
				}catch(Exception e){}
            }

			//on ajoute un bonus vide pour 1 item
			_bonuses.add(new Stats());
			//parse bonuses String
			for(String str : bonuses.split(";"))
			{
				Stats S = new Stats();
				//s?paration des bonus pour un m?me nombre d'item
				for(String str2 : str.split(","))
				{
					try
					{
						String[] infos = str2.split(":");
						int stat = Integer.parseInt(infos[0]);
						int value = Integer.parseInt(infos[1]);
						//on ajoute a la stat
						S.addOneStat(stat, value);
					}catch(Exception e){}
                }
				//on ajoute la stat a la liste des bonus
				_bonuses.add(S);
			}
		}

		public int getId()
		{
			return _id;
		}

		public Stats getBonusStatByItemNumb(int numb)
		{
			if(numb>_bonuses.size())return new Stats();
			return _bonuses.get(numb-1);
		}

		public ArrayList<ObjTemplate> getItemTemplates()
		{
			return _itemTemplates;
		}
	}

	public static class SuperArea {
		private final int _id;
		private final ArrayList<Area> _areas = new ArrayList<>();

		public SuperArea(int a_id) {
			_id = a_id;
		}

		public void addArea(Area A) {
			_areas.add(A);
		}

		public int get_id()
		{
			return _id;
		}
	}

	public static class ExpLevel {
		public long perso;
		public int metier;
		public int dinde;
		public int pvp;
		public long guilde;
		
		public ExpLevel(long c, int m, int d, int p) {
			perso = c;
			metier = m;
			dinde = d;
			pvp = p;
			guilde = perso*10;
		}
		
	}
	
	public static void createWorld() {
		System.out.println("====>Cargando estaticos<====");
		System.out.print("Cargando los niveles y las experiencias: ");
		SQLManager.LOAD_EXP();
		System.out.println(ExpLevels.size()+" niveles cargados.");
		System.out.print("Cargando hechizos: ");
		SQLManager.LOAD_SORTS();
		System.out.println(Sorts.size()+" hechizos cargados.");
		System.out.print("Cargando dise?os de monstruos: ");
		SQLManager.LOAD_MOB_TEMPLATE();
		System.out.println(MobTemplates.size()+" dise?os cargados.");
		System.out.print("Cargando dise?os de objetos: ");
		SQLManager.LOAD_OBJ_TEMPLATE();
		System.out.println(ObjTemplates.size()+" dise?os cargados.");
		System.out.print("Cargando los dise?os de los NPC: ");
		SQLManager.LOAD_NPC_TEMPLATE();
		System.out.println(NPCTemplates.size()+" dise?os cargados.");
		System.out.print("Cargando las preguntas de los NPC: ");
		SQLManager.LOAD_NPC_QUESTIONS();
		System.out.println(NPCQuestions.size()+" preguntas cargadas.");
		System.out.print("Cargando las respuestas de los NPC: ");
		SQLManager.LOAD_NPC_ANSWERS();
		System.out.println(NPCReponses.size()+" respuestas cargadas.");
		System.out.print("Cargando las zonas: ");
		SQLManager.LOAD_AREA();
		System.out.println(Areas.size()+" zonas cargadas.");
		System.out.print("Cargando las sub zonas: ");
		SQLManager.LOAD_SUBAREA();
		System.out.println(SubAreas.size()+" sub zonas cargadas.");
		System.out.print("Cargando los dise?os de los objetos interactivos: ");
		SQLManager.LOAD_IOTEMPLATE();
		System.out.println(IOTemplate.size()+" dise?os cargados.");
		System.out.print("Cargando las recetas: ");
		SQLManager.LOAD_CRAFTS();
		System.out.println(Crafts.size()+" recetas cargadas.");
		System.out.print("Cargando los oficios: ");
		SQLManager.LOAD_JOBS();
		System.out.println(Jobs.size()+" oficios cargados.");
		System.out.print("Cargando set completos: ");
		SQLManager.LOAD_ITEMSETS();
		System.out.println(ItemSets.size()+" set cargados.");
		System.out.print("Cargando mapas: ");
		SQLManager.LOAD_MAPS();
		System.out.println(Cartes.size()+" mapas cargados.");
		System.out.print("Cargando las celdas: ");
		int nbr = SQLManager.LOAD_TRIGGERS();
		System.out.println(nbr+" celdas cargadas.");
		System.out.print("Cargando las acciones fin de combate: ");
		nbr = SQLManager.LOAD_ENDFIGHT_ACTIONS();
		System.out.println(nbr+" acciones cargadas.");
		System.out.print("Cargando los NPC: ");
		nbr = SQLManager.LOAD_NPCS();
		System.out.println(nbr+" NPC cargados.");
		System.out.print("Cargando las acciones de los objetos: ");
		nbr = SQLManager.LOAD_ITEM_ACTIONS();
		System.out.println(nbr+" acciones cargadas.");
		System.out.print("Cargando los drops: ");
		nbr = SQLManager.LOAD_DROPS();
		System.out.println(nbr+" drops cargados.");
		System.out.print("Cargando las animaciones: ");
		SQLManager.LOAD_ANIMATIONS();
		System.out.println(Animations.size() + " animaciones cargadas.");
		System.out.print("Cargando los zaaps: ");
		nbr = SQLManager.LOAD_ZAAPS();
		System.out.println(nbr+" zaaps cargados.");
		System.out.print("Cargando los zappis: ");
		nbr = SQLManager.LOAD_ZAAPIS();
		System.out.println(nbr+" zaapis cargados.");
		System.out.print("Cargando los mercadillos: ");
		nbr = SQLManager.LOAD_HDVS();
		System.out.println(nbr+" mercadillos cargados.");
		System.out.print("Cargando las mascotas: ");
		nbr = SQLManager.LOAD_PETS();
		System.out.println(nbr+" mascotas cargadas.");
		System.out.print("Cargando los retos: ");
		nbr = SQLManager.LOAD_CHALLENGES();
		System.out.println(nbr+" retos cargados");
		System.out.println("Cargando los regalos: ");
		nbr = SQLManager.LOAD_GIFTS();
		System.out.println(nbr+" regalos cargados");
		
		System.out.println("\n====>Cargando dinamicos<====");
		System.out.print("Cargando los objetos: ");
		SQLManager.LOAD_ITEMS();
		System.out.println("Cargados.");
		System.out.print("Cargando los personajes: ");
		SQLManager.LOAD_PERSOS();
		System.out.println(Persos.size()+" personajes cargados.");
	    System.out.print("Cargando los gremios: ");
		SQLManager.LOAD_GUILDS();
		System.out.println(Guildes.size()+" gremios cargados.");
		System.out.print("Cargando los dragopavos: ");
		SQLManager.LOAD_MOUNTS();
		System.out.println(Dragodindes.size()+" dragopavos cargados.");
		System.out.print("Cargando los miembros de gremios: ");
		SQLManager.LOAD_GUILD_MEMBERS();
		System.out.println("Cargados.");
		System.out.print("Cargando los cercados: ");
		nbr = SQLManager.LOAD_MOUNTPARKS();
		System.out.println(nbr+" cercados cargados.");
		System.out.print("Cargando los recaudadores: ");
		nbr = SQLManager.LOAD_PERCEPTEURS();
		System.out.println(nbr+" recaudadores cargados.");
		System.out.print("Cargando las casas: ");
		nbr = SQLManager.LOAD_HOUSES();
		System.out.println(nbr+" casas cargadas.");
		System.out.print("Cargando los cofres: ");
		nbr = SQLManager.LOAD_TRUNK();
		System.out.println(nbr+" cofres cargados.");
		System.out.print("Cargando las cuentas: ");
		SQLManager.LOAD_ACCOUNTS_DATA();
		System.out.println(nbr+" cuentas cargadas.");
		System.out.print("Cargando los objetos de los mercadillos: ");
		nbr = SQLManager.LOAD_HDVS_ITEMS();
		System.out.println(nbr+" objetos cargados.");
		System.out.print("Cargando las mascotas: ");
		nbr = SQLManager.LOAD_PETS_ENTRY();
		System.out.println(nbr+" mascotas cargadas.");
		
		nextObjetID = SQLManager.getNextObjetID();
	}
	
	public static Area getArea(int areaID)
	{
		return Areas.get(areaID);
	}

	public static SuperArea getSuperArea(int areaID)
	{
		return SuperAreas.get(areaID);
	}
	
	public static SubArea getSubArea(int areaID)
	{
		return SubAreas.get(areaID);
	}
	
	public static void addArea(Area area)
	{
		Areas.put(area.get_id(), area);
	}
	
	public static void addSuperArea(SuperArea SA)
	{
		SuperAreas.put(SA.get_id(), SA);
	}
	
	public static void addSubArea(SubArea SA)
	{
		SubAreas.put(SA.get_id(), SA);
	}
	
	public static void addNPCreponse(NPC_reponse rep)
	{
		NPCReponses.put(rep.get_id(), rep);
	}
	
	public static NPC_reponse getNPCreponse(int guid)
	{
		return NPCReponses.get(guid);
	}
	
	public static int getExpLevelSize()
	{
		return ExpLevels.size();
	}
	
	public static void addExpLevel(int lvl,ExpLevel exp)
	{
		ExpLevels.put(lvl, exp);
	}
	
	public static Compte getCompte(int guid)
	{
		return Comptes.get(guid);
	}
	
	public static void addNPCQuestion(NPC_question quest)
	{
		NPCQuestions.put(quest.get_id(), quest);
	}
	
	public static NPC_question getNPCQuestion(int guid)
	{
		return NPCQuestions.get(guid);
	}
	public static NPC_tmpl getNPCTemplate(int guid)
	{
		return NPCTemplates.get(guid);
	}
	
	public static void addNpcTemplate(NPC_tmpl temp)
	{
		NPCTemplates.put(temp.get_id(), temp);
	}
	
	public static Mapa getCarte(short id) {
		return Cartes.get(id);
	}

	public static Collection<Mapa> getCartes() {
		return Cartes.values();
	}

	public static void addCarte(Mapa map) {
		if (!Cartes.containsKey(map.get_id()))
			Cartes.put(map.get_id(), map);
	}

	public static class Area {
		private final int _id;
		private SuperArea _superArea;
		private final String _name;
		private final ArrayList<SubArea> _subAreas = new ArrayList<>();

		public Area(int id, int superArea, String name) {
			_id = id;
			_name = name;
			_superArea = World.getSuperArea(superArea);
			//Si le continent n'est pas encore cr?er, on le cr?er et on l'ajoute au monde
			if (_superArea == null) {
				_superArea = new SuperArea(superArea);
				World.addSuperArea(_superArea);
			}
		}

		public String get_name() {
			return _name;
		}

		public int get_id() {
			return _id;
		}

		public SuperArea get_superArea() {
			return _superArea;
		}

		public void addSubArea(SubArea sa) {
			_subAreas.add(sa);
		}

		public ArrayList<Mapa> getMaps() {
			ArrayList<Mapa> maps = new ArrayList<>();
			for (SubArea SA : _subAreas) maps.addAll(SA.getMaps());
			return maps;
		}
	}

	public static Compte getCompteByName(String name) {
		return (ComptebyName.get(name.toLowerCase()) != null ? Comptes.get(ComptebyName.get(name.toLowerCase())) : null);
	}

	public static Personaje getPersonnage(int guid) {
		return Persos.get(guid);
	}
	
	public static void addAccount(Compte compte)
	{
		Comptes.put(compte.get_GUID(), compte);
		ComptebyName.put(compte.get_name().toLowerCase(), compte.get_GUID());
	}
	
	public static void removeAccount(int guid, String name)
	{
		if(Comptes.containsKey(guid))
		{
			Comptes.remove(guid);
			ComptebyName.remove(name);
		}
	}
	
	public static void addPersonnage(Personaje perso)
	{
		Persos.put(perso.get_GUID(), perso);
		PersosbyName.put(perso.get_name().toLowerCase(), perso.get_GUID());
	}
	
	public static Personaje getPersoByName(String name)
	{
		return (PersosbyName.get(name.toLowerCase())!=null?Persos.get(PersosbyName.get(name.toLowerCase())):null);
	}
	
	public static void deletePerso(Personaje perso)
	{
		if(perso.get_guild() != null)
		{
			if(perso.get_guild().getMembers().size() <= 1)//Il est tout seul dans la guilde : Supression
			{
				World.removeGuild(perso.get_guild().get_id());
			}else if(perso.getGuildMember().getRank() == 1)//On passe les pouvoir a celui qui a le plus de droits si il est meneur
			{
				int curMaxRight = 0;
				Personaje Meneur = null;
				for(Personaje newMeneur : perso.get_guild().getMembers())
				{
					if(newMeneur == perso) continue;
					if(newMeneur.getGuildMember().getRights() < curMaxRight)
					{
						Meneur = newMeneur;
					}
				}
				perso.get_guild().removeMember(perso);
				Meneur.getGuildMember().setRank(1);
			}else//Supression simple
			{
				perso.get_guild().removeMember(perso);
			}
		}
		perso.remove();//Supression BDD Perso, items, monture.
		World.unloadPerso(perso.get_GUID());//UnLoad du des item
		World.Persos.remove(perso.get_GUID());
	}

	public static String getSousZoneStateString()
	{
		String data = "";
		/* TODO: Sous Zone Alignement */
		return data;
	}
	
	public static long getPersoXpMin(int _lvl)
	{
		if(_lvl > getExpLevelSize()) 	_lvl = getExpLevelSize();
		if(_lvl < 1) 	_lvl = 1;
		return ExpLevels.get(_lvl).perso;
	}
	
	public static long getPersoXpMax(int _lvl)
	{
		if(_lvl >= getExpLevelSize()) 	_lvl = (getExpLevelSize()-1);
		if(_lvl <= 1)	 	_lvl = 1;
		return ExpLevels.get(_lvl+1).perso;
	}
	
	public static void addSort(Sort sort)
	{
		Sorts.put(sort.getSpellID(), sort);
	}

	public static void addObjTemplate(ObjTemplate obj)
	{
		ObjTemplates.put(obj.getID(), obj);
	}
	
	public static Sort getSort(int id)
	{
		return Sorts.get(id);
	}

	public static ObjTemplate getObjTemplate(int id)
	{
		return ObjTemplates.get(id);
	}
	
	public static Collection<ObjTemplate> getObjTemplates()
	{
		return ObjTemplates.values();
	}
	
	public synchronized static int getNewItemGuid()
	{
		nextObjetID++;
		return nextObjetID;
	}

	public static void addMobTemplate(int id,Monstre mob)
	{
		MobTemplates.put(id, mob);
	}

	public static Monstre getMonstre(int id)
	{
		return MobTemplates.get(id);
	}

	public static List<Personaje> getOnlinePersos()
	{
		List<Personaje> online = new ArrayList<>();
		for(Entry<Integer, Personaje> perso : Persos.entrySet())
		{
			if(perso.getValue().isOnline() && perso.getValue().get_compte().getGameThread() != null)
			{
				if(perso.getValue().get_compte().getGameThread().get_out() != null)
				{
					online.add(perso.getValue());
				}
			}
		}
		return online;
	}

	public static void addObjet(Objet item, boolean saveSQL)
	{
		Objets.put(item.getGuid(), item);
		if(saveSQL)
			SQLManager.SAVE_NEW_ITEM(item);
	}
	public static Objet getObjet(int guid)
	{
		return Objets.get(guid);
	}

	public static void removeItem(int guid)
	{
		if(Objets.get(guid).getTemplate().getType() == 18)
		{
			SQLManager.REMOVE_PETS_DATA(guid);
			PetsEntry.remove(guid);
		}
		Objets.remove(guid);
		SQLManager.DELETE_ITEM(guid);
	}

	public static void addIOTemplate(IOTemplate IOT)
	{
		IOTemplate.put(IOT.getId(), IOT);
	}
	
	public static Dragodinde getDragoByID(int id)
	{
		return Dragodindes.get(id);
	}

	public static void addDragodinde(Dragodinde DD) {
		Dragodindes.put(DD.get_id(), DD);
	}

	public static void removeDragodinde(int DID) {
		Dragodindes.remove(DID);
	}

	public static class SubArea {
		private final int _id;
		private final Area _area;
		private final int _alignement;
		private final String _name;
		private final boolean _subscribeNeed;
		private final ArrayList<Mapa> _maps = new ArrayList<>();

		public SubArea(int id, int areaID, int alignement, String name, boolean subscribe) {
			_id = id;
			_name = name;
			_area = World.getArea(areaID);
			_alignement = alignement;
			_subscribeNeed = subscribe;
		}

		public String get_name() {
			return _name;
		}

		public int get_id() {
			return _id;
		}

		public Area get_area() {
			return _area;
		}

		public int get_alignement() {
			return _alignement;
		}

		public ArrayList<Mapa> getMaps() {
			return _maps;
		}

		public void addMap(Mapa carte) {
			_maps.add(carte);
		}

		public boolean get_subscribe() {
			return _subscribeNeed;
		}

	}

	public static void RefreshAllMob() {
		SocketManager.GAME_SEND_MESSAGE_TO_ALL("Recharge des Mobs en cours, des latences peuvent survenir.", Main.CONFIG_MOTD_COLOR);
		for (Mapa map : Cartes.values()) {
			map.refreshSpawns();
		}
		SocketManager.GAME_SEND_MESSAGE_TO_ALL("Recharge des Mobs finie. La prochaine recharge aura lieu dans 5heures.", Main.CONFIG_MOTD_COLOR);
	}

	public static ExpLevel getExpLevel(int lvl)
	{
		return ExpLevels.get(lvl);
	}
	public static IOTemplate getIOTemplate(int id)
	{
		return IOTemplate.get(id);
	}
	public static Metier getMetier(int id)
	{
		return Jobs.get(id);
	}

	public static void addJob(Metier metier)
	{
		Jobs.put(metier.getId(), metier);
	}

	public static void addCraft(int id, ArrayList<Couple<Integer, Integer>> m)
	{
		Crafts.put(id,m);
	}
	
	public static ArrayList<Couple<Integer,Integer>> getCraft(int i)
	{
		return Crafts.get(i);
	}

	public static int getObjectByIngredientForJob( ArrayList<Integer> list, Map<Integer, Integer> ingredients)
	{
		if(list == null)return -1;
		for(int tID : list)
		{
			ArrayList<Couple<Integer,Integer>> craft = World.getCraft(tID);
			if(craft == null)
			{
				GameServer.addToLog("/!\\Recette pour l'objet "+tID+" non existante !");
				continue;
			}
			if(craft.size() != ingredients.size())continue;
			boolean ok = true;
			for(Couple<Integer,Integer> c : craft) {
				//si ingredient non pr?sent ou mauvaise quantit?
				if(!Objects.equals(ingredients.get(c.first), c.second))ok = false;
			}
			if(ok)return tID;
		}
		return -1;
	}
	public static Compte getCompteByPseudo(String p) {
		for(Compte C : Comptes.values())if(C.get_pseudo().equals(p))return C;
		return null;
	}

	public static void addItemSet(ItemSet itemSet)
	{
		ItemSets.put(itemSet.getId(), itemSet);
	}

	public static ItemSet getItemSet(int tID)
	{
		return ItemSets.get(tID);
	}

	public static int getItemSetNumber()
	{
		return ItemSets.size();
	}

	public static int getNextIdForMount()
	{
		int max = 1;
		for(int a : Dragodindes.keySet())if(a > max)max = a;
		return max+1;
	}

	public static Mapa getCarteByPosAndCont(int mapX, int mapY, int contID)
	{
		for(Mapa map : Cartes.values())
		{
			if( map.getX() == mapX
			&&	map.getY() == mapY
			&&	map.getSubArea().get_area().get_superArea().get_id() == contID)
				return map;
		}
		return null;
	}
	public static void addGuild(Guild g,boolean save)
	{
		Guildes.put(g.get_id(), g);
		if(save)SQLManager.SAVE_NEWGUILD(g);
	}
	public static int getNextHighestGuildID()
	{
		if(Guildes.isEmpty())return 1;
		int n = 0;
		for(int x : Guildes.keySet())if(n<x)n = x;
		return n+1;
	}

	public static boolean guildNameIsUsed(String name)
	{
		for(Guild g : Guildes.values())if(g.get_name().equalsIgnoreCase(name))return true;
		return false;
	}
	public static boolean guildEmblemIsUsed(String emb)
	{
		for(Guild g : Guildes.values())
		{
			if(g.get_emblem().equals(emb))return true;
		}
		return false;
	}
	public static Guild getGuild(int i)
	{
		return Guildes.get(i);
	}
	public static long getGuildXpMax(int _lvl)
	{
		if(_lvl >= 200) 	_lvl = 199;
		if(_lvl <= 1)	 	_lvl = 1;
		return ExpLevels.get(_lvl+1).guilde;
	}
	
	public static void ReassignAccountToChar(Compte C)
	{
		C.get_persos().clear();
		SQLManager.LOAD_PERSO_BY_ACCOUNT(C.get_GUID());
		for(Personaje P : Persos.values())
		{
			if(P.getAccID() == C.get_GUID())
			{
				C.addPerso(P);
			}
		}
	}
	
	public static int getZaapCellIdByMapId(short i)
	{
		for(Entry<Integer, Integer> zaap : Constants.ZAAPS.entrySet())
		{
			if(zaap.getKey() == i)return zaap.getValue();
		}
		return -1;
	}
	public static int getEncloCellIdByMapId(short i)
	{
		if(World.getCarte(i).getMountPark() != null)
		{
			if(World.getCarte(i).getMountPark().get_cellid() > 0)
			{
				return World.getCarte(i).getMountPark().get_cellid();
			}
		}
		
		return -1;
	}

	public static void delDragoByID(int getId)
	{
		Dragodindes.remove(getId);
	}

	public static void removeGuild(int id)
	{
		//Maison de guilde+SQL
		House.removeHouseGuild(id);
		//Enclo+SQL
		Mapa.MountPark.removeMountPark(id);
		//Percepteur+SQL
		Percepteur.removePercepteur(id);
		//Guilde
		Guildes.remove(id);
		SQLManager.DEL_ALL_GUILDMEMBER(id);//Supprime les membres
		SQLManager.DEL_GUILD(id);//Supprime la guilde
	}

	public static boolean ipIsUsed(String ip)
	{
		for(Compte c : Comptes.values())if(c.get_curIP().compareTo(ip) == 0)return true;
		return false;
	}
	
	public static void unloadPerso(int g)
	{
		Personaje toRem = Persos.get(g);
		if(!toRem.getItems().isEmpty())
		{
			for(Entry<Integer,Objet> curObj : toRem.getItems().entrySet())
			{
				Objets.remove(curObj.getKey());
			}
		}
		toRem = null;
	}
	
	public static boolean isArenaMap(int mapID)
	{
		for(int curID : Main.arenaMap)
		{
			if(curID == mapID)
				return true;
		}
		return false;
	}
	public static Objet newObjet(int Guid, int template,int qua, int pos, String strStats)
	{
		if(World.getObjTemplate(template) == null) 
		{ 
			System.out.println("ItemTemplate "+template+" inexistant, GUID dans la table `items`:"+Guid);
			Main.closeServers();
		} 
		
		if(World.getObjTemplate(template).getType() == 85)
			return new PierreAme(Guid, qua, template, pos, strStats);
		else
			return new Objet(Guid, template, qua, pos, strStats);
	}
	
	public static byte getGmAccess()
	{
		return _GmAccess;
	}
	
	public static void setGmAccess(byte GmAccess)
	{
		_GmAccess = GmAccess;
	}
	/** HDV **/
	public static Hdv getHdv(int mapID)
	{
		return Hdvs.get(mapID);
	}
	
	public static void addHdv(Hdv toAdd)
	{
		Hdvs.put(toAdd.get_mapID(),toAdd);
	}
	
	public static String parse_EHl(int Tid)
	{
		StringBuilder str = new StringBuilder();
		str.append(Tid);
		for(Entry<Integer, HdvEntry> Obj : HdvsTemplates.get(Tid).entrySet())
		{
			HdvEntry item = Obj.getValue();
			str.append("|").append(item.get_ObjetID()).append(";").append(item.get_obj().parseStatsString()).append(";");
			if(item.get_qua() == 1) str.append(item.get_price()).append(";").append(";");
			if(item.get_qua() == 10) str.append(";").append(item.get_price()).append(";");
			if(item.get_qua() == 100) str.append(";").append(";").append(item.get_price());
		}
		return str.toString();
	}
	
	public static int get_averagePrice(int Tid)
	{
		int averagePrice = 0;
		for(Entry<Integer, HdvEntry> Obj : HdvsTemplates.get(Tid).entrySet())
		{
			if(Obj.getValue().get_qua() == 1) averagePrice += Obj.getValue().get_price();
			if(Obj.getValue().get_qua() == 10) averagePrice += Math.ceil(Obj.getValue().get_price()/10);
			if(Obj.getValue().get_qua() == 100) averagePrice += Math.ceil(Obj.getValue().get_price()/100);
		}
		return (int) Math.ceil(averagePrice/HdvsTemplates.get(Tid).size());
	}

	public static class IOTemplate {
		private final int _id;
		private final int _respawnTime;
		private final int _duration;
		private final int _unk;
		private final boolean _walkable;

		public IOTemplate(int a_i, int a_r, int a_d, int a_u, boolean a_w) {
			_id = a_i;
			_respawnTime = a_r;
			_duration = a_d;
			_unk = a_u;
			_walkable = a_w;
		}

		public int getId() {
			return _id;
		}

		public boolean isWalkable() {
			return _walkable;
		}

		public int getRespawnTime() {
			return _respawnTime;
		}

		public int getDuration() {
			return _duration;
		}

		public int getUnk() {
			return _unk;
		}
	}
	
	public static Map<Integer, ArrayList<HdvEntry>> getMyItems(int compteID)
	{
		if(HdvsItems.get(compteID) == null)//Si le compte n'est pas dans la memoire
			HdvsItems.put(compteID, new HashMap<>());//Ajout du compte cl?:compteID et un nouveau map<hdvID,items<>>
			
		return HdvsItems.get(compteID);
	}
	
	public static HdvEntry get_HdvEntry(int Tid, int ObjetID)
	{
		return HdvsTemplates.get(Tid).get(ObjetID);
	}
	
	public static void addHdvItem(int compteID, int hdvID, HdvEntry toAdd)
	{
		if(HdvsItems.get(compteID) == null)
			HdvsItems.put(compteID, new HashMap<>());
		if(HdvsTypes.get(toAdd.get_obj().getTemplate().getType()) == null)
			HdvsTypes.put(toAdd.get_obj().getTemplate().getType(), new HashMap<>());
		if(HdvsTemplates.get(toAdd.get_obj().getTemplate().getID()) == null)
			HdvsTemplates.put(toAdd.get_obj().getTemplate().getID(), new HashMap<>());
		
		if(HdvsItems.get(compteID).get(hdvID) == null)
			HdvsItems.get(compteID).put(hdvID, new ArrayList<>());
		if(HdvsTypes.get(toAdd.get_obj().getTemplate().getType()).get(hdvID) == null)
			HdvsTypes.get(toAdd.get_obj().getTemplate().getType()).put(hdvID, new HashMap<>());
			
		if(HdvsTypes.get(toAdd.get_obj().getTemplate().getType()).get(hdvID).get(toAdd.get_obj().getTemplate().getID()) == null)
			HdvsTypes.get(toAdd.get_obj().getTemplate().getType()).get(hdvID).put(toAdd.get_obj().getTemplate().getID(), new HashMap<>());
		
		HdvsTemplates.get(toAdd.get_obj().getTemplate().getID()).put(toAdd.get_ObjetID(), toAdd);
		HdvsTypes.get(toAdd.get_obj().getTemplate().getType()).get(hdvID).get(toAdd.get_obj().getTemplate().getID()).put(toAdd.get_ObjetID(), toAdd.get_obj());
		HdvsItems.get(compteID).get(hdvID).add(toAdd);
	}
	
	public static void removeHdvItem(int compteID,int hdvID,HdvEntry toDel)
	{
		HdvsItems.get(compteID).get(hdvID).remove(toDel);
		HdvsTypes.get(toDel.get_obj().getTemplate().getType()).get(hdvID).get(toDel.get_obj().getTemplate().getID()).remove(toDel.get_ObjetID());
		HdvsTemplates.get(toDel.get_obj().getTemplate().getID()).remove(toDel.get_ObjetID());
		
		if(HdvsTypes.get(toDel.get_obj().getTemplate().getType()).get(hdvID).get(toDel.get_obj().getTemplate().getID()).isEmpty())
		{
			HdvsTypes.get(toDel.get_obj().getTemplate().getType()).get(hdvID).remove(toDel.get_obj().getTemplate().getID());
		}
	}
	/** HDV **/
	public static Personaje getMarried(int ordre)
	{
		return Married.get(ordre);
	}
	
	public static void AddMarried(int ordre, Personaje perso)
	{
		Personaje Perso = Married.get(ordre);
		if(Perso != null)
		{
			if(perso.get_GUID() == Perso.get_GUID()) // Si c'est le meme joueur...
				return;
			if(Perso.isOnline())// Si perso en ligne...
			{
				Married.remove(ordre);
				Married.put(ordre, perso);
				return;
			}
			
			return;
		}else
		{
			Married.put(ordre, perso);
			return;
		}
	}
	
	public static void PriestRequest(Personaje perso, Mapa carte, int IdPretre)
	{
		Personaje Homme = Married.get(0);
		Personaje Femme = Married.get(1);
		if(Homme.getWife() != 0){
			SocketManager.GAME_SEND_MESSAGE_TO_MAP(carte, Homme.get_name()+" est deja marier!", Main.CONFIG_MOTD_COLOR);
			return;
		}
		if(Femme.getWife() != 0){
			SocketManager.GAME_SEND_MESSAGE_TO_MAP(carte, Femme.get_name()+" est deja marier!", Main.CONFIG_MOTD_COLOR);
			return;
		}
		SocketManager.GAME_SEND_cMK_PACKET_TO_MAP(perso.get_curCarte(), "", -1, "Pr?tre", perso.get_name()+" acceptez-vous d'?pouser "+getMarried((perso.get_sexe()==1?0:1)).get_name()+" ?");
		SocketManager.GAME_SEND_WEDDING(carte, 617, (Homme==perso?Homme.get_GUID():Femme.get_GUID()), (Homme==perso?Femme.get_GUID():Homme.get_GUID()), IdPretre);
	}
	
	public static void Wedding(Personaje Homme, Personaje Femme, int isOK)
	{
		if(isOK > 0)
		{
			SocketManager.GAME_SEND_cMK_PACKET_TO_MAP(Homme.get_curCarte(), "", -1, "Pr?tre", "Je d?clare "+Homme.get_name()+" et "+Femme.get_name()+" unis par les liens sacr?s du mariage.");
			Homme.MarryTo(Femme);
			Femme.MarryTo(Homme);
		}else
		{
			SocketManager.GAME_SEND_Im_PACKET_TO_MAP(Homme.get_curCarte(), "048;"+Homme.get_name()+"~"+Femme.get_name());
		}
		Married.get(0).setisOK(0);
		Married.get(1).setisOK(0);
		Married.clear();
	}
	
	public static Animations getAnimation(int AnimationId)
	{
		return Animations.get(AnimationId);
	}
	
	public static void addAnimation(Animations animation)
	{
		Animations.put(animation.getId(), animation);
	}
	
	public static void addHouse(House house)
	{
		Houses.put(house.get_id(), house);
	}
	
	public static Map<Integer, House> getHouses()
	{
		return Houses;
	}
	
	public static House getHouse(int id)
	{
		return Houses.get(id);
	}
	
	public static void addPerco(Percepteur perco)
	{
		Percepteurs.put(perco.getGuid(), perco);
	}
	
	public static Percepteur getPerco(int percoID)
	{
		return Percepteurs.get(percoID);
	}
	
	public static Map<Integer, Percepteur> getPercos()
	{
		return Percepteurs;
	}
	
	public static void addTrunk(Trunk trunk)
	{
		Trunks.put(trunk.get_id(), trunk);
	}
	
	public static Trunk getTrunk(int id)
	{
		return Trunks.get(id);
	}
	
	public static Map<Integer, Trunk> getTrunks()
	{
		return Trunks;
	}
	
	public static void addMountPark(Mapa.MountPark mp)
	{
		MountPark.put(mp.get_map().get_id(), mp);
	}
	
	public static Map<Short, Mapa.MountPark> getMountPark()
	{
		return MountPark;
	}
	
	public static String parseMPtoGuild(int GuildID)
	{
		Guild G = World.getGuild(GuildID);
		byte enclosMax = (byte)Math.floor(G.get_lvl()/10);
		StringBuilder packet = new StringBuilder();
		packet.append(enclosMax);
		
		for(Entry<Short, Mapa.MountPark> mp : MountPark.entrySet())
		{
			if(mp.getValue().get_guild() != null && mp.getValue().get_guild().get_id() == GuildID)
			{
				packet.append("|").append(mp.getValue().get_map().get_id()).append(";").append(mp.getValue().get_size()).append(";").append(mp.getValue().getObjectNumb());// Nombre d'objets pour le dernier
			}else
			{
				continue;
			}
		}
		return packet.toString();
	}
	
	public static int totalMPGuild(int GuildID)
	{
		int i = 0;
		for(Entry<Short, Mapa.MountPark> mp : MountPark.entrySet())
		{
			if(mp.getValue().get_guild() != null && mp.getValue().get_guild().get_id() == GuildID)
			{
				i++;
			}else
			{
				continue;
			}
		}
		return i;
	}
	
	public static void addSeller(Personaje p)
	{
		if(Seller.get(p.get_curCarte().get_id()) == null)
		{
			ArrayList<Integer> PersoID = new ArrayList<>();
			PersoID.add(p.get_GUID());
			Seller.put(p.get_curCarte().get_id(), PersoID);
		}else
		{
			ArrayList<Integer> PersoID = new ArrayList<>();
			PersoID.addAll(Seller.get(p.get_curCarte().get_id()));
			PersoID.add(p.get_GUID());
			Seller.remove(p.get_curCarte().get_id());
			Seller.put(p.get_curCarte().get_id(), PersoID);
		}
	}
	
	public static Collection<Integer> getSeller(short mapID)
	{
		return Seller.get(mapID);
	}
	
	public static void removeSeller(int pID, short mapID)
	{
		Seller.get(mapID).remove(pID);
	}
	
	public static Bank GetBank(int guid)
	{
		return Banks.get(guid);
	}
	
	public static void AddBank(Bank bk)
	{
		Banks.put(bk.getGuid(), bk);
	}
	
	public static FriendList GetFriends(int guid)
	{
		return Friends.get(guid);
	}
	
	public static void AddFriendList(int guid, FriendList fr)
	{
		Friends.put(guid, fr);
	}
	
	public static EnemyList GetEnemys(int guid)
	{
		return Enemys.get(guid);
	}
	
	public static void AddEnemyList(int guid, EnemyList fr)
	{
		Enemys.put(guid, fr);
	}
	
	public static Collection<Compte> getComptes()
	{
		return Comptes.values();
	}
	
	public static void addPets(Pets pets)
	{
		Pets.put(pets.get_Tid(), pets);
	}
	
	public static Pets get_Pets(int Tid)
	{
		return Pets.get(Tid);
	}
	
	public static Map<Integer, objects.Pets> get_Pets()
	{
		return Pets;
	}
	
	public static void addPetsEntry(PetsEntry pets)
	{
		PetsEntry.put(pets.get_ObjectID(), pets);
	}
	
	public static PetsEntry get_PetsEntry(int guid)
	{
		return PetsEntry.get(guid);
	}
	
	public static Percepteur getPercepteur(int guid)
    {
        return getPercos().get(guid);
    }
	
	public static void addChallenge(String chal)
	{	
		//FormaType : ChalID,gainXP,gainDrop,gainParMob,Conditions;
		if(!Challenges.toString().isEmpty())
			Challenges.append(";");
		Challenges.append(chal);
	}
	
	public static String getChallengeFromConditions(boolean sevEnn, boolean sevAll, boolean bothSex, boolean EvenEnn, boolean MoreEnn, boolean hasCaw, boolean hasChaf, boolean hasRoul, boolean hasArak, boolean isBoss)
	{
		//sevEnn : N?cessite plusieurs ennemis
		//secAll : N?cessite plusieurs alli?s
		//bothSex : N?cessite les deux sexe
		//EvenEnn : True : Nb ennemis pair, False : impaire
		//MoreEnn : Plus d'ennemis que d'alli?
		//hasCaw : Poss?de le sort cawotte
		//hasChaf : Poss?de le sort chafer
		//hasRoul : Poss?de le sort roulette
		//hasArak : Poss?de le sort arakne
		String noBossChals = ";2;5;9;17;19;24;38;47;50;";//Liste des challenge impossibles contre un boss
		StringBuilder toReturn = new StringBuilder();
		boolean isFirst = true, isGood = false;
		int cond = 0;
		for(String chal : Challenges.toString().split(";"))
		{
			if(!isFirst && isGood)
				toReturn.append(";");
			isGood = true;
			cond = Integer.parseInt(chal.split(",")[4]);
			//Necessite plusieurs ennemis
			if(((cond & 1) == 1) && !sevEnn)
				isGood = false;
			//Necessite plusieurs allies
			if((((cond>>1) & 1) == 1) && !sevAll)
				isGood = false;
			//Necessite les deux sexes
			if((((cond>>2) & 1) == 1) && !bothSex)
				isGood = false;
			//Necessite un nombre pair d'ennemis
			if((((cond>>3) & 1) == 1) && !EvenEnn)
				isGood = false;
			//Necessite plus d'ennemis que d'allies
			if((((cond>>4) & 1) == 1) && !MoreEnn)
				isGood = false;
			//Jardinier
			if(!hasCaw && (Integer.parseInt(chal.split(",")[0]) == 7))
				isGood = false;
			//Fossoyeur
			if(!hasChaf && (Integer.parseInt(chal.split(",")[0]) == 12))
				isGood = false;
			//Casino Royal
			if(!hasRoul && (Integer.parseInt(chal.split(",")[0]) == 14))
				isGood = false;
			//Araknophile
			if(!hasArak && (Integer.parseInt(chal.split(",")[0]) == 15))
				isGood = false;
			//Contre un boss de donjon
			if (isBoss && noBossChals.contains(";" + chal.split(",")[0] + ";"))
				isGood = false;
			if (isGood)
				toReturn.append(chal);
			isFirst = false;
		}
		return toReturn.toString();
	}

	public static class Exchange {
		private final Personaje perso1;
		private final Personaje perso2;
		private long kamas1 = 0;
		private long kamas2 = 0;
		private final ArrayList<Couple<Integer, Integer>> items1 = new ArrayList<>();
		private final ArrayList<Couple<Integer, Integer>> items2 = new ArrayList<>();
		private boolean ok1;
		private boolean ok2;

		public Exchange(Personaje p1, Personaje p2) {
			perso1 = p1;
			perso2 = p2;
		}

		synchronized public long getKamas(int guid) {
			int i = 0;
			if (perso1.get_GUID() == guid)
				i = 1;
			else if (perso2.get_GUID() == guid)
				i = 2;

			if (i == 1)
				return kamas1;
			else if (i == 2)
				return kamas2;
			return 0;
		}

		synchronized public void toogleOK(int guid) {
			int i = 0;
			if (perso1.get_GUID() == guid)
				i = 1;
			else if (perso2.get_GUID() == guid)
				i = 2;

			if (i == 1) {
				ok1 = !ok1;
				SocketManager.GAME_SEND_EXCHANGE_OK(perso1.get_compte().getGameThread().get_out(), ok1, guid);
				SocketManager.GAME_SEND_EXCHANGE_OK(perso2.get_compte().getGameThread().get_out(), ok1, guid);
			} else if (i == 2) {
				ok2 = !ok2;
				SocketManager.GAME_SEND_EXCHANGE_OK(perso1.get_compte().getGameThread().get_out(), ok2, guid);
				SocketManager.GAME_SEND_EXCHANGE_OK(perso2.get_compte().getGameThread().get_out(), ok2, guid);
			} else
				return;


			if (ok1 && ok2)
				apply();
		}

		synchronized public void setKamas(int guid, long k) {
			ok1 = false;
			ok2 = false;

			int i = 0;
			if (perso1.get_GUID() == guid)
				i = 1;
			else if (perso2.get_GUID() == guid)
				i = 2;
			SocketManager.GAME_SEND_EXCHANGE_OK(perso1.get_compte().getGameThread().get_out(), ok1, perso1.get_GUID());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso2.get_compte().getGameThread().get_out(), ok1, perso1.get_GUID());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso1.get_compte().getGameThread().get_out(), ok2, perso2.get_GUID());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso2.get_compte().getGameThread().get_out(), ok2, perso2.get_GUID());

			if (i == 1) {
				kamas1 = k;
				SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso1, 'G', "", k + "");
				SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso2.get_compte().getGameThread().get_out(), 'G', "", k + "");
			} else if (i == 2) {
				kamas2 = k;
				SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso1.get_compte().getGameThread().get_out(), 'G', "", k + "");
				SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso2, 'G', "", k + "");
			}
		}

		synchronized public void cancel() {
			if (perso1.get_compte() != null) if (perso1.get_compte().getGameThread() != null)
				SocketManager.GAME_SEND_EV_PACKET(perso1.get_compte().getGameThread().get_out());
			if (perso2.get_compte() != null) if (perso2.get_compte().getGameThread() != null)
				SocketManager.GAME_SEND_EV_PACKET(perso2.get_compte().getGameThread().get_out());
			perso1.set_isTradingWith(0);
			perso2.set_isTradingWith(0);
			perso1.setCurExchange(null);
			perso2.setCurExchange(null);
		}

		synchronized public void apply() {
			//Gestion des Kamas
			perso1.addKamas((-kamas1 + kamas2));
			perso2.addKamas((-kamas2 + kamas1));
			for (Couple<Integer, Integer> couple : items1) {
				if (couple.second == 0) continue;
				if (!perso1.hasItemGuid(couple.first))//Si le perso n'a pas l'item (Ne devrait pas arriver)
				{
					couple.second = 0;//On met la quantit? a 0 pour ?viter les problemes
					continue;
				}
				Objet obj = World.getObjet(couple.first);
				if ((obj.getQuantity() - couple.second) < 1)//S'il ne reste plus d'item apres l'?change
				{
					perso1.removeItem(couple.first);
					couple.second = obj.getQuantity();
					SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(perso1, couple.first);
					if (!perso2.addObjet(obj, true))//Si le joueur avait un item similaire
						World.removeItem(couple.first);//On supprime l'item inutile
				} else {
					obj.setQuantity(obj.getQuantity() - couple.second);
					SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(perso1, obj);
					Objet newObj = Objet.getCloneObjet(obj, couple.second);
					if (perso2.addObjet(newObj, true))//Si le joueur n'avait pas d'item similaire
						World.addObjet(newObj, true);//On ajoute l'item au World
				}
			}
			for (Couple<Integer, Integer> couple : items2) {
				if (couple.second == 0) continue;
				if (!perso2.hasItemGuid(couple.first))//Si le perso n'a pas l'item (Ne devrait pas arriver)
				{
					couple.second = 0;//On met la quantit? a 0 pour ?viter les problemes
					continue;
				}
				Objet obj = World.getObjet(couple.first);
				if ((obj.getQuantity() - couple.second) < 1)//S'il ne reste plus d'item apres l'?change
				{
					perso2.removeItem(couple.first);
					couple.second = obj.getQuantity();
					SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(perso2, couple.first);
					if (!perso1.addObjet(obj, true))//Si le joueur avait un item similaire
						World.removeItem(couple.first);//On supprime l'item inutile
				} else {
					obj.setQuantity(obj.getQuantity() - couple.second);
					SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(perso2, obj);
					Objet newObj = Objet.getCloneObjet(obj, couple.second);
					if (perso1.addObjet(newObj, true))//Si le joueur n'avait pas d'item similaire
						World.addObjet(newObj, true);//On ajoute l'item au World
				}
			}
			//Fin
			perso1.set_isTradingWith(0);
			perso2.set_isTradingWith(0);
			perso1.setCurExchange(null);
			perso2.setCurExchange(null);
			SocketManager.GAME_SEND_Ow_PACKET(perso1);
			SocketManager.GAME_SEND_Ow_PACKET(perso2);
			SocketManager.GAME_SEND_STATS_PACKET(perso1);
			SocketManager.GAME_SEND_STATS_PACKET(perso2);
			SocketManager.GAME_SEND_EXCHANGE_VALID(perso1.get_compte().getGameThread().get_out(), 'a');
			SocketManager.GAME_SEND_EXCHANGE_VALID(perso2.get_compte().getGameThread().get_out(), 'a');
			SQLManager.SAVE_PERSONNAGE(perso1, true);
			SQLManager.SAVE_PERSONNAGE(perso2, true);
		}

		synchronized public void addItem(int guid, int qua, int pguid) {
			ok1 = false;
			ok2 = false;

			Objet obj = World.getObjet(guid);
			int i = 0;

			if (perso1.get_GUID() == pguid) i = 1;
			if (perso2.get_GUID() == pguid) i = 2;

			if (qua == 1) qua = 1;
			String str = guid + "|" + qua;
			if (obj == null) return;
			String add = "|" + obj.getTemplate().getID() + "|" + obj.parseStatsString();
			SocketManager.GAME_SEND_EXCHANGE_OK(perso1.get_compte().getGameThread().get_out(), ok1, perso1.get_GUID());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso2.get_compte().getGameThread().get_out(), ok1, perso1.get_GUID());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso1.get_compte().getGameThread().get_out(), ok2, perso2.get_GUID());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso2.get_compte().getGameThread().get_out(), ok2, perso2.get_GUID());
			if (i == 1) {
				Couple<Integer, Integer> couple = getCoupleInList(items1, guid);
				if (couple != null) {
					couple.second += qua;
					SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso1, 'O', "+", "" + guid + "|" + couple.second);
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso2.get_compte().getGameThread().get_out(), 'O', "+", "" + guid + "|" + couple.second + add);
					return;
				}
				SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso1, 'O', "+", str);
				SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso2.get_compte().getGameThread().get_out(), 'O', "+", str + add);
				items1.add(new Couple<>(guid, qua));
			} else if (i == 2) {
				Couple<Integer, Integer> couple = getCoupleInList(items2, guid);
				if (couple != null) {
					couple.second += qua;
					SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso2, 'O', "+", "" + guid + "|" + couple.second);
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso1.get_compte().getGameThread().get_out(), 'O', "+", "" + guid + "|" + couple.second + add);
					return;
				}
				SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso2, 'O', "+", str);
				SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso1.get_compte().getGameThread().get_out(), 'O', "+", str + add);
				items2.add(new Couple<>(guid, qua));
			}
		}


		synchronized public void removeItem(int guid, int qua, int pguid) {
			int i = 0;
			if (perso1.get_GUID() == pguid)
				i = 1;
			else if (perso2.get_GUID() == pguid)
				i = 2;
			ok1 = false;
			ok2 = false;

			SocketManager.GAME_SEND_EXCHANGE_OK(perso1.get_compte().getGameThread().get_out(), ok1, perso1.get_GUID());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso2.get_compte().getGameThread().get_out(), ok1, perso1.get_GUID());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso1.get_compte().getGameThread().get_out(), ok2, perso2.get_GUID());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso2.get_compte().getGameThread().get_out(), ok2, perso2.get_GUID());

			Objet obj = World.getObjet(guid);
			if (obj == null) return;
			String add = "|" + obj.getTemplate().getID() + "|" + obj.parseStatsString();
			if (i == 1) {
				Couple<Integer, Integer> couple = getCoupleInList(items1, guid);
				int newQua = couple.second - qua;
				if (newQua < 1)//Si il n'y a pu d'item
				{
					items1.remove(couple);
					SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso1, 'O', "-", "" + guid);
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso2.get_compte().getGameThread().get_out(), 'O', "-", "" + guid);
				} else {
					couple.second = newQua;
					SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso1, 'O', "+", "" + guid + "|" + newQua);
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso2.get_compte().getGameThread().get_out(), 'O', "+", "" + guid + "|" + newQua + add);
				}
			} else if (i == 2) {
				Couple<Integer, Integer> couple = getCoupleInList(items2, guid);
				int newQua = couple.second - qua;

				if (newQua < 1)//Si il n'y a pu d'item
				{
					items2.remove(couple);
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso1.get_compte().getGameThread().get_out(), 'O', "-", "" + guid);
					SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso2, 'O', "-", "" + guid);
				} else {
					couple.second = newQua;
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso1.get_compte().getGameThread().get_out(), 'O', "+", "" + guid + "|" + newQua + add);
					SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso2, 'O', "+", "" + guid + "|" + newQua);
				}
			}
		}

		synchronized private Couple<Integer, Integer> getCoupleInList(ArrayList<Couple<Integer, Integer>> items, int guid) {
			for (Couple<Integer, Integer> couple : items) {
				if (couple.first == guid)
					return couple;
			}
			return null;
		}

		public synchronized int getQuaItem(int itemID, int playerGuid) {
			ArrayList<Couple<Integer, Integer>> items;
			if (perso1.get_GUID() == playerGuid)
				items = items1;
			else
				items = items2;

			for (Couple<Integer, Integer> curCoupl : items) {
				if (curCoupl.first == itemID) {
					return curCoupl.second;
				}
			}

			return 0;
		}

	}
	
	public static void addCrafterOnBook(int guid, int jobID)
	{
		if(CraftBook.get(jobID) == null)
		{
			ArrayList<Integer> Guid = new ArrayList<>();
			Guid.add(guid);
			CraftBook.put(jobID, Guid);
		}else
		{
			ArrayList<Integer> Guid = new ArrayList<>();
			Guid.addAll(CraftBook.get(jobID));
			Guid.add(guid);
			CraftBook.remove(jobID);
			CraftBook.put(jobID, Guid);
		}
	}
	
	public static Collection<Integer> getCrafterOnBook(int jobID)
	{
		return CraftBook.get(jobID);
	}
	
	public static void removeCrafterOnBook(int guid, int jobID)
	{
		CraftBook.get(jobID).remove(guid);
	}
	
	public static void removeCrafterOnBook(int guid)
	{
		for(Entry<Integer, Collection<Integer>> ID : CraftBook.entrySet())
		{
			for(Integer ID2 : ID.getValue())
			{
				if(ID2 == guid && ID.getValue().size() > 1) CraftBook.get(ID.getKey()).remove(guid);
				else if(ID2 == guid && ID.getValue().size() <= 1) CraftBook.get(ID.getKey()).clear();
			}
		}
	}
	
	public static void MoveMobsOnMaps()
	{
		for(Mapa map : Cartes.values())
		{
			map.onMap_MonstersDisplacement();
		}
	}
	
	public static Gift getGift(int giftId)
	{
		return Gifts.get(giftId);
	}

	public static void addGift(Gift gift)
	{
		Gifts.put(gift.getId(), gift);
	}
}
