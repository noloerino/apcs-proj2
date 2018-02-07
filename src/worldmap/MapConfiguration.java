package worldmap;

import ui.*;
import characterbuilder.GameCharacter;
import characterbuilder.MoveGhost;
import control.DebugLogger;
import control.MapViewListener;
import core.*;
import core.animations.*;
import core.exceptions.*;

import java.awt.Color;
import java.awt.Graphics;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MapConfiguration {
	
	private List<MapEntity[]> tiles;
	private String name;
	private MapViewState s;
	private int height;
	private int width;
	private int xTiles;
	private int yTiles;
	private String configFile;
	
	private int turnNumber;
	public int getTurnNumber() {
		return turnNumber;
	}
	private Alignment toMove;
	
	private Pathfinder pf;
	public Pathfinder getPF() {
		return pf;
	}
	
	private String saveName;
	
	public static DebugLogger log = new DebugLogger("MapConfiguration");
	
	public MapConfiguration(String mapFolder, MapViewState s, String saveName) {
		/*
		 * 3 things need to be done here:
		 * 1) initialize terrain map and variants simultaneously to make sure the paths are correct
		 * 2) initialize characters, according to either some file or a class
		 */
		this.saveName = saveName;
		this.name = mapFolder;
		mapFolder = "res/stageMaps/" + mapFolder;
		this.configFile = mapFolder + (mapFolder.endsWith("/") ? "" : "/");
		File fTerr = new File(getTerrainFilePath());
		File fVar = new File(getVariantFilePath());
		File fStart = new File(getStartsFilePath());
		this.s = s;
		
		Map<Character, GameCharacter> units = initUnits(configFile + "units.txt", saveName);
		
		try {
			BufferedReader rTerr = new BufferedReader(new FileReader(fTerr));
			BufferedReader rVar = new BufferedReader(new FileReader(fVar));
			BufferedReader rStart = new BufferedReader(new FileReader(fStart));
			
			tiles = new ArrayList<MapEntity[]>();
			String lTerr = rTerr.readLine();
			String lVar = rVar.readLine();
			String lStart = rStart.readLine();
			
			int y = 0;
			int totalHeight = 0;
			int tilesInY = 0;
			int tilesInX = 0;
			while(lTerr != null) {
				if(lVar == null)
					lVar = "";
				if(lStart == null)
					lStart = "";
				MapEntity[] thisRow = new MapEntity[lTerr.length()];
				tilesInX = 0;
				tiles.add(thisRow);
				int lineWidth = 0;
				int lineTiles = 0;
				char terrainMark;
				char unitMark;
				for(int x = 0; x < thisRow.length; x++) {
					if(x >= lVar.length())
						terrainMark = ' ';
					else
						terrainMark = lVar.charAt(x);
					if(x >= lStart.length())
						unitMark = '#';
					else
						unitMark = lStart.charAt(x);
					thisRow[x] = MapEntity.create(lTerr.charAt(x), terrainMark, x, y);
					if(unitMark != '#') {
						thisRow[x].setOccupying(units.get(unitMark));
						if(thisRow[x].getOccupying().getSide() == Alignment.FRIENDLY) {
							friendlies.add(thisRow[x].getOccupying());
							if(thisRow[x].getOccupying().getEnclosed().isMissionCritical())
								friendlyMissionCriticals.add(thisRow[x].getOccupying());
						}
						else if(thisRow[x].getOccupying().getSide() == Alignment.ENEMY)
							enemies.add(thisRow[x].getOccupying());
						log.log("Placing character <" + thisRow[x].getOccupying().getName() + "> at (" + x + ", " + y + ")");
					}
					
					lineWidth += thisRow[x].getSpriteWidth();
					tilesInX++;
				}
				
				if(thisRow.length > 0)
					totalHeight += thisRow[0].getSpriteHeight();
				y++;
				tilesInY++;
				if(lineWidth > width) {
					width = lineWidth;
					tilesInX = lineTiles;
				}
				
				lTerr = rTerr.readLine();
				lVar = rVar.readLine();
				lStart = rStart.readLine();
			}
			xTiles = tilesInX;
			yTiles = tilesInY;
			height = totalHeight;
			
			rTerr.close();
			rVar.close();
			rStart.close();
		}
		catch (IOException e) {
			log.add(e.getMessage());
		}
		finally {
			log.log("Map <" + name + "> initialized with dimensions (" + width + ", " + height + ")");
			pf = new Pathfinder(this);
			turnNumber = 0;
			toMove = Alignment.FRIENDLY;
			startTurn(null);
		}
	}
	
	public void setActivePath(List<IntVector2D> activePath) {
		this.activePath = activePath;
	}
	
	private List<MapCharacter> friendlies = new ArrayList<>();
	public List<MapCharacter> getFriendlies() {
		return friendlies;
	}
	private List<MapCharacter> friendlyMissionCriticals = new ArrayList<>();
	
	private List<MapCharacter> enemies = new ArrayList<>();
	public List<MapCharacter> getEnemies() {
		return enemies;
	}
	
	private void removeUnit(MapCharacter mc, IntVector2D pos) {
		if(mc.getSide() == Alignment.ENEMY) {
			shownEnemies.remove(pos);
			enemies.remove(mc);
		}
		else if(mc.getSide() == Alignment.FRIENDLY)
			friendlies.remove(mc);
		else {
			log.log("Attempted to remove a character that is neither enemy nor friendly.");
			return;
		}
		for(int i = 0; i < tiles.size(); i++) {
			for(int j = 0; j < tiles.get(i).length; j++) {
				if(getUnitAt(j, i) == mc)
					getTileAt(j, i).removeOccupying();
			}
		}
	}
	private static Map<Character, GameCharacter> initUnits(String path, String saveName) {
		Map<Character, GameCharacter> map = new HashMap<>();
		try(BufferedReader r = new BufferedReader(new FileReader(path))) {
			String l = r.readLine();
			while(l != null) {
				String[] split = l.split(":");
				if(split.length != 2) {
					log.add("Malformed string in units file <" + l + ">");
					l = r.readLine();
					continue;
				}
				map.put(split[0].charAt(0), GameCharacter.create(split[1], split[1], saveName));
				l = r.readLine();
			}			
		}
		catch(IOException e) {
			log.add("Could not find units file from file <" + path + ">");
		}
		
		return map;
	}
	
	private List<IntVector2D> activePath = new LinkedList<>();
	public List<IntVector2D> getActivePath() {
		return activePath;
	}
	public IntVector2D getEndOfPath() {
		if(activePath == null || activePath.size() == 0)
			return null;
		else
			return activePath.get(activePath.size() - 1);
	}
	
	/**
	 * Sends the game into the status of prompting for a move.
	 * 
	 * @param caller
	 * @param g
	 * @return false if the current active path is invalid.
	 */
	public boolean initiateMovePrompt(MapPanel caller) {
		if(activePath == null || !pf.isValidPath(activePath))
			return false;
		log.log("Now prompting player for move.");
		MapViewListener l = caller.getMapViewListener();
		l.setStatus(GameState.PLAYER_CONFIRMING_MOVE);
		attackableFromHere = canAttackFromDestination();
		withinRange = findSquaresWithinAtkRange();
		l.setMoveMenu(caller, attackableFromHere.size() != 0);
		return true;
	}
	
	/**
	 * A set of all squares that contain units that can be attacked.
	 */
	private Set<IntVector2D> attackableFromHere;
	
	/**
	 * A set of all squares within the examined unit's attacking range; excluding
	 * those in attackableFromHere.
	 */
	private Set<IntVector2D> withinRange;
	
	/**
	 * Checks whether or not there are units that can be attacked from the
	 * destination square of activePath.
	 * 
	 * @return
	 */
	private Set<IntVector2D> canAttackFromDestination() {
		if(activePath == null || !pf.isValidPath(activePath))
			return new HashSet<>();
		GameCharacter gc = getUnitAt(activePath.get(0)).getEnclosed();
		boolean ranged = gc.isRanged();
		boolean melee = gc.isMelee();
		Alignment atkSide = gc.getSide();
		Set<IntVector2D> attackable = new HashSet<>();
		int x = activePath.get(activePath.size() - 1).getX();
		int y = activePath.get(activePath.size() - 1).getY();
		if(ranged) {
			List<IntVector2D> arr = new LinkedList<>();
			arr.add(new IntVector2D(x + 2, y));
			arr.add(new IntVector2D(x - 2, y));
			arr.add(new IntVector2D(x, y + 2));
			arr.add(new IntVector2D(x, y - 2));
			arr.add(new IntVector2D(x + 1, y + 1));
			arr.add(new IntVector2D(x - 1, y + 1));
			arr.add(new IntVector2D(x + 1, y - 1));
			arr.add(new IntVector2D(x - 1, y - 1));
			attackable.addAll(arr.stream()
					.filter((vec) -> (
							isTileOnMap(vec) // not sure why this is necessary
							&& getTileAt(vec).isOccupied()
							&& Alignment.areOpposed(atkSide, getUnitAt(vec).getSide())))
					.collect(Collectors.toList()));
		}
		if(melee) {
			List<IntVector2D> arr = new LinkedList<>();
			arr.add(new IntVector2D(x + 1, y));
			arr.add(new IntVector2D(x - 1, y));
			arr.add(new IntVector2D(x, y + 1));
			arr.add(new IntVector2D(x, y - 1));
			attackable.addAll(arr.stream()
					.filter((vec) -> (
							isTileOnMap(vec) // not sure why this is necessary
							&& getTileAt(vec).isOccupied()
							&& Alignment.areOpposed(atkSide, getUnitAt(vec).getSide())))
					.collect(Collectors.toList()));
		}
		return attackable;
	}
	
	private Set<IntVector2D> findSquaresWithinAtkRange() {
		if(activePath == null || !pf.isValidPath(activePath))
			return new HashSet<>();
		GameCharacter gc = getUnitAt(activePath.get(0)).getEnclosed();
		boolean ranged = gc.isRanged();
		boolean melee = gc.isMelee();
		Set<IntVector2D> attackable = new HashSet<>();
		int x = activePath.get(activePath.size() - 1).getX();
		int y = activePath.get(activePath.size() - 1).getY();
		if(ranged) {
			List<IntVector2D> arr = new LinkedList<>();
			arr.add(new IntVector2D(x + 2, y));
			arr.add(new IntVector2D(x - 2, y));
			arr.add(new IntVector2D(x, y + 2));
			arr.add(new IntVector2D(x, y - 2));
			arr.add(new IntVector2D(x + 1, y + 1));
			arr.add(new IntVector2D(x - 1, y + 1));
			arr.add(new IntVector2D(x + 1, y - 1));
			arr.add(new IntVector2D(x - 1, y - 1));
			attackable.addAll(arr.stream()
					.filter((vec) -> (isTileOnMap(vec) && getTileAt(vec).isPassable()))
					.collect(Collectors.toList()));
		}
		if(melee) {
			List<IntVector2D> arr = new LinkedList<>();
			arr.add(new IntVector2D(x + 1, y));
			arr.add(new IntVector2D(x - 1, y));
			arr.add(new IntVector2D(x, y + 1));
			arr.add(new IntVector2D(x, y - 1));
			attackable.addAll(arr.stream()
					.filter((vec) -> (isTileOnMap(vec) && getTileAt(vec).isPassable()))
					.collect(Collectors.toList()));
		}
		return attackable;
	}
	
	/**
	 * Attempts to make a move. Returns true if a move was made.
	 * If the game status is confirming attack, then checks are outsourced to that.
	 * 
	 * @return
	 */
	public boolean attemptMove(MapPanel caller) {
		GameState status = caller.getGameStatus();
		if(activePath == null || activePath.size() == 0) {
			log.log("Move not attempted.");
			return false;
		}
		else if(status == GameState.PLAYER_CONFIRMING_ATTACK) {
			return attemptAttack(caller, s.getSelected());
		}
		else if(status == GameState.PLAYER_CONFIRMING_MOVE) {
			makeMove(caller);
			return true;
		}
		return false;
	}
	
	private boolean attemptAttack(MapPanel caller, IntVector2D v) {
		if(!isValidAttack(v))
			return false;
		makeMove(caller, v);
		return true;
	}
	
	private boolean isValidAttack(IntVector2D v) {
		if(attackableFromHere == null || attackableFromHere.size() == 0
				&& !attackableFromHere.contains(v))
			return false;
		MapCharacter start = getUnitAt(activePath.get(0));
		MapCharacter end = getUnitAt(v);
		return end != null && start.getSide() != end.getSide();
	}
	
	private IntVector2D lastDef;
	
	
	private boolean atk_animating;
	private int result;
	public void makeMove(MapPanel caller, IntVector2D def) {
		MapCharacter mc = getUnitAt(activePath.get(0));
		lastDef = def;
		if(mc != null) { // If mc == null, then the moving character died (usually after making an attack)
			if(caller.getGameStatus() != GameState.MOVE_ANIMATION) {
				mc.getEnclosed().runMvAnimation(caller, activePath, this);
			}
			else if(caller.getGameStatus() == GameState.MOVE_ANIMATION) {
				Animation panim = mc.getEnclosed().getActiveMapAnimation();
				if(panim instanceof SequentialAnimation
						&& !((SequentialAnimation) panim).isFinished())
					return;
				if(def != null && !atk_animating) {
					result = combat(caller, activePath.get(0), def);
					return;
				}
				atk_animating = false;
				mc.afterMoved(caller);
				resolveCombat(activePath.get(0), def, result);
				result = 0;
				getTileAt(activePath.get(0)).removeOccupying();
				if(enemies.contains(mc) || friendlies.contains(mc))
					getTileAt(activePath.get(activePath.size() - 1)).setOccupying(mc);
				s.unexamine(caller);
				log.log("Confirmed move from " + activePath.get(0) + " to " + activePath.get(activePath.size() - 1));
			}
		}
		if(caller.getGameStatus() != GameState.MOVE_ANIMATION) {
			recalculateRed();
			checkGameEndState();
			if(allUnitsHaveMoved(toMove))
				endTurn(caller);
			activePath.clear();
		}
	}
	
	public void makeMove(MapPanel caller) {
		makeMove(caller, null);
	}
	
	/**
	 * Puts two characters at combat. Note that attacking is the original location of
	 * the attacking character, not the location after moving.
	 * 
	 * @param attacking
	 * @param defending
	 */
	private int combat(MapPanel caller, IntVector2D attacking, IntVector2D defending) {
		GameCharacter c1 = getUnitAt(attacking).getEnclosed();
		GameCharacter c2 = getUnitAt(defending).getEnclosed();
		int result;
		if(getEndOfPath().isAdjacentTo(defending))
			result = c1.initiateFightMelee(c2);
		else
			result = c1.initiateFightRanged(c2);
		atk_animating = true;
		if(c1.isFriendly() && getSideToMove() == Alignment.FRIENDLY) {
			if(result == GameCharacter.DEFENDER_DEATH)
				c1.giveXP(30);
			else
				c1.giveXP(10);
		}
		else if(c2.isFriendly() && getSideToMove() == Alignment.ENEMY) {
			if(result == GameCharacter.ATTACKER_DEATH)
				c2.giveXP(30);
			else
				c2.giveXP(10);
		}
		c1.runAtkAnimation(caller, getEndOfPath(), defending, this);
		return result;
	}
	
	private void resolveCombat(IntVector2D attacking, IntVector2D defending, int result) {
		if(result == GameCharacter.NO_DEATHS);
		else if(result == GameCharacter.ATTACKER_DEATH) {
			log.log(getUnitAt(attacking).getName() + " was killed");
			removeUnit(getUnitAt(activePath.get(0)), activePath.get(0));
		}
		else if(result == GameCharacter.DEFENDER_DEATH) {
			log.log(getUnitAt(defending).getName() + " was killed");
			removeUnit(getUnitAt(defending), defending);
		}
	}
	
	private MoveGhost mg;
	private IntVector2D lastMG;
	public void drawMoveGhost(MapPanel caller, Graphics g) {
		if(activePath != null && activePath.size() != 0) {
			MapCharacter mc = getUnitAt(activePath.get(0));
			if(mg == null || activePath.get(activePath.size() - 1) != lastMG) {
				mg = new MoveGhost(getMapViewState().tileToCanvas(activePath.get(activePath.size() - 1)), mc.getEnclosed());
				lastMG = activePath.get(activePath.size() - 1);
			}
		}
		mg.paint(caller, g);
	}
	public void disposeMoveGhost() {
		if(mg != null)
			mg.dispose();
		mg = null;
	}
	
	/**
	 * Returns whether or not the given coordinate represents a tile on this configuration.
	 * 
	 * @param v A coordinate pair.
	 * @return Whether or not there is a thing there.
	 */
	public boolean isTileOnMap(IntVector2D v) {
		return getTileAt(v) != null;
	}
	
	/**
	 * Returns whether or not the given coordinate represents a tile on this configuration.
	 * 
	 * @param x An x-coordinate.
	 * @param y A y-coordinate.
	 * @return Whether or not there is a thing there.
	 */
	public boolean isTileOnMap(int x, int y) {
		return getTileAt(x, y) != null;
	}
	
	/**
	 * Returns the MapEntity object at the given coordinates.
	 * Returns null if there is no such object.
	 * 
	 * @param v A coordinate pair.
	 * @return A MapEntity.
	 */
	public MapEntity getTileAt(IntVector2D v) {
		if(v == null)
			return null;
		return getTileAt(v.getX(), v.getY());
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
		try {
			return tiles.get(y)[x];
		}
		catch(IndexOutOfBoundsException | NullPointerException e) {
			return null;
		}
	}
	
	public List<IntVector2D> getEnemyCoords() {
		List<IntVector2D> l = new LinkedList<>();
		for(MapEntity[] m : tiles) {
			for(MapEntity e : m) {
				if(e.isOccupied() && e.getOccupying().isEnemy())
					l.add(e.getPos());
			}
		}
		return l;
	}
	
	public List<IntVector2D> getFriendlyCoords() {
		List<IntVector2D> l = new LinkedList<>();
		for(MapEntity[] m : tiles) {
			for(MapEntity e : m) {
				if(e.isOccupied() && e.getOccupying().isFriendly())
					l.add(e.getPos());
			}
		}
		return l;
	}
	
	public MapCharacter getUnitAt(IntVector2D v) {
		try {
			return getTileAt(v).getOccupying();
		}
		catch(NullPointerException e) {
			return null;
		}
	}
	
	public MapCharacter getUnitAt(int x, int y) {
		try {
			return getTileAt(x, y).getOccupying();
		}
		catch(NullPointerException e) {
			return null;
		}
	}
	
	public boolean isUnitAt(IntVector2D v) {
		return getUnitAt(v) != null;
	}
	
	public boolean isUnitAt(int x, int y) {
		return getUnitAt(x, y) != null;
	}
	
	public String getTerrainFilePath() {
		return configFile + "terrain.txt";
	}
	
	public String getVariantFilePath() {
		return configFile + "variants.txt";
	}
	
	public String getStartsFilePath() {
		return configFile + "starts.txt";
	}
	
	public MapViewState getMapViewState() {
		return s;
	}
	
	public int getPxHeight() {
		return height;
	}
	
	public int getPxWidth() {
		return width;
	}
	
	public int getYTiles() {
		return yTiles;
	}
	
	public int getXTiles() {
		return xTiles;
	}
	
	/**
	 * @return if all units of an alignment have moved.
	 */
	public boolean allUnitsHaveMoved(Alignment side) {
		if(side == Alignment.FRIENDLY) {
			for(MapCharacter c : friendlies) {
				if(!c.hasMoved())
					return false; 
			}
		}
		else if(side == Alignment.ENEMY) {
			for(MapCharacter c : enemies) {
				if(!c.hasMoved())
					return false; 
			}
		}
		return true;
	}
	
	/**
	 * Checks whether or not the game has ended.
	 * 
	 * @return
	 */
	public void checkGameEndState() {
		for(MapCharacter c : friendlyMissionCriticals) {
			if(!friendlies.contains(c))
				throw new PlayerLosesException();
		}
		if(getEnemies().size() == 0)
			throw new PlayerWinsException();
	}
	
	private void startTurn(MapPanel caller) {
		log.log("Started " + toMove + " turn");
		shownEnemies.clear();
		if(caller == null || toMove == Alignment.FRIENDLY) {
			turnNumber++;
			for(MapCharacter c : friendlies) {
				c.prepForOwnTurn();
			}
			for(MapCharacter c : enemies) {
				c.prepForEnemyTurn();
			}
			if(caller != null) {
				s.changeSelectedToCenter(caller);
				caller.getMapViewListener().switchActionableTo(MapViewListener.VIEW_STATE);
			}
		}
		else if(toMove == Alignment.ENEMY) {
			for(MapCharacter c : enemies) {
				c.prepForOwnTurn();
			}
			for(MapCharacter c : friendlies) {
				c.prepForEnemyTurn();
			}
			caller.getMapViewListener().switchActionableTo(MapViewListener.LOC_CTRL);
			caller.getMapViewListener().eController.startTurn(caller);
		}
	}
	
	/**
	 * Cleans up the turn of the given side to move, and changes the status
	 * of the MVL.
	 * 
	 */
	public void endTurn(MapPanel caller) {
		log.log("Ending turn for " + toMove);
		s.unexamine(caller);
		toMove = Alignment.opposing(toMove);
		if(toMove == Alignment.FRIENDLY)
			caller.setGameStatus(GameState.PLAYER_MOVING);
		else
			caller.setGameStatus(GameState.ENEMY_TURN);
		startTurn(caller);
	}
	
	public final static Color ALPHA_BLUE_20 = initAlphaColor(Color.BLUE, 0.2);
	public final static Color ALPHA_ORANGE_20 = initAlphaColor(new Color(255, 153, 0), 0.2);
	public final static Color ALPHA_BLUE_30 = initAlphaColor(Color.BLUE, 0.3);
	public final static Color ALPHA_ORANGE_30 = initAlphaColor(new Color(255, 153, 0), 0.3);
	public final static Color ALPHA_ORANGE_60 = initAlphaColor(new Color(255, 153, 0), 0.6);
	public final static Color ALPHA_RED_20 = initAlphaColor(Color.RED, 0.2);
	public final static Color ALPHA_RED_60 = initAlphaColor(Color.RED, 0.6);
	public final static Color ALPHA_PURPLE = initAlphaColor(new Color(200, 0, 200), 0.2);
	public final static Color PATH_COLOR = initAlphaColor(new Color(102, 102, 255), 0.7);
	public final static Color BACKGROUND_COLOR = new Color(217, 217, 217);
	private static Color initAlphaColor(Color c, double d) {
		float[] comps = c.getColorComponents(null);
		return new Color(comps[0], comps[1], comps[2], (float) d);
	}
	
	/**
	 * Highlights a tile at a given coordinate a given color.
	 * 
	 * @param pos The position to highlight.
	 * @param c The color in which to highlight.
	 * @param g The graphics context being used.
	 */
	private void highlight(IntVector2D pos, Color c, Graphics g) {
		IntVector2D start = s.tileToCanvas(pos).scalarSub(s.adjustmentToCenterOnTile());
		int size = (int) (getTileAt(pos).getSpriteHeight() * s.getMapScale());
		g.setColor(c);
		g.fillRect(start.getX(), start.getY(), size, size);
	}
	
	private int msgHigh = 0;
	private void paintTurnString(MapPanel caller, Graphics g) {
		GameState status = caller.getGameStatus();
		if(msgHigh == 0)
			msgHigh = g.getFontMetrics().getHeight();
		int x = 40 + msgHigh / 2, y = caller.getHeight() - 40;
		String str = "";
		Color strColor = Color.RED;
		g.setFont(MoveMenu.OPTIMA);
		if(getSideToMove() == Alignment.FRIENDLY) {
			strColor = Color.GREEN;
			if(status == GameState.MOVE_ANIMATION)
				str = "Moving...";
			else if(status == GameState.PLAYER_CONFIRMING_MOVE)
				str = "Confirm move";
			else if(status == GameState.PLAYER_CONFIRMING_ATTACK)
				str = "Confirm attack";
			else
				str = "Player turn";
		}
		else {
			strColor = Color.RED;
			str = "Enemy turn";
		}
		int wide = g.getFontMetrics().stringWidth(str);
		g.setColor(Color.BLACK);
		g.fillRect(x - 3, y - msgHigh - 2, wide + 8, msgHigh + 8);
		g.setColor(Color.YELLOW);
		g.drawRect(x - 3, y - msgHigh - 2, wide + 8, msgHigh + 8);
		g.setColor(strColor);
		g.drawString(str, x, y);
	}
	
	private IntVector2D lastSelected;
	private IntVector2D lastExamined;
	private List<IntVector2D> blueSelecteds = new LinkedList<>();
	private List<IntVector2D> orangeSelecteds = new LinkedList<>();
	private List<IntVector2D> blueExamineds = new LinkedList<>();
	private List<IntVector2D> orangeExamineds = new LinkedList<>();
	public void paintAll(MapPanel caller, Graphics g) {
		GameState state = caller.getGameStatus();
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 0, caller.getWidth(), caller.getHeight());
		// First, paint terrain
		for(MapEntity[] r : tiles) {
			for(MapEntity e : r) {
				e.paintOnMap(caller, g);
			}
		}
		// (Check if state is animating)
		if(state == GameState.MOVE_ANIMATION) {
			makeMove(caller, lastDef);
		}
		// (Check if state is enemy turn)
		else if(state == GameState.ENEMY_TURN)
				caller.getMapViewListener().eController.next(caller, pf);
		else {
			// Then, paint layers of highlighting
			if(s.isSelecting() && !s.isExamining()) {
				searchForHighlights(g);
			}
			else if(s.isExamining()) {
				searchForHighlights(g);
			}
			if(state == GameState.PLAYER_CONFIRMING_ATTACK
					&& attackableFromHere != null
					&& withinRange != null) {
				for(IntVector2D vec : withinRange) {
					highlight(vec, ALPHA_ORANGE_30, g);
				}
				for(IntVector2D vec : attackableFromHere) {
					highlight(vec, ALPHA_ORANGE_60, g);
				}
			}
			if(state != GameState.PLAYER_CONFIRMING_ATTACK)
				drawHighlights(g);
			drawDangerZone(g);
			// Then, draw highlighted path
			if(state == GameState.PLAYER_CONFIRMING_ATTACK);
			else if(state != GameState.PLAYER_CONFIRMING_MOVE
					&& (!s.isExamining() || !s.isSelecting()
					|| (getUnitAt(s.getExamined()) != null
						&& getUnitAt(s.getExamined()).getSide() == Alignment.ENEMY)))
				activePath = new LinkedList<>();
			else {
				activePath =  pf.getFirstPath(s.getExamined(), s.getSelected());
			}
			pf.drawPath(activePath, g);
		}
		// Draw ghost if applicable
		if(state == GameState.PLAYER_CONFIRMING_MOVE || state == GameState.PLAYER_CONFIRMING_ATTACK)
			drawMoveGhost(caller, g);
		else if(mg != null)
			disposeMoveGhost();
		// Paint sprites
		for(MapEntity[] r : tiles) {
			for(MapEntity e : r) {
				e.paintOccupying(caller, g);
			}
		}
		// Paint status message
		paintTurnString(caller, g);
	}
	private void searchForHighlights(Graphics g) {
		if(getUnitAt(s.getSelected()) != null) {
			if(s.getSelected() != lastSelected) {
				lastSelected = s.getSelected();
				blueSelecteds = findMovableTo(s.getSelected());
				if(getUnitAt(s.getSelected()) != null) {
					List<IntVector2D> attackableTo = searchForAttackables(blueSelecteds, 
							getUnitAt(s.getSelected()).getEnclosed());
					orangeSelecteds = attackableTo.stream()
							.filter((elem) -> !blueSelecteds.contains(elem))
							.collect(Collectors.toList());
				}
				else {
					orangeSelecteds.clear();
				}
			}
		}
		else {
			blueSelecteds.clear();
			orangeSelecteds.clear();
		}
		if(getUnitAt(s.getExamined()) != null
				&& !(!s.isMoving() && !s.getSelected().equals(s.getExamined()))
				&& getUnitAt(s.getExamined()).getSide() != Alignment.ENEMY) {
			if(s.getExamined() != lastExamined) {
				lastExamined = s.getExamined();
				blueExamineds = findMovableTo(s.getExamined());
				if(getUnitAt(s.getExamined()) != null) {
					List<IntVector2D> attackableTo = searchForAttackables(blueExamineds, 
							getUnitAt(s.getExamined()).getEnclosed());
					orangeExamineds = attackableTo.stream()
							.filter((elem) -> !blueExamineds.contains(elem))
							.collect(Collectors.toList());
				}
				else {
					orangeExamineds.clear();
				}
			}
		}
		else if(!s.getSelected().equals(s.getExamined())){
			blueExamineds.clear();
			orangeExamineds.clear();
		}
	}
	
	/**
	 * Stores all enemies whose attack ranges are currently being shown.
	 */
	private Map<IntVector2D, List<IntVector2D>> shownEnemies = new HashMap<>();
	public Map<IntVector2D, List<IntVector2D>> getShownEnemies() {
		return shownEnemies;
	}
	
	private void drawHighlights(Graphics g) {
		if(blueSelecteds != null) {
			for(IntVector2D v : blueSelecteds) {
				highlight(v, ALPHA_BLUE_20, g);
			}
		}
		if(orangeSelecteds != null) {
			for(IntVector2D v : orangeSelecteds) {
				highlight(v, ALPHA_ORANGE_20, g);
			}
		}
		if(blueExamineds != null) {
			for(IntVector2D v : blueExamineds) {
				highlight(v, ALPHA_BLUE_30, g);
			}
		}
		if(orangeExamineds != null) {
			for(IntVector2D v : orangeExamineds) {
				highlight(v, ALPHA_ORANGE_30, g);
			}
		}
	}
	
	/**
	 * Draws squares in the "danger zone," i.e. squares that can be attacked by enemies.
	 * Just as in the Fire Emblem games, selected enemies will show red and unselected ones
	 * will show purple.
	 */
	private Set<IntVector2D> nonRedEnemySquares;
	public void clearNonRedEnemySquares() {
		nonRedEnemySquares = null;
	}
	
	private Set<IntVector2D> recalculateRed() {
		Set<IntVector2D> s = new HashSet<>();
		for(Map.Entry<IntVector2D, List<IntVector2D>> e : shownEnemies.entrySet()) {
			s.addAll(e.getValue());
		}
		return s;
	}
	private void drawDangerZone(Graphics g) {
		Set<IntVector2D> s = recalculateRed();
		// Draws red
		for(IntVector2D v : s) {
			highlight(v, shownEnemies.containsKey(v) ? ALPHA_RED_60 : ALPHA_RED_20, g);
		}
		// Draw purple
		if(getMapViewState().isDangerZoneOn() && nonRedEnemySquares == null) {
			List<IntVector2D> unshownEnemies = getEnemyCoords()
					.stream()
					.filter((elem) -> !shownEnemies.containsKey(elem))
					.collect(Collectors.toList());
			Set<IntVector2D> attackableSquares = new HashSet<>();
			for(IntVector2D v : unshownEnemies) {
				attackableSquares.addAll(findAttackableTo(v));
			}
			nonRedEnemySquares = attackableSquares;
		}
		if(nonRedEnemySquares != null) {
			nonRedEnemySquares
				.stream()
				.filter((v) -> !s.contains(v))
				.forEach((v) -> highlight(v, ALPHA_PURPLE, g));
		}
	}
	
	/**
	 * Finds all tiles that the character occupying a square can move to, and
	 * returns a sparse list identifying the locations of all such tiles.
	 * 
	 * @param pos
	 * @param g
	 * @return
	 */
	public List<IntVector2D> findMovableTo(IntVector2D pos) {
		Map<IntVector2D, Integer> m = new HashMap<>();
		if(!getTileAt(pos).isOccupied())
			return new LinkedList<>(m.keySet());
		log.log("Searching for movable from " + pos);
		searchForMovables(m, getUnitAt(pos).getMvtPts(), getUnitAt(pos).getSide(), pos);
		return new LinkedList<>(m.keySet());
	}
	/**
	 * Recursively identifies all squares that can be moved to.
	 * 
	 * @param m
	 * @param ptsLeft
	 * @param startSide
	 * @param pos
	 */
	// TODO note that this and getFirstPath in pathfinder mess up because it calculates the penalty
	// on the starting square as well, which is technically wrong
	private void searchForMovables(Map<IntVector2D, Integer> m, int ptsLeft, Alignment startSide,
			IntVector2D pos) {
		m.put(pos, ptsLeft);
		searchForMovablesHelper(m, ptsLeft, startSide, pos);
	}
	
	private void searchForMovablesHelper(Map<IntVector2D, Integer> m, int ptsLeft, Alignment startSide,
			IntVector2D pos) {
		if(ptsLeft < 0 || (m.containsKey(pos) && m.get(pos) > ptsLeft)
				|| !isTileOnMap(pos) || !getTileAt(pos).isPassable()
				|| (isUnitAt(pos) && Alignment.areOpposed(getUnitAt(pos).getSide(), startSide)))
			return;
		if(!isUnitAt(pos))
			m.put(pos, ptsLeft);
		if(ptsLeft == 0)
			return;
		else {
			ptsLeft -= getTileAt(pos).getMvtPenalty();
			searchForMovablesHelper(m, ptsLeft, startSide, new IntVector2D(pos.getX(), pos.getY() + 1));
			searchForMovablesHelper(m, ptsLeft, startSide, new IntVector2D(pos.getX(), pos.getY() - 1));
			searchForMovablesHelper(m, ptsLeft, startSide, new IntVector2D(pos.getX() + 1, pos.getY()));
			searchForMovablesHelper(m, ptsLeft, startSide, new IntVector2D(pos.getX() - 1, pos.getY()));
		}
	}
	
	/**
	 * Returns a list of vectors representing all units that are within the attack range of
	 * the character at pos.
	 * 
	 * @param g
	 * @param pos
	 * @return
	 */
	public List<IntVector2D> findUnitsWithinAttackableRange(IntVector2D pos) {
		Alignment ta = getUnitAt(pos).getSide();
		return findAttackableTo(pos).stream()
				.filter(v -> Alignment.areOpposed(getUnitAt(v).getSide(), ta))
				.collect(Collectors.toList());
	}
	public List<IntVector2D> findUnitsWithinRangedRange(IntVector2D pos) {
		return searchForRangedAttackables(findMovableTo(pos), getUnitAt(pos).getEnclosed());
	}
	public List<IntVector2D> findUnitsWithinMeleeRange(IntVector2D pos) {
		return searchForMeleeAttackables(findMovableTo(pos), getUnitAt(pos).getEnclosed());
	}
	
	/**
	 * Finds all squares that the unit at a given position can attack; returns an empty list
	 * if there is no unit at that position.
	 * Note that a direct call to searchForAttackables may be faster in some situations.
	 * 
	 * @param pos
	 * @param g
	 */
	public List<IntVector2D> findAttackableTo(IntVector2D pos) {
		return searchForAttackables(findMovableTo(pos), getUnitAt(pos).getEnclosed());
	}
	/**
	 * Recursively identifies all squares that can be attacked by a given unit, given the possible
	 * moves it can make.
	 * In some situations, a call to this method should be made instead of findAttackableTo to
	 * minimize calculation time.
	 * 
	 * @param movables The list of squares that this unit can move to.
	 * @param unit The unit in question; used to identify whether it is melee and/or ranged.
	 * @param pos The position. Should be the same as the position of the unit parameter.
	 * @param g The graphics instance.
	 * @return A list of vector coordinates that this unit can attack.
	 */
	private List<IntVector2D> searchForAttackables(List<IntVector2D> movables, GameCharacter unit) {
		if(unit == null)
			return new LinkedList<>();
		Set<IntVector2D> attackable = new HashSet<>();
		if(unit.isRanged()) {
			for(IntVector2D v : movables) {
				List<IntVector2D> arr = new ArrayList<>();
				int x = v.getX();
				int y = v.getY();
				arr.add(new IntVector2D(x + 2, y));
				arr.add(new IntVector2D(x - 2, y));
				arr.add(new IntVector2D(x, y + 2));
				arr.add(new IntVector2D(x, y - 2));
				arr.add(new IntVector2D(x + 1, y + 1));
				arr.add(new IntVector2D(x + 1, y - 1));
				arr.add(new IntVector2D(x - 1, y - 1));
				arr.add(new IntVector2D(x - 1, y + 1));
				attackable.addAll(arr.stream()
						.filter(vec -> isTileOnMap(vec) && getTileAt(vec).isPassable())
						.collect(Collectors.toList()));
			}
		}
		if(unit.isMelee()) {
			for(IntVector2D v : movables) {
				List<IntVector2D> arr = new ArrayList<>();
				int x = v.getX();
				int y = v.getY();
				arr.add(new IntVector2D(x + 1, y));
				arr.add(new IntVector2D(x - 1, y));
				arr.add(new IntVector2D(x, y + 1));
				arr.add(new IntVector2D(x, y - 1));
				attackable.addAll(arr.stream()
						.filter(vec -> isTileOnMap(vec) && getTileAt(vec).isPassable())
						.collect(Collectors.toList()));
			}
		}
		return new LinkedList<>(attackable);
	}
	
	private List<IntVector2D> searchForRangedAttackables(List<IntVector2D> movables, GameCharacter unit) {
		List<IntVector2D> arr = new ArrayList<>();
		if(!unit.isRanged())
			return arr;
		Alignment ta = unit.getSide();
		for(IntVector2D v : movables) {
			int x = v.getX();
			int y = v.getY();
			arr.add(new IntVector2D(x + 2, y));
			arr.add(new IntVector2D(x - 2, y));
			arr.add(new IntVector2D(x, y + 2));
			arr.add(new IntVector2D(x, y - 2));
			arr.add(new IntVector2D(x + 1, y + 1));
			arr.add(new IntVector2D(x + 1, y - 1));
			arr.add(new IntVector2D(x - 1, y - 1));
			arr.add(new IntVector2D(x - 1, y + 1));
			arr = arr.stream()
					.filter(vec -> isUnitAt(vec) && Alignment.areOpposed(getUnitAt(vec).getSide(), ta))
					.collect(Collectors.toList());
		}
		return arr;
	}
	
	private List<IntVector2D> searchForMeleeAttackables(List<IntVector2D> movables, GameCharacter unit) {
		List<IntVector2D> arr = new ArrayList<>();
		if(!unit.isMelee())
			return arr;
		Alignment ta = unit.getSide();
		for(IntVector2D v : movables) {
			int x = v.getX();
			int y = v.getY();
			arr.add(new IntVector2D(x + 1, y));
			arr.add(new IntVector2D(x - 1, y));
			arr.add(new IntVector2D(x, y + 1));
			arr.add(new IntVector2D(x, y - 1));
			arr = arr.stream()
					.filter(vec -> isUnitAt(vec) && Alignment.areOpposed(getUnitAt(vec).getSide(), ta))
					.collect(Collectors.toList());
		}
		return arr;
	}
	
	public Alignment getSideToMove() {
		return toMove;
	}
	
	public void setToMove(Alignment toMove) {
		this.toMove = toMove;
	}
	
	public String getFullSavePath() {
		return "res/saves/" + saveName + "/";
	}
	
	/**
	 * Writes the characters to the folder specified in the saveName field.
	 */
	public void save() {
		log.log("Attempting to save...");
		Path spath = Paths.get(getFullSavePath());
		Path cpath = Paths.get(getFullSavePath() + "units/");
		try {
			if(!Files.exists(spath))
				Files.createDirectory(spath);
			if(!Files.exists(cpath))
				Files.createDirectory(cpath);
		}
		catch (IOException e) {
			log.log("Save failed.");
			return;
		}

		File[] files = new File(cpath.toString()).listFiles();
		for(File f : files) {
			f.delete();
		}
		
		try {
			BufferedWriter mfestWriter = new BufferedWriter(new FileWriter(getFullSavePath() + "progress.txt"));
			mfestWriter.write(name);
			mfestWriter.close();
			for(MapCharacter mc : getFriendlies()) {
				BufferedWriter charWriter = new BufferedWriter(new FileWriter(getFullSavePath() + "units/"
						+ mc.getEnclosed().getDirName()));
				charWriter.write(mc.getEnclosed().save());
				charWriter.close();
			}
		}
		catch(IOException e) {
			log.log("Save failed.");
			return;
		}
		log.log("Game saved.");
	}
	
	public static MapConfiguration create(String mapName, MapViewState s, String saveName) {
		return new MapConfiguration(mapName, s, saveName);
	}

}
