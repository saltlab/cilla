package com.crawljax.plugins.cilla.analysis;

import org.w3c.dom.Element;

import com.crawljax.util.DomUtils;

public class ElementWrapper {

	private String stateName;
	private Element element;

	/**
	 * @param stateName
	 *            name of the corresponding DOM state.
	 * @param element
	 *            the affected element.
	 */
	public ElementWrapper(String stateName, Element element) {
		this.stateName = stateName;
		this.element = element;
	}

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		this.element = element;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Statename: " + stateName + "\n");
		buffer.append("<" + element.getNodeName() + " "
		        + DomUtils.getAllElementAttributes(element) + ">");
		if (element.getNodeValue() != null) {
			buffer.append(element.getNodeValue());
		}

		buffer.append("\n");
		// buffer.append("</" + element.getNodeName() + ">\n");

		// buffer.append(Helper.getDocumentToString(element.getOwnerDocument()));

		return buffer.toString();
	}

}
