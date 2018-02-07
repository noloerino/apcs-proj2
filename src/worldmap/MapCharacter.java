package worldmap;

import characterbuilder.*;
import core.*;
import core.animations.Animation;
import ui.MapPanel;
import ui.MapViewState;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Map;
import java.util.function.BiFunction;

public class MapCharacter extends MapEntity {

	private GameCharacter enclosing;
	private boolean hasMoved;
	
	public MapCharacter(GameCharacter gc, MapEntity enclosedBy) {
		super('*', gc.getStationarySpritePath(), true, gc.getSide(), enclosedBy.getPos(), gc.getName(), gc.getDescription());
		this.enclosing = gc;
		gc.getActiveMapAnimation().restart();
		gc.getActiveMapAnimation().unpause();
		hasMoved = false;
	}
	
	public boolean hasMoved() {
		return hasMoved;
	}
	
	public void setHasMoved(boolean hasMoved) {
		this.hasMoved = hasMoved;
	}
	
	@Override
	public String getName() {
		return enclosing.getName();
	}
	
	public GameCharacter getEnclosed() {
		return enclosing;
	}
	
	public void paintRelativeToMapEntity(MapEntity e, MapPanel caller, Graphics g) {
		MapViewState vs = caller.getMapViewState();
		IntVector2D pos = e.getPaintPos(vs.getMapScale(), vs.getMapViewDisp());
		paint(pos, caller, g);
	}
	
	public void paint(IntVector2D pos, MapPanel caller, Graphics g) {
		Animation a = enclosing.getActiveMapAnimation();
		if(a != null) {
			a.setPos(
					pos.scalarSub((int) (MapEntity.getCoordDisp() / 2 * caller.getMapViewState().getMapScale())));
			a.paint(caller, g);
			paintHPBar(a.getPos().scalarSum((int) (MapEntity.getCoordDisp() * caller.getMapViewState().getMapScale())), caller, g);
		}
	}
	
	private void paintHPBar(IntVector2D pos, MapPanel caller, Graphics g) {
		int iconSize = caller.getMapViewState().getScaledImgSize();
		int xLen = iconSize * enclosing.getBaseHP() / 25;
		double prop = (double) enclosing.getHP() / enclosing.getBaseHP();
		int x1 = pos.getX() + 4;
		int y1 = pos.getY() + iconSize - 8 - MapEntity.getCoordDisp();
		int x2 = x1 + (int) (prop * (xLen - 8 - MapEntity.getCoordDisp()));
		int x3 = pos.getX() + xLen - MapEntity.getCoordDisp() - 4;
		final int height = 6;
		g.setColor(Color.GREEN);
		g.fillRect(x1, y1, x2 - x1, height);
		g.setColor(Color.RED);
		g.fillRect(x2, y1, x3 - x2, height);
		((Graphics2D) g).setStroke(new BasicStroke(1));
		g.setColor(Color.BLACK);
		g.drawRect(x1, y1, x3 - x1, height);
	}
	
	public Alignment getSide() {
		return getEnclosed().getSide();
	}
	
	public int getMvtPts() {
		return enclosing.getMvtPts();
	}
	
	public double[] getLvlUpRates() {
		return enclosing.getLvlUpRates();
	}

	@Override
	protected void addSymbolToMaps(Map<Character, BiFunction<Integer, Integer, MapEntity>> symbolMap,
			Map<Character, BiFunction<IntVector2D, Character, MapEntity>> variantMap) { }
	
	// What follows is a lesson in overzealous abstraction. getX() and getY() should now throw NPE
	@Override
	public IntVector2D getPos() {
		return null;
	}
	
	private void setAnimationGray(boolean gray) {
		enclosing.getActiveMapAnimation().setGray(gray);
	}
	
	public void afterMoved(MapPanel caller) {
		setHasMoved(true);
		enclosing.endMvOrAtkAnimation(caller);
		enclosing.getActiveMapAnimation().pause();
		setAnimationGray(true);
	}
	
	public void prepForOwnTurn() {
		setHasMoved(false);
		setAnimationGray(false);
		enclosing.setToBaseAnimation();
		enclosing.getActiveMapAnimation().unpause();
	}
	
	public void prepForEnemyTurn() {
		setAnimationGray(false);
		enclosing.setToBaseAnimation();
		enclosing.getActiveMapAnimation().unpause();
	}

}
