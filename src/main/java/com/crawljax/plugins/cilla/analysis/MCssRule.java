package com.crawljax.plugins.cilla.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.css.sac.Locator;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;

import com.crawljax.plugins.cilla.util.CssToXpathConverter;
import com.crawljax.plugins.cilla.util.specificity.SpecificityCalculator;
import com.steadystate.css.dom.CSSStyleRuleImpl;
import com.steadystate.css.userdata.UserDataConstants;

public class MCssRule {

	private CSSRule rule;
	private List<MSelector> selectors;
	private String ruleSelector;

	private static Set<String> ignorePseudoClasses = new HashSet<String>(Arrays.asList(":link",
	        ":visited", ":hover", ":focus", ":active", ":target", ":lang", ":enabled",
	        ":disabled", ":checked", ":indeterminate"));

	/*
	 * ":nth-child", ":nth-last-child", ":nth-of-type", ":nth-last-of-type", ":first-child",
	 * ":last-child", ":first-of-type", ":last-of-type", ":only-child", ":only-of-type", ":empty",
	 * ":contains", ":not", ":before", ":after", ":first-line", ":first-letter", ":selection")
	 */

	/**
	 * Constructor.
	 * 
	 * @param rule
	 *            the CSS rule.
	 */
	public MCssRule(CSSRule rule) {

		this.rule = rule;
		selectors = new ArrayList<MSelector>();
		setSelectors();
	}

	public CSSRule getRule() {
		return rule;
	}

	public List<MSelector> getSelectors() {
		return selectors;
	}

	private void setSelectors() {
		if (this.rule instanceof CSSStyleRule) {
			CSSStyleRule styleRule = (CSSStyleRule) rule;

			this.ruleSelector = styleRule.getSelectorText();
			this.ruleSelector = this.ruleSelector.replace("*", " ");
			//this.ruleSelector = CssToXpathConverter.removeChar(this.ruleSelector, '*');
			// in case there are Grouping selectors: p, div, .news { }
			List<MProperty> props = getProperties();
			for (String sel : ruleSelector.split(",")) {
				selectors.add(new MSelector(sel.trim(), props, shouldIgnore(sel)));
			}
		}

	}

	private boolean shouldIgnore(String sel) {
		for (String ignore : ignorePseudoClasses) {
			if (sel.contains(ignore)) {
				return true;
			}
		}
		return false;
	}

	public List<MProperty> getProperties() {
		CSSStyleDeclaration styleDeclaration = null;
		List<MProperty> properties = new ArrayList<MProperty>();

		if (this.rule instanceof CSSStyleRule) {
			CSSStyleRule styleRule = (CSSStyleRule) rule;
			styleDeclaration = styleRule.getStyle();

			for (int j = 0; j < styleDeclaration.getLength(); j++) {
				String property = styleDeclaration.item(j);
				String value = styleDeclaration.getPropertyCSSValue(property).getCssText();
				properties.add(new MProperty(property, value));
			}

		}

		return properties;
	}

	/**
	 * @return the CSS Style declaration of this rule.
	 */
	public CSSStyleDeclaration getStyleDeclaration() {
		CSSStyleDeclaration styleDeclaration = null;

		if (this.rule instanceof CSSStyleRule) {
			CSSStyleRule styleRule = (CSSStyleRule) rule;
			styleDeclaration = styleRule.getStyle();

			for (int j = 0; j < styleDeclaration.getLength(); j++) {
				String property = styleDeclaration.item(j);
				System.out.println("property: " + property);
				System.out.println("value: "
				        + styleDeclaration.getPropertyCSSValue(property).getCssText());
			}

		}

		return styleDeclaration;
	}

	public static List<MCssRule> convertToMCssRules(CSSRuleList ruleList) {

		List<MCssRule> mCssRules = new ArrayList<MCssRule>();

		for (int i = 0; i < ruleList.getLength(); i++) {
			mCssRules.add(new MCssRule(ruleList.item(i)));
		}

		return mCssRules;
	}

	@Override
	public String toString() {

		StringBuffer buffer = new StringBuffer();
		Locator locator = getLocator();

		buffer.append("locator: line=" + locator.getLineNumber() + " col="
		        + locator.getColumnNumber() + "\n");
		buffer.append("Rule: " + rule.getCssText() + "\n");

		for (MSelector selector : this.selectors) {
			buffer.append(selector.toString());
		}

		return buffer.toString();
	}

	/**
	 * @return the selectors that are not matched (not associated DOM elements have been detected).
	 */
	public List<MSelector> getUnmatchedSelectors() {
		List<MSelector> unmatched = new ArrayList<MSelector>();

		for (MSelector selector : this.selectors) {
			if (!selector.isMatched() && !selector.isIgnore()) {
				unmatched.add(selector);
			}
		}

		return unmatched;

	}

	/**
	 * @return the selectors that are effective (associated DOM elements have been detected).
	 */
	public List<MSelector> getMatchedSelectors() {
		List<MSelector> effective = new ArrayList<MSelector>();

		for (MSelector selector : this.selectors) {
			if (selector.isMatched() && !selector.isIgnore()) {
				effective.add(selector);
			}
		}

		return effective;

	}

	/**
	 * @return the Locator of this rule (line number, column).
	 */
	public Locator getLocator() {
		if (this.rule instanceof CSSStyleRuleImpl) {
			return (Locator) ((CSSStyleRuleImpl) this.rule)
			        .getUserData(UserDataConstants.KEY_LOCATOR);
		}

		return null;
	}

