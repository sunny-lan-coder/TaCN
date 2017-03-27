package tk.sunnylan.tacn.parse;

import java.util.HashMap;

public class Update {
	public final HashMap<String, SubjectChange> updates;
	public final HashMap<String, SubjectChange> additions;

	public Update() {
		updates = new HashMap<>();
		additions=new HashMap<>();
	}
}