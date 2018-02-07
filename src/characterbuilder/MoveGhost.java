package characterbuilder;

import core.IntVector2D;
import core.animations.Animation;
import ui.MapPanel;

import java.awt.Graphics;
import javax.swing.ImageIcon;

public class MoveGhost {

	private ImageIcon[] icons;
	
	private Animation anim;
	
	public MoveGhost(IntVector2D pos, GameCharacter enclosed) {
		icons = enclosed.getActiveMapAnimation().getIcons();
		/*new ImageIcon[enclosed.getActiveMapAnimation().getIcons().length];
		ImageIcon[] oldIcons = enclosed.getActiveMapAnimation().getIcons();
		for(int i = 0; i < icons.length; i++) {
			icons[i] = new AlphaIcon(oldIcons[i]);
		}*/
		anim = new Animation(pos, enclosed.getActiveMapAnimation().getDelay(), icons);
		anim.restart();
	}
	
	public void paint(MapPanel caller, Graphics g) {
		anim.paint(caller, g);
	}
	
	public void dispose() {
		anim.stop();
	}
	
	// taken from http://www.camick.com/java/source/AlphaIcon.java
	/*
	private static class AlphaIcon extends ImageIcon {
		
		private ImageIcon icon;
		private final float alpha = 0.6F;

		public AlphaIcon(ImageIcon old) {
			icon = old;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g.create();
		    g2.setComposite(AlphaComposite.SrcAtop.derive(alpha));
		    icon.paintIcon(c, g2, x, y);
		    g2.dispose();
		}
		
	}
	*/
}
