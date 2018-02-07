package control;

import ui.MapViewState;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Just indicates that an enum is a keybinding because inheritance is hard.
 * 
 * @author jhshi
 *
 */
public class KeyBinding<E extends Enum<E>> {
	
	private Map<Integer, E> bindings;
	
	public final static String startOfPath = "res/settings/keybindings_";
	
	public static DebugLogger log = new DebugLogger("KeyBinding");
	
	@SuppressWarnings("rawtypes")
	public static List<KeyBinding> allBindings = new ArrayList<KeyBinding>();
	
	private String path;
	
	public static void initAllBindings() {
		Controller.initGBABindings(1);
		MapViewState.initVisualBindings();
		
		checkAllBindingsForConflicts();
	}
	
	@SuppressWarnings("rawtypes")
	private static void checkAllBindingsForConflicts() {
		Set<Integer> keys = new HashSet<>();
		for(KeyBinding b : allBindings) {
			for(Object e : b.bindings.keySet()) {
				Integer f = (Integer) e;
				if(keys.contains(f))
					throw new IllegalArgumentException("Initialization failed; key <" + f + "> appears more than once.");
				keys.add(f);
			}
		}
		log.add("No conflicting bindings detected; all clear");
	}
	
	public KeyBinding(String name, E en) {
		this.path = startOfPath + name + ".txt";
		bindings = init(path, en);
		allBindings.add(this);
	}
	
	public E get(int n) {
		return bindings.get(n);
	}
	
	private static <E extends Enum<E>> Map<Integer, E> init(String path, Enum<E> en) {
		HashMap<Integer, E> bindings = new HashMap<Integer, E>();
		try(BufferedReader r = new BufferedReader(new FileReader(path))) {
			String line = r.readLine();
			String[] split;
			String k;
			String v;
			int c;
			while(line != null) {
				split = line.split(":");
				if(split.length != 2)
					throw new IllegalArgumentException("Improper colon detected in key binding file <" + path + ">");
				v = split[0];
				k = split[1];
				if(k.length() == 1)
					c = getKeyCode(k.charAt(0));
				else
					c = getKeyCode(k);
				try {
					bindings.put(c, Enum.valueOf(en.getDeclaringClass(), v));
					log.add("Generated keybinding from pair <" + k + ", " + v + ">");
				}
				catch (IllegalArgumentException | NullPointerException e) {
					log.add("Failed to generate keybinding from pair <" + k + ", " + v + ">");
					e.printStackTrace();
				}
				line = r.readLine();
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return bindings;
	}

	public static int getKeyCode(String s) {
		s = s.toUpperCase();
		if(s.equals("BACKSPACE"))
			return KeyEvent.VK_BACK_SPACE;
		else if(s.equals("RETURN") || s.equals("ENTER"))
			return KeyEvent.VK_ENTER;
		else if(s.equals("UP"))
			return KeyEvent.VK_UP;
		else if(s.equals("DOWN"))
			return KeyEvent.VK_DOWN;
		else if(s.equals("LEFT"))
			return KeyEvent.VK_LEFT;
		else if(s.equals("RIGHT"))
			return KeyEvent.VK_RIGHT;
		else if(s.equals("SPACE"))
			return KeyEvent.VK_SPACE;
		else
			return getKeyCode(s.charAt(0));
	}
	
	public static int getKeyCode(char c) {
		return KeyEvent.getExtendedKeyCodeForChar(c);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("KEY BINDING AT <<" + path + ">>\n");
		for(Map.Entry<Integer, E> entry : bindings.entrySet()) {
			sb.append(entry.getKey() + " -> " + entry.getValue() + "\n");
		}
		sb.append("\n");
		return sb.toString();
	}
	
	
}
