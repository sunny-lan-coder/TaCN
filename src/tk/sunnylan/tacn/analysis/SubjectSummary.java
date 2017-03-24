package tk.sunnylan.tacn.analysis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import tk.sunnylan.tacn.data.Assignment;
import tk.sunnylan.tacn.data.Mark;
import tk.sunnylan.tacn.data.Subject;

public class SubjectSummary {
	private Subject s;
	public final HashMap<String, Double> averages;
	public Double average;

	public SubjectSummary(Subject s) {
		this.s = s;
		averages = new HashMap<>();
	}

	public void refresh() {
		average = 0.0;
		double tw = 0;
		for (String section : s.sections) {
			double totalWeight = 0;
			double totalPercent = 0;
			Iterator<Entry<String, Assignment>> iter = s.getAssignmentIterator();
			while (iter.hasNext()) {
				Assignment curr = iter.next().getValue();
				if (curr.containsMark(section)) {
					Mark m = curr.getMark(section);
					totalWeight += m.getWeight();
					totalPercent += m.percentage() * m.getWeight();
				}
			}
			if (totalWeight != 0) {
				averages.put(section, totalPercent / totalWeight);
				Double sectionW = s.weights.get(section);
				if (sectionW == null)
					sectionW = 0.0;
				tw += sectionW;
				average += sectionW * (totalPercent / totalWeight);
			} else {
				totalWeight = totalPercent = 100;
			}
			
		}
		if (tw == 0)
			average = null;
		else
			average /= tw;

	}
}
