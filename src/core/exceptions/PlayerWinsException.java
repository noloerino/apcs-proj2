package core.exceptions;

public class PlayerWinsException extends LevelEndingException {

	public PlayerWinsException() {
		super("Level ended by player win", PLAYER_WIN);
	}
	
}
