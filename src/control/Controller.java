package control;

public class Controller {
	
	public static KeyBinding<DS_Action> DS_BINDINGS;
	
	public static DebugLogger log = new DebugLogger("Controller");
	
	protected static void initGBABindings(int num) {
		DS_BINDINGS = new DS_Binding(1);
	}
	
}
