package core.animations;

import core.DoubleVector2D;
import core.IntVector2D;
import ui.MapPanel;
import ui.MapViewState;
import worldmap.MapConfiguration;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

public class PathAnimation extends SequentialAnimation {
	
	private List<DoubleVector2D> drawnPath;
	private MapViewState vs;
	private ImageIcon left;
	private ImageIcon right;
	private ImageIcon up;
	private ImageIcon down;
	private ImageIcon activeIcon;

	// moveIcons should be down left right up (alpha order)
	public PathAnimation(List<IntVector2D> path, MapConfiguration config, int delay, ImageIcon... icons) {
		super(delay, icons);
		log.log("Creating path animation from " + path);
		this.down = icons[0];
		this.left = icons[1];
		this.right = icons[2];
		this.up = icons[3];
		activeIcon = down;
		this.vs = config.getMapViewState();
		drawnPath = new ArrayList<DoubleVector2D>();
		DoubleVector2D fst = path.get(0).asDoubleVector();
		drawnPath.add(fst);
		DoubleVector2D snd;
		DoubleVector2D diff;
		for(int i = 1; i < path.size(); i++) {
			snd = path.get(i).asDoubleVector();
			diff = snd.sub(fst);
			for(int j = 1; j <= delay; j++) {
				drawnPath.add(fst.sum(diff.scalarDiv(delay).scalarMult(j)));
			}
			fst = snd;
		}
		if(path.size() != 0)
			setPos(vs.tileToCanvas(path.get(0)));
	}
	
	@Override
	public void paint(MapPanel caller, Graphics g) {
		if(frameNumber == getFramesToLoop()) { // failsafe
			finished = true;
			return;
		}
		setPos(vs.tileToCanvas(drawnPath.get(frameNumber)).asIntVector());
		double x = getPos().getX();
		double y = getPos().getY();
		if(frameNumber == 0)
			activeIcon = down;
		else {
			DoubleVector2D last = drawnPath.get(frameNumber - 1);
			DoubleVector2D current = drawnPath.get(frameNumber);
			if(current.getY() < last.getY())
				activeIcon = up;
			else if(current.getY() > last.getY())
				activeIcon = down;
			else if(current.getX() > last.getX())
				activeIcon = right;
			else if(current.getX() < last.getX())
				activeIcon = left;
		}
		g.drawImage(MapViewState.getScaledImage(activeIcon, caller.getMapViewState()).getImage(),
				(int) x, (int) y, null);
		super.paint(caller);
	}
	
	@Override
	public ImageIcon getCurrentIcon() {
		return up;
	}
	
	@Override
	public int getFramesToLoop() {
		if(drawnPath == null)
			return super.getFramesToLoop();
		else
			return drawnPath.size();
	}
	
}
