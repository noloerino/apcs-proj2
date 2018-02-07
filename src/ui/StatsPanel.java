package ui;

import control.*;
import core.*;
import worldmap.MapCharacter;
import worldmap.MapConfiguration;
import worldmap.MapEntity;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import characterbuilder.GameCharacter;

public class StatsPanel extends JPanel {

	private MapViewState s;
	private MapPanel mp;
	private MapViewListener l;
	
	public StatsPanel(MapViewState s, MapPanel mp, MapViewListener l) {
		this.s = s;
		this.mp = mp;
		this.l = l;
	}
	
	public final static Color BACKGROUND  = new Color(214, 208, 203);
	public final static Color FRAMING = new Color(124, 120, 117);
	public final static Color ENEMY_RED = new Color(96, 0, 2);
	public final static Color FRIENDLY_BLUE = new Color(1, 24, 96);
	public final static Color TEXT_GRAY = new Color(71, 65, 59);
	
	private int fontHeight;
	public final static Font OPTIMA = MoveMenu.OPTIMA;
	@Override
	public void paintComponent(Graphics g) {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			     RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(BACKGROUND);
		int width = getWidth(), height = getHeight();
		g.fillRect(0, 0, width, height);
		int middleWidth = width / 2;
		boolean selecting = s.isSelecting();
		boolean examining = s.isExamining();
		MapConfiguration c = getMapConfig();
		IntVector2D exa = s.getExamined();
		boolean exaOcc = c.isUnitAt(exa);
		MapCharacter exaUni = c.getUnitAt(exa);
		GameCharacter exaG = exaOcc ? exaUni.getEnclosed() : null;
		MapEntity exaT = c.getTileAt(exa);
		IntVector2D sel = s.getSelected();
		boolean selOcc = c.isUnitAt(sel);
		MapCharacter selUni = c.getUnitAt(sel);
		GameCharacter selG = selOcc ? selUni.getEnclosed() : null;
		MapEntity selT = c.getTileAt(sel);
		int center1 = width / 4;
		int center2 = 3 * center1;
		int iconSize = MapEntity.SIZE;
		g.setColor(FRAMING);
		int y = 20;
		g.fillRoundRect(center1 - iconSize / 2 - 2, y - 2, iconSize + 4, iconSize + 4, 4, 4);
		g.fillRoundRect(center2 - iconSize / 2 - 2, y - 2, iconSize + 4, iconSize + 4, 4, 4);
		if(getGameState() == GameState.BASE_MENU)
			return;
		// Draw icons
		if(exaOcc && examining) {
			exaG.getActiveMapAnimation().getCurrentIcon().paintIcon(this, g, center1 - iconSize / 2, y);
		}
		else if(examining) {
			if(c.getTileAt(exa) != null)
				c.getTileAt(exa).getMapSprite().paintIcon(this, g, center1 - iconSize / 2, y);
		}
		if(selOcc && selecting) {
			selG.getActiveMapAnimation().getCurrentIcon().paintIcon(this, g, center2 - iconSize / 2, y);
		}
		else if(selecting){
			if(c.getTileAt(sel) != null)
				c.getTileAt(sel).getMapSprite().paintIcon(this, g, center2 - iconSize / 2, y);
		}
		y += iconSize + fontHeight;
		// Draw names, center justified
		g.setFont(OPTIMA);
		if(fontHeight == 0)
			fontHeight = g.getFontMetrics().getHeight();
		String name = "";
		int nameWidth;
		if(exaOcc && examining) {
			setTextColor(exaG, g);
			name = exaUni.getName();
		}
		else if(examining) {
			g.setColor(TEXT_GRAY);
			name = "(" + exaT.getName() + ")";
		}
		nameWidth = g.getFontMetrics().stringWidth(name);
		g.drawString(name, center1 - nameWidth / 2, y);
		if(selOcc && selecting) {
			setTextColor(selG, g);
			name = selUni.getName();
		}
		else if(selecting) {
			g.setColor(TEXT_GRAY);
			name = "(" + selT.getName() + ")";
		}
		nameWidth = g.getFontMetrics().stringWidth(name);
		g.drawString(name, center2 - nameWidth / 2, y);
		y = fontHeight + drawStats(center1, center2, y, middleWidth, iconSize, exaG, selG, g);
		if(getGameState() == GameState.PLAYER_CONFIRMING_ATTACK) {
			if(selOcc && selG.isEnemy()) {
				int atkSpd = exaG.getEffectiveSpd();
				int atkAtk = exaG.getEffectiveAtk();
				int atkDSt = selG.isWeaponPhys() ? exaG.getDef() : exaG.getRes();
				int defSpd = selG.getEffectiveSpd();
				int defAtk = selG.getEffectiveAtk();
				int defDSt = exaG.isWeaponPhys() ? selG.getDef() : selG.getRes();
				int atkDmg = atkAtk - defDSt;
				int defDmg = defAtk - atkDSt;
				String msgL = "Deals " + Math.max(0, atkDmg) + (atkSpd > defSpd ? " damage (x2)" : " damage");
				int lWidth = g.getFontMetrics().stringWidth(msgL);
				setTextColor(exaG, g);
				g.drawString(msgL, center1 - lWidth / 2, y);
				String msgR = "Deals " + Math.max(0, defDmg) + (defSpd > atkSpd ? " damage (x2)" : " damage");
				int rWidth = g.getFontMetrics().stringWidth(msgR);
				setTextColor(selG, g);
				g.drawString(msgR, center2 - rWidth / 2, y);
			}
		}
	}
	
