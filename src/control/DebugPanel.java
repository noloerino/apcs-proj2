package control;

import ui.MapPanel;
import ui.MapViewState;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.function.Supplier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JPanel;

public class DebugPanel extends JPanel {

	private MapViewState s;
	private MapPanel p;
	private MapViewListener l;
	
	private Map<String, Supplier<String>> funcs;
	
	public DebugPanel(MapViewState s, MapPanel p, MapViewListener l) {
		this.s = s;
		this.p = p;
		this.l = l;
		funcs = new HashMap<>();
		funcs.put("Game status (l)", () -> l.getStatus().toString());		
		funcs.put("Examined (s)", () -> s.getExamined().toString());
		funcs.put("Examined real coord (s)", () -> s.tileToCanvas(s.getExamined()).toString());
		funcs.put("Selected (s)", () -> s.getSelected().toString());
		funcs.put("Selected real coord (s)", () -> s.tileToCanvas(s.getSelected()).toString());
		funcs.put("Side to move (p/c)", () -> p.getMapConfig().getSideToMove().toString());
		funcs.put("Danger zone on (s)", () -> Boolean.toString(s.isDangerZoneOn()));
		funcs.put("Active actionable (l)", () -> l.activeActionableStr());
		funcs.put("Is moving (s)", () -> Boolean.toString(s.isMoving()));
		funcs.put("Is examining (s)", () -> Boolean.toString(s.isExamining()));
		funcs.put("Is selecting (s)", () -> Boolean.toString(s.isSelecting()));
		funcs.put("Active path (p/c)", () -> p.getMapConfig().getActivePath().toString());
	}
	
	@Override
	public void paintComponent(Graphics g) {
		g.setFont(new Font("Helvetica", Font.PLAIN, 12));
		g.setColor(Color.BLACK);
		int fontHeight = g.getFontMetrics().getHeight();
		Iterator<String> iter = funcs.keySet().iterator();
		int y = 20;
		for(int i = 0; i < funcs.size(); i++) {
			try {
				String key = iter.next();
				g.drawString(key + ": " + funcs.get(key).get(), 5, y + (fontHeight + 4) * i);
			}
			catch(NullPointerException e) {
				
			}
		}
	}
	
}
