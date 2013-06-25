package com.crawljax.plugins.cilla.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import com.crawljax.plugins.cilla.util.CssToXpathConverter;
import com.crawljax.plugins.cilla.util.specificity.Specificity;
import com.crawljax.plugins.cilla.util.specificity.SpecificityCalculator;

/**
 * POJO class for a CSS selector.
 * 
 */
public class MSelector {
	private static final Logger LOGGER = Logger.getLogger(MSelector.class.getName());

	private String cssSelector;
	private String xpathSelector;
	private boolean ignore;
	private Specificity specificity;
	private boolean matched;
	private boolean effective;

	private List<ElementWrapper> matchedElements;
	private List<MProperty> properties;

	public MSelector(String selector, List<MProperty> properties) {
		this(selector, properties, false);
	}

	/**
	 * Constructor.
	 * 
	 * @param selector
	 *            the selector text (CSS).
	 */
	public MSelector(String selector, List<MProperty> properties, boolean ignore) {
		this.cssSelector = selector;
		this.ignore = ignore;
		this.matchedElements = new ArrayList<ElementWrapper>();
		this.properties = properties;
		setXPathSelector();
		setSpecificity();
	}

	public Specificity getSpecificity() {
		return specificity;
	}

	private void setSpecificity() {
		this.specificity = new SpecificityCalculator().getSpecificity(this.cssSelector);
	}

	public void addProperty(MProperty property) {
		this.properties.add(property);
	}

	public List<MProperty> getProperties() {
		return this.properties;
	}

	public boolean isEffective() {
		return effective;
	}

	public void setEffective(boolean effective) {
		this.effective = effective;
	}

	public boolean isMatched() {

		return matched;
	}

	public void setMatched(boolean matched) {
		this.matched = matched;
	}

	public List<ElementWrapper> getAffectedElements() {
		return matchedElements;
	}

	public void addMatchedElement(ElementWrapper element) {
		if (element != null) {
			this.matchedElements.add(element);
			setMatched(true);
		}
	}

	public String getCssSelector() {
		return cssSelector;
	}

	private void setXPathSelector() {
		this.xpathSelector = CssToXpathConverter.convert(this.cssSelector);
	}

	public String getXpathSelector() {
		return xpathSelector;
	}

	@Override
	public String toString() {

		StringBuffer buffer = new StringBuffer();

		buffer.append("Selector: " + this.cssSelector + "\n");
		buffer.append(" XPath: " + this.xpathSelector + "\n");
		buffer.append(" Matched?: " + this.matched + "\n");

		if (this.matchedElements.size() > 0) {
			buffer.append(" Matched elements:: \n");
		}

		for (ElementWrapper eWrapper : this.matchedElements) {
			buffer.append(eWrapper.toString());
		}

		return buffer.toString();
	}

	public boolean isIgnore() {
		return ignore;
	}

	public static void orderSpecificity(List<MSelector> selectors) {
		Collections.sort(selectors, new Comparator<MSelector>() {

			public int compare(MSelector o1, MSelector o2) {
				int value1 = o1.getSpecificity().getValue();
				int value2 = o2.getSpecificity().getValue();

				// if two selectors have the same com.crawljax.plugins.cilla.util.specificity, the
				// one closest to element is
				// effective
				if (value1 == value2) {
					return -1;
				}

				return new Integer(value1).compareTo(new Integer(value2));
			}

		});

		Collections.reverse(selectors);

	}

	public boolean hasEffectiveProperties() {

		for (MProperty p : this.properties) {
			if (p.isEffective()) {
				return true;
			}
		}

		return false;
	}

	public int getSize() {
		int propsSize = 0;
		for (MProperty prop : this.properties) {
			propsSize += prop.getsize();
		}
		return (propsSize + cssSelector.trim().replace(" ", "").getBytes().length);
	}
}
