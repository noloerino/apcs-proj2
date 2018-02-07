package core.exceptions;

public class PlayerLosesException extends LevelEndingException {

	public PlayerLosesException() {
		super("Level ended by player loss", PLAYER_LOSS);
	}
	
}
