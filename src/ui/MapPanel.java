package ui;

import control.MapViewListener;
import core.GameState;
import core.exceptions.*;
import runner.MapWrapper;
import worldmap.*;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;
import javax.swing.JPanel;

public class MapPanel extends JPanel {

	private MapModelState map;
	private MapViewListener l;
	private String mapName;
	
	public MapPanel(MapViewState s, String mapName, String saveName) {
		MapEntity._setup();
		this.mapName = mapName;
		try {
			map = new MapModelState(new MapConfiguration(mapName, s, saveName));
		}
		catch(NullPointerException e) {
			System.out.println(MapEntity.log.toString());
			e.printStackTrace();
		}
		transitionFramesRemaining = 300;
		status = -1;
		s.centerScreen(this); // May not work until content pane is added.
	}
	
	public GameState getGameStatus() {
		return l.getStatus();
	}
	
	public void setGameStatus(GameState status) {
		l.setStatus(status);
	}
	
	public void setMapViewListener(MapViewListener l) {
		this.l = l;
	}
	
	public MapViewListener getMapViewListener() {
		return l;
	}
	
	public MapModelState getMapModelState() {
		return map;
	}
	
	public MapConfiguration getMapConfig() {
		return map.getConfig();
	}
	
	public MapViewState getMapViewState() {
		return map.getConfig().getMapViewState();
	}
	
	/**
	 * Gets the pixel height of the map.
	 * 
	 * @return
	 */
	public int getMapHeight() {
		return map.getConfig().getPxHeight();
	}
	
	/**
	 * Gets the pixel width of the map.
	 * 
	 * @return
	 */
	public int getMapWidth() {
		return map.getConfig().getPxWidth();
	}
	
	public int getYTiles() {
		return map.getConfig().getYTiles();
	}
	
	public int getXTiles() {
		return map.getConfig().getXTiles();
	}
	
	public final static Font BIG_OPTIMA = new Font("Optima", Font.BOLD, 36);
	
	private int status;
	private int transitionFramesRemaining;
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			     RenderingHints.VALUE_ANTIALIAS_ON);
		if(status == -1) { // Level starting
			String[] msgs = {
					"<<" + mapName + ">>",
					"OBJECTIVE:",
					"Rout all enemies."
			};
			g.setFont(BIG_OPTIMA);
			int fontHeight = g.getFontMetrics().getHeight();
			for(int i = 0; i < msgs.length; i++) {
				int wide = g.getFontMetrics().stringWidth(msgs[i]);
				g.drawString(msgs[i], getWidth() / 2 - wide / 2, getHeight() / 2 - (2 - i) * fontHeight);
			}
			transitionFramesRemaining--;
			if(transitionFramesRemaining <= 0)
				status = 0;
		}
		else if(status == LevelEndingException.PLAYER_WIN) {
			String[] msgs = {"Stage clear!", "Returning to menu..."};
			g.setFont(BIG_OPTIMA);
			int fontHeight = g.getFontMetrics().getHeight();
			for(int i = 0; i < msgs.length; i++) {
				int wide = g.getFontMetrics().stringWidth(msgs[i]);
				g.drawString(msgs[i], getWidth() / 2 - wide / 2, getHeight() / 2 - (2 - i) * fontHeight);
			}
			if(getGameStatus() != GameState.BASE_MENU)
				setGameStatus(GameState.BASE_MENU);
			transitionFramesRemaining--;
			if(transitionFramesRemaining == 0)
				MapWrapper.returnToMenu();
		}
		else if(status == LevelEndingException.PLAYER_LOSS) {
			String[] msgs = {"Stage failed!", "Returning to menu..."};
			g.setFont(BIG_OPTIMA);
			int fontHeight = g.getFontMetrics().getHeight();
			for(int i = 0; i < msgs.length; i++) {
				int wide = g.getFontMetrics().stringWidth(msgs[i]);
				g.drawString(msgs[i], getWidth() / 2 - wide / 2, getHeight() / 2 - (2 - i) * fontHeight);
			}
			if(getGameStatus() != GameState.BASE_MENU)
				setGameStatus(GameState.BASE_MENU);
			transitionFramesRemaining--;
			if(transitionFramesRemaining == 0)
				MapWrapper.returnToMenu();
		}
		else {
			try {
				/**
				 * Order should be:
				 * 1) Paint terrain
				 * 2) Paint viewState elements
				 * 3) Paint characters and ghosts
				 * 4) Paint menus
				 */
				map.paint(this, g);
				getMapViewState().paintElements(this, g);
				l.paintMenus(this, g);
			}
			catch(PlayerWinsException e) {
				status = e.EXCEPTION_CODE;
				transitionFramesRemaining = 200;
				map.getConfig().save();
			}
			catch(PlayerLosesException e) {
				status = e.EXCEPTION_CODE;
				transitionFramesRemaining = 200;
			}
			catch(NullPointerException | IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}

}
