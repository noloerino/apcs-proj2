package worldmap;

import core.*;

import java.util.Map;
import java.util.function.BiFunction;

public class Wall extends MapTerrain {
	
	public final static char WALL_SYMBOL = '#';

	public Wall(IntVector2D currentCoord) {
		super(DEFAULT_DEBUFFS, DEFAULT_BUFFS, 100, WALL_SYMBOL, "res/terrain/wall.gif", false, Alignment.NEUTRAL, currentCoord,
				"Wall", "A wall. Do not attempt to traverse, at risk of collision.");
		}
	
	public Wall(IntVector2D currentCoord, char variant) {
		super(DEFAULT_DEBUFFS, DEFAULT_BUFFS, 100, WALL_SYMBOL, variant, "wall", false, Alignment.NEUTRAL, currentCoord,
				"Wall", "A wall. Do not attempt to traverse, at risk of collision.");
	}
	
	protected Wall() {
		super(WALL_SYMBOL);
	}
	
	public static Wall create(int x, int y) {
		return new Wall(new IntVector2D(x, y));
	}
	
	public static Wall create(IntVector2D pos, char variant) {
		return new Wall(pos, variant);
	}

	@Override
	protected void addSymbolToMaps(Map<Character, BiFunction<Integer, Integer, MapEntity>> symbolMap,
			Map<Character, BiFunction<IntVector2D, Character, MapEntity>> variantMap) {
		symbolMap.put(WALL_SYMBOL, Wall :: create);
		variantMap.put(WALL_SYMBOL, Wall :: create);
	}


	
}
