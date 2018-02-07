package core.animations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.GrayFilter;
import javax.swing.ImageIcon;

import control.DebugLogger;
import core.IntVector2D;
import ui.MapPanel;
import ui.MapViewState;
import ui.MoveMenu;

public class Animation {

	protected int frameNumber;
	private int delay;	
	private boolean paused;
	private boolean hidden;
	private ImageIcon[] activeIcons;
	private ImageIcon[] grayIcons;
	private ImageIcon[] normalIcons;
		
	private IntVector2D pos;
	
	public static DebugLogger log = new DebugLogger("Animation");
	
	public Animation(int delay, ImageIcon...icons) {
		this(IntVector2D.ORIGIN(), delay, icons);
	}
	
	public Animation(IntVector2D pos, int delay, ImageIcon... icons) {
		this.pos = pos;
		normalIcons = icons;
		grayIcons = new ImageIcon[icons.length];
		for(int i = 0; i < icons.length; i++) {
			grayIcons[i] = new ImageIcon(GrayFilter.createDisabledImage(icons[i].getImage()));
		}
		frameNumber = 0;
		this.delay = delay;
		paused = true;
		hidden = false;
		
		activeIcons = icons;
		log.add("Created animation with <" + getFramesToLoop() + "> frames total, <" + delay + "> delay between frames.");
	}
	
	public ImageIcon getCurrentIcon() {
		return activeIcons[getDrawnFrameNumber()];
	}
	
	private int msgFramesLeft = 0;
	private Map<String, Color> msg = new HashMap<>();
	public void attachMsg(int frames, String msg, Color color) {
		msgFramesLeft = frames;
		this.msg.put(msg, color);
	}
	
	public void restart() {
		frameNumber = 0;
		unpause();
	}
	
	public void pause() {
		paused = true;
	}
	
	public void unpause() {
		paused = false;
	}
	
	public void hide() {
		hidden = true;
	}
	
	public void unhide() {
		hidden = false;
	}
	
	public void stop() {
		hide();
		pause();
	}
	
	public void paint(MapPanel caller, Graphics g) {
		if(hidden) {
			paint(caller);
			return;
		}
		ImageIcon sprite = activeIcons[getDrawnFrameNumber()];
		ImageIcon img = MapViewState.getScaledImage(sprite, caller.getMapViewState());
		img.paintIcon(caller, g, getX(), getY());
		paint(caller);
		if(msgFramesLeft > 0) {
			g.setFont(MoveMenu.OPTIMA);
			int high = g.getFontMetrics().getHeight();
			Iterator<String> iter = msg.keySet().iterator();
			for(int i = 0; i < msg.size(); i++) {
				String key = iter.next();
				g.setColor(msg.get(key));
				String[] tokens = key.split("\n");
				int wide = 0;
				for(int j = 0; j < tokens.length; j++) {
					wide = g.getFontMetrics().stringWidth(tokens[j]);
					g.drawString(tokens[j], getX() + img.getIconWidth() / 2 - wide / 2,
							getY() + i * high + high * j);
				}
			}
			msgFramesLeft--;
		}
		else if(msgFramesLeft == 0)
			msg.clear();
	}
	
	protected int getDrawnFrameNumber() {
		return (frameNumber - 1) / delay;
	}
	
	protected void paint(MapPanel caller) {
		if(!paused) {
			frameNumber++;
			if(frameNumber == getFramesToLoop())
				frameNumber = 0;
		}
	}
	
	public IntVector2D getPos() {
		return pos;
	}
	
	public void setPos(IntVector2D pos) {
		this.pos.set(pos);
	}
	
	public int getX() {
		return pos.getX();
	}
	
	public void setX(int x) {
		pos.setX(x);
	}
	
	public int getY() {
		return pos.getY();
	}
	
	public void setY(int y) {
		pos.setY(y);
	}
	
	public int getFrameNumber() {
		return frameNumber;
	}

	public int getDelay() {
		return delay;
	}

	public int getFramesToLoop() {
		return activeIcons.length * delay;
	}

	public boolean isPaused() {
		return paused;
	}
	
	public boolean isHidden() {
		return hidden;
	}

	public ImageIcon[] getIcons() {
		return activeIcons;
	}
	
	/**
	 * Sets an animation to be in all gray.
	 * 
	 * @param grayScale
	 */
	public void setGray(boolean grayScale) {
		if(grayScale)
			activeIcons = grayIcons;
		else
			activeIcons = normalIcons;
	}
	
}
