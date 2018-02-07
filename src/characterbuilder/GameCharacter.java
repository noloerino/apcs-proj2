package characterbuilder;

import core.*;
import core.animations.Animation;
import core.animations.PathAnimation;
import core.animations.SequentialAnimation;
import core.animations.SymmetricAnimation;
import items.Item;
import ui.MapPanel;
import weapons.IronSword;
import weapons.RedLaserSword;
import weapons.Tome;
import weapons.Weapon;
import worldmap.MapConfiguration;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;

import control.DebugLogger;
import control.MapViewListener;

public class GameCharacter extends Entity {

	private Alignment side;
	private int level;
	private int[] baseStats;
	private int[] currentStats;
	
	private int exp;
	private boolean missionCritical;
	private GameCharacterClass gclass;
//	private Skill[] skills;
	private Item[] inventory;
	private Weapon activeWeapon;
	private Animation activeMapAnimation;
	private Animation baseMapAnimation;
	private ImageIcon[] mvIcons;
	private ImageIcon[] atkIcons;
	
	public static DebugLogger log = new DebugLogger("GameCharacter");
	
	public final static String RES_FOLDER = "res/characters/";
	
	private final String thisFolder;
	
	public static GameCharacter create(String name, String dir, String saveName) {
		return new GameCharacter(name, dir, saveName);
	}
	
	public GameCharacter(String imgFile, String dir, String saveName) {
		super("Unnamed", "That which shall not be named", RES_FOLDER, imgFile, ' ');
		thisFolder = dir + (dir.endsWith("/") ? "" : "/");

//		this.skills = skills;
		initInfo();
		String statsFile = "";
		try {
			 statsFile = "res/saves/" + saveName + "/units/" + getDirName().substring(0, getDirName().length() - 1);
		}
		catch(IndexOutOfBoundsException e) { }
		if(!Files.exists(Paths.get(statsFile))) {
			log.log("Did not find stats file in " + statsFile);
			statsFile = getPath() + "start_stats.txt";
		}
		this.baseStats = readStatsFile(this, statsFile);
		currentStats = Arrays.copyOf(baseStats, baseStats.length);
		this.inventory = new Item[5];
		log.add("Initialized character <" + getName() + "> with base stats " + Arrays.toString(baseStats)
				+ ",\n\tmove points <" + getMvtPts() + ">"
				+ ", animation of <" + activeMapAnimation.getFramesToLoop() + "> frames"
				+ ", Alignment <" + side.toString() + ">, and Class <" + gclass.getName() + ">");
	}
	
	private static Weapon getWeapon(String s) {
		s = s.toUpperCase();
		if(s.equals("RED_LASER_SWORD"))
			return new RedLaserSword();
		else if(s.equals("TOME"))
			return new Tome();
		else
			return new IronSword();
	}
	
	private void initInfo() {
		// default values, overridden if no exception
		this.side = Alignment.FRIENDLY;
		this.level = 1;
		this.exp = 0;
		this.gclass = GameCharacterClass.DEFAULT_CLASS;
		boolean symmetricAnim = true;
		
		String weaponStr = "";
		try(BufferedReader r = new BufferedReader(new FileReader(getPath() + "start_info.txt"))) {
			Map<String, String> map = new HashMap<>();
			String l = r.readLine();
			while(l != null) {
				String[] split = l.split(":");
				if(l.length() == 0) {
					l = r.readLine();
					continue;
				}
				if(split.length != 2) {
					log.add("Malformed string in info file: " + l);
					l = r.readLine();
					continue;
				}
				map.put(split[0].toUpperCase(), split[1]);
				l = r.readLine();
			}
			if(map.containsKey("NAME"))
				setName(map.get("NAME"));
			if(map.containsKey("DESCRIPTION"))
				setDescription(map.get("DESCRIPTION"));
			if(map.containsKey("SIDE"))
				this.side = Alignment.valueOf(map.get("SIDE"));
			if(map.containsKey("CLASS"))
				this.gclass = GameCharacterClass.create(map.get("CLASS").toLowerCase() + ".txt");
			if(map.containsKey("SYM_ANIM"))
				symmetricAnim = Boolean.parseBoolean(map.get("SYM_ANIM"));
			if(map.containsKey("IS_MISSION_CRITICAL"))
				missionCritical = Boolean.parseBoolean(map.get("IS_MISSION_CRITICAL"));
			if(map.containsKey("WEAPON"))
				weaponStr = map.get("WEAPON");
		}
		catch (IOException e) {
			log.add("Could not find info file from file " + getPath() + "start_info.txt using default stats");
		}
		
		this.activeWeapon = getWeapon(weaponStr);

		readMoveAndAtkIcons();
		List<ImageIcon> anims = getAnimIcons(this, "base_anim/");
		if(anims.size() == 0)
			anims.add(getDefaultSprite());
		if(symmetricAnim)
			baseMapAnimation = new SymmetricAnimation(4, anims.toArray(new ImageIcon[0]));
		else
			baseMapAnimation = new Animation(4, anims.toArray(new ImageIcon[0]));
		activeMapAnimation = baseMapAnimation;
	}
	
