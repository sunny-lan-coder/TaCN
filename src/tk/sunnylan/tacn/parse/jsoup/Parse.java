package tk.sunnylan.tacn.parse.jsoup;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import tk.sunnylan.tacn.data.Assignment;
import tk.sunnylan.tacn.data.Mark;
import tk.sunnylan.tacn.data.Subject;
import tk.sunnylan.tacn.parse.ChangeType;
import tk.sunnylan.tacn.parse.SubjectChange;
import tk.sunnylan.tacn.parse.htmlunit.Util;

public class Parse {
	static final String MARKTABLE_KEYWORD = "Assignment";
	static final int MARKTABLE_NAME_COL = 0;
	static final int MARKTABLE_SECTION_ROW = 0;
	static final String WEIGHT_KEYWORD = "weight=";

	static final String WEIGHTTABLE_KEYWORD = "Course.Weighting";
	static final int WEIGHTTABLE_HEADER_ROW = 0;
	static final int WEIGHTTABLE_NAME_COL = 0;
	static final int WEIGHTTABLE_WEIGHT_COL = 2;

	public static String getCourseCode(Document page) {
		return page.select("div.green_border_message.box > div > div > h2").first().text();
	}

	public static boolean parseMark(Mark m, String text) {
		return _parseMark(m, text).output;

	}

	public static SubjectChange parseSubject(Document page, Subject s) throws Exception {
		SubjectChange changes = new SubjectChange();
		Element tableMarks = page.select("th:contains(" + MARKTABLE_KEYWORD + ")").first().parent().parent();
		Elements l = page.select("th:contains(course), th:contains(weighting)");
		Element tableWeights = null;
		if (l.size() > 0){
			tableWeights=l.first().parent().parent().parent();
		}
		
		// TODO error checking

		Elements rows = tableMarks.select(":root > tr");

		Elements sectionRow = rows.get(MARKTABLE_SECTION_ROW).select(":root > td, th");
		int idx = 0;
		for (Element cell : sectionRow) {
			if (idx == MARKTABLE_NAME_COL) {
				idx++;
				continue;
			}
			s.sections.add(Util.sanitizeSectionName(cell.text()));
			idx++;
		}

		// compare for additions
		HashSet<String> additions = new HashSet<>();
		for (int i = 0; i < rows.size(); i++) {
			if (i == MARKTABLE_SECTION_ROW)
				continue;
			Elements currRow = rows.get(i).select(":root > td");
			String name = currRow.get(MARKTABLE_NAME_COL).text();
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

			if (updateAssignment(sectionRow, rows, i, a))
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
			Elements weightRows = tableWeights.select(":root > tbody > tr");
			for (int i = 0; i < weightRows.size(); i++) {
				if (i == WEIGHTTABLE_HEADER_ROW)
					continue;
				Elements cells = weightRows.get(i).select(":root > td");
				double val = Double.parseDouble(
						// TODO need to replace with a better section module
						cells.get(WEIGHTTABLE_WEIGHT_COL).text().replace("%", "").trim());
				s.weights.put(Util.sanitizeSectionName(cells.get(WEIGHTTABLE_NAME_COL).text()), val);
			}
		}
		return changes;
	}

	private static boolean updateAssignment(Elements toprowcells, Elements tablerows, int row, Assignment a)
			throws Exception {
		boolean changed = false;
		// compare for additions
		HashSet<String> additions = new HashSet<>();
		Elements cells = tablerows.get(row).select(":root > td");

		for (int index = 0; index < cells.size(); index++) {
			if (index == MARKTABLE_NAME_COL) {
				continue;
			}
			Element cell = cells.get(index);
			String s = toprowcells.get(index).text();
			s = Util.sanitizeSectionName(s);
			// System.out.println("curr:" + s + " -> " + index);
			Elements sub = cell.select(":root > table");
			if (sub.size() == 1) {
				String text = cell.text();

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
