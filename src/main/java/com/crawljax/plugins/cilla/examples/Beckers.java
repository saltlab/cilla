package com.crawljax.plugins.cilla.examples;

import org.apache.commons.configuration.ConfigurationException;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.plugins.cilla.CillaPlugin;

public class Beckers {

	private static final int waitAfterEvent = 400;
	private static final int waitAfterReload = 400;
	private static BrowserType browser = BrowserType.firefox;

	private static final String INDEX = "http://www.beckerelectric.com";

	public static void main(String[] args) {

		CrawljaxConfiguration crawljaxConfiguration = new CrawljaxConfiguration();
		crawljaxConfiguration.setCrawlSpecification(getCrawlSpecification(INDEX, waitAfterEvent,
		        waitAfterReload));

		crawljaxConfiguration.setBrowser(browser);

		crawljaxConfiguration.addPlugin(new CillaPlugin());

		try {
			new CrawljaxController(crawljaxConfiguration).run();
		} catch (ConfigurationException e) {

			e.printStackTrace();
		} catch (CrawljaxException e) {

			e.printStackTrace();
		}

	}

	protected static CrawlSpecification getCrawlSpecification(String url, int waintAfterEvent,
	        int waitAfterReload) {

		CrawlSpecification crawler = new CrawlSpecification(url);
		crawler.setWaitTimeAfterEvent(waintAfterEvent);
		crawler.setWaitTimeAfterReloadUrl(waitAfterReload);
		crawler.setDepth(3);
		crawler.setClickOnce(true);
		crawler.click("a");

		crawler.setMaximumStates(2);
		return crawler;
	}
}
