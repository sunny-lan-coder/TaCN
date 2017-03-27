package tk.sunnylan.tacn.parse;

import java.util.HashMap;
import java.util.Map.Entry;

public class Update {
	public final HashMap<String, SubjectChange> updates;
	public final HashMap<String, SubjectChange> additions;

	public Update() {
		updates = new HashMap<>();
		additions=new HashMap<>();
	}
	
	@Override
	public String toString(){
		String res="";
		res+="Additions:\n";
		for(Entry<String, SubjectChange> addition:additions.entrySet()){
			res+=addition.getKey()+":\n";
			res+=addition.getValue().toString()+"\n";
		}
		res+="Updates:\n";
		for(Entry<String, SubjectChange> update:updates.entrySet()){
			res+=update.getKey()+":\n";
			res+=update.getValue().toString()+"\n";
		}
		return res;
	}
}