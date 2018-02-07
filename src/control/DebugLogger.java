package control;

import java.util.LinkedList;

public class DebugLogger {

	private LinkedList<String> msgList = new LinkedList<String>();
	private String name;
	private int maxSize;
	
	public static boolean global_verbose;
		
	public DebugLogger(String name) {
		this.name = name;
	}
	
	public void add(String msg) {
		msgList.add(msg);
		if(global_verbose)
			System.out.println(name + ": " + msg);
		if(maxSize == 0)
			return;
		else if(msgList.size() > maxSize)
			msgList.pop();
	}
	
	public void log(String msg) {
		add(msg);
	}
	
	public void log(Object o) {
		add(o.toString());
	}
	
	public void add(Object o) {
		add(o.toString());
	}
	
	public void flush() {
		msgList.clear();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("\n" + name + "\n____\n");
		for(String s : msgList) {
			sb.append(s + "\n");
		}
		sb.append("____");
		return sb.toString();
	}
	
}
