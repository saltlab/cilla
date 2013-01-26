package com.crawljax.plugins.cilla.util.specificity;

public class Specificity {

	/**
	 * The com.crawljax.plugins.cilla.util.specificity.
	 */
	private final int value;

	/**
	 * Package restricted initialiser to hide the fact that internally this is treated as an int.
	 * 
	 * @param value
	 *            The com.crawljax.plugins.cilla.util.specificity as an int.
	 */
	Specificity(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public boolean equals(Object object) {
		if (!(object instanceof Specificity)) {
			return false;
		}

		Specificity other = (Specificity) object;

		return value == other.value;
	}

	public int hashCode() {
		return value;
	}

	public int compareTo(Object o) {
		Specificity other = (Specificity) o;
		return value - other.value;
	}

	public String toString() {
		int v = value;

		int d = v % SpecificityCalculator.BASE;
		v = v / SpecificityCalculator.BASE;
		int c = v % SpecificityCalculator.BASE;
		v = v / SpecificityCalculator.BASE;
		int b = v % SpecificityCalculator.BASE;
		v = v / SpecificityCalculator.BASE;
		int a = v % SpecificityCalculator.BASE;

		return "{" + a + ", " + b + ", " + c + ", " + d + "}";
	}
}
