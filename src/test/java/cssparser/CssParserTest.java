package cssparser;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.crawljax.plugins.cilla.analysis.MCssRule;
import com.crawljax.plugins.cilla.util.CssParser;


public class CssParserTest {

	@Test
	public void testGetMCSSRules() {
		List<MCssRule> mRules =
		        CssParser.getMCSSRules("h p { color: red;} div, a, span { font: black}");

		Assert.assertEquals(2, mRules.size());

		MCssRule mRule = mRules.get(1);

		Assert.assertEquals(3, mRule.getSelectors().size());
	}
}
