package tk.sunnylan.tacn.parse;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import tk.sunnylan.tacn.data.Mark;

public class Util {
	public static String sanitizeFileName(String s){
		return s.replaceAll("[^a-zA-Z0-9.-]", "_");
	}
	// TODO need to replace with a better section module
	public static String sanitizeSectionName(String s) {
		return s.replaceAll("\\s", "");
	}
	
	public static String convertMarkToString(Mark m){
		if(m.getWeight()==0)
			return String.format("%.1f/%.0f=%.0f%%%nno weight", m.getNumerator(),
					m.getDenominator(), m.percentage(), m.getWeight());
		return String.format("%.1f/%.0f=%.0f%%%nweight=%.0f", m.getNumerator(),
				m.getDenominator(), m.percentage(), m.getWeight());
	}
	
	public static Element getFirstElement(NodeList lst){
		for(int i=0;i<lst.getLength();i++){
			if(lst.item(i) instanceof Element)
				return (Element) lst.item(i);
		}
		return null;
	}
}
