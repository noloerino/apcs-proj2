package core.exceptions;

public abstract class LevelEndingException extends RuntimeException {

	public final int EXCEPTION_CODE;
	public final static int PLAYER_WIN = 1;
	public final static int PLAYER_LOSS = 2;
	
	public LevelEndingException(String msg, int code) {
		super(msg);
		EXCEPTION_CODE = code;
	}
	
}
