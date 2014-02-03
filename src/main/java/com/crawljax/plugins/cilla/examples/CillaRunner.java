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

//private static final String INDEX = "http://www.ece.ubc.ca/~amesbah/exp";
 private static final String INDEX = "http://www.gokartrecords.com";
        
     
        
public static String name;        
public static long startTime;
public static String b;
public static String urlScope;
    public static void main(String[] args) {
    	
  
          /*
                String[] urlArray = new String[100];
                        urlArray = GetUrls.getArray("src//main//resources//WebsitesUnderStudy.txt", 10);
                        for (int i = 0 ; i < 20; i++) {
                                getName(urlArray[i]);
                                startTime = System.currentTimeMillis();
                                urlScope = urlArray[i];
                
                                b = urlArray[i].replaceAll("http://", "");
                   */
        
        b = INDEX.replaceAll("http://", "");
      //   urlScope = INDEX;        

           CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(INDEX);
//CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(urlArray[i]);

                builder.crawlRules().insertRandomDataInInputForms(false);

                // Set timeouts
                builder.crawlRules().waitAfterReloadUrl(waitAfterReload, TimeUnit.MILLISECONDS);
                builder.crawlRules().waitAfterEvent(waitAfterEvent, TimeUnit.MILLISECONDS);
                builder.setMaximumDepth(3);
                builder.crawlRules().clickOnce(true);

                builder.crawlRules().click("a");
builder.crawlRules().click("div");
builder.crawlRules().click("img");
builder.crawlRules().click("button");
builder.crawlRules().click("span");
builder.crawlRules().click("input");

builder.setMaximumStates(50);

                builder.setMaximumRunTime(1500, TimeUnit.SECONDS);


                builder.setBrowserConfig(new BrowserConfiguration(BrowserType.firefox, 1));

                builder.addPlugin(new CillaPlugin());

                CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
                crawljax.call();

        }
// }
    
    private static void getName(String URLstring) {
            String initialization = "", resticting = "";
            int first = 0, second = 0;
            initialization = URLstring;
            first = initialization.indexOf(".");
            if (initialization.contains(".org"))
                    second = initialization.indexOf("org");
            else if (initialization.contains(".ru"))
                            second = URLstring.indexOf(".ru");
            else if (initialization.contains(".ca"))
                    second = URLstring.indexOf(".ca");
            else if (initialization.contains(".co"))
                    second = URLstring.indexOf(".co");
            else if (initialization.contains(".ly"))
                    second = URLstring.indexOf(".ly");
            else if (initialization.contains(".net"))
                second = URLstring.indexOf(".net");
            else if (initialization.contains(".se"))
                    second = URLstring.indexOf(".se");
            else
                    second = URLstring.indexOf(".com");
            resticting = initialization.substring(first + 1, second);
            name = "-".concat(resticting);

    }

}