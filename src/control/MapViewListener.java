package control;

import core.GameState;
import core.IntVector2D;
import ui.MapPanel;
import ui.MapViewState;
import ui.MoveMenu;
import ui.VisualAction;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.*;

public class MapViewListener implements MouseListener, MouseWheelListener, MouseMotionListener,
		KeyListener {

	private MapPanel panel;
	// DS_Actionables
	private MapViewState viewState;
	private MoveMenu moveMenu;
	public EnemyController eController;
	public LOC locController;
	
	private GameState status;
	
	public GameState getStatus() {
		return status;
	}
	
	public void setStatus(GameState status) {
		this.status = status;
		log.log("Setting status to " + status);
	}
	
	private boolean dragging;
	private IntVector2D lastEvt;
	
	private final static Cursor dragCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
	private final static Cursor defCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	
	public static DebugLogger log = new DebugLogger("MapViewListener");
	
	private DS_Actionable activeDS_Actionable;
	
	public MapViewListener(MapPanel panel, MapViewState viewState) {
		this.panel = panel;
		this.viewState = viewState;
		panel.addMouseListener(this);
		panel.addMouseWheelListener(this);
		panel.addMouseMotionListener(this);
		panel.addKeyListener(this);
		panel.setMapViewListener(this);
		log.add("Initialized MapViewListener");
		status = GameState.PLAYER_MOVING;
		activeDS_Actionable = viewState;
		eController = new EnemyController(panel.getMapConfig());
		locController = new LOC();
	}
	
	public void refresh(MapPanel panel) {
		this.panel = panel;
		panel.addMouseListener(this);
		panel.addMouseWheelListener(this);
		panel.addMouseMotionListener(this);
		panel.addKeyListener(this);
		panel.setMapViewListener(this);
		log.add("Refreshed MapViewListener");
		status = GameState.PLAYER_MOVING;
		activeDS_Actionable = viewState;
		eController = new EnemyController(panel.getMapConfig());
		locController = new LOC();
	}
	
	public final static int VIEW_STATE = 0;
	public final static int MOVE_MENU = 1;
	public final static int LOC_CTRL = 2;
	public final static int ENEMY_CTRL = 3;
	public void switchActionableTo(int act) {
		switch(act) {
			case VIEW_STATE:
				this.activeDS_Actionable = viewState;
				break;
			case MOVE_MENU:
				this.activeDS_Actionable = moveMenu;
				break;
			case LOC_CTRL:
				this.activeDS_Actionable = locController;
				break;
			case ENEMY_CTRL:
				this.activeDS_Actionable = eController;
			default:
				throw new IllegalArgumentException();
		}
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(statusAllowsVisualControl())
			viewState.zoom(panel, -1 * e.getWheelRotation(), e.getX(), e.getY());
	}
	
	public MapViewState getMapViewState() {
		return viewState;
	}
	
	public String activeActionableStr() {
		if(activeDS_Actionable == viewState)
			return "viewState";
		else if(activeDS_Actionable == moveMenu)
			return "moveMenu";
		else if(activeDS_Actionable == locController)
			return "locController";
		else if(activeDS_Actionable == eController)
			return "eController";
		else
			return "";
	}
	
	public void paintMenus(MapPanel caller, Graphics g) {
		if(moveMenu != null) {
			moveMenu.paint(caller, g);
		}
	}
	
	public void setMoveMenu(MapPanel caller, boolean canAttack) {
		moveMenu = new MoveMenu(caller, canAttack,
				viewState.tileToCanvas(viewState.getSelected()).getX() + viewState.getScaledImgSize() / 2 < caller.getWidth() / 2);
		log.log("Creating moveMenu");
		switchActionableTo(MOVE_MENU);
	}
	
	/**
	 * Disposes of the move menu.
	 * 
	 * @param action A string representing the action to be taken after disposal.
	 */
	public void disposeMoveMenu(String action) {
		log.log("Disposing of moveMenu");
		if(action.equals(MoveMenu.CANCEL)) {
			if(getStatus() == GameState.PLAYER_CONFIRMING_MOVE) {
				moveMenu = null;
				switchActionableTo(VIEW_STATE);
				setStatus(GameState.PLAYER_MOVING);
			}
			else if(getStatus() == GameState.PLAYER_CONFIRMING_ATTACK) {
				moveMenu.unhide();
				switchActionableTo(MOVE_MENU);
				setStatus(GameState.PLAYER_CONFIRMING_MOVE);
			}
		}
		else if(action.equals(MoveMenu.ATTACK)) {
			if(getStatus() == GameState.PLAYER_CONFIRMING_MOVE) {
				moveMenu.hide();
				setStatus(GameState.PLAYER_CONFIRMING_ATTACK);
				switchActionableTo(VIEW_STATE);
			}
			else {
				moveMenu = null;
				switchActionableTo(VIEW_STATE);
			}
		}
		else if(action.equals(MoveMenu.ITEMS)) {
			
		}
		else if(action.equals(MoveMenu.MOVE)) {
			moveMenu = null;
			switchActionableTo(VIEW_STATE);
			// order matters here
			panel.getMapConfig().attemptMove(panel);
		}
		else {
			switchActionableTo(VIEW_STATE);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) { }

	@Override
	public void mousePressed(MouseEvent e) {
		if(statusAllowsVisualControl()) {
			if(e.getButton() != MouseEvent.BUTTON3)
				return;
			panel.setCursor(dragCursor);
			dragging = true;
			lastEvt = new IntVector2D(e.getX(), e.getY());
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		panel.setCursor(defCursor);
		dragging = false;
		lastEvt = null;
	}

	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) { }

	@Override
	public void mouseDragged(MouseEvent e) {
		if(statusAllowsVisualControl()) {
			if(!dragging || lastEvt == null)
				return;
			IntVector2D nextEvt = new IntVector2D(e.getX(), e.getY());
			viewState.pan(panel, nextEvt.sub(lastEvt));
			lastEvt.set(nextEvt);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) { }

	@Override
	public void keyTyped(KeyEvent e) { }

	@Override
	public void keyPressed(KeyEvent e) {
		int c = e.getKeyCode();
		if(e.isShiftDown())
			c = KeyBinding.getKeyCode(e.getKeyChar());
		DS_Action dsaction = Controller.DS_BINDINGS.get(c);
		if(dsaction != null) {
			keyPressFromDS_Action(dsaction);
			return;
		}
		if(statusAllowsVisualControl()) {
			VisualAction vaction = MapViewState.VISUAL_BINDINGS.get(c);
			if(vaction != null) {
				keyPressFromVisualAction(vaction);
				return;
			}
		}
	}
	
	private void keyPressFromVisualAction(VisualAction action) {
		log.add("Triggered VisualAction: " + action);
		switch(action) {
			case PAN_DOWN:
				viewState.pan(panel, new IntVector2D(0, -72));
				break;
			case PAN_LEFT:
				viewState.pan(panel, new IntVector2D(72, 0));
				break;
			case PAN_RIGHT:
				viewState.pan(panel, new IntVector2D(-72, 0));
				break;
			case PAN_UP:
				viewState.pan(panel, new IntVector2D(0, 72));
				break;
			case ZOOM_IN:
				viewState.zoom(panel, 10, panel.getWidth() / 2, panel.getHeight() / 2);
				break;
			case ZOOM_OUT:
				viewState.zoom(panel, -10, panel.getWidth() / 2, panel.getHeight() / 2);
				break;
			default:
				break;
		}
	}
	
	private void keyPressFromDS_Action(DS_Action action) {
		log.add("Triggered DS_Action: " + action);
		switch(action) {
			case CANCEL:
				activeDS_Actionable.onCancel(panel);
				break;
			case CONFIRM:
				activeDS_Actionable.onConfirm(panel);
				break;
			case DOWN:
				activeDS_Actionable.onDown(panel);
				break;
			case LEFT:
				activeDS_Actionable.onLeft(panel);
				break;
			case L_TRIGGER:
				activeDS_Actionable.onLTrigger(panel);
				break;
			case RIGHT:
				activeDS_Actionable.onRight(panel);
				break;
			case R_TRIGGER:
				activeDS_Actionable.onRTrigger(panel);
				break;
			case SELECT:
				activeDS_Actionable.onSelect(panel);
				break;
			case START:
				activeDS_Actionable.onStart(panel);
				break;
			case UP:
				activeDS_Actionable.onUp(panel);
				break;
			default:
				break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) { }
	
	public boolean statusAllowsVisualControl() {
		return (status == GameState.PAUSE_MENU || status == GameState.PLAYER_MOVING
				|| status == GameState.PLAYER_CONFIRMING_MOVE
				|| status == GameState.PLAYER_CONFIRMING_ATTACK
				|| status == GameState.ENEMY_TURN);
	}

}
