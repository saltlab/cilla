package com.crawljax.plugins.cilla.util.specificity;

/**
 * Represents a CSS com.crawljax.plugins.cilla.util.specificity calculation. The com.crawljax.plugins.cilla.util.specificity for a <code>Matcher</code> is
 * calculated according to the <a href="http://www.w3.org/TR/CSS21/cascade.html#specificity"> 2.1
 * Specification</a>. It stores the com.crawljax.plugins.cilla.util.specificity as an int in base 100. There are four rules used to
 * calculate the com.crawljax.plugins.cilla.util.specificity.
 * 
 * A. # count 1 if the selector is a 'style' attribute rather than a selector, 0 otherwise (= a) (In
 * HTML, values of an element's "style" attribute are style sheet rules. These rules have no
 * selectors, so a=1, b=0, c=0, and d=0.)
 * 
 * B. # count the number of ID attributes in the selector (= b)
 * 
 * C. # count the number of other attributes and pseudo-classes in the selector (= c)
 * 
 * D. # count the number of element names and pseudo-elements in the selector (= d)
 * 
 * Taken and adapted from (c) Volantis Systems Ltd 2005.
 */
public class SpecificityCalculator {

	/** @TODO !important */

	static final int BASE = 100;

	/**
	 * The number of units representing a single Element Count. This unit represents (d) in the 4
	 * rules used to calculate com.crawljax.plugins.cilla.util.specificity.
	 */
	static final int ELEMENT_NAMES_AND_PSEUDO_ELEMENT_UNITS = 1;

	/**
	 * The number of units representing a single Class Count. This unit represents (c) in the 4
	 * rules used to calculate com.crawljax.plugins.cilla.util.specificity.
	 */
	static final int OTHER_ATTRIBUTES_AND_PSEUDO_CLASS_UNITS =
	        ELEMENT_NAMES_AND_PSEUDO_ELEMENT_UNITS * BASE;

	/**
	 * The number of units representing a single ID Count. This unit represents (b) in the 4 rules
	 * used to calculate com.crawljax.plugins.cilla.util.specificity.
	 */
	static final int ID_ATTRIBUTE_UNITS = OTHER_ATTRIBUTES_AND_PSEUDO_CLASS_UNITS * BASE;

	/**
	 * The number of units representing a markup specified attribute. This unit represents (a) in
	 * the 4 rules used to calculate com.crawljax.plugins.cilla.util.specificity.
	 */
	static final int STYLE_UNITS = ID_ATTRIBUTE_UNITS * BASE;

	/**
	 * The com.crawljax.plugins.cilla.util.specificity value.
	 */
	protected int value;

	public void addElementSelector() {
		value += ELEMENT_NAMES_AND_PSEUDO_ELEMENT_UNITS;
	}

	public void addPseudoElementSelector() {
		value += ELEMENT_NAMES_AND_PSEUDO_ELEMENT_UNITS;
	}

	public void addClassSelector() {
		value += OTHER_ATTRIBUTES_AND_PSEUDO_CLASS_UNITS;
	}

	public void addPseudoClassSelector() {
		value += OTHER_ATTRIBUTES_AND_PSEUDO_CLASS_UNITS;
	}

	public void addAttributeSelector() {
		value += OTHER_ATTRIBUTES_AND_PSEUDO_CLASS_UNITS;
	}

	public void addIDSelector() {
		value += ID_ATTRIBUTE_UNITS;
	}

	public Specificity getSpecificity() {
		return new Specificity(value);
	}

	public void reset() {
		value = 0;
	}

	public Specificity getSpecificity(String selector) {
		String[] parts = selector.split(" ");
		for (String part : parts) {
			// CLASS: DIV.news or .news
			if (part.contains(".")) {

				String[] temp = part.split("\\.");

				if (temp.length > 1 && !temp[0].equals("")) {

					this.addElementSelector();
				}
				this.addClassSelector();
			} else {
				// ID: DIV#news
				if (part.contains("#")) {
					String[] temp = part.split("\\#");
					if (temp.length > 1 && !temp[0].equals("")) {
						this.addElementSelector();
					}
					this.addIDSelector();
				} else {
					// Element: DIV
					this.addElementSelector();
				}
			}
		}
		return getSpecificity();
	}

}