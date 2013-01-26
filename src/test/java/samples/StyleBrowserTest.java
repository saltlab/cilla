package samples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSFontFaceRule;
import org.w3c.dom.css.CSSImportRule;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;

import com.steadystate.css.dom.CSSStyleDeclarationImpl;
import com.steadystate.css.parser.CSSOMParser;

public class StyleBrowserTest {

	@Test
	@Ignore
	public void firstTest() throws IOException {
		InputStream is = getClass().getClassLoader().getResourceAsStream("dojo.css");
		assertNotNull(is);

		InputSource source = new InputSource(new InputStreamReader(is));

		CSSOMParser parser = new CSSOMParser();

		CSSStyleDeclaration style = parser.parseStyleDeclaration(source);

		// Enumerate the properties and retrieve their values
		System.out.println("No. of properties: " + style.getLength());

		for (int i = 0; i < style.getLength(); i++) {
			String name = style.item(i);
			System.out.println(name + " : " + style.getPropertyValue(name));
		}

		// Get the style declaration as a single lump of text
		System.out.println("\ngetCssText");
		System.out.println(style.getCssText());

	}

	@Test
	public void testEmptyUrl() throws Exception {

		CSSOMParser parser = new CSSOMParser();

		Reader r = new StringReader("{ background: url() }");
		InputSource source = new InputSource(r);
		CSSStyleDeclarationImpl style =
		        (CSSStyleDeclarationImpl) parser.parseStyleDeclaration(source);

		assertEquals("", style.getCssText());
	}

	@Test
	@Ignore
	public void serializeTest() throws FileNotFoundException {
		String cssText =
		        "h1 {\n" + "  font-size: 2em\n" + "}\n" + "\n" + ".news {}"
		                + "@media handheld {\n" + "  h1 {\n" + "    font-size: 1.5em\n" + "  }\n"
		                + "}";
		// InputSource source = new InputSource(new StringReader(cssText));
		// InputStream is = new FileInputStream(new File("basic.css"));
		InputStream is = getClass().getClassLoader().getResourceAsStream("dojo.css");
		CSSOMParser cssomParser = new CSSOMParser();

		Reader r = new InputStreamReader(is);
		InputSource source = new InputSource(r);

		try {
			CSSStyleSheet css =
			        cssomParser.parseStyleSheet(source, null,
			                "http://www.example.org/css/style.css");
			System.out.println(css.toString());

			CSSRuleList rules = css.getCssRules();
			for (int i = 0; i < rules.getLength(); i++) {
				CSSRule rule = rules.item(i);
				System.out.println("rule: " + rule.getCssText());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void test() throws Exception {

		InputStream is = new FileInputStream(new File("test.css"));

		assertNotNull(is);

		Reader r = new InputStreamReader(is);
		InputSource source = new InputSource(r);

		CSSOMParser parser = new CSSOMParser();
		CSSStyleSheet stylesheet = parser.parseStyleSheet(source, null, null);
		CSSRuleList rules = stylesheet.getCssRules();

		for (int i = 0; i < rules.getLength(); i++) {
			CSSRule rule = rules.item(i);
			System.out.println(rule.getCssText());
		}

		// Do some modifications and output the results

		// Style Rules
		rules.item(9).setCssText("apple { color: green }"); // GOOD
		// rules.item(9).setCssText("@font-face { src: url(null) }"); // BAD

		CSSRule rule = rules.item(9);
		System.out.println(rule.getCssText());

		((CSSStyleRule) rules.item(9)).setSelectorText("banana"); // GOOD

		System.out.println(rule.getCssText());

		((CSSStyleRule) rules.item(9)).setSelectorText("banana, orange tangerine, grapefruit"); // GOOD

		System.out.println(rule.getCssText());

		((CSSStyleRule) rules.item(9)).getStyle().setCssText(
		        "{ color: red green brown; smell: sweet, sour; taste: sweet/tart }"); // GOOD

		System.out.println(rule.getCssText());

		// Import rules
		stylesheet.insertRule("@import \"thing.css\";", 0); // GOOD
		// stylesheet.insertRule("@import \"thing.css\";", 10); // BAD

		rule = rules.item(0);
		System.out.println(rule.getCssText());

		((CSSImportRule) rules.item(0)).setCssText("@import \"thing-hack.css\";");

		System.out.println(rule.getCssText());

		// Font-face rules
		stylesheet.insertRule("@font-face { src: \"#foo\" }", 10); // GOOD

		rule = rules.item(10);
		System.out.println(rule.getCssText());

		((CSSFontFaceRule) rules.item(10)).setCssText("@font-face { src: \"#bar\" }"); // GOOD
		// ((CSSFontFaceRule)rules.item(10)).setCssText("@import \"thing-hack.css\";");
		// // BAD

		System.out.println(rule.getCssText());

		// Media rules

	}

}