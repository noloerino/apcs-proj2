package weapons;

public class Sword extends Weapon {

	public Sword(int value, int baseDmg, int baseSpd, boolean physical, String invSpritePath,
			String name, String description) {
		super(value, baseDmg, baseSpd, 1, physical, invSpritePath, name, description);
	}

}
