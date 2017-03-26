package tk.sunnylan.tacn.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import tk.sunnylan.tacn.parse.htmlunit.Util;

public class Subject {
	public String courseCode;
	public HashSet<String> sections;
	public HashMap<String, Double> weights;
	private HashMap<String, Assignment> assignments;

	public Subject(String courseCode) {
		this.courseCode = courseCode;
		assignments = new HashMap<>();
		sections = new HashSet<>();
		weights = new HashMap<>();
	}

	public Subject(Element docroot) {
		this(docroot.getElementsByTagName("courseCode").item(0).getTextContent());
		NodeList l = docroot.getElementsByTagName("sections").item(0).getChildNodes();
		for (int i = 0; i < l.getLength(); i ++) {
			String s=Util.sanitizeSectionName(l.item(i).getTextContent());
			if(!s.isEmpty())
			sections.add(s);
		}
		l = docroot.getElementsByTagName("weights").item(0).getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			if(!(l.item(i) instanceof Element))
				continue;
			String sect = Util.sanitizeSectionName(
					((Element) l.item(i)).getElementsByTagName("section").item(0).getTextContent());
			weights.put(sect, Double
					.parseDouble(((Element) l.item(i)).getElementsByTagName("value").item(0).getTextContent().trim()));

		}
		l = docroot.getElementsByTagName("assignments").item(0).getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			if(!(l.item(i) instanceof Element))
				continue;
			Element tmp = ((Element) l.item(i));
			String name = tmp.getElementsByTagName("name").item(0).getTextContent();
			Assignment a = new Assignment((Element) tmp.getElementsByTagName("value").item(0));
			assignments.put(name, a);
		}
	}

	public Iterator<Entry<String, Assignment>> getAssignmentIterator() {
		return new Iterator<Entry<String, Assignment>>() {
			private Iterator<Entry<String, Assignment>> wrap = assignments.entrySet().iterator();

			@Override
			public boolean hasNext() {
				return wrap.hasNext();
			}

			@Override
			public Entry<String, Assignment> next() {
				return  wrap.next();
			}

			@Override
			public void remove() {
				wrap.remove();
			}
		};
	}

	public void addAssignment(String name, Assignment a) {
		assignments.put(name, a);
	}

	public void clearAssignment(String name) {
		assignments.remove(name);
	}

	public boolean containsAssignment(String name) {
		return assignments.containsKey(name);
	}

	public Assignment getAssignment(String name) {
		return assignments.get(name);
	}

	@Override
	public String toString() {
		String s = "";
		s += "<courseCode>" + courseCode + "</courseCode>";
		s += "<sections>";
		for (String section : sections) {
			s += "<section>" + section + "</section>";
		}
		s += "</sections>";
		s += "<weights>";
		for (String section : weights.keySet()) {
			s += "<weight><section>" + section + "</section>";
			if (weights.containsKey(section))
				s += "<value>" + weights.get(section) + "</value></weight>";
		}
		s += "</weights>";
		s += "<assignments>";
		for (String name : assignments.keySet()) {
			s += "<assignment><name>" + name + "</name>";
			s += "<value>" + assignments.get(name).toString() + "</value>";
			s += "</assignment>";
		}
		s += "</assignments>";
		return s;
	}
}
