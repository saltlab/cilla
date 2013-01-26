package com.crawljax.plugins.cilla.analysis;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

public class ElementWithClass extends ElementWrapper {

	private List<String> classValues;
	private List<String> unmatchedClasses;

	public ElementWithClass(String stateName, Element element, List<String> classValues) {
		super(stateName, element);
		this.classValues = classValues;
		this.unmatchedClasses = new ArrayList<String>();
	}

	public List<String> getUnmatchedClasses() {
		return unmatchedClasses;
	}

	public void setUnmatchedClass(String unmatchedClass) {
		this.unmatchedClasses.add(unmatchedClass);
	}

	public List<String> getClassValues() {
		return classValues;
	}

	public void setClassValues(List<String> classValues) {
		this.classValues = classValues;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