	private void readMoveAndAtkIcons() {
		mvIcons = getAnimIcons(this, "moving/").toArray(new ImageIcon[0]);
		atkIcons = getAnimIcons(this, "attacking/").toArray(new ImageIcon[0]);
		if(mvIcons.length == 4)
			log.log("Moving icons initialized properly");
		else {
			log.log("Moving icons not initialized, replacing with default sprites");
			mvIcons = new ImageIcon[4];
			for(int i = 0; i < mvIcons.length; i++) {
				mvIcons[i] = new ImageIcon(getStationarySpritePath());
			}
		}
		if(atkIcons.length == 4)
			log.log("Attacking icons initialized properly");
		else {
			log.log("Attacking icons not initialized, replacing with default sprites");
			atkIcons = new ImageIcon[4];
			for(int i = 0; i < atkIcons.length; i++) {
				atkIcons[i] = new ImageIcon(getStationarySpritePath());
			}
		}
	}
	
	public boolean isMissionCritical() {
		return missionCritical;
	}
	
	public final static int HP = 0;
	public final static int ATK = 1;
	public final static int SPD = 2;
	public final static int DEF = 3;
	public final static int RES = 4;
	public final static String[] STAT_NAMES = {"HP", "ATK", "SPD", "DEF", "RES"};
	
	private static int[] readStatsFile(GameCharacter me, String statsFile) {
		//             hp atk spd def res
		int[] stats = {15, 4, 5, 5, 5};
		try(BufferedReader r = new BufferedReader(new FileReader(statsFile))) {
			Map<String, Integer> map = new HashMap<>();
			String l = r.readLine();
			while(l != null) {
				String[] split = l.split(":");
				if(split.length != 2) {
					log.add("Malformed string in stats file: " + l);
					l = r.readLine();
					continue;
				}
				try {
					map.put(split[0].toUpperCase(), Integer.parseInt(split[1]));
				}
				catch(NumberFormatException e) {
					log.add("Value of stat " + split[0] + " was not a number: " + split[1]);
				}
				l = r.readLine();
			}
			if(map.containsKey("LEVEL"))
				me.level = map.get("LEVEL");
			if(map.containsKey("EXP"))
				me.exp = map.get("EXP");
			for(int i = 0; i < stats.length; i++) {
				if(map.containsKey(STAT_NAMES[i]))
					stats[i] = map.get(STAT_NAMES[i]);
			}
		}
		catch(IOException e) {
			log.add("Could not find stats file from file " + me.getPath() + statsFile + "; using default stats");
		}
		return stats;
	}
	
	public String getPath() {
		return RES_FOLDER + thisFolder;
	}
	
	public String getDirName() {
		return thisFolder;
	}
	
	public String getStationarySpritePath() {
		return getPath() + "default.gif";
	}
	
