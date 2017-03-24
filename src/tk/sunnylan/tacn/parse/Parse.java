package tk.sunnylan.tacn.parse;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

import tk.sunnylan.tacn.data.Assignment;
import tk.sunnylan.tacn.data.Mark;
import tk.sunnylan.tacn.data.Subject;

public class Parse {
	static final String MARKTABLE_KEYWORD = "Assignment";
	static final int MARKTABLE_NAME_COL = 0;
	static final int MARKTABLE_SECTION_ROW = 0;
	static final String WEIGHT_KEYWORD = "weight=";

	static final String WEIGHTTABLE_KEYWORD = "Course Weighting";
	static final int WEIGHTTABLE_HEADER_ROW = 0;
	static final int WEIGHTTABLE_NAME_COL = 0;
	static final int WEIGHTTABLE_WEIGHT_COL = 2;

	public static String getCourseCode(HtmlPage page) {
		return page.getElementsByTagName("h2").get(0).getTextContent();
	}

	public static void parseSubject(HtmlPage page, Subject s) throws Exception {
		DomNodeList<DomElement> tables = page.getElementsByTagName("table");
		HtmlTable tableMarks = null;
		HtmlTable tableWeights = null;
		for (DomElement elem : tables) {
			HtmlTable table = (HtmlTable) elem;
			DomNodeList<HtmlElement> headers = table.getElementsByTagName("th");

			if (elem.asText().contains(WEIGHTTABLE_KEYWORD)) {
				tableWeights = table;
			}

			if (tableMarks == null)
				for (HtmlElement e : headers) {
					if (e.getTextContent().contains(MARKTABLE_KEYWORD)) {
						tableMarks = table;
					}
				}
		}
		if (tableMarks == null)
			throw new Exception("Cannot find mark table");

		// TODO replace with two way compare (so object reference is not
		// updated)
		HtmlTableRow firstRow = tableMarks.getRow(MARKTABLE_SECTION_ROW);
		HashSet<String> newSections = new HashSet<>();
		int idx = 0;
		for (HtmlTableCell cell : firstRow.getCells()) {
			if (idx == MARKTABLE_NAME_COL) {
				idx++;
				continue;
			}
			newSections.add(Util.sanitizeSectionName(cell.getTextContent()));
			idx++;
		}
		s.sections = newSections;

		// compare for additions
		HashSet<String> additions = new HashSet<>();
		for (int i = 0; i < tableMarks.getRowCount(); i++) {
			if (i == MARKTABLE_SECTION_ROW)
				continue;
			String name = tableMarks.getCellAt(i, MARKTABLE_NAME_COL).getTextContent();
			if (name.isEmpty())
				continue;
			Assignment a;
			if (s.containsAssignment(name)) {
				a = s.getAssignment(name);
			} else {
				a = new Assignment();
			}

			updateAssignment(tableMarks, i, a);

			if (!s.containsAssignment(name)) {
				s.addAssignment(name, a);
			}
			additions.add(name);
			i++;
		}

		// process removals
		Iterator<Entry<String, Assignment>> it = s.getAssignmentIterator();
		Entry<String, Assignment> curr;
		while (it.hasNext()) {
			curr = it.next();
			if (!additions.contains(curr.getKey()))
				it.remove();
		}

		if (tableWeights != null) {

			// look for weights

			for (int i = 0; i < tableWeights.getRowCount(); i++) {
				if (i == WEIGHTTABLE_HEADER_ROW)
					continue;
				double val = Double.parseDouble(
						// TODO need to replace with a better section module
						tableWeights.getCellAt(i, WEIGHTTABLE_WEIGHT_COL).asText().replace("%", "").trim());
				s.weights.put(Util.sanitizeSectionName(tableWeights.getCellAt(i, WEIGHTTABLE_NAME_COL).asText()), val);
			}
		}
	}

	private static void updateAssignment(HtmlTable table, int row, Assignment a) throws Exception {
		// compare for additions
		HashSet<String> additions = new HashSet<>();
		List<HtmlTableCell> cells = table.getRow(row).getCells();

		for (int index = 0; index < cells.size(); index++) {
			if (index == MARKTABLE_NAME_COL) {
				continue;
			}
			HtmlTableCell cell = cells.get(index);
			String s = table.getCellAt(MARKTABLE_SECTION_ROW, index).getTextContent();
			s = Util.sanitizeSectionName(s);
			// System.out.println("curr:" + s + " -> " + index);
			DomNodeList<HtmlElement> sub = cell.getElementsByTagName("table");
			if (sub.size() == 1) {
				String text = cell.getTextContent();

				Mark m;
				boolean res;
				if (a.containsMark(s)) {
					m = a.getMark(s);
					res = parseMark(m, text);
				} else {
					m = new Mark(0, 0, 0);
					res = parseMark(m, text);
					a.addMark(m, s);
				}
				if (res)
					additions.add(s);

			}
		}

		// process removals
		Entry<String, Mark> curr;
		Iterator<Entry<String, Mark>> it = a.getMarkIterator();
		while (it.hasNext()) {
			curr = it.next();
			if (!additions.contains(curr.getKey())) {
				it.remove();
			}
		}
	}

	public static boolean parseMark(Mark m, String text) {
		try {
			String[] tmp1 = text.split("/");
			String[] tmp2 = tmp1[1].split("=");
			String x = tmp1[0].trim();
			String y = tmp2[0].trim();

			double num;
			double weight;
			if (x.isEmpty()) {
				num = 0;
				weight = 0;
			} else {
				num = Double.parseDouble(x);
			}
			double den;

			if (y.isEmpty()) {
				den = 0;
				weight = 0;
			} else {
				den = Double.parseDouble(y);
			}

			if (text.contains("no")) {
				weight = 0;
			} else {
				weight = Double
						.parseDouble(text.substring(text.indexOf(WEIGHT_KEYWORD) + WEIGHT_KEYWORD.length()).trim());
			}

			m.updateNumerator(num);
			m.updateDenominator(den);
			m.updateWeight(weight);
		} catch (Exception ex) {
			return false;
		}
		return true;

	}
}
