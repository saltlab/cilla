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
//private static final String INDEX = "http://www.facebook.com";
        
     
      static String name;        
public static long startTime;
public static String b;

    public static void main(String[] args) {
    	
    	
    	
   
                String[] urlArray = new String[100];
                        urlArray = GetUrls.getArray("src//main//resources//WebsitesUnderStudy.txt", 10);
                        for (int i = 0 ; i < 50; i++) {
                        	CillaPlugin.count = 0;
                                getName(urlArray[i]);
                                startTime = System.currentTimeMillis();
                               
                
                                b = urlArray[i].replaceAll("http://", "");

 //  b = INDEX.replaceAll("http://", "");
           

 // CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(INDEX);
CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(urlArray[i]);

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

          //   builder.setMaximumRunTime(60, TimeUnit.SECONDS);


               builder.setBrowserConfig(new BrowserConfiguration(BrowserType.firefox, 1));

                builder.addPlugin(new CillaPlugin());
                
                CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
                crawljax.call();
             
        }
}
    
    private static void getName(String URLstring) {
            String initialization = "", resticting = "";
            int first = 0, second = 0;
            initialization = URLstring;
            first = initialization.indexOf(".");
            if (initialization.contains(".org"))
                    second = initialization.indexOf("org");
           // else if (initialization.contains(".ru"))
             //               second = URLstring.indexOf(".ru");
         //   else if (initialization.contains(".ca"))
          //          second = URLstring.indexOf(".ca");
          //  else if (initialization.contains(".it"))
           //               second = URLstring.indexOf(".it");
          //  else if (initialization.contains(".fr"))
          //      second = URLstring.indexOf(".fr");
           else if (initialization.contains(".co"))
                   second = URLstring.indexOf(".co");
            else if (initialization.contains(".ly"))
                    second = URLstring.indexOf(".ly");
          //  else if (initialization.contains(".to"))
            //    second = URLstring.indexOf(".to");
            else if (initialization.contains(".net"))
              second = URLstring.indexOf(".net");
          //  else if (initialization.contains(".cn"))
           //     second = URLstring.indexOf(".cn");
            else if (initialization.contains(".nu"))
                second = URLstring.indexOf(".nu");
          // else if (initialization.contains(".de"))
         //       second = URLstring.indexOf(".de");
            else if (initialization.contains(".jp"))
               second = URLstring.indexOf(".jp");
            else if (initialization.contains(".pk"))
                second = URLstring.indexOf(".pk");
         //   else if (initialization.contains(".pl"))
         //       second = URLstring.indexOf(".pl");
            else if (initialization.contains(".info"))
                second = URLstring.indexOf(".info");
          //  else if (initialization.contains(".in"))
          //      second = URLstring.indexOf(".in");
            else if (initialization.contains(".gl"))
                second = URLstring.indexOf(".gl");
           // else if (initialization.contains(".se"))
           //         second = URLstring.indexOf(".se");
            else if (initialization.contains(".br"))
                second = URLstring.indexOf(".br");
            else if (initialization.contains(".tv"))
                second = URLstring.indexOf(".tv");
            else if (initialization.contains(".eu"))
                second = URLstring.indexOf(".eu");
            else if (initialization.contains(".cc"))
                second = URLstring.indexOf(".cc");
            else if (initialization.contains(".cz"))
                second = URLstring.indexOf(".cz");
            else if (initialization.contains(".es"))
                second = URLstring.indexOf(".es");
            else if (initialization.contains(".gov"))
                second = URLstring.indexOf(".gov");
            else if (initialization.contains(".vn"))
                second = URLstring.indexOf(".vn");
            else if (initialization.contains(".it"))
                second = URLstring.indexOf(".it");
            else if (initialization.contains(".at"))
               second = URLstring.indexOf(".at");
            else if (initialization.contains(".nl"))
                second = URLstring.indexOf(".nl");
            else
                    second = URLstring.indexOf(".com");
            resticting = initialization.substring(first + 1, second);
            name = "-".concat(resticting);

    }

}