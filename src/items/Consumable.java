package items;

public abstract class Consumable extends Item {

	public final int MAX_USES;
	private int usesRemaining;
	
	public Consumable(int maxUses, int value, String invSpritePath, String name, String description) {
		super(value, invSpritePath, name, description);
		this.MAX_USES = maxUses;
		usesRemaining = maxUses;
	}
	
	public void use() {
		if(usesRemaining > 0) {
			useCharge();
			usesRemaining--;
		}
	}

	/**
	 * Describes only what happens upon consuming a charge - decrementing the use count
	 * is already done in the use method.
	 */
	public abstract void useCharge();
	
	public void refill(int charges) {
		usesRemaining += charges;
		if(charges > MAX_USES)
			usesRemaining = MAX_USES;
	}
	
	public void refill() {
		usesRemaining = MAX_USES;
	}
	
	public int getUsesRemaining() {
		return usesRemaining;
	}
	
	public void setUsesRemaining(int usesRemaining) {
		this.usesRemaining = usesRemaining;
	}
	
}