	private static List<ImageIcon> getAnimIcons(GameCharacter me, String animDir) {
		List<ImageIcon> anims = new LinkedList<>();
		File folder = new File(me.getPath() + animDir);
		File[] files = folder.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".gif") || name.endsWith(".png");
			}
			
		});
		Arrays.sort(files);
		for(File f : files) {
			try {
				anims.add(new ImageIcon(f.getPath()));
			}
			catch(NullPointerException e) {
				log.add("Failed to find image from file " + f.getPath());
			}
		}
		return anims;
	}
	
	public Animation getActiveMapAnimation() {
		return activeMapAnimation;
	}
	
	public void setToBaseAnimation() {
		activeMapAnimation = baseMapAnimation;
	}
	
	public void runMvAnimation(MapPanel caller, List<IntVector2D> path, MapConfiguration config) {
		log.log("Starting move animation");
		activeMapAnimation = new PathAnimation(path, config, 3, mvIcons);
		caller.setGameStatus(GameState.MOVE_ANIMATION);
		caller.getMapViewListener().switchActionableTo(MapViewListener.LOC_CTRL);
	}
	
	public void runAtkAnimation(MapPanel caller, IntVector2D start, IntVector2D end, MapConfiguration config) {
		log.log("Starting attack animation");
		List<IntVector2D> path = new ArrayList<IntVector2D>();
		path.add(start);
		path.add(end);
		activeMapAnimation = new PathAnimation(path, config, 4, atkIcons);
		caller.setGameStatus(GameState.MOVE_ANIMATION);
		caller.getMapViewListener().switchActionableTo(MapViewListener.LOC_CTRL);
	}
	
	public void endMvOrAtkAnimation(MapPanel caller) {
		log.log("Ending move animation");
		if(activeMapAnimation instanceof SequentialAnimation)
			activeMapAnimation.stop();
		caller.setGameStatus(caller.getMapConfig().getSideToMove() == Alignment.FRIENDLY
				? GameState.PLAYER_MOVING : GameState.ENEMY_TURN);
		if(caller.getMapConfig().getSideToMove() == Alignment.FRIENDLY)
			caller.getMapViewListener().switchActionableTo(MapViewListener.VIEW_STATE);
		else
			caller.getMapViewListener().switchActionableTo(MapViewListener.LOC_CTRL);
		setToBaseAnimation();
	}
	
	public boolean isRanged() {
		return gclass.isRanged();
	}
	
	public boolean isMelee() {
		return gclass.isMelee();
	}
	
	private final static Color LVL_UP = new Color(222, 225, 229);
	public void giveXP(int pts) {
		exp += pts;
		if(exp >= 100) {
			exp %= 100;
			double[] rates = gclass.getLevelUpRates();
			StringBuilder msg = new StringBuilder("Level up!\n");
			for(int i = 0; i < baseStats.length; i++) {
				if(Math.random() >= rates[i]) {
					baseStats[i]++;
					msg.append(STAT_NAMES[i] + ": " + (baseStats[i] - 1) + "->" + baseStats[i] + "\n");
				}
			}
			baseMapAnimation.attachMsg(20, msg.toString(), LVL_UP);
		}
		else {
			baseMapAnimation.attachMsg(5, "+" + pts + "exp", LVL_UP);
		}
	}

	public final static int NO_DEATHS = 0;
	public final static int ATTACKER_DEATH = 1;
	public final static int DEFENDER_DEATH = 2;
	
	/**
	 * Initiates combat at a distance. If the speed of this character is greater than that of
	 * the opponent, the follow-up attack will be invoked in the reactFightRanged method
	 * regardless of whether or not the opponent can fight back.
	 * 
	 * @param opp
	 * @return An int representing the outcome of the battle.
	 */
	public int initiateFightRanged(GameCharacter opp) {
		log.log(getName() + " initiates ranged combat against " + opp.getName());
		if(!isRanged())
			throw new IllegalArgumentException("Non-ranged character cannot initated ranged combat.");
		if(opp.takeDmg(this))
			return DEFENDER_DEATH;
		else
			return opp.reactFightRanged(this);
	}

	/**
	 * Counterattacks at a distance. If the speed of the enemy character is greater than that of this one,
	 * the enemy's followup also occurs here.
	 * 
	 * @param opp
	 * @return An int representing the outcome of the battle.
	 */
	private int reactFightRanged(GameCharacter opp) {
		if(isRanged()) {
			if(opp.takeDmg(this))
				return ATTACKER_DEATH;
		}
		if(opp.getEffectiveSpd() > getEffectiveSpd()) {
			if(takeDmg(opp))
				return DEFENDER_DEATH;
		}
		else if(opp.getEffectiveSpd() < getEffectiveSpd()) {
			if(isRanged() && opp.takeDmg(this))
				return ATTACKER_DEATH;
		}
		return NO_DEATHS;
	}
	
	/**
	 * Deals damage to this character, accounting for reductions.
	 * 
	 * @param atk The attack power of the enemy.
	 * @param physical Whether or not the damage is a physical source. If true, then def
	 * is subtracted from atk, else res is subtracted.
	 * @return True if this character is killed, false otherwise.
	 */
	public boolean takeDmg(GameCharacter atker) {
		int atk = atker.getEffectiveAtk();
		if(atker.isWeaponPhys())
			atk -= getDef();
		else
			atk -= getRes();
		if(atk < 0)
			atk = 0;
		currentStats[HP] -= atk;
		if(getHP() <= 0)
			atker.baseMapAnimation.attachMsg(20, getName() + " was killed!", Color.RED);
		log.log(getName() + " takes " + atk + " damage, HP at " + currentStats[HP] + "/" + baseStats[HP]);
		return getHP() <= 0;
	}

	/**
	 * Initiates combat at melee range. If the speed of this character is greater than that of
	 * the opponent, the follow-up attack will be invoked in the reactFightRanged method
	 * regardless of whether or not the opponent can fight back.
	 * 
	 * @param opp
	 * @return An int representing the outcome of the battle.
	 */
	public int initiateFightMelee(GameCharacter opp) {
		log.log(getName() + " initiates melee combat against " + opp.getName());
		if(!isMelee())
			throw new IllegalArgumentException("Non-ranged character cannot initated ranged combat.");
		if(opp.takeDmg(this))
			return DEFENDER_DEATH;
		else
			return opp.reactFightMelee(this);
	}

	/**
	 * Counterattacks at melee range. If the speed of the enemy character is greater than that of this one,
	 * the enemy's followup also occurs here.
	 * 
	 * @param opp
	 * @return An int representing the outcome of the battle.
	 */
	private int reactFightMelee(GameCharacter opp) {
		if(isMelee()) {
			if(opp.takeDmg(this))
				return ATTACKER_DEATH;
		}
		if(opp.getEffectiveSpd() > getEffectiveSpd()) {
			if(takeDmg(opp))
				return DEFENDER_DEATH;
		}
		else if(opp.getEffectiveSpd() < getEffectiveSpd()) {
			if(isMelee() && opp.takeDmg(this))
				return ATTACKER_DEATH;
		}
		return NO_DEATHS;
	}
	
	public int getHP() {
		return currentStats[HP];
	}
	
	public int getBaseHP() {
		return baseStats[HP];
	}
	
	public int getAtk() {
		return currentStats[ATK];
	}
	
	public int getBaseAtk() {
		return baseStats[ATK];
	}
	
	public int getWeaponAtk() {
		return activeWeapon == null ? 0 : activeWeapon.getBaseDmg();
	}
	
	public int getEffectiveAtk() {
		return getAtk() + getWeaponAtk();
	}
	
	public int getWeaponSpd() {
		return activeWeapon == null ? 0 : activeWeapon.getBaseSpd();
	}
	
	public boolean isWeaponPhys() {
		return activeWeapon == null ? true : activeWeapon.isPhysical();
	}
	
	public int getSpd() {
		return currentStats[SPD];
	}
	
	public int getEffectiveSpd() {
		return getSpd() + getWeaponSpd();
	}
	
	public int getDef() {
		return currentStats[DEF];
	}
	
	public int getRes() {
		return currentStats[RES];
	}
	
	public boolean isFriendly() {
		return side == Alignment.FRIENDLY;
	}
	
	public boolean isEnemy() {
		return side == Alignment.ENEMY;
	}
	
	public boolean isNeutral() {
		return side == Alignment.NEUTRAL;
	}

	public Alignment getSide() {
		return side;
	}

	public void setSide(Alignment side) {
		this.side = side;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public GameCharacterClass getGclass() {
		return gclass;
	}

	public void setGclass(GameCharacterClass gclass) {
		this.gclass = gclass;
	}

	public Weapon getActiveWeapon() {
		return activeWeapon;
	}

	public void setActiveWeapon(Weapon activeWeapon) {
		this.activeWeapon = activeWeapon;
	}

	public int[] getStats() {
		return baseStats;
	}

//	public Skill[] getSkills() {
//		return skills;
//	}

	public Item[] getInventory() {
		return inventory;
	}
	
	public int getMvtPts() {
		return gclass.getMvtPoints();
	}
	
	public double[] getLvlUpRates() {
		return gclass.getLevelUpRates();
	}
	
	/**
	 * Returns a long newline separated string representing this character's statistics.
	 * @return
	 */
	public String save() {
		log.log("Serializing " + getName());
		StringBuilder sb = new StringBuilder();
		sb.append("LEVEL:" + level + "\n");
		sb.append("EXP:" + exp + "\n");
		for(int i = 0; i < STAT_NAMES.length; i++) {
			sb.append(STAT_NAMES[i] + ":" + baseStats[i] + "\n");
		}
		return sb.toString();
	}

}
