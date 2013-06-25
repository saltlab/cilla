package analysis;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import se.fishtank.css.selectors.NodeSelector;
import se.fishtank.css.selectors.NodeSelectorException;
import se.fishtank.css.selectors.dom.DOMNodeSelector;

import com.crawljax.plugins.cilla.analysis.ElementWrapper;
import com.crawljax.plugins.cilla.analysis.MSelector;
import com.crawljax.plugins.cilla.analysis.MatchedElements;
import com.crawljax.util.DomUtils;

public class MatchedElementsTest {

	@Test
	public void testMatchedElements() {

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
			MSelector selector = new MSelector("#div1", null);
			Element e = dom.getElementById("div1");

			MatchedElements.setMatchedElement(new ElementWrapper("index", e), selector);
			selector = new MSelector("div", null);
			MatchedElements.setMatchedElement(new ElementWrapper("index", e), selector);

			Assert.assertEquals(2, MatchedElements.elementSelectors.size());

			for (String key : MatchedElements.elementSelectors.keySet()) {
				System.out.println("key: " + key);
				List<MSelector> selectors = MatchedElements.elementSelectors.get(key);
				for (MSelector s : selectors) {
					// System.out.println("Selector: " + s);
				}
			}

		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}

	}

	@Test
	public void testCssSelectors() throws IOException, NodeSelectorException {

		String html =
		        "<html>"
		                + "<head><title>example</title> \n"
		                + "<link href='basic.css' rel='stylesheet' type='text/css'>"
		                + "<style> \n"
		                + ".newsletter { color: bla;} #world {dec: 234 }"
		                + "</style>"
		                + "<style>"
		                + ".nrc { font: bold;}"
		                + "</style>"
		                + "</head>"
		                +

		                "<body>"
		                + "<span>"
		                + "  <div width='56' id='div1' class='news plusnews votebutton medium green'>"
		                + "  <a href=`google.com'>googooli</a>"
		                + "  <p id='24' class=''>this is just a test</p>" + "</div>"
		                + "  <span id='span1' class='news'/>" + "  <div>" + "    <p>bla</p>"
		                + "  </div>" + "</span></body></html>";

		Document dom = DomUtils.asDocument(html);
		NodeSelector selector = new DOMNodeSelector(dom);
		Set<Node> result = selector.querySelectorAll(":indeterminate");

		for (Node node : result) {
			System.out.println("Node:" + node);
		}
	}
}
