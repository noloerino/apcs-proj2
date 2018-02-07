package core;

public enum Alignment {
	NEUTRAL, FRIENDLY, ENEMY;
	
	public static boolean areOpposed(Alignment a, Alignment b) {
		if(a == NEUTRAL || b == NEUTRAL)
			return false;
		else
			return a != b;
	}
	
	public static Alignment opposing(Alignment a) {
		if(a == NEUTRAL)
			return a;
		else if(a == FRIENDLY)
			return ENEMY;
		else
			return FRIENDLY;
	}
}
