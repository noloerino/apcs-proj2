package worldmap;

import core.Alignment;
import core.IntVector2D;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import control.DS_Action;

/**
 * Finds paths on a MapConfiguration.
 * 
 * @author jhshi
 *
 */
public class Pathfinder {
	
	private MapConfiguration c;
	
	public Pathfinder(MapConfiguration c) {
		this.c = c;
	}
	
	/**
	 * Returns the first path that can be recursively found between two locations.
	 * Movement points are taken based on the character at the specified starting tile; if there
	 * is not a character on that tile, then this function returnsn= null.
	 * 
	 * If such a path does not exist, an empty list is returned.
	 *
	 * @param start
	 * @param end
	 * @return
	 */
	public List<IntVector2D> getFirstPath(IntVector2D start, IntVector2D end) {
		MapCharacter unit = c.getUnitAt(start);
		if(unit == null)
			return null;
		List<IntVector2D> path = new LinkedList<>();
		return getFirstPath(unit.getSide(), unit.getMvtPts(), path, start, end);
	}
	
	/**
	 * Returns the first path that can be recursively found given this number of movement points
	 * remaining.
	 * If such a path does not exist, an empty list is returned.
	 * 
	 * @param ptsLeft
	 * @param current
	 * @param end
	 * @return
	 */
	public List<IntVector2D> getFirstPath(Alignment startSide, int ptsLeft, List<IntVector2D> pathSoFar,
			IntVector2D current, IntVector2D end) {
		if(ptsLeft < 0 || pathSoFar.contains(current)) {
			return new ArrayList<>();
		}
		else if(!c.isTileOnMap(current) || !c.getTileAt(current).isPassable()
				|| (c.isUnitAt(current) && Alignment.areOpposed(c.getUnitAt(current).getSide(), startSide))) {
			return new ArrayList<>();
		}
		else if(current.equals(end)) {
			pathSoFar.add(end);
			return pathSoFar;
		}
		else {
			List<IntVector2D> newPath = new ArrayList<>(pathSoFar);
			newPath.add(current);
			ptsLeft -= c.getTileAt(current).getMvtPenalty();
			// reorder calculation based on displacement direction
			DS_Action[] dirs = new DS_Action[4];
			IntVector2D diff = end.sub(current);
			// go up or down first
			if(Math.abs(diff.getX()) < Math.abs(diff.getY())) {
				if(diff.getY() > 0) {
					dirs[0] = DS_Action.DOWN;
					dirs[3] = DS_Action.UP;
				}
				else {
					dirs[0] = DS_Action.UP;
					dirs[3] = DS_Action.DOWN;
				}
				if(diff.getX() > 0) {
					dirs[1] = DS_Action.RIGHT;
					dirs[2] = DS_Action.LEFT;
				}
				else {
					dirs[1] = DS_Action.LEFT;
					dirs[2] = DS_Action.RIGHT;
				}
			}
			// go left or right first
			else {
				if(diff.getX() > 0) {
					dirs[0] = DS_Action.RIGHT;
					dirs[3] = DS_Action.LEFT;
				}
				else {
					dirs[0] = DS_Action.LEFT;
					dirs[3] = DS_Action.RIGHT;
				}
				if(diff.getY() > 0) {
					dirs[1] = DS_Action.UP;
					dirs[2] = DS_Action.DOWN;
				}
				else {
					dirs[1] = DS_Action.DOWN;
					dirs[2] = DS_Action.UP;
				}
			}
			return goInOrderKindOfLazy(startSide, dirs, ptsLeft, newPath, current, end);
		}
	}
	
