package tk.sunnylan.tacn.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Assignment {
	private HashMap<String, Mark> marks;
	public int timeCode;

	public Assignment() {
		marks = new HashMap<>();
	}

	public Assignment(Element docroot) {
		this();
		timeCode = Integer.parseInt(docroot.getElementsByTagName("timecode").item(0).getTextContent().trim());
		NodeList l = docroot.getElementsByTagName("marks").item(0).getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			if (!(l.item(i) instanceof Element))
				continue;
			Element tmp = (Element) l.item(i);
			String section = tmp.getElementsByTagName("section").item(0).getTextContent();
			Mark m = new Mark((Element) tmp.getElementsByTagName("value").item(0));
			marks.put(section, m);
		}
	}

	public Iterator<Entry<String, Mark>> getMarkIterator() {
		return new Iterator<Entry<String, Mark>>() {
			private Iterator<Entry<String, Mark>> wrap = marks.entrySet().iterator();

			@Override
			public boolean hasNext() {
				return wrap.hasNext();
			}

			@Override
			public Entry<String, Mark> next() {
				return wrap.next();
			}

			@Override
			public void remove() {
				wrap.remove();
				// TODO raise event
			}
		};
	}

	public Mark getMark(String section) {
		return marks.get(section);
	}

	public void addMark(Mark mark, String section) throws Exception {
		if (marks.containsKey(section))
			throw new Exception("There is already a mark in that section");
		marks.put(section, mark);
	}

	public void clearMark(String section) {
		marks.remove(section);
	}

	public boolean containsMark(String section) {
		return marks.containsKey(section);
	}

	@Override
	public String toString() {
		String s = "<timecode>";
		s += timeCode;
		s += "</timecode>";
		s += "<marks>";
		for (String section : marks.keySet()) {
			s += "<mark><section>" + section + "</section><value>" + marks.get(section).toString() + "</value></mark>";
		}
		s += "</marks>";
		return s;
	}
}
