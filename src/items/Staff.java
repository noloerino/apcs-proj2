package items;

public abstract class Staff extends Item {

	private int baseHeal;
	
	public Staff(int baseHeal, int value, String invSpritePath, String name, String description) {
		super(value, invSpritePath, name, description);
		this.baseHeal = baseHeal;
	}

	public int getBaseHeal() {
		return baseHeal;
	}
	
	public void setBaseHeal(int baseHeal) {
		this.baseHeal = baseHeal;
	}
	
}
