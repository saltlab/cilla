package com.crawljax.plugins.cilla.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Locator;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;


import com.crawljax.plugins.cilla.analysis.MCssRule;
import com.steadystate.css.dom.CSSStyleRuleImpl;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.userdata.UserDataConstants;

public class CssParser {

	private static final Logger LOGGER = Logger.getLogger(CssParser.class.getName());

	private CSSOMParser cssomParser = new CSSOMParser();

	/**
	 * @param filename
	 *            the CSS filename.
	 * @return an array list of all CSS selectors.
	 * @throws IOException
	 *             if the file is not found.
	 */
	public List<String> getSelectorsFromFile(String filename) throws IOException {

		final List<String> selectors = new ArrayList<String>();

		// new InputSource(new InputStreamReader(is))
		CSSStyleSheet css =
		        cssomParser.parseStyleSheet(new InputSource(new FileReader(new File(filename))),
		                null, null);

		CSSRuleList rules = css.getCssRules();
		for (int i = 0; i < rules.getLength(); i++) {
			CSSRule rule = rules.item(i);
			LOGGER.debug("rule: " + rule.getCssText());

			if (rule instanceof CSSStyleRule) {
				// CSSStyleRule styleRule = (CSSStyleRule) rule;

				CSSStyleRuleImpl styleRule = (CSSStyleRuleImpl) rule;
				Locator loc = (Locator) styleRule.getUserData(UserDataConstants.KEY_LOCATOR);

				LOGGER.info("Location: " + loc.getLineNumber());

				String selector = styleRule.getSelectorText();

				LOGGER.debug("selectorText: " + styleRule.getSelectorText());

				// grouping selectors: p, div, .news { }
				String[] group = selector.split(",");

				for (int k = 0; k < group.length; k++) {
					LOGGER.debug("selector: " + group[k]);
					selectors.add(group[k]);
				}
			}
		}

		return selectors;

	}

	public void fromFile(InputStream is) throws IOException {

		// InputStream is =
		// getClass().getClassLoader().getResourceAsStream("basic.css");

		List<String> selectors = new ArrayList<String>();

		Reader r = new InputStreamReader(is);
		InputSource source = new InputSource(r);

		CSSStyleSheet css = cssomParser.parseStyleSheet(source, null, null);
		System.out.println(css.toString());

		CSSRuleList rules = css.getCssRules();
		for (int i = 0; i < rules.getLength(); i++) {
			CSSRule rule = rules.item(i);
			System.out.println("rule: " + rule.getCssText());

			if (rule instanceof CSSStyleRule) {
				CSSStyleRule styleRule = (CSSStyleRule) rule;
				selectors.add(styleRule.getSelectorText());
				System.out.println("selector: " + styleRule.getSelectorText());
				CSSStyleDeclaration styleDeclaration = styleRule.getStyle();

				for (int j = 0; j < styleDeclaration.getLength(); j++) {
					String property = styleDeclaration.item(j);
					System.out.println("property: " + property);
					System.out.println("value: "
					        + styleDeclaration.getPropertyCSSValue(property).getCssText());
				}
			}
			System.out.println("");

		}

	}

	/**
	 * @param cssText
	 *            the CSS text.
	 * @return a list of MCssRule objects (Wrappers around CSSRules)
	 */
	public static List<MCssRule> getMCSSRules(String cssText) {

		CSSRuleList ruleList = getCSSRuleList(cssText);

		return MCssRule.convertToMCssRules(ruleList);
	}

	public static CSSRuleList getCSSRuleList(String cssText) {
		InputSource source = new InputSource(new StringReader(cssText));
		CSSOMParser cssomParser = new CSSOMParser();
		CSSRuleList rules = null;

		try {
			CSSStyleSheet css = cssomParser.parseStyleSheet(source, null, null);

			rules = css.getCssRules();
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return rules;
	}

}
