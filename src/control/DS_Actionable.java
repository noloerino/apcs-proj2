package control;

import ui.MapPanel;

/**
 * Any object that implements responses to all of the buttons found on
 * the old Nintendo DS.
 * 
 * @author jhshi
 *
 */
public interface DS_Actionable {

	public void onUp(MapPanel caller);
	
	public void onDown(MapPanel caller);
	
	public void onLeft(MapPanel caller);
	
	public void onRight(MapPanel caller);
	
	public void onCancel(MapPanel caller);
	
	public void onConfirm(MapPanel caller);
	
	public void onSelect(MapPanel caller);
	
	public void onStart(MapPanel caller);
	
	public void onLTrigger(MapPanel caller);
	
	public void onRTrigger(MapPanel caller);
	
}
