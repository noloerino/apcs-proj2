package core.animations;

import javax.swing.ImageIcon;

import ui.MapPanel;

/**
 * An animation that repeats the same frames going forwards and backwards.
 * 
 * @author jhshi
 *
 */
public class SymmetricAnimation extends Animation {

	private boolean fwds = true;
	
	public SymmetricAnimation(int delay, ImageIcon[] icons) {
		super(delay, icons);
	}

	@Override
	protected void paint(MapPanel caller) {
		if(!isPaused()) {
			if(fwds)
				frameNumber++;
			else
				frameNumber--;
			if(frameNumber == getFramesToLoop()) {
				fwds = false;
				frameNumber -= getDelay();
			}
			else if(frameNumber == 0) {
				fwds = true;
				frameNumber = getDelay() + 1;
			}
		}
	}
	
}
