package worldmap;

import core.*;
import characterbuilder.GameCharacter;
import control.DebugLogger;
import ui.*;

import java.util.Map;
import java.util.function.BiFunction;
import java.awt.Graphics;
import java.io.File;
import java.util.HashMap;
import javax.swing.ImageIcon;

public abstract class MapEntity extends Entity {

	private char mapSymbol;
	private ImageIcon mapSprite;
	private boolean passable;
	private Alignment side;
	private IntVector2D currentCoord;
	private MapCharacter occupying;
	private char variant;
	
	public final static int SIZE = 72;
	
	/**
	 * Maps a character identifying the type of map tile to be created.
	 */
	private static Map<Character, BiFunction<Integer, Integer, MapEntity>> symbolMap 
			= new HashMap<>();
	
	/**
	 * Maps a character to a creation function for a specified variant.
	 * Unlike the symbolMap specified above, this takes an {@link IntVector2D}
	 * in order to satisfy to BiFunction stuff.
	 */
	private static Map<Character, BiFunction<IntVector2D, Character, MapEntity>> variantMap
			= new HashMap<>();
	
	public static DebugLogger log = new DebugLogger("MapEntity");
	
	protected MapEntity(char mapSymbol) {
		super(mapSymbol + " initializer", "MISSING", null);
		addSymbolToMaps(symbolMap, variantMap);
	}
	
	/**
	 * Creates a new instance of this map entity.
	 * 
	 * @param mapSymbol The plaintext symbol representing this map element.
	 * @param mapSpritePath The path to the image of this map element.
	 * @param passable Whether or not this entity may be passed.
	 * @param side The alignment of this entity.
	 * @param currentCoord The coordinates of this entity.
	 * @param name The name of this entity.
	 * @param description A description of this entity.
	 */
	public MapEntity(char mapSymbol, String mapSpritePath, boolean passable, Alignment side, IntVector2D currentCoord,
			String name, String description) {
		super(name, description, mapSpritePath);
		this.mapSymbol = mapSymbol;
		this.mapSprite = new ImageIcon(mapSpritePath);
		this.passable = passable;
		this.side = side;
		this.currentCoord = currentCoord;
		this.variant = 0;
	}
	
	/**
	 * Creates a new instance of this map entity with a given variant.
	 * If an image of this variant cannot be found, then an attempt will be made
	 * to use a variantless version of the image. For example, if "symbol_c.gif"
	 * cannot be found, then "symbol.gif" will be used instead. If that file doesn't
	 * exist, then it will simply use the missing image icon.
	 * 
	 * @param mapSymbol The plaintext symbol representing this map element.
	 * @param variant The symbol representing the variant of this tile.
	 * @param folder The folder the tile belongs to. Should be specified by some superclass.
	 * @param fileName The name of the thing.
	 * @param passable Whether or not the entity can be passed through.
	 * @param side The alignment of the entity.
	 * @param currentCoord The location of the entity.
	 * @param name The name of the entity.
	 * @param description A description of this entity.
	 */
	public MapEntity(char mapSymbol, char variant, String folder, String fileName, boolean passable, Alignment side,
			IntVector2D currentCoord, String name, String description) {
		this(mapSymbol, figureOutPath(folder, fileName, variant), passable, side, currentCoord, name, description);
		this.variant = variant;
	}
	
	/**
	 * Attempts to find a valid .gif file matching the given parameters.
	 * 
	 * @param folder The directory to search.
	 * @param fileName The name of the entity.
	 * @param variant The theme variant of the entity.
	 * @return A string that should represent a valid path. If the desired resource cannot be
	 * found, then returns "res/missing.gif".
	 */
	private static String figureOutPath(String folder, String fileName, char variant) {
		String testPath = folder + fileName + "_" + variant + ".gif";
		String testPath2 = folder + fileName + ".gif";
		if(new File(testPath).isFile())
			return testPath;
		else if(new File(testPath2).isFile())
			return testPath2;
		else
			return "res/missing.gif";
	}
	
	/**
	 * Instantiates the maps of the .create method in each class.
	 */
	public static void _setup() {
		new MapNullTerrain();
		new Floor();
		new Wall();
	}
	
