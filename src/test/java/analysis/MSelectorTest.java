package analysis;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.crawljax.plugins.cilla.analysis.MSelector;
import com.crawljax.plugins.cilla.util.specificity.Specificity;

public class MSelectorTest {

	@Test
	public void testGetXpath() {
		MSelector selectorPojo = new MSelector("p#242", null);

		Assert.assertEquals("./descendant::P[@id = '242']", selectorPojo.getXpathSelector());

		selectorPojo = new MSelector("div.news p", null);

		Assert.assertEquals(
		        "./descendant::DIV[contains(concat(' ', @class, ' '), ' news ')]/descendant::P",
		        selectorPojo.getXpathSelector());

		Assert.assertFalse(selectorPojo.isMatched());

		selectorPojo.setMatched(true);

		Assert.assertTrue(selectorPojo.isMatched());

		selectorPojo = new MSelector("#UbcMainContent ul", null);

		Assert.assertEquals("./descendant::*[@id = 'UbcMainContent']/descendant::UL",
		        selectorPojo.getXpathSelector());
	}

	@Test
	public void testSpecificity() {

		MSelector selector = new MSelector("p#242", null);

		Specificity sp = selector.getSpecificity();

		Assert.assertNotNull(sp);
		Assert.assertEquals(10001, sp.getValue());

		selector = new MSelector("p.newsitem", null);
		sp = selector.getSpecificity();

		Assert.assertNotNull(sp);
		Assert.assertEquals(101, sp.getValue());

		selector = new MSelector(".newsitem", null);
		sp = selector.getSpecificity();

		Assert.assertNotNull(sp);
		Assert.assertEquals(100, sp.getValue());

		selector = new MSelector("#newsitem", null);
		sp = selector.getSpecificity();

		Assert.assertNotNull(sp);
		Assert.assertEquals(10000, sp.getValue());

		selector = new MSelector("div", null);
		sp = selector.getSpecificity();

		Assert.assertNotNull(sp);
		Assert.assertEquals(1, sp.getValue());

		selector = new MSelector("div div", null);
		sp = selector.getSpecificity();

		Assert.assertNotNull(sp);
		Assert.assertEquals(2, sp.getValue());

		selector = new MSelector("div span#news .item", null);
		sp = selector.getSpecificity();

		Assert.assertNotNull(sp);
		Assert.assertEquals(10102, sp.getValue());

	}

	@Test
	public void testorderSpecificity() {
		List<MSelector> list = new ArrayList<MSelector>();
		list.add(new MSelector("p#242", null));
		list.add(new MSelector("p p#news", null));
		list.add(new MSelector("p.algo", null));
		list.add(new MSelector("span div#aha #cal", null));
		list.add(new MSelector("a", null));
		list.add(new MSelector("span span", null));
		list.add(new MSelector("A", null));

		MSelector.orderSpecificity(list);

		Assert.assertEquals("span div#aha #cal", list.get(0).getCssSelector());
		Assert.assertEquals("A", list.get(list.size() - 1).getCssSelector());

		for (MSelector s : list) {
			System.out.println("Selector: " + s.getCssSelector());
		}

	}
}