	private int drawStats(int center1, int center2, int y, int mid, int iconSize, GameCharacter left,
			GameCharacter right, Graphics g) {
		boolean isLeft = left != null;
		boolean isRight = right != null;
		int snw;
		String stn;
		y += fontHeight;
		// Draw level
		stn = "LVL";
		snw = g.getFontMetrics().stringWidth(stn);
		g.setColor(TEXT_GRAY);
		g.drawString(stn, mid - snw / 2, y);
		if(isLeft) {
			setTextColor(left, g);
			stn = Integer.toString(left.getLevel());
			snw = g.getFontMetrics().stringWidth(stn);
			g.drawString(stn, center1 - snw / 2, y);
		}
		if(isRight) {
			setTextColor(right, g);
			stn = Integer.toString(right.getLevel());
			snw = g.getFontMetrics().stringWidth(stn);
			g.drawString(stn, center2 - snw / 2, y);
		}
		y += fontHeight;
		// Draw exp
		stn = "EXP";
		snw = g.getFontMetrics().stringWidth(stn);
		g.setColor(TEXT_GRAY);
		g.drawString(stn, mid - snw / 2, y);
		if(isLeft) {
			setTextColor(left, g);
			stn = Integer.toString(left.getExp());
			snw = g.getFontMetrics().stringWidth(stn);
			g.drawString(stn, center1 - snw / 2, y);
		}
		if(isRight) {
			setTextColor(right, g);
			stn = Integer.toString(right.getExp());
			snw = g.getFontMetrics().stringWidth(stn);
			g.drawString(stn, center2 - snw / 2, y);
		}
		y += fontHeight;
		// Draw HP bar
		int barHeight = fontHeight - 2;
		if(isLeft) {
			stn = left.getHP() + "/" + left.getBaseHP();
			snw = g.getFontMetrics().stringWidth(stn);
			setTextColor(left, g);
			int x1 = center1 - iconSize / 2;
			g.drawString(stn, x1 - snw - 4, y);
			int x2 = x1 + (left.getHP() * iconSize) / 25;
			int x3 = x1 + (left.getBaseHP() * iconSize) / 25;
			g.setColor(Color.GREEN);
			int y1 = y - barHeight + 4;
			g.fillRect(x1, y1, x2 - x1, barHeight);
			g.setColor(Color.RED);
			g.fillRect(x2, y1, x3 - x2, barHeight);
			g.setColor(Color.BLACK);
			g.drawRect(x1, y1, x3 - x1, barHeight);
		}
		if(isRight) {
			stn = right.getHP() + "/" + right.getBaseHP();
			snw = g.getFontMetrics().stringWidth(stn);
			setTextColor(right, g);
			int x1 = center2 + iconSize / 2;
			g.drawString(stn, x1 + 4, y);
			int x2 = x1 - (right.getHP() * iconSize) / 25;
			int x3 = x1 - (right.getBaseHP() * iconSize) / 25;
			g.setColor(Color.GREEN);
			int y1 = y - barHeight + 4;
			g.fillRect(x2, y1, x1 - x2, barHeight);
			g.setColor(Color.RED);
			g.fillRect(x3, y1, x2 - x3, barHeight);
			g.setColor(Color.BLACK);
			g.drawRect(x3, y1, x1 - x3, barHeight);
		}
		stn = "HP";
		snw = g.getFontMetrics().stringWidth(stn);
		g.setColor(TEXT_GRAY);
		g.drawString(stn, mid - snw / 2, y);
		y += fontHeight;
		// Draw attack
		stn = "ATK";
		snw = g.getFontMetrics().stringWidth(stn);
		g.setColor(TEXT_GRAY);
		g.drawString(stn, mid - snw / 2, y);
		if(isLeft) {
			setTextColor(left, g);
			stn = Integer.toString(left.getEffectiveAtk());
			snw = g.getFontMetrics().stringWidth(stn);
			g.drawString(stn, center1 - snw / 2, y);
		}
		if(isRight) {
			setTextColor(right, g);
			stn = Integer.toString(right.getEffectiveAtk());
			snw = g.getFontMetrics().stringWidth(stn);
			g.drawString(stn, center2 - snw / 2, y);
		}
		y += fontHeight;
		// Draw speed
		stn = "SPD";
		snw = g.getFontMetrics().stringWidth(stn);
		g.setColor(TEXT_GRAY);
		g.drawString(stn, mid - snw / 2, y);
		if(isLeft) {
			setTextColor(left, g);
			stn = Integer.toString(left.getEffectiveSpd());
			snw = g.getFontMetrics().stringWidth(stn);
			g.drawString(stn, center1 - snw / 2, y);
		}
		if(isRight) {
			setTextColor(right, g);
			stn = Integer.toString(right.getEffectiveSpd());
			snw = g.getFontMetrics().stringWidth(stn);
			g.drawString(stn, center2 - snw / 2, y);
		}
		// Draw other stats
		for(int i = 2; i < GameCharacter.STAT_NAMES.length; i++) {
			y += fontHeight;
			stn = GameCharacter.STAT_NAMES[i];
			snw = g.getFontMetrics().stringWidth(stn);
			g.setColor(TEXT_GRAY);
			g.drawString(stn, mid - snw / 2, y);
			if(isLeft) {
				setTextColor(left, g);
				stn = Integer.toString(left.getStats()[i]);
				snw = g.getFontMetrics().stringWidth(stn);
				g.drawString(stn, center1 - snw / 2, y);
			}
			if(isRight) {
				setTextColor(right, g);
				stn = Integer.toString(right.getStats()[i]);
				snw = g.getFontMetrics().stringWidth(stn);
				g.drawString(stn, center2 - snw / 2, y);
			}
		}
		return y;
	}
	
	private void setTextColor(GameCharacter gc, Graphics g) {
		if(gc.isEnemy())
			g.setColor(ENEMY_RED);
		else
			g.setColor(FRIENDLY_BLUE);
	}
	
	public MapConfiguration getMapConfig() {
		return mp.getMapConfig();
	}
	
	public GameState getGameState() {
		return mp.getGameStatus();
	}
	
}
