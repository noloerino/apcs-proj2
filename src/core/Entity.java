package core;

import javax.swing.ImageIcon;

public abstract class Entity {

	private String name;
	private String description;
	private ImageIcon defaultSprite;
	private final static ImageIcon SPRITE_MISSING = new ImageIcon("res/missing.gif");
	
	public Entity(String name, String description, String imgPath) {
		this.name = name;
		this.description = description;
		try {
			this.defaultSprite = new ImageIcon(imgPath);
		}
		catch(NullPointerException e) {
			defaultSprite = SPRITE_MISSING;
		}
	}
	
	protected Entity(String name, String description, String folder, String fileName, char variant) {
		this.name = name;
		this.description = description;
		try {
			this.defaultSprite = new ImageIcon(folder + fileName + "_" + variant + ".gif");
		}
		catch(NullPointerException e) {
			try {
				this.defaultSprite = new ImageIcon(folder + fileName + ".gif");
			}
			catch(NullPointerException f) {
				defaultSprite = SPRITE_MISSING;
			}
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public ImageIcon getDefaultSprite() {
		return defaultSprite;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setDefaultSpriteByPath(String imgPath) {
		this.defaultSprite = new ImageIcon(imgPath);
	}
	
}
