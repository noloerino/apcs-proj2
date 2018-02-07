package worldmap;

import core.Alignment;
import core.IntVector2D;

import java.util.Map;
import java.util.function.BiFunction;

public class MapNullTerrain extends MapEntity {

	public final static char NULL_TILE_SYMBOL = 'N';
	
	public MapNullTerrain(IntVector2D currentCoord) {
		super(NULL_TILE_SYMBOL, "res/default.gif", false, Alignment.NEUTRAL, currentCoord,
				"NULL TILE", "A nameless tile whose existence means something is terribly wrong.");
	}
	
	protected MapNullTerrain() {
		super(NULL_TILE_SYMBOL);
	}

	private static MapNullTerrain create(int x, int y) {
		return new MapNullTerrain(new IntVector2D(x, y));
	}
	
	@Override
	public void addSymbolToMaps(Map<Character, BiFunction<Integer, Integer, MapEntity>> symbolMap,
			Map<Character, BiFunction<IntVector2D, Character, MapEntity>> variantMap) {
		symbolMap.put(NULL_TILE_SYMBOL, MapNullTerrain :: create);
	}

}
