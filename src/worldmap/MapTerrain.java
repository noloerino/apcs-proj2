package worldmap;

import core.*;
import ui.MapPanel;

import java.awt.Graphics;

public abstract class MapTerrain extends MapEntity {

	public final static int[] DEFAULT_DEBUFFS = new int[9];
	public final static int[] DEFAULT_BUFFS = new int[9];
	
	public final static String PATH = "res/terrain/";
	
	private int[] debuffs;
	private int[] buffs;
	private int mvtPenalty;
		
	public MapTerrain(int[] debuffs, int[] buffs, int mvtPenalty,
			char mapSymbol, String mapSpritePath, boolean passable, Alignment side, IntVector2D currentCoord,
			String name, String description) {
		super(mapSymbol, mapSpritePath, passable, side, currentCoord, name, description);
		this.debuffs = debuffs;
		this.buffs = buffs;
		this.mvtPenalty = mvtPenalty;
	}
	
	public MapTerrain(int[] debuffs, int[] buffs, int mvtPenalty, char mapSymbol, char variant,
			String fileName, boolean passable, Alignment side, IntVector2D currentCoord,
			String name, String description) {
		super(mapSymbol, variant, PATH, fileName, passable, side, currentCoord, name, description);
		this.debuffs = debuffs;
		this.buffs = buffs;
		this.mvtPenalty = mvtPenalty;
	}
	
	protected MapTerrain(char mapSymbol) {
		super(mapSymbol);
	}
	
	@Override
	public void paintOnMap(MapPanel caller, Graphics g) {
		super.paintOnMap(caller, g);
	}
	
	public int[] getDebuffs() {
		return debuffs;
	}
	
	public int[] getBuffs() {
		return buffs;
	}
	
	public int getMvtPenalty() {
		return mvtPenalty;
	}
	
	public void setMvtPenalty(int mvtPenalty) {
		this.mvtPenalty = mvtPenalty;
	}

}