	public String getRuleSelector() {
		return ruleSelector;
	}
	
	
public List<MSelector> getTooSpecificSelectors(){
SpecificityCalculator sc = new SpecificityCalculator();

List<MSelector> tooSpecific = new ArrayList<MSelector>();
			
		for (MSelector selector : this.selectors){
			sc.reset();

	String s = sc.getSpecificity(selector.getCssSelector()).toString();
	int a = Integer.parseInt(s.substring(1, 2));
	int b = Integer.parseInt(s.substring(4, 5));
	int c = Integer.parseInt(s.substring(7, 8));
	int d = Integer.parseInt(s.substring(10, 11));
	String e = s.substring(1, 2);
	String f = s.substring(4, 5);
	String g = s.substring(7, 8);
	String h = s.substring(10, 11);
	int i = Integer.parseInt(e+f+g+h);
						
						if(a+b+c+d>3 && !selector.isIgnore()){
							tooSpecific.add(selector);
						}
						if( i> 122 && !selector.isIgnore()){
							tooSpecific.add(selector);
							
						}
				
					}
					
				
			return tooSpecific;
		}
			
public List<MSelector> getLazyRules(){


List<MSelector> tooLazy = new ArrayList<MSelector>();
			
			for (MSelector selector : this.selectors){
						int z = selector.getProperties().size();
						
						if(z < 3 && !selector.isIgnore()){
					
							tooLazy.add(selector);
						}
				
					}
					
				
			return tooLazy;
		}
			
public List<MSelector> getTooLongRules(){


List<MSelector> tooLong = new ArrayList<MSelector>();
			
			for (MSelector selector : this.selectors){
						int z = selector.getProperties().size();
						
						if(z > 5 && !selector.isIgnore()){
					
							tooLong.add(selector);
						}
				
					}
					
				
			return tooLong;
		}
			

public List<MSelector> getEmptyCatch(){


List<MSelector> empCatch = new ArrayList<MSelector>();
			
			for (MSelector selector : this.selectors){
						int z = selector.getProperties().size();
						
						if(z == 0 && !selector.isIgnore()){
					
							empCatch.add(selector);
						}
				
					}
					
				
			return empCatch;
		}

public List<MSelector> getUndoingStyle(){


List<MSelector> undoing = new ArrayList<MSelector>();
			
		for (MSelector selector : this.selectors){
						
						CSSStyleDeclaration styleDeclaration = null;
					//	List<MProperty> properties = new ArrayList<MProperty>();

						if (this.rule instanceof CSSStyleRule) {
							CSSStyleRule styleRule = (CSSStyleRule) rule;
							styleDeclaration = styleRule.getStyle();

							for (int j = 0; j < styleDeclaration.getLength(); j++) {
								String property1 = styleDeclaration.item(j);
								for (int i = j+1; i < styleDeclaration.getLength(); i++){
									String property2 = styleDeclaration.item(i);
									if(property1 == property2 && i!=j){
										undoing.add(selector);
										
									}
								}
								
								
							}

						}
						
						
						}
				
					
					
				
			return undoing;
		}

public List<MSelector> checkFontSize(){
	List<MSelector> inappfontsize = new ArrayList<MSelector>();
	for (MSelector selector : this.selectors){
				
				CSSStyleDeclaration styleDeclaration = null;
			

				if (this.rule instanceof CSSStyleRule) {
					CSSStyleRule styleRule = (CSSStyleRule) rule;
					styleDeclaration = styleRule.getStyle();
					for (int i = 0; i < styleDeclaration.getLength(); i++) {
						String property = styleDeclaration.item(i);
						if(property.equalsIgnoreCase("font-size")){
							String value = styleDeclaration.getPropertyValue(property);
							if(value.equalsIgnoreCase("inherit")){
								inappfontsize.add(selector);
								
							}
							
							if(value.contains("0")|| value.contains("1")||value.contains("2")||value.contains("3")||value.contains("4")||value.contains("5")||value.contains("6")||value.contains("7")||value.contains("8")||value.contains("9")){
								if(value.contains("%") == false){
							inappfontsize.add(selector);
								}
							}
						}
						
				}
				}
			}
			return inappfontsize;
			
		}


public List<MSelector> getIdWithClassOrElement(){
SpecificityCalculator sc = new SpecificityCalculator();

List<MSelector> idWith = new ArrayList<MSelector>();
			
			for (MSelector selector : this.selectors){
					sc.reset();

						String s = sc.getSpecificity(selector.getCssSelector()).toString();
					//	int a = Integer.parseInt(s.substring(1, 2));
						int b = Integer.parseInt(s.substring(4, 5));
						int c = Integer.parseInt(s.substring(7, 8));
						int d = Integer.parseInt(s.substring(10, 11));
						
						if(b!=0 && !selector.isIgnore()){
							if(c!=0 || d!=0){
								
								idWith.add(selector);
								
								
							}
						}
				
					}
					
				
			return idWith;
		}
			
public List<MSelector> getReactiveImportant(){


List<MSelector> reactiveImportant = new ArrayList<MSelector>();
			
		for (MSelector selector : this.selectors){
						
						CSSStyleDeclaration styleDeclaration = null;
					//	List<MProperty> properties = new ArrayList<MProperty>();

						if (this.rule instanceof CSSStyleRule) {
							CSSStyleRule styleRule = (CSSStyleRule) rule;
							styleDeclaration = styleRule.getStyle();

							for (int j = 0; j < styleDeclaration.getLength(); j++) {
								String property = styleDeclaration.item(j);
								String value = styleDeclaration.getPropertyCSSValue(property).getCssText();
								if(property.contains("!important") || value.contains("!important")){
									
									reactiveImportant.add(selector);	
									}
								
								
								
							}

						}
						
						
						}
				
					
				
			return reactiveImportant;
		}

}
