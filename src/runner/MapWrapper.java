package runner;

import control.*;
import ui.*;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.Timer;

public class MapWrapper { 
	
	public static DebugLogger log = new DebugLogger("MapWrapper");
	
	private static DebugPanel dbp;
	private static StatsPanel stp;
	private static MapPanel mp;
	private static MapViewState s;
	private static MapViewListener l;
	private static JFrame mainWindow;
	private static JFrame debugWindow;
	private static JFrame statsWindow;
	private static Timer t;
	
	private static boolean hasRun = false;
	
	private static MainMenu mainMenu;
		
	private MapWrapper() { }
	
	public static void main(String[] args) {
		DebugLogger.global_verbose = true;
		
		mainMenu = new MainMenu("res/saves/");
		
		mainWindow = new JFrame("Definitely Not Fire Emblem");
		mainWindow.setResizable(false);
		mainWindow.setContentPane(mainMenu);
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setSize(720, 720);
		mainWindow.setLocation(100, 200);
		mainWindow.setVisible(true);
		
		KeyBinding.initAllBindings();
	}
	
	public static void run(String mapName, String saveName) {
		if(hasRun) {
			runNextLevel(mapName, saveName);
			return;
		}
		s = new MapViewState();
		mp = new MapPanel(s, mapName, saveName);
		mainWindow.setContentPane(mp);
		mainWindow.setVisible(true);
		l = new MapViewListener(mp, s);
		if(DebugLogger.global_verbose) {
			debugWindow = new JFrame("Debug");
			debugWindow.setResizable(false);
			dbp = new DebugPanel(s, mp, l);
			debugWindow.setLocation(1000, 800);
			debugWindow.setSize(400, 300);
			debugWindow.setContentPane(dbp);
			debugWindow.setVisible(true);
		}
		statsWindow = new JFrame("Stats View");
		statsWindow.setResizable(false);
		stp = new StatsPanel(s, mp, l);
		statsWindow.setLocation(1000, 200);
		statsWindow.setSize(400, 320);
		statsWindow.setContentPane(stp);
		statsWindow.setVisible(true);
		statsWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		s.setMapViewDisp(1000, 1000);
		Graphics g = mp.getGraphics(); // resolves lag
		g.setFont(MoveMenu.OPTIMA);
		g.drawString("TEST", -100, -100);
		
		s.centerScreen(mp);
		
		log.log("Map " + mapName + " on save " + saveName + " is ready to go!");
		
		mainWindow.toFront();
		mp.requestFocusInWindow();
		t = new Timer(7, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				mp.repaint();
				if(dbp != null)
					dbp.repaint();
				if(stp != null)
					stp.repaint();
			}
			
		});
		t.start();
		hasRun = true;
	}
	
	public static void runNextLevel(String mapName, String saveName) {
		mp = new MapPanel(s, mapName, saveName);
		mainWindow.setContentPane(mp);
		mainWindow.setVisible(true);
		l.refresh(mp);
		s.setMapViewDisp(1000, 1000);
		dbp = new DebugPanel(s, mp, l);
		debugWindow.setContentPane(dbp);
		debugWindow.setVisible(true);
		stp = new StatsPanel(s, mp, l);
		statsWindow.setContentPane(stp);
		statsWindow.setVisible(true);
		Graphics g = mp.getGraphics(); // resolves lag
		g.setFont(MoveMenu.OPTIMA);
		g.drawString("TEST", -100, -100);
		
		s.centerScreen(mp);
		
		log.log("Map " + mapName + " on save " + saveName + " is ready to go!");
		mp.requestFocusInWindow();
		t.start();
	}
	
	public static void returnToMenu() {
		t.stop();
		mp.removeKeyListener(l);
		mp.removeMouseListener(l);
		mp.removeMouseMotionListener(l);
		mp.removeMouseWheelListener(l);
		mainMenu.refresh();
		mainWindow.setContentPane(mainMenu);
		mainWindow.setVisible(true);
	}

}
