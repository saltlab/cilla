package convert;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Document;

import com.crawljax.plugins.cilla.analysis.ElementWithClass;
import com.crawljax.plugins.cilla.util.CSSDOMHelper;
import com.crawljax.util.DomUtils;

public class CSSDOMHelperTest {

	@Test
	public void testCssFileExtraction() {

		String html =
		        "<html>" + "<head><title>example</title>"
		                + "<link href='basic.css' rel='stylesheet' type='text/css'></head>"
		                + "<link href='wrong.css' rel='bla' type='text/css'></head>"
		                + "<link href='default.css' rel='stylesheet' type='text/css'></head>" +

		                "<body><span><div width='56' id='div1' class='news plusnews votebutton medium green'>"
		                + "<p id='24'>this is just a test</p></div>"
		                + "<span id='span1' class='news'/><p>bla</p></span></body></html>";

		try {
			Document dom = DomUtils.asDocument(html);
			List<String> names = CSSDOMHelper.extractCssFilenames(dom);

			Assert.assertEquals(2, names.size());
			Assert.assertEquals("basic.css", names.get(0));

			Assert.assertEquals("default.css", names.get(1));

		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void getGetAbsPath() {

		Assert.assertEquals("http://www.google.com/world/bla/file.css", CSSDOMHelper.getAbsPath(
		        "http://www.google.com/world/tech/index.html", "../bla/file.css"));

		Assert.assertEquals("http://www.google.com/bla/file.css", CSSDOMHelper.getAbsPath(
		        "http://www.google.com/world/tech/index.html", "../../bla/file.css"));

		Assert.assertEquals("http://www.google.com/world/bla/file.css",
		        CSSDOMHelper.getAbsPath("http://www.google.com/world/tech/", "../bla/file.css"));

		Assert.assertEquals("http://www.google.com/world/bla/file.css",
		        CSSDOMHelper.getAbsPath("http://www.google.com/world/tech/", "../bla/file.css"));

		Assert.assertEquals("http://www.google.com/world/bla/file.css", CSSDOMHelper.getAbsPath(
		        "http://www.google.com/world/tech/", "http://www.google.com/world/bla/file.css"));

		Assert.assertEquals("http://www.google.com/world/tech/file.css",
		        CSSDOMHelper.getAbsPath("http://www.google.com/world/tech/", "file.css"));

		Assert.assertEquals("http://www.google.com/file.css",
		        CSSDOMHelper.getAbsPath("http://www.google.com/world/tech/", "/file.css"));
	}

	@Test
	public void testGetEmbeddedStyles() {
		String html =
		        "<html>" + "<head><title>example</title>"
		                + "<link href='basic.css' rel='stylesheet' type='text/css'>" + "<style>"
		                + ".newsletter { color: bla;} #world {dec: 234 }" + "</style>"
		                + "<style>" + ".nrc { font: bold;}" + "</style>" + "</head>" +

		                "<body><span><div width='56' id='div1' class='news plusnews votebutton medium green'>"
		                + "<p id='24'>this is just a test</p></div>"
		                + "<span id='span1' class='news'/><p>bla</p></span></body></html>";

		try {
			Document dom = DomUtils.asDocument(html);

			String result = CSSDOMHelper.getEmbeddedStyles(dom);

			Assert.assertNotNull(result);

			Assert.assertEquals(true, result.contains(".newsletter"));
			Assert.assertEquals(true, result.contains(".nrc"));
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetElementWithClass() {
		String html =
		        "<html>" + "<head><title>example</title> \n"
		                + "<link href='basic.css' rel='stylesheet' type='text/css'>"
		                + "<style> \n" + ".newsletter { color: bla;} #world {dec: 234 }"
		                + "</style>" + "<style>" + ".nrc { font: bold;}" + "</style>" + "</head>"
		                +

		                "<body><span><div width='56' id='div1' class='news plusnews votebutton medium green'>"
		                + "<p id='24' class=''>this is just a test</p></div>"
		                + "<span id='span1' class='news'/><p>bla</p></span></body></html>";

		try {
			Document dom = DomUtils.asDocument(html);
			List<ElementWithClass> elements = CSSDOMHelper.getElementWithClass("state1", dom);
			Assert.assertEquals(2, elements.size());

			Assert.assertEquals(5, elements.get(0).getClassValues().size());

		} catch (IOException e) {
			fail(e.getMessage());
		} catch (XPathExpressionException e) {
			fail(e.getMessage());
		}
	}
}
