package weapons;

import items.Item;

public abstract class Weapon extends Item {

	private int baseDmg;
	private int baseSpd;
	private int range;
	private boolean physical;
	
	private final static String WEAPON_DIR = "weapons/";
	
	public Weapon(int value, int baseDmg, int baseSpd, int range, boolean physical,
			String invSpritePath, String name, String description) {
		super(value, WEAPON_DIR + invSpritePath, name, description);
		this.baseDmg = baseDmg;
		this.baseSpd = baseSpd;
		this.range = range;
		this.physical = physical;
	}

	public int getBaseDmg() {
		return baseDmg;
	}

	public int getBaseSpd() {
		return baseSpd;
	}

	public int getRange() {
		return range;
	}

	public boolean isPhysical() {
		return physical;
	}

	public static String getWeaponDir() {
		return WEAPON_DIR;
	}

}
