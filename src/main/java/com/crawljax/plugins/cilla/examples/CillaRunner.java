package com.crawljax.plugins.cilla.examples;

import java.util.concurrent.TimeUnit;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.plugins.cilla.CillaPlugin;

public class CillaRunner {

	private static final int waitAfterEvent = 400;
	private static final int waitAfterReload = 400;

	private static final String INDEX = "http://www.ece.ubc.ca/~amesbah/exp";

	public static void main(String[] args) {

		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(INDEX);
		builder.crawlRules().insertRandomDataInInputForms(false);

		// Set timeouts
		builder.crawlRules().waitAfterReloadUrl(waitAfterReload, TimeUnit.MILLISECONDS);
		builder.crawlRules().waitAfterEvent(waitAfterEvent, TimeUnit.MILLISECONDS);
		builder.setMaximumDepth(3);
		builder.crawlRules().clickOnce(true);

		builder.crawlRules().click("a");

		builder.setMaximumRunTime(600, TimeUnit.SECONDS);

		builder.setBrowserConfig(new BrowserConfiguration(BrowserType.firefox, 1));

		builder.addPlugin(new CillaPlugin());

		CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
		crawljax.call();

	}

}
