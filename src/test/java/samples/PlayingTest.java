package samples;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.crawljax.plugins.cilla.analysis.CssAnalyzer;
import com.crawljax.plugins.cilla.analysis.MCssRule;
import com.crawljax.plugins.cilla.util.CssParser;
import com.crawljax.plugins.cilla.util.CssToXpathConverter;
import com.crawljax.util.DomUtils;
import com.crawljax.util.XPathHelper;

public class PlayingTest {

	public static void main(String[] args) throws SAXException, IOException {

		Set<String> ignoreSelectors =
		        new HashSet<String>(Arrays.asList(":link", ":visited", ":hover", ":focus",
		                ":active"));

		String selec = "#news A:active";

		for (String ignore : ignoreSelectors) {
			if (selec.contains(ignore)) {
				System.out.println("ignoring: " + selec + " because of: " + ignore);
			}
		}
	}

	@Test
	@Ignore
	public void testGetURLContent() {
		String url = "http://www.ece.ubc.ca/~amesbah/style/jquery-ui-1.8.6.custom.css";

		try {
			System.out.println("Content:" + getURLContent(url));
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getURLContent(String url) throws HttpException, IOException {

		GetMethod method = new GetMethod(url);

		int returnCode = new HttpClient().executeMethod(method);
		if (returnCode == 200) {
			return method.getResponseBodyAsString();
		}

		System.out.println("returnCode:" + returnCode);

		return "";
	}

	public String convertStreamToString(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	public String absPath(String location, String url) {

		String loc = location.substring(0, location.lastIndexOf('/'));

		while (url.contains("../")) {
			url = url.substring(3);
			loc = loc.substring(0, loc.lastIndexOf('/'));

		}

		return loc + '/' + url;
	}

	@Test
	@Ignore
	public void testCssOnDom() throws SAXException, IOException {
		String html =
		        "<html>" + "<head><title>example</title>"
		                + "<link href='basic.css' rel='stylesheet' type='text/css'></head>" +

		                "<body><span><div width='56' id='div1' class='news plusnews votebutton medium green'>"
		                + "<p id='24'>this is just a test</p></div>"
		                + "<span id='span1' class='news'/><p>bla</p></span></body></html>";

		Document dom = DomUtils.asDocument(html);

		List<MCssRule> mRules =
		        CssParser.getMCSSRules("div.newsx { color: red;} div, a, span { font: black}");

		List<MCssRule> rules = CssAnalyzer.checkCssRulesOnDom("state1", dom, mRules);

		html = "<html><body><div id='div22'><a href='google.com'>Google</a></div></body></html>";
		dom = DomUtils.asDocument(html);
		rules = CssAnalyzer.checkCssRulesOnDom("state2", dom, mRules);
	}

	@Test
	@Ignore
	public void endtoendTest() throws SAXException, IOException {
		String html =
		        "<html><body><span><div width='56' id='div1' class='news plusnews votebutton medium green'>"
		                + "<p id='24'>this is just a test</p></div>"
		                + "<span id='span1' class='news'/><p>bla</p></span></body></html>";

		Document dom = DomUtils.asDocument(html);

		// CssToXpathConverter converter = new CssToXpathConverter();

		CssParser parser = new CssParser();
		List<String> selectors =
		        parser.getSelectorsFromFile("//Users//amesbah//workspace//CSS-Evolver//src//main//resources//basic.css");

		for (String cssSelector : selectors) {
			System.out.println("SEL: " + cssSelector);
			String xpath = CssToXpathConverter.convert(cssSelector);
			System.out.println("Xpath: " + xpath);

			try {
				NodeList nodeList = XPathHelper.evaluateXpathExpression(dom, xpath);

				for (int i = 0; i < nodeList.getLength(); i++) {
					Element e = (Element) nodeList.item(i);
					System.out.println("Matched Element: " + e.toString() + " with attributes: "
					        + DomUtils.getAllElementAttributes(e));
				}
			} catch (XPathExpressionException e) {
				fail(e.getMessage());
			}

		}

	}

	@Test
	@Ignore
	public void testCSSToXpath() throws SAXException, IOException {
		String html =
		        "<html><body><div width='56' id='div1' class='newsplus news votebutton medium green'>"
		                + "<p id='24'>this is just a test</p></div>"
		                + "<span id='span1' class='news'/></body></html>";

		String cssSelector = ".news";

		CssToXpathConverter converter = new CssToXpathConverter();

		String xpath = converter.convert(cssSelector);
		System.out.println("Xpath: " + xpath);

		Document dom = DomUtils.asDocument(html);
		try {
			NodeList nodeList = XPathHelper.evaluateXpathExpression(dom, xpath);

			for (int i = 0; i < nodeList.getLength(); i++) {
				Element e = (Element) nodeList.item(i);
				System.out.println("Matched Element: " + e.toString() + " with attributes: "
				        + DomUtils.getAllElementAttributes(e));
			}
		} catch (XPathExpressionException e) {
			fail(e.getMessage());
		}

	}

}
