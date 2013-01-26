package analysis;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;

import com.crawljax.plugins.cilla.analysis.MCssRule;
import com.crawljax.plugins.cilla.analysis.MSelector;
import com.crawljax.plugins.cilla.util.CssParser;


public class MCSSRuleTest {

	@Test
	public void testSelectors() {

		CSSRuleList rules =
		        CssParser.getCSSRuleList("h p { color: red;} \n div, a, span { font: black}");
		Assert.assertNotNull(rules);
		CSSRule rule = rules.item(0);
		Assert.assertNotNull(rule);

		MCssRule cssPojo = new MCssRule(rule);

		List<MSelector> selectors = cssPojo.getSelectors();

		Assert.assertEquals(1, selectors.size());

		MSelector selector = selectors.get(0);
		Assert.assertEquals("./descendant::H/descendant::P", selector.getXpathSelector());

		Assert.assertFalse(selector.isMatched());

		// second rule
		rule = rules.item(1);
		Assert.assertNotNull(rule);

		cssPojo = new MCssRule(rule);

		selectors = cssPojo.getSelectors();

		Assert.assertEquals(3, selectors.size());
		MSelector selectorPojo = selectors.get(1);
		Assert.assertEquals("a", selectorPojo.getCssSelector());

	}

	@Test
	public void testLocator() {

		CSSRuleList rules =
		        CssParser
		                .getCSSRuleList("h p { color: red;} \n\n div { font: black} \n span {color: white}");
		Assert.assertNotNull(rules);
		CSSRule rule = rules.item(0);
		Assert.assertNotNull(rule);

		MCssRule mRule = new MCssRule(rule);

		Assert.assertEquals(1, mRule.getLocator().getLineNumber());

		rule = rules.item(1);
		Assert.assertNotNull(rule);

		mRule = new MCssRule(rule);

		Assert.assertEquals(3, mRule.getLocator().getLineNumber());

		rule = rules.item(2);
		Assert.assertNotNull(rule);

		mRule = new MCssRule(rule);

		Assert.assertEquals(4, mRule.getLocator().getLineNumber());

		System.out.println("rule: ");
		System.out.println(mRule.toString());

	}

	@Test
	public void testProperties() {

		CSSRuleList rules =
		        CssParser
		                .getCSSRuleList("h p { color: red;} \n\n div, #news { font: black; color: red} \n span .class {color: white}");
		Assert.assertNotNull(rules);
		CSSRule rule = rules.item(0);
		Assert.assertNotNull(rule);

		MCssRule mRule = new MCssRule(rule);

		Assert.assertEquals(1, mRule.getProperties().size());

		rule = rules.item(1);
		Assert.assertNotNull(rule);

		mRule = new MCssRule(rule);

		Assert.assertEquals(2, mRule.getProperties().size());

		List<MSelector> selectors = mRule.getSelectors();

		MSelector sel = selectors.get(1);
		Assert.assertEquals(2, sel.getProperties().size());

		Assert.assertEquals("font", sel.getProperties().get(0).getName());
		Assert.assertEquals("black", sel.getProperties().get(0).getValue());
		// Assert.assertEquals("notset", sel.getProperties().get(0).getStatus());
	}
}
