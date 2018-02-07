package items;

import core.Entity;

import javax.swing.ImageIcon;

public abstract class Item extends Entity {

	private ImageIcon inventorySprite;
	private int value;
	
	public final static String ITEM_DIR = "res/items/";
	
	public Item(int value, String invSpritePath, String name, String description) {
		super(name, description, ITEM_DIR + invSpritePath);
		this.value = value;
		// TODO for this and all other sprites: add a default image as a fallback
		this.inventorySprite = new ImageIcon(ITEM_DIR + invSpritePath);
	}
		
	public int getValue() {
		return value;
	}
	
	public ImageIcon getInventorySprite() {
		return inventorySprite;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public void setInventorySpriteByPath(String imgPath) {
		this.inventorySprite = new ImageIcon(imgPath);
	}

}