	/**
	 * Creates a MapEntity object at a given coordinate based on a first character identifying
	 * the class, followed by a second character identifying its variant.
	 * 
	 * Not to be confused with the static create method which should be used in each subclass.
	 * @param terrainSymbol The symbol of the object to be created.
	 * @param x
	 * @param y
	 * @return The created MapEntity, or null if the operation failed.
	 */
	public static MapEntity create(char terrainSymbol, char variantSymbol, int x, int y) {
		BiFunction<Integer, Integer, MapEntity> noVariantCreator = null;
		BiFunction<IntVector2D, Character, MapEntity> variantCreator = null;
		try {
			if(variantSymbol == ' ') {
				noVariantCreator = symbolMap.get(terrainSymbol);
				if(noVariantCreator == null) {
					log.add("Attempt to create MapEntity from symbol <" + terrainSymbol + "> failed. Replacing with default terrain.");
					return symbolMap.get('N').apply(x, y);
				}
				else {
					MapEntity e = noVariantCreator.apply(x, y);
					if(e.getSpriteWidth() != e.getSpriteHeight() || e.getSpriteHeight() != SIZE) {
						log.add("MapEntity from symbol <" + terrainSymbol +"> did not have the correct dimensions."
								+ "Replacing with default terrain.");
						return symbolMap.get('N').apply(x, y);
					}
					log.add("Successfully created <" + e.getNameWithVariant() + "> at (" + e.getX() + ", " + e.getY() + ")");
					return e;
				}
			}
			else {
				variantCreator = variantMap.get(terrainSymbol);
				if(variantCreator == null) {
					log.add("Attempt to create MapEntity from symbol <" + terrainSymbol + "> and variant <"
							+ variantSymbol + "> failed. Replacing with default terrain.");
					return symbolMap.get('N').apply(x, y);
				}
				else {
					MapEntity e = variantCreator.apply(new IntVector2D(x, y), variantSymbol);
					if(e.getSpriteWidth() != e.getSpriteHeight() || e.getSpriteHeight() != SIZE) {
						log.add("MapEntity from symbol <" + terrainSymbol +"> and variant <"
								+ variantSymbol + "> did not have the correct dimensions. Replacing with default terrain.");
						return symbolMap.get('N').apply(x, y);
					}
					log.add("Successfully created <" + e.getNameWithVariant() + "> at (" + e.getX() + ", " + e.getY() + ")");
					return e;
				}
			}
		}
		catch(NullPointerException e) {
			log.add(e.getMessage());
			return null;
		}
	}
	
	public void paintOnMap(MapPanel caller, Graphics g) {
		double mapScale = caller.getMapViewState().getMapScale();
		IntVector2D mapDisp = caller.getMapViewState().getMapViewDisp();
		MapViewState.getScaledImage(mapSprite, (int) (getSpriteWidth() * mapScale), (int) (getSpriteHeight() * mapScale))
			.paintIcon(caller, g, getPaintX(mapScale, mapDisp), getPaintY(mapScale, mapDisp));
	}
	
	public void paintOccupying(MapPanel caller, Graphics g) {
		if(occupying != null)
			occupying.paintRelativeToMapEntity(this, caller, g);
	}
	
	public static int getCoordDisp() {
		return 4;
	}
	
	public static int getCoordAdjustment() {
		return SIZE - getCoordDisp();
	}
	
	public IntVector2D getPaintPos(double mapScale, IntVector2D mapDisp) {
		return new IntVector2D(getPaintX(mapScale, mapDisp), getPaintY(mapScale, mapDisp));
	}
	
	public int getPaintX(double mapScale, IntVector2D mapDisp) {
		return (int) ((getX() * getCoordAdjustment() - mapDisp.getX()) * mapScale);
	}
	
	public int getPaintY(double mapScale, IntVector2D mapDisp) {
		return (int) ((getY() * getCoordAdjustment() - mapDisp.getY()) * mapScale);
	}
	
	public int getSpriteWidth() {
		return SIZE;
	}
	
	public int getSpriteHeight() {
		return SIZE;
	}
	
	public String getNameWithVariant() {
		if(getVariant() == 0)
			return super.getName();
		else
			return super.getName() + " (variant " + getVariant() + ")";
	}
		
	public char getVariant() {
		return variant;
	}
	
	public MapCharacter getOccupying() {
		return occupying;
	}
	
	public void setOccupying(GameCharacter c) {
		this.occupying = new MapCharacter(c, this);
	}
	
	public void setOccupying(MapCharacter c) {
		this.occupying = c;
	}
	
	public void removeOccupying() {
		this.occupying = null;
	}
	
	public boolean isOccupied() {
		return this.occupying != null;
	}
	
	public char getMapSymbol() {
		return mapSymbol;
	}

	/**
	 * Adds the class's creation function to a map. An inelegant solution that allows
	 * MapEntity.create() to be a little less clunky.
	 */
	protected abstract void addSymbolToMaps(Map<Character, BiFunction<Integer, Integer, MapEntity>> symbolMap,
			Map<Character, BiFunction<IntVector2D, Character, MapEntity>> variantMap);
	
	public boolean isPassable() {
		return passable;
	}
	
	/**
	 * Super hacky fix for overzealous abstraction.
	 * 
	 * @return If this is an instance of MapTerrain, returns its movement penalty. If it is not,
	 * returns 0.
	 */
	public int getMvtPenalty() {
		if(this instanceof MapTerrain)
			return ((MapTerrain) this).getMvtPenalty();
		else
			return 0;
	}
	
	public IntVector2D getPos() {
		return currentCoord;
	}
	
	public void setPos(IntVector2D pos) {
		this.currentCoord = pos;
	}
	
	public int getX() {
		return getPos().getX();
	}
	
	public void setX(int x) {
		getPos().setX(x);
	}
	
	public int getY() {
		return getPos().getY();
	}
	
	public void setY(int y) {
		getPos().setY(y);
	}
	
	public boolean isFriendly() {
		return side == Alignment.FRIENDLY;
	}
	
	public boolean isEnemy() {
		return side == Alignment.ENEMY;
	}
	
	public ImageIcon getMapSprite() {
		return mapSprite;
	}
	
}
