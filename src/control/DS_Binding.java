package control;

public class DS_Binding extends KeyBinding<DS_Action> {
	
	public DS_Binding(int num) {
		super("ds_" + num, DS_Action.CANCEL);
	}

}
