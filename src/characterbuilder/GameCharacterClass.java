package characterbuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import control.DebugLogger;

public class GameCharacterClass {

	private String name;

	private String classDescription;
	private double[] levelUpRates;
//	private GameCharacterClass[] prestigeOptions;
//	private Map<Skill, Integer> learnableSkills;
	private int mvtPoints;
	
	private boolean ranged;
	private boolean melee;
	
	public static DebugLogger log = new DebugLogger("GameCharacterClass");
	
	private static Map<String, GameCharacterClass> initializedClasses = new HashMap<>();
	
	public final static String[] STAT_NAMES = GameCharacter.STAT_NAMES;
	public final static double[] DEFAULT_RATES = new double[]{0.5, 0.5, 0.5, 0.5, 0.5};
	
	public final static GameCharacterClass DEFAULT_CLASS = new GameCharacterClass();
	
	private final static int DEFAULT_MVT_POINTS = 4;
	private GameCharacterClass() {
		this.name = "Default";
		this.classDescription = "A character that is boring and unoriginal, and not very bravely so at that.";
		this.levelUpRates = Arrays.copyOf(DEFAULT_RATES, DEFAULT_RATES.length);
		this.ranged = false;
		this.melee = true;
		this.mvtPoints = DEFAULT_MVT_POINTS;
	}
	
	public GameCharacterClass(String file) {
		if(initializedClasses.containsKey(file)) {
			copy(initializedClasses.get(file));
			return;
		}
		// default values
		this.name = "Classless";
		this.classDescription = "A character of no class. Disgusting.";
		this.levelUpRates = Arrays.copyOf(DEFAULT_RATES, DEFAULT_RATES.length);
		this.ranged = false;
		this.melee = true;
		this.mvtPoints = DEFAULT_MVT_POINTS;
		try(BufferedReader r = new BufferedReader(new FileReader(getPath(file)))) {
			Map<String, String> map = new HashMap<>();
			String l = r.readLine();
			while(l != null) {
				String[] split = l.split(":");
				if(split.length != 2) {
					log.add("Malformed string in gclass file: " + l);
					l = r.readLine();
					continue;
				}
				map.put(split[0].toUpperCase(), split[1]);
				l = r.readLine();
			}
			
			if(map.containsKey("NAME"))
				this.name = map.get("NAME");
			if(map.containsKey("DESCRIPTION"))
				this.classDescription = map.get("DESCRIPTION");
			if(map.containsKey("RANGED"))
				this.ranged = Boolean.parseBoolean(map.get("RANGED"));
			if(map.containsKey("MELEE"))
				this.melee = Boolean.parseBoolean(map.get("MELEE"));
			for(int i = 0; i < STAT_NAMES.length; i++) {
				String k = "RATE_" + STAT_NAMES[i];
				if(map.containsKey(k))
					DEFAULT_RATES[i] = Double.parseDouble(map.get(k));
			}
			if(map.containsKey("MVT_POINTS")) {
				this.mvtPoints = Integer.parseInt(map.get("MVT_POINTS"));
			}
			initializedClasses.put(file, this);
		}
		catch(IOException e) {
			log.add("Could not find gclass file from file " + getPath(file) + "; using default stats");
		}
	}
	
	private static String getPath(String file) {
		return "res/classes/" + file;
	}
	
	private void copy(GameCharacterClass o) {
		this.name = o.name;
		this.classDescription = o.classDescription;
		this.levelUpRates = Arrays.copyOf(o.levelUpRates, o.levelUpRates.length);
		this.mvtPoints = o.mvtPoints;
		this.ranged = o.ranged;
		this.melee = o.melee;
	}
	
	public String getName() {
		return name;
	}
	
	public GameCharacterClass(String name, String classDescription, double[] levelUpRates, int mvtPoints,
			boolean ranged, boolean melee) {
		this.name = name;
		this.classDescription = classDescription;
		this.levelUpRates = levelUpRates;
		this.mvtPoints = mvtPoints;
		this.ranged = ranged;
		this.melee = melee;
		if(!initializedClasses.containsKey(name))
			initializedClasses.put(name, this);
	}
	
	public String getClassDescription() {
		return classDescription;
	}

	public double[] getLevelUpRates() {
		return levelUpRates;
	}

	public int getMvtPoints() {
		return mvtPoints;
	}

	public boolean isRanged() {
		return ranged;
	}

	public boolean isMelee() {
		return melee;
	}
	
	public static GameCharacterClass create(String file) {
		return new GameCharacterClass(file);
	}
	
}
