package core.animations;

import ui.MapPanel;

import java.awt.Graphics;
import javax.swing.ImageIcon;

public class SequentialAnimation extends Animation {
	
	protected boolean finished;
	
	/**
	 * Creates an animation that will run sequentially; that is, calling anim.run
	 * on an instance of this class will delay execution of the program until the animation
	 * fully completes.
	 * 
	 * 
	 * @param delay The delay between frames.
	 * @param icons The array of icons to use.
	 */
	public SequentialAnimation(int delay, ImageIcon... icons) {
		super(delay, icons);
		finished = false;
		unpause();
	}
	
	@Override
	public void paint(MapPanel caller, Graphics g) {
		super.paint(caller, g);
		if(frameNumber == getFramesToLoop()) { // failsafe
			finished = true;
			return;
		}
	}
	
	@Override
	protected void paint(MapPanel caller) {
		if(isFinished()) {
			hide();
			return;
		}
		if(!isPaused()) {
			frameNumber++;
			if(frameNumber == getFramesToLoop()) {
				finished = true;
				return;
			}
		}
	}
	
	public boolean isFinished() {
		return finished;
	}

}
