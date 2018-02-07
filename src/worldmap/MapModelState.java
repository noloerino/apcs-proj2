package worldmap;

import core.*;
import ui.MapPanel;

import java.awt.Graphics;

public class MapModelState {
	
	private MapConfiguration config;
	
	public MapModelState(MapConfiguration initConfig) {
		config = initConfig;
	}
	
	public MapConfiguration getConfig() {
		return config;
	}
	
	public void paint(MapPanel caller, Graphics g) {
		config.paintAll(caller, g);
	}

	/**
	 * Returns whether or not the given coordinate represents a tile on this configuration.
	 * 
	 * @param v A coordinate pair.
	 * @return Whether or not there is a thing there.
	 */
	public boolean isTileOnMap(IntVector2D v) {
		return config.isTileOnMap(v);
	}
	
	/**
	 * Returns whether or not the given coordinate represents a tile on this configuration.
	 * 
	 * @param x An x-coordinate.
	 * @param y A y-coordinate.
	 * @return Whether or not there is a thing there.
	 */
	public boolean isTileOnMap(int x, int y) {
		return config.isTileOnMap(x, y);
	}
	
	/**
	 * Returns the MapEntity object at the given coordinates.
	 * Returns null if there is no such object.
	 * 
	 * @param v A coordinate pair.
	 * @return A MapEntity.
	 */
	public MapEntity getTileAt(IntVector2D v) {
		return config.getTileAt(v);
	}
	
	/**
	 * Returns the MapEntity object at the given coordinates.
	 * Returns null if there is no such object.
	 * 
	 * @param x An x-coordinate.
	 * @param y A y-coordinate.
	 * @return A MapEntity.
	 */
	public MapEntity getTileAt(int x, int y) {
		return config.getTileAt(x, y);
	}
	
	public Alignment getSideToMove() {
		return config.getSideToMove();
	}
	
}
