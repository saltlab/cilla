package convert;

import junit.framework.Assert;

import org.junit.Test;

import com.crawljax.plugins.cilla.util.CssToXpathConverter;

public class CssToXpathConverterTest {

	public static void main(String[] args) {
		String selector = "*#felan ul *.class";

		if (selector.startsWith("*")) {
			selector = selector.substring(1, selector.length());
			System.out.println("sel: " + selector);
		}

	}

	@Test
	public void testConvert() {
		Assert.assertEquals("./descendant::P[@id = '123']", CssToXpathConverter.convert("p#123"));

		Assert.assertEquals("./descendant::*[@id = '123']", CssToXpathConverter.convert("#123"));

		Assert.assertEquals("./descendant::*[@id = 'UbcMainContent']/descendant::UL",
		        CssToXpathConverter.convert("#UbcMainContent ul"));

		Assert.assertEquals("./descendant::*[@id = 'UbcMainContent']/descendant::UL",
		        CssToXpathConverter.convert("*#UbcMainContent ul"));

		Assert.assertEquals(
		        "./descendant::*[@id = 'UbcMainContent']/descendant::*[contains(concat(' ', @class, ' '), ' classname ')]",
		        CssToXpathConverter.convert("*#UbcMainContent *.classname"));

		System.out.println(CssToXpathConverter.convert("#news span"));

	}

	@Test
	public void testRemove() {
		String sel = "*.news *#go";
		String result = CssToXpathConverter.removeChar(sel, '*');

		System.out.println("result: " + result);

	}

}