	private List<IntVector2D> goInOrderKindOfLazy(Alignment startSide, DS_Action[] dirs, int ptsLeft,
			List<IntVector2D> p, IntVector2D pos, IntVector2D end) {
		List<IntVector2D> l = new ArrayList<>();
		for(DS_Action d : dirs) {
			if(d == DS_Action.LEFT) {
				l = goLeft(startSide, ptsLeft, p, pos, end);
				if(l.size() != 0)
					return l;
			}
			else if(d == DS_Action.RIGHT) {
				l = goRight(startSide, ptsLeft, p, pos, end);
				if(l.size() != 0)
					return l;
			}
			else if(d == DS_Action.UP) {
				l = goUp(startSide, ptsLeft, p, pos, end);
				if(l.size() != 0)
					return l;
			}
			else if(d == DS_Action.DOWN) {
				l = goDown(startSide, ptsLeft, p, pos, end);
				if(l.size() != 0)
					return l;
			}
		}
		return l;
	}
	private List<IntVector2D> goLeft(Alignment startSide, int ptsLeft, List<IntVector2D> p, 
			IntVector2D pos, IntVector2D end) {
		return getFirstPath(startSide, ptsLeft, p, new IntVector2D(pos.getX() - 1, pos.getY()), end);
	}
	private List<IntVector2D> goRight(Alignment startSide, int ptsLeft, List<IntVector2D> p, 
			IntVector2D pos, IntVector2D end) {
		return getFirstPath(startSide, ptsLeft, p, new IntVector2D(pos.getX() + 1, pos.getY()), end);
	}
	private List<IntVector2D> goUp(Alignment startSide, int ptsLeft, List<IntVector2D> p, 
			IntVector2D pos, IntVector2D end) {
		return getFirstPath(startSide, ptsLeft, p, new IntVector2D(pos.getX(), pos.getY() - 1), end);
	}
	private List<IntVector2D> goDown(Alignment startSide, int ptsLeft, List<IntVector2D> p, 
			IntVector2D pos, IntVector2D end) {
		return getFirstPath(startSide, ptsLeft, p, new IntVector2D(pos.getX(), pos.getY() + 1), end);
	}
	
	/**
	 * Draws a path specified by the argument list.
	 * If the path list is invalid, i.e. every vector  in the list is not adjacent to both the 
	 * preceding and following vectors, the drawing operation will not be carried out.
	 * 
	 * @param path
	 * @param g
	 */
	public void drawPath(List<IntVector2D> path, Graphics g) {
		if(!isValidPath(path))
			return;
		int size = c.getMapViewState().getScaledImgSize();
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(MapConfiguration.PATH_COLOR);
		g2.setStroke(new BasicStroke(size / 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		IntVector2D last = path.get(0);
		IntVector2D current;
		Iterator<IntVector2D> iter = path.iterator();
		iter.next();
		for(int i = 0; i < path.size() - 1; i++) {
			current = iter.next();
			drawSegment(size, last, current, g2);
			last = current;
		}
	}
	
	/**
	 * Draws a line segment between two tiles, connecting the centers of each.
	 * 
	 * Precondition: The styles of the graphics context are set before this call is made.
	 * 
	 * @param a The starting point.
	 * @param b The ending point.
	 * @param g2
	 */
	private void drawSegment(int size, IntVector2D a, IntVector2D b, Graphics2D g2) {
		IntVector2D startPx = c.getMapViewState().tileToCanvas(a).scalarSum(size / 2);
		IntVector2D endPx = c.getMapViewState().tileToCanvas(b).scalarSum(size / 2);
		g2.drawLine(startPx.getX(), startPx.getY(), endPx.getX(), endPx.getY());
	}
	
	/**
	 * Returns true iff each element of the vector list representing a path is adjacent
	 * to the next and preceding ones.
	 * 
	 * @param path
	 * @return
	 */
	public boolean isValidPath(List<IntVector2D> path) {
		if(path == null || path.size() == 0
				|| !c.getTileAt(path.get(path.size() - 1)).isPassable())
			return false;
		MapEntity end = c.getTileAt(path.get(path.size() - 1));
		if(path.size() != 1 && end.isOccupied())
			return false;
		// return isValidPath(path, null);
		IntVector2D last = path.get(0);
		IntVector2D current;
		Iterator<IntVector2D> iter = path.iterator();
		iter.next();
		for(int i = 0; i < path.size() - 1; i++) {
			current = iter.next();
			if(!current.isAdjacentTo(last))
				return false;
			last = current;
		}
		return true;
	}
	
	@SuppressWarnings("unused")
	private boolean isValidPath(List<IntVector2D> path, IntVector2D last) {
		if(path.size() == 0)
			return true;
		else if(last == null || last.isAdjacentTo(path.get(0)))
			return isValidPath(path.subList(1, path.size()), path.get(0));
		else
			return false;
	}
	
}
