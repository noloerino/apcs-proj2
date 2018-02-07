package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import control.DS_Actionable;
import control.DebugLogger;
import control.MapViewListener;

public class MoveMenu implements DS_Actionable {
	
	public final static String ATTACK = "Attack";
	public final static String MOVE = "Move"
			+ "";
	public final static String ITEMS = "Items";
	public final static String CANCEL = "Cancel";
	
	private String[] options1 = {ATTACK, MOVE, CANCEL};
	private String[] options2 = {MOVE, CANCEL};
	private String[] availableOptions;
	private int lastItem;
	private int firstItem;
	private int selected;
	
	private boolean onLeft;
	
	private boolean hidden;
	
	public static DebugLogger log = new DebugLogger("MoveMenu");
	
	private static int getBoxHeight(MapPanel caller) {
		return caller.getHeight() / 20;
	}
	
	public MoveMenu(MapPanel caller, boolean canAttack, boolean onLeft) {
		availableOptions = canAttack ? options1 : options2;
		firstItem = 0;
		lastItem = availableOptions.length - 1;
		selected = firstItem;
		this.onLeft = onLeft;
		hidden = false;
	}
	
	public int getX(MapPanel caller) {
		return onLeft ? caller.getWidth() * 1/7 : caller.getWidth() * 6/7 - getWidth(caller);
	}
	
	public int getY(MapPanel caller) {
		return caller.getHeight() * 1/7;
	}
	
	public int getWidth(MapPanel caller) {
		return caller.getWidth() / 6;
	}
	
	private void checkSelectedOOB() {
		if(selected < firstItem)
			selected = lastItem;
		else if(selected > lastItem)
			selected = firstItem;
	}
	
	private final Color BACKGROUND = new Color(255, 255, 204);
	private final Color TEXT = new Color(0, 0, 153);
	private final Color SELECTED = new Color(255, 191, 128);
	public final static Font OPTIMA = new Font("Optima", Font.BOLD, 16);
	public void paint(MapPanel caller, Graphics g) {
		if(hidden)
			return;
		g.setFont(OPTIMA);
		int x = getX(caller);
		int y = getY(caller);
		int wide = getWidth(caller);
		int high = getBoxHeight(caller);
		int fontHeight = 20;// cut because super laggy g.getFontMetrics().getHeight();
		g.setColor(BACKGROUND);
		((Graphics2D) g).setStroke(new BasicStroke(2));
		int y1 = y + high * selected;
		if(selected != 0)
			g.fillRect(x, y, wide, high * selected);
		if(selected != lastItem)
			g.fillRect(x, y1 + high, wide, high * (availableOptions.length - selected - 1));
		g.setColor(SELECTED);
		g.fillRect(x, y1, wide, high);
		for(int i = 0; i < availableOptions.length; i++) {
			g.setColor(Color.BLACK);
			g.drawLine(x, y, x + wide, y);
			g.setColor(TEXT);
			g.drawString(availableOptions[i], x + 10, y + high - (high - fontHeight) / 2);
			y += high;
		}

		g.setColor(Color.BLACK);
		g.drawRect(x, getY(caller), wide, high * availableOptions.length);
	}

	public void hide() {
		hidden = true;
	}
	
	public void unhide() {
		hidden = false;
	}
	
	@Override
	public void onUp(MapPanel caller) {
		selected--;
		checkSelectedOOB();
	}

	@Override
	public void onDown(MapPanel caller) {
		selected++;
		checkSelectedOOB();
	}

	@Override
	public void onLeft(MapPanel caller) { }

	@Override
	public void onRight(MapPanel caller) { }

	@Override
	public void onCancel(MapPanel caller) {
		dispose(caller.getMapViewListener(), CANCEL);
	}
	
	private void dispose(MapViewListener l, String cancel) {
		l.disposeMoveMenu(cancel);
	}

	@Override
	public void onConfirm(MapPanel caller) {
		dispose(caller.getMapViewListener(), availableOptions[selected]);
	}

	@Override
	public void onSelect(MapPanel caller) { }

	@Override
	public void onStart(MapPanel caller) { }

	@Override
	public void onLTrigger(MapPanel caller) { }

	@Override
	public void onRTrigger(MapPanel caller) { }

}
