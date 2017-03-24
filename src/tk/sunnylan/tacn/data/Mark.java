package tk.sunnylan.tacn.data;

import org.w3c.dom.Element;

public class Mark {
	private double numerator;
	private double denominator;
	private double weight;

	public Mark(double numerator, double denominator, double weight) {
		this.updateNumerator(numerator);
		this.updateDenominator(denominator);
		this.updateWeight(weight);
	}
	
	public Mark(Element docroot){
		double numerator=Double.parseDouble(docroot.getElementsByTagName("numerator").item(0).getTextContent());
		double denominator=Double.parseDouble(docroot.getElementsByTagName("denominator").item(0).getTextContent());
		double weight=Double.parseDouble(docroot.getElementsByTagName("weight").item(0).getTextContent());
		this.updateNumerator(numerator);
		this.updateDenominator(denominator);
		this.updateWeight(weight);
	}

	public double getNumerator() {
		return numerator;
	}

	public void updateNumerator(double numerator) {
		this.numerator = numerator;
	}

	public double getDenominator() {
		return denominator;
	}

	public void updateDenominator(double denominator) {
		this.denominator = denominator;
	}

	public double getWeight() {
		return weight;
	}

	public void updateWeight(double weight) {
		this.weight = weight;
	}

	public double percentage() {
		return (100 * numerator) /  denominator;
	}

	@Override
	public String toString() {
		return "<numerator>" + numerator + "</numerator><denominator>" + denominator + "</denominator><weight>" + weight
				+ "</weight>";
	}
}
