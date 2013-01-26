package analysis;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.crawljax.plugins.cilla.analysis.CssAnalyzer;
import com.crawljax.plugins.cilla.analysis.MCssRule;
import com.crawljax.plugins.cilla.util.CssParser;


public class CssAnalyzerTest {

	@Test
	public void testGetDuplicateSelectors() {
		List<MCssRule> mRules =
		        CssParser.getMCSSRules("h p { color: red;} div, a, span { font: black}");

		Assert.assertEquals(0, CssAnalyzer.getDuplicateSelectors(mRules).size());

		mRules = CssParser.getMCSSRules("h p { color: red;} div, a, span, h p { font: black}");

		Assert.assertEquals(1, CssAnalyzer.getDuplicateSelectors(mRules).size());
		Assert.assertEquals("h p", CssAnalyzer.getDuplicateSelectors(mRules).iterator().next());

		mRules =
		        CssParser
		                .getMCSSRules("h p, a { color: red;} div, a, span, h p { font: black} a { }");

		Assert.assertEquals(2, CssAnalyzer.getDuplicateSelectors(mRules).size());

	}
}
