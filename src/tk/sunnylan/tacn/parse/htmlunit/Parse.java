package tk.sunnylan.tacn.parse.htmlunit;

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
import tk.sunnylan.tacn.parse.ChangeType;
import tk.sunnylan.tacn.parse.SubjectChange;

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

	public static SubjectChange parseSubject(HtmlPage page, Subject s) throws Exception {
		SubjectChange changes = new SubjectChange();
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

		HtmlTableRow firstRow = tableMarks.getRow(MARKTABLE_SECTION_ROW);
		int idx = 0;
		for (HtmlTableCell cell : firstRow.getCells()) {
			if (idx == MARKTABLE_NAME_COL) {
				idx++;
				continue;
			}
			s.sections.add(Util.sanitizeSectionName(cell.getTextContent()));
			idx++;
		}

		// compare for additions
		HashSet<String> additions = new HashSet<>();
		for (int i = 0; i < tableMarks.getRowCount(); i++) {
			if (i == MARKTABLE_SECTION_ROW)
				continue;
			String name = tableMarks.getCellAt(i, MARKTABLE_NAME_COL).getTextContent();
			if (name.isEmpty())
				continue;
			Assignment a;
			boolean flag = false;
			if (s.containsAssignment(name)) {
				a = s.getAssignment(name);
			} else {
				a = new Assignment();
				flag = true;
				changes.changes.put(name, ChangeType.ADDED);

			}

			if (updateAssignment(tableMarks, i, a))
				if (!flag)
					changes.changes.put(name, ChangeType.UPDATED);

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
			if (!additions.contains(curr.getKey())) {
				changes.changes.put(curr.getKey(), ChangeType.REMOVED);
				it.remove();
			}
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
		return changes;
	}

	private static boolean updateAssignment(HtmlTable table, int row, Assignment a) throws Exception {
		boolean changed = false;
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
			DomNodeList<HtmlElement> sub = cell.getElementsByTagName("table");
			if (sub.size() == 1) {
				String text = cell.getTextContent();

				Mark m;
				MarkChange res;
				if (a.containsMark(s)) {
					m = a.getMark(s);

					res = _parseMark(m, text);

					changed |= res.changed;
				} else {
					m = new Mark(0, 0, 0);
					res = _parseMark(m, text);
					a.addMark(m, s);
					changed = true;
				}
				if (res.output)
					additions.add(s);
			}
		}

		// process removals
		Entry<String, Mark> curr;
		Iterator<Entry<String, Mark>> it = a.getMarkIterator();
		while (it.hasNext()) {
			curr = it.next();
			if (!additions.contains(curr.getKey())) {
				changed = true;
				it.remove();
			}
		}
		a.timeCode = row;

		return changed;
	}

	public static boolean parseMark(Mark m, String text) {
		return _parseMark(m, text).output;

	}

	private static MarkChange _parseMark(Mark m, String text) {
		boolean changed = false;
		try {
			String[] tmp1 = text.split("/");
			String[] tmp2 = tmp1[1].split("=");
			String x = tmp1[0].trim();
			String y = tmp2[0].trim();

			double weight;
			if (text.contains("no")) {
				weight = 0;
			} else {
				weight = Double
						.parseDouble(text.substring(text.indexOf(WEIGHT_KEYWORD) + WEIGHT_KEYWORD.length()).trim());
			}
			double num;
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

			if (m.getDenominator() != den)
				changed = true;
			if (m.getNumerator() != num)
				changed = true;
			if (m.getWeight() != weight)
				changed = true;
			m.updateNumerator(num);
			m.updateDenominator(den);
			m.updateWeight(weight);
		} catch (Exception ex) {
			return new MarkChange(false, changed);
		}
		return new MarkChange(true, changed);

	}

	private static class MarkChange {
		public final boolean output;
		public final boolean changed;

		public MarkChange(boolean output, boolean changed) {
			this.output = output;
			this.changed = changed;
		}
	}

}
