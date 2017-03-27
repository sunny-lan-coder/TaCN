package tk.sunnylan.tacn.parse;

import java.util.HashMap;
import java.util.Map.Entry;

public class SubjectChange {
	public final HashMap<String, ChangeType> changes;

	public SubjectChange() {
		changes = new HashMap<>();
	}
	
	@Override
	public String toString(){
		String s="";
		for(Entry<String, ChangeType> change:changes.entrySet()){
			s+=change.getKey()+": "+change.getValue().toString()+"\n";
		}
		return s;
	}
}