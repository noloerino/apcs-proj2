package worldmap;

import java.util.Map;
import java.util.function.BiFunction;
import core.*;

public class Floor extends MapTerrain {
	
	public final static char FLOOR_SYMBOL = '_';
	
	public Floor(IntVector2D currentCoord) {
		super(DEFAULT_DEBUFFS, DEFAULT_BUFFS, 1, FLOOR_SYMBOL, "res/terrain/floor.gif", true, Alignment.NEUTRAL, currentCoord,
				"Floor", "A simple piece of floor. You can walk all over it.");
	}
	
	public Floor(IntVector2D currentCoord, char variant) {
		super(DEFAULT_DEBUFFS, DEFAULT_BUFFS, 1, FLOOR_SYMBOL, variant, "floor", true, Alignment.NEUTRAL, currentCoord,
				"Floor", "A simple piece of floor. You can walk all over it.");
	}
	
	protected Floor() {
		super(FLOOR_SYMBOL);
	}
	
	public static Floor create(int x, int y) {
		return new Floor(new IntVector2D(x, y));
	}
	
	public static Floor create(IntVector2D pos, char variant) {
		return new Floor(pos, variant);
	}

	@Override
	protected void addSymbolToMaps(Map<Character, BiFunction<Integer, Integer, MapEntity>> symbolMap,
			Map<Character, BiFunction<IntVector2D, Character, MapEntity>> variantMap) {
		symbolMap.put(FLOOR_SYMBOL, Floor :: create);
		variantMap.put(FLOOR_SYMBOL, Floor :: create);
	}
	
}
