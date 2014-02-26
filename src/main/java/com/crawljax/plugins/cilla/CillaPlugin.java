package com.crawljax.plugins.cilla;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import se.fishtank.css.selectors.NodeSelectorException;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.state.StateVertex;
import com.crawljax.plugins.cilla.analysis.CssAnalyzer;
import com.crawljax.plugins.cilla.analysis.ElementWithClass;
import com.crawljax.plugins.cilla.analysis.ElementWrapper;
import com.crawljax.plugins.cilla.analysis.MCssRule;
import com.crawljax.plugins.cilla.analysis.MProperty;
import com.crawljax.plugins.cilla.analysis.MSelector;
import com.crawljax.plugins.cilla.analysis.MatchedElements;
import com.crawljax.plugins.cilla.examples.CillaRunner;
import com.crawljax.plugins.cilla.util.CSSDOMHelper;
import com.crawljax.plugins.cilla.util.CssParser;
import com.crawljax.plugins.cilla.visualizer.AdditionalVisualization;
import com.crawljax.plugins.cilla.visualizer.CillaVisualizer;
import com.crawljax.plugins.cilla.visualizer.VisualizerServlet;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.fishtank.css.selectors.NodeSelector;
import se.fishtank.css.selectors.NodeSelectorException;
import se.fishtank.css.selectors.dom.DOMNodeSelector;
public class CillaPlugin implements OnNewStatePlugin, PostCrawlingPlugin {

public static int totalCssRules = 0;
public static int totalCssSelectors = 0;

        private static final Logger LOGGER = Logger.getLogger(CillaPlugin.class.getName());

        private Map<String, List<MCssRule>> cssRules = new HashMap<String, List<MCssRule>>();
        
public Map<String, List<MCssRule>> embeddedcssRules1 = new HashMap<String, List<MCssRule>>();
        public static final Set<String> cssEffectiveRuntime = new HashSet<String>();

        final SetMultimap<String, ElementWithClass> elementsWithNoClassDef = HashMultimap.create();

        private final SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy-hhmmss");

        private final File outputFile = new File("output/cilla"
         + String.format("%s", sdf.format(new Date())) + ".txt");

        private final Random random = new Random();

        private int cssLOC;
        private int ineffectivePropsSize;
        private int totalCssRulesSize;
         
public static String[] visualFilename;

public static int outputNum = 1;
public static int outputNum1;

public static double Mean = 0;
public static double Median = 0;
public static int min = 0;
public static int max = 0;
public static double meanSelector;
public static double medianSelector;
public static double minSelector;
public static double maxSelector;
public int countEmbeddedRules = 0;
public static List<String> allEmbeddedRules;
List<MSelector> allSelectors;
public static double uni = 0;
public static double AS = 0;
public static double abstFactor = 0;
int[] h;
public static double id;
public static double clas;
public static double element;
public static double averageid = 0;
public static double averageclas = 0;
public static double averageelement = 0;
List<Integer> numElementsDomStates = new ArrayList<Integer>();


List<Integer> totalSelectorsEachDomState = new ArrayList<Integer>();
List<Double> universalityArray = new ArrayList<Double>();
List<Double> nodeThemselves = new ArrayList<Double>();
double uni1 = 0;
double totaldescendants;
List<Double> numDescendantsDomStates = new ArrayList<Double>();
List<Node> finalChildren;
int count = 0;
        public void onNewState(CrawlerContext context, StateVertex newState) {
        
         count++;
                // if the external CSS files are not parsed yet, do so
                parseCssRules(context, newState);

                // now we have all the CSS rules neatly parsed in "rules"
                checkCssOnDom(newState);
 
                checkClassDefinitions(newState);
                storeNumElementsOfEachDomState(newState);
                getTotalSelectorsEachDomState();


               
        }

    private void getTotalSelectorsEachDomState(){
    
          totalCssSelectors = 0;
        
          for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {
                     
                  for (MCssRule mrule : entry.getValue()) {
                          totalCssSelectors += mrule.getSelectors().size();
  
                  }
                  
            
          }
          totalSelectorsEachDomState.add(totalCssSelectors);
         
    
    }
     
      private void storeNumElementsOfEachDomState(StateVertex state){
    
     Document dom;
try {
dom = state.getDocument();
numElementsDomStates.add(CSSDOMHelper.numElementsDocumentTree(dom));
try {
checkCssSelectorRulesOnDom1(state.getName(), dom);
} catch (NodeSelectorException e) {

numDescendantsDomStates.add((double) finalChildren.size());
// TODO Auto-generated catch block
e.printStackTrace();
}

} catch (IOException e) {
// TODO Auto-generated catch block
e.printStackTrace();
}
    
    
        }
      
      public void checkCssSelectorRulesOnDom1(String stateName, Document dom
              ) throws NodeSelectorException {
       
     finalChildren = new ArrayList<Node>();
     int w = 0;
       List<MCssRule> mRules = null;
       for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {
        mRules = CssAnalyzer.checkCssRulesOnDom(stateName, dom, entry.getValue());
          
     totaldescendants = 0;
     
                     for (MCssRule mRule : mRules) {
                             List<MSelector> selectors = mRule.getSelectors();
                              
                             for (MSelector selec : selectors) {

                                     String cssSelector = selec.getCssSelector();

                                     NodeSelector selector = new DOMNodeSelector(dom);
                                     Set<Node> result = selector.querySelectorAll(cssSelector);
                                    
     
     NodeList children = null;
   

   
     

                                     for (Node node : result) {
      
      
     finalChildren.add(node);
                                           
                                             
        
         children = node.getChildNodes();
       
          for(int x = 0; x< children.getLength(); x++){
                finalChildren.add(children.item(x));
             
                                                         }
                   for(int x = w; x< finalChildren.size(); x++){
                     node = finalChildren.get(x);
                children = node.getChildNodes();
             for(int y = 0; y< children.getLength(); y++ ){
               finalChildren.add(children.item(y));
                                                                   }
                                                                  }
                            w = finalChildren.size();
                                                                             
              
     
      }
      }
        }
       }
       totaldescendants = finalChildren.size();
      numDescendantsDomStates.add((double) finalChildren.size());
               
        }
       
          
           
                   
        private void checkClassDefinitions(StateVertex state) {
                LOGGER.info("Checking CSS class definitions...");
                try {

                        List<ElementWithClass> elementsWithClass =
                         CSSDOMHelper.getElementWithClass(state.getName(), state.getDocument());

                        for (ElementWithClass element : elementsWithClass) {

                                for (String classDef : element.getClassValues()) {
                                        boolean matchFound = false;

                                        search: for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {
                                                for (MCssRule rule : entry.getValue()) {
                                                        for (MSelector selector : rule.getSelectors()) {
                                                                if (selector.getCssSelector().contains("." + classDef)) {
                                                                        // TODO e.g. css: div.news { color: blue} <span><p>
                                                                        // if (selector.getCssSelector().startsWith("." + classDef)) {
                                                                        matchFound = true;
                                                                        break search;
                                                                        // }
                                                                }
                                                        }
                                                }
                                        }

                                        if (!matchFound) {
                                                element.setUnmatchedClass(classDef);
                                                elementsWithNoClassDef.put(element.getStateName(), element);

                                        }
                                }
                        }

                } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                } catch (XPathExpressionException e) {
                        LOGGER.error(e.getMessage(), e);
                }

        }

        private void checkCssOnDom(StateVertex state) {
                LOGGER.info("Checking CSS on DOM...");
                // check the rules on the current DOM state.
        
                try {
           
                        for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {
                        
                                CssAnalyzer.checkCssSelectorRulesOnDom(state.getName(), state.getDocument(),
                                 entry.getValue());

                               
      
                        }
         
 
                   

                } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                } catch (NodeSelectorException e) {
                        LOGGER.error(e.getMessage(), e);
                }
               
        }

        private int countLines(String cssText) {
                int count = 0;
                cssText = cssText.replaceAll("\\{", "{\n");
                cssText = cssText.replaceAll("\\}", "}\n");
                cssText = cssText.replaceAll("\\}", "}\n");
                cssText = cssText.replaceAll("\\;", ";\n");

                if (cssText != null && !cssText.equals("")) {
                        LineNumberReader ln = new LineNumberReader(new StringReader(cssText));
                        try {
                                while (ln.readLine() != null) {
                                        count++;
                                }
                        } catch (IOException e) {
                                LOGGER.error(e.getMessage(), e);
                        }
                }
                return count;
        }

        private void parseCssRules(CrawlerContext context, StateVertex state) {
                String url = context.getBrowser().getCurrentUrl();
int num1 = 0;
int num2 = 0;
double num3 = 0;

                try {
                        final Document dom = state.getDocument();

                        for (String relPath : CSSDOMHelper.extractCssFilenames(dom)) {
                                String cssUrl = CSSDOMHelper.getAbsPath(url, relPath);
                                if (!cssRules.containsKey(cssUrl)) {
                                        LOGGER.info("CSS URL: " + cssUrl);

                                        String cssContent = CSSDOMHelper.getURLContent(cssUrl);
                                        cssLOC += countLines(cssContent);
                                        List<MCssRule> rules = CssParser.getMCSSRules(cssContent);
                                       
                                        if (rules != null && rules.size() > 0) {
 for(int i = 0; i< rules.size(); i++){
num1 += rules.get(i).getSelectors().size();


 }

                                         cssRules.put(cssUrl, rules);
                                     
   List<MSelector> selectorsinDom = new ArrayList<MSelector>();
   int r = 0;
   int[] g;
 for(int i = 0; i< rules.size(); i++){
selectorsinDom.addAll(rules.get(i).getSelectors());

 }
 g = new int[selectorsinDom.size()];
 for(int i = 0; i< rules.size(); i++){
String s = selectorsinDom.get(i).getSpecificity().toString();

int l = Integer.parseInt(s.substring(1, 2));
int m = Integer.parseInt(s.substring(4, 5));
int n = Integer.parseInt(s.substring(7, 8));
int o = Integer.parseInt(s.substring(10, 11));
int t = l+m+n+o;
g[r]= t;
r++;

 
 }
 for(int y = 0; y<g.length;y++){
     int a = Integer.parseInt(selectorsinDom.get(y).getSpecificity().toString().substring(10, 11));
     // checks simple element selector
             if(g[y] == 1 && a == 1){
                     num3++;
                    
             }
 }
 // checks combined selectors in which the last simple selector is element
 for(int j0=0; j0<selectorsinDom.size();j0++){
     
  String[] parts2 = selectorsinDom.get(j0).toString().split("XPath");
  
  
 String f = parts2[0].replace("Selector:", "");

 String[] parts3 = f.split(" ");
 int k = parts3.length-1;
 while(parts3[k].isEmpty()){
         k--;
 }
 
             if(!parts3[k].isEmpty() && !parts3[k].contains(".") && !parts3[k].contains("#") && g[j0]!=1){
         num3++;
 
 
   
             }
}
 

                                        }
                                }
                        }

                        // get all the embedded <STYLE> rules, save per HTML page
                        if (!cssRules.containsKey(url)) {
                                String embeddedRules = CSSDOMHelper.getEmbeddedStyles(dom);
                                cssLOC += countLines(embeddedRules);

                                List<MCssRule> rules = CssParser.getMCSSRules(embeddedRules);
                               
                               
 countEmbeddedRules+= rules.size();

                                if (rules != null && rules.size() > 0) {
                                        cssRules.put(url, rules);
                                         
   embeddedcssRules1.put(url, rules);
   for(int i = 0; i< rules.size(); i++){
num2 += rules.get(i).getSelectors().size();

}
   
   List<MSelector> selectorsinDom = new ArrayList<MSelector>();
   int r = 0;
   int[] g;
 for(int i = 0; i< rules.size(); i++){
selectorsinDom.addAll(rules.get(i).getSelectors());

 }
 g = new int[selectorsinDom.size()];
 for(int i = 0; i< selectorsinDom.size(); i++){
String s = selectorsinDom.get(i).getSpecificity().toString();

int l = Integer.parseInt(s.substring(1, 2));
int m = Integer.parseInt(s.substring(4, 5));
int n = Integer.parseInt(s.substring(7, 8));
int o = Integer.parseInt(s.substring(10, 11));
int t = l+m+n+o;
g[r]= t;
r++;

 
 }
 for(int y = 0; y<g.length;y++){
     int a = Integer.parseInt(selectorsinDom.get(y).getSpecificity().toString().substring(10, 11));
     // checks simple element selector
             if(g[y] == 1 && a == 1){
                     num3++;
                    
             }
 }
 // checks combined selectors in which the last simple selector is element
 for(int j0=0; j0<selectorsinDom.size();j0++){
     
  String[] parts2 = selectorsinDom.get(j0).toString().split("XPath");
  
  
 String f = parts2[0].replace("Selector:", "");

 String[] parts3 = f.split(" ");
 int k = parts3.length-1;
 while(parts3[k].isEmpty()){
         k--;
 }
 
             if(!parts3[k].isEmpty() && !parts3[k].contains(".") && !parts3[k].contains("#") && g[j0]!=1){
         num3++;
 
 
   
             }
}
 
                                }
                        }

                } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                }
  int summation = num1+num2;
  if(summation != 0){
  uni1 = num3/summation;
  }
  
  
  universalityArray.add(uni1);
  

        }

        
        public void getEmbeddedRules(){
            
         allEmbeddedRules = new ArrayList<String>();
        
         for (Map.Entry<String, List<MCssRule>> entry : embeddedcssRules1.entrySet()) {
         for (MCssRule mrule : entry.getValue()){
      
         allEmbeddedRules.add(mrule.getRule().toString());
       
         }
        
         }
        
         }
       
        public void determineThreshold(){
            
            allSelectors=new ArrayList<MSelector>();
            int[] b = new int[totalCssSelectors];
            int j = 0;
            double sum = 0;
            double sum1 = 0;
            
            
          
            
            for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {
                  for (MCssRule mrule : entry.getValue()) {
                    
                            allSelectors.addAll(mrule.getSelectors());
                            
                            
                            
                    }
                    
            }
           
            
            for(int i=0; i<allSelectors.size();i++){
                    
                    b[j]= allSelectors.get(i).getProperties().size();
                    
                    j++;
                 
                    
            }
            
          
            for(int k=0; k<b.length;k++){
                   
                    sum+= b[k];
            }
            if(totalCssSelectors != 0){
            Mean = sum/totalCssSelectors;
            Arrays.sort(b);
            min = b[0];
            max = b[b.length-1];
            if(b.length%2 == 0){
                    Median = (b[(b.length)/2]+b[(b.length)/2+1])/2;
            }
            else{
                    Median = b[(b.length)/2+1];
            }
           
            
            int z = 0; int r= 0;
           int[] c = new int[totalCssSelectors];
           h = new int[totalCssSelectors];
            for(int p = 0;p<allSelectors.size();p++){
            String s = allSelectors.get(p).getSpecificity().toString();
 
            int l = Integer.parseInt(s.substring(1, 2));
            int m = Integer.parseInt(s.substring(4, 5));
            int n = Integer.parseInt(s.substring(7, 8));
            int o = Integer.parseInt(s.substring(10, 11));
            int q = l+m+n+o;
            c[z]= q;
            h[r] = q; // I need this unsorted array for universality method
            z++;
            r++;
            
            }
            for(int y = 0; y<c.length;y++){
                    sum1+= c[y];
                    
                    
            }
           
           
           Arrays.sort(c);
          
            meanSelector = sum1/c.length;
            minSelector = c[0];
            maxSelector = c[c.length-1];
            
            if(c.length%2 == 0){
                    medianSelector = (c[(c.length)/2]+c[(c.length)/2+1])/2;
            }
            else{
                    medianSelector = c[(c.length)/2+1];
            }
        
          
            }
            
            
            }
        
        public void countCharacteristics(){
            id= 0;
            clas = 0;
            element = 0;
            if(totalCssSelectors !=0){
            for(int p = 0;p<allSelectors.size();p++){
            String s = allSelectors.get(p).getSpecificity().toString();
            id = id+ Integer.parseInt(s.substring(4, 5));
            clas = clas+ Integer.parseInt(s.substring(7, 8));
            element = element+ Integer.parseInt(s.substring(10, 11));
            }
            averageid = id / totalCssSelectors;
           averageclas = clas / totalCssSelectors;
           averageelement = element / totalCssSelectors;
            }
   }
       
        
        public void universality(){
         double summ = 0;
         for(int i = 0; i< universalityArray.size(); i++){
         summ+= universalityArray.get(i);	
         }
         if(universalityArray.size()!=0){
         uni = summ / universalityArray.size();
         }
        
        
       
   }

        public void averageScope() throws Exception{
         double ASPerPage = 0;
         double sumASPerPage = 0;
        

        
         for(int i = 0; i< numElementsDomStates.size(); i++){
    
         if(numElementsDomStates.get(i)!=0 && totalSelectorsEachDomState.get(i)!=0){
        ASPerPage = (numDescendantsDomStates.get(i))/(numElementsDomStates.get(i)*totalSelectorsEachDomState.get(i));
        sumASPerPage += ASPerPage;
        
         }
         }
        
         AS = sumASPerPage/numDescendantsDomStates.size();
        
        
         

   
           }
        public void abstractnessFactor(){
            
            abstFactor = Math.min(uni, AS);
    }
public void writecssintoFileCssLint(){

outputNum1 = 1;

new File("C:/Users/Golnaz/cilla/CsslintReports").mkdirs();

FileOutputStream fop =null;


for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {
visualFilename = new String[cssRules.entrySet().size()+1];

}
    
      for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {
     String filename = new String();
     filename = entry.getKey();
    
     visualFilename[outputNum1] = filename;
     
              for (MCssRule mrule : entry.getValue()) {
                    
                      
                              File file;
                             
                              try{
                                      
                                      file = new File("C:/Users/Golnaz/cssfile"+outputNum+outputNum1+".css");
                                      fop = new FileOutputStream(file, true);
                                      if (!file.exists()) {
                                                              file.createNewFile();
                                                      }
                                       byte[] contentInBytes = mrule.getRule().getCssText().getBytes();

               fop.write(contentInBytes);
                                      fop.flush();
                                      fop.close();
                                      
                              } catch (IOException e) {
                                      e.printStackTrace();
                                      } finally {
                                      try {
                                              if (fop != null) {
                                                      fop.close();
                                              }
                                      } catch (IOException e) {
                                              e.printStackTrace();
                                      }
                                      }
                      
              }
             outputNum1++;
      }
     
      Runtime rt = Runtime.getRuntime();
      
      try {
             for(int i = 1; i< outputNum1; i++) {
              FileOutputStream fop1 =null;
               String[] command = {"java","-jar", "js.jar", "csslint-rhino.js", "cssfile"+outputNum+i+".css"};
                  
       ProcessBuilder probuilder = new ProcessBuilder( command );
       //You can set up your work directory
       probuilder.directory(new File("C:/Users/Golnaz"));

       Process process = probuilder.start();

       //Read out dir output
       InputStream is = process.getInputStream();
       InputStreamReader isr = new InputStreamReader(is);
       BufferedReader br = new BufferedReader(isr);
       String line;
       System.out.printf("Output of running %s is:\n", Arrays.toString(command));
       while ((line = br.readLine()) != null) {
      
       // System.out.println(line);
       File file1;
                              try{
                                      
                                      file1 = new File("C:/Users/Golnaz/cilla/CsslintReports/output"+outputNum+i+".txt");
                                      
                                      fop1 = new FileOutputStream(file1, true);
                                      
                                      if (!file1.exists()) {
                                                              file1.createNewFile();
                                                      }
                                       byte[] contentInBytes = line.getBytes();
                                      
                                      
                                      
                                      
fop1.write(contentInBytes);
// go to next line
fop1.write(13);
fop1.write(10);
                                      fop1.flush();
                                      fop1.close();
                                      
                              } catch (IOException e) {
                                      e.printStackTrace();
                                      } finally {
                                      try {
                                              if (fop1 != null) {
                                                      fop1.close();
                                              }
                                      } catch (IOException e) {
                                              e.printStackTrace();
                                      }
                                      }
                      
       }


       //Wait to get exit value
       try {
       int exitValue = process.waitFor();
       System.out.println("\n\nExit Value is " + exitValue);
       } catch (InterruptedException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
       }
                      

             }
             AdditionalVisualization av = new AdditionalVisualization();
             av.addCssLint();
           //av.addCssCss();
 
outputNum++;

      }
      
              catch (IOException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
      }
      
}

public void getCssCssReport(){

//outputNum = 1;
new File("D:/CssCssReports").mkdirs();




// Runtime rt = Runtime.getRuntime();

try {
for(int i = 1; i< outputNum1; i++) {
FileOutputStream fop1 =null;
String[] command = {"C:/Ruby193/bin/csscss.bat", "-n", "1","-v", "C:/Users/Golnaz/cssfile"+(outputNum-1)+i+".css"};
Process p = Runtime.getRuntime().exec(command);
InputStream is = p.getInputStream();
InputStreamReader isr = new InputStreamReader(is);
BufferedReader br = new BufferedReader(isr);
String line;
System.out.printf("Output of running %s is:\n", Arrays.toString(command));
while ((line = br.readLine()) != null) {

// System.out.println(line);
File file1;
try{

file1 = new File("D:/CssCssReports/output"+(outputNum-1)+i+".txt");

fop1 = new FileOutputStream(file1, true);

if (!file1.exists()) {
file1.createNewFile();
}
byte[] contentInBytes = line.getBytes();




fop1.write(contentInBytes);
// go to next line
fop1.write(13);
fop1.write(10);
fop1.flush();
fop1.close();

} catch (IOException e) {
e.printStackTrace();
} finally {
try {
if (fop1 != null) {
fop1.close();
}
} catch (IOException e) {
e.printStackTrace();
}
}

}


//Wait to get exit value
try {
int exitValue = p.waitFor();
System.out.println("\n\nExit Value is " + exitValue);
} catch (InterruptedException e) {
// TODO Auto-generated catch block
e.printStackTrace();
}


}
AdditionalVisualization av = new AdditionalVisualization();

av.addCssCss();

//outputNum++;

}

          catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
  }
  

}




        @Override
        public void postCrawling(CrawlSession session, ExitStatus exitReason) {
        
        
        
                totalCssRules = 0;
                totalCssSelectors = 0;
             
                for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {
                        totalCssRules += entry.getValue().size();
  
                        for (MCssRule mrule : entry.getValue()) {
                                totalCssSelectors += mrule.getSelectors().size();
        
                        }
                  
                }
                
             
getEmbeddedRules();


determineThreshold();
countCharacteristics();
universality();
try {
    averageScope();
} catch (Exception e1) {
    // TODO Auto-generated catch block
    e1.printStackTrace();
}
abstractnessFactor();

//writecssintoFileCssLint();
//getCssCssReport();
                StringBuffer output = new StringBuffer();
                StringBuffer bufferUnused = new StringBuffer();
                StringBuffer bufferUsed = new StringBuffer();
                StringBuffer undefinedClasses = new StringBuffer();
                int used = getUsedRules(bufferUsed);
                int unused = getUnmatchedRules(bufferUnused);
                analyzeProperties();

                int undefClasses = getUndefinedClasses(undefinedClasses);
                StringBuffer effective = new StringBuffer();
                int effectiveInt = getEffectiveSelectorsBasedOnProps(effective);
                
                
StringBuffer tooSpecific = new StringBuffer();
int toospec = getTooSpecificSelectors(tooSpecific);
StringBuffer tooSpecific2 = new StringBuffer();
int toospec2 = getTooSpecificSelectors2(tooSpecific2);
StringBuffer tooLazy = new StringBuffer();
int toolaz = getLazyRules(tooLazy);
StringBuffer tooLong = new StringBuffer();
int toolog = getTooLongRules(tooLong);
StringBuffer emptyCatch = new StringBuffer();
int emptycat = getEmptyCatch(emptyCatch);
StringBuffer undoingStyle = new StringBuffer();
int undostyle = getUndoingStyle(undoingStyle);
StringBuffer idWithClassOrElement = new StringBuffer();
int idwith = getIdWithClassOrElement(idWithClassOrElement);
StringBuffer reactiveImportant = new StringBuffer();
int impo = getReactiveImportant(reactiveImportant);
StringBuffer inappFontSize = new StringBuffer();
int inappfo = getInnappFontSize(inappFontSize);
StringBuffer embeddedRules = new StringBuffer();
int dsf = getNumEmbeddedRules(embeddedRules);
StringBuffer dangerousSelectors = new StringBuffer();
int danSel = getDangerousSelectors(dangerousSelectors);
StringBuffer invalidSelectors = new StringBuffer();
int invalidSyn = getInvalidSelectors(invalidSelectors);


                StringBuffer ineffectiveBuffer = new StringBuffer();
                int ineffectiveInt = getIneffectiveSelectorsBasedOnProps(ineffectiveBuffer);

                output.append("Analyzed " + session.getConfig().getUrl() + " on "
                 + new SimpleDateFormat("dd/MM/yy-hh:mm:ss").format(new Date()) + "\n");

                output.append("-> Files with CSS code: " + cssRules.keySet().size() + "\n");
                for (String address : cssRules.keySet()) {
                        output.append(" Address: " + address + "\n");
                }
                // output.append("Total CSS Size: " + getTotalCssRulesSize() + " bytes" + "\n");

                output.append("-> LOC (CSS): " + cssLOC + "\n");
                output.append("-> Total Defined CSS rules: " + totalCssRules + "\n");
                output.append("-> Total Defined CSS selectors: " + totalCssSelectors + " from which: \n");
                int ignored = totalCssSelectors - (unused + used);
                output.append(" -> Ignored (:link, :hover, etc): " + ignored + "\n");
                output.append(" -> Unmatched: " + unused + "\n");
                output.append(" -> Matched: " + used + "\n");
                output.append(" -> Ineffective: " + ineffectiveInt + "\n");
                output.append(" -> Effective: " + effectiveInt + "\n");
                output.append("-> Total Defined CSS Properties: " + countProperties() + "\n");
                output.append(" -> Ignored Properties: " + countIgnoredProperties() + "\n");
                output.append(" -> Unused Properties: " + countUnusedProps() + "\n");

                // output.append("-> Effective CSS Rules: " + CSSProxyPlugin.cssTraceSet.size() + "\n");

                // output.append(" -> Effective: " + effectiveInt + "\n");
                output.append("->> Undefined Classes: " + undefClasses + "\n");
                // output.append("->> Duplicate Selectors: " + duplicates + "\n\n");
                output.append("By deleting unused rules, css size reduced by: "
                 + Math.ceil((double) reducedSize() / getTotalCssRulesSize() * 100) + " percent"
                 + "\n");

                
// prints number of css smells in the summary tab
output.append("CSS SMELLS: " + "\n");
output.append(" -> Rules with Too Specific Selectors Type I: "+ toospec+ "\n");
output.append(" -> Rules with Too Specific Selectors Type II: "+ toospec2+ "\n");
output.append(" -> Lazy Rules: "+ toolaz+ "\n");
output.append(" -> Too Long Rules: "+ toolog+ "\n");
output.append(" -> Rules with Empty Catch: "+ emptycat+ "\n");
output.append(" -> Overriding Properties: "+ undostyle+ "\n");
output.append(" -> Selectors with ID and at least one class or element: "+ idwith + "\n");
output.append(" -> Total Number of !important used in the code(Reactiveness): "+ impo + "\n");
output.append(" -> Selectors with Inappropriate Font-size Value for their Properties: "+ inappfo + "\n");
output.append(" -> Embedded Rules: "+ countEmbeddedRules +"\n");
output.append(" -> Rules with Dangerous Selectors: "+ danSel +"\n");
output.append(" -> Rules with Invalid Selectors: "+ invalidSyn +"\n");


                /*
* This is where the com.crawljax.plugins.cilla.visualizer gets called.
*/
                String tmpStr = new String();
                tmpStr = output.toString().replace("\n", "<br>");
                String url = session.getConfig().getUrl().toString();

                /* This is where the Visualizer plug-in is invoked */
                CillaVisualizer cv = new CillaVisualizer();
                cv.openVisualizer(url, tmpStr, cssRules, elementsWithNoClassDef);

                output.append(ineffectiveBuffer.toString());
                output.append(effective.toString());
                output.append(bufferUnused.toString());
                // output.append(bufferUsed.toString());
                output.append(undefinedClasses);
                // output.append(duplicateSelectors);

                
output.append(tooSpecific.toString());
output.append(tooSpecific2.toString());
output.append(tooLazy.toString());
output.append(tooLong.toString());
output.append(emptyCatch.toString());
output.append(undoingStyle.toString());
output.append(idWithClassOrElement.toString());
output.append(reactiveImportant.toString());
output.append(inappFontSize.toString());
output.append(embeddedRules.toString());
output.append(dangerousSelectors.toString());
output.append(invalidSelectors.toString());


               try {
                        FileUtils.writeStringToFile(outputFile, output.toString());

                } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                }

//getTotalSelectorsEachDomState();

        }

        private int countProperties() {
                int counter = 0;
                for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {
                        List<MCssRule> rules = entry.getValue();

                        for (MCssRule rule : rules) {
                                for (int i = 0; i < rule.getSelectors().size(); i++)
                                        counter += rule.getProperties().size();
                        }
                }

                return counter;

        }

        private int countIgnoredProperties() {
                int counter = 0;
                for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {
                        for (MCssRule rule : entry.getValue()) {
                                List<MSelector> selectors = rule.getSelectors();
                                if (selectors.size() > 0) {
                                        for (MSelector selector : selectors) {
                                                if (selector.isIgnore()) {
                                                        counter += rule.getProperties().size();
                                                }

                                        }

                                }
                        }
                }
                return counter;

        }

        private int getEffectiveSelectorsBasedOnProps(StringBuffer buffer) {

                int counterEffectiveSelectors = 0;
                buffer.append("\n========== EFFECTIVE CSS SELECTORS ==========\n");
                for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {
                        List<MCssRule> rules = entry.getValue();

                        buffer.append("\n== IN CSS: " + entry.getKey() + "\n");

                        for (MCssRule rule : rules) {
                                List<MSelector> selectors = rule.getMatchedSelectors();

                                if (selectors.size() > 0) {

                                        for (MSelector selector : selectors) {
                                                if (selector.hasEffectiveProperties()) {
                                                        buffer.append("CSS rule: " + rule.getRule().getCssText() + "\n");
                                                        buffer.append("at line: " + rule.getLocator().getLineNumber() + "\n");
                                                        buffer.append(" Selector: " + selector.getCssSelector() + "\n");
                                                        counterEffectiveSelectors++;

                                                        // buffer.append(" has effective properties: \n");
                                                        for (MProperty prop : selector.getProperties()) {
                                                                buffer.append(" Property " + prop + "\n");
                                                        }
                                                }
                                                buffer.append("\n");
                                        }
                                }
                        }
                }

                return counterEffectiveSelectors;
        }

        private int getIneffectiveSelectorsBasedOnProps(StringBuffer buffer) {

                int counterIneffectiveSelectors = 0;
                buffer.append("========== INEFFECTIVE CSS SELECTORS ==========\n");
                for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {
                        List<MCssRule> rules = entry.getValue();

                        buffer.append("== IN CSS: " + entry.getKey() + "\n");

                        for (MCssRule rule : rules) {
                                List<MSelector> selectors = rule.getMatchedSelectors();

                                if (selectors.size() > 0) {

                                        for (MSelector selector : selectors) {
                                                if (!selector.hasEffectiveProperties() && !selector.isIgnore()) {
                                                        buffer.append("Ineffective: ");
                                                        buffer.append("CSS rule: " + rule.getRule().getCssText() + "\n");

                                                        buffer.append("at line: " + rule.getLocator().getLineNumber() + "\n");
                                                        buffer.append(" Selector: " + selector.getCssSelector() + "\n\n");
                                                        counterIneffectiveSelectors++;
                                                        // ineffectivePropsSize+=selector.getCssSelector().getBytes().length;

                                                }

                                        }
                                }
                        }
                }

                return counterIneffectiveSelectors;
        }

        private int countUnusedProps() {

                int counter = 0;
                for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {
                        List<MCssRule> rules = entry.getValue();

                        for (MCssRule rule : rules) {
                                List<MSelector> selectors = rule.getSelectors();
                                if (selectors.size() > 0) {
                                        for (MSelector selector : selectors) {
                                                if (!selector.isIgnore()) {
                                                        for (MProperty prop : selector.getProperties()) {
                                                                if (!prop.isEffective()) {
                                                                        counter++;
                                                                        // ineffectivePropsSize+=prop.getsize();
                                                                }

                                                        }
                                                }

                                        }
                                }
                        }
                }

                return counter;
        }

        private void analyzeProperties() {

                for (String keyElement : MatchedElements.elementSelectors.keySet()) {
                        LOGGER.debug("keyElement: " + keyElement);
                        List<MSelector> selectors = MatchedElements.elementSelectors.get(keyElement);
                        // order according to the com.crawljax.plugins.cilla.util.specificity rules
                        MSelector.orderSpecificity(selectors);
                        String overridden = "overridden-" + random.nextInt();
                        LOGGER.debug("RANDOM: " + overridden);
                        for (int i = 0; i < selectors.size(); i++) {
                                MSelector selector = selectors.get(i);
                                for (MProperty property : selector.getProperties()) {
                                        if (!property.getStatus().equals(overridden)) {
                                                property.setEffective(true);
                                                LOGGER.debug("SET effective: " + property);

                                                // not set all the similar properties in other selectors to overridden

                                                for (int j = i + 1; j < selectors.size(); j++) {
                                                        MSelector nextSelector = selectors.get(j);
                                                        for (MProperty nextProperty : nextSelector.getProperties()) {
                                                                if (property.getName().equalsIgnoreCase(nextProperty.getName())) {

                                                                        nextProperty.setStatus(overridden);
                                                                }
                                                        }
                                                }
                                        } else {
                                                LOGGER.debug("OVRRIDDEN: " + property);
                                        }
                                }
                        }
                }

        }

        private int getDuplicateSelectors(StringBuffer buffer) {
                buffer.append("========== Duplicate CSS Selectors ==========\n");
                final List<MCssRule> allRules = new ArrayList<MCssRule>();
                for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {
                        allRules.addAll(entry.getValue());
                }

                Set<String> dups = CssAnalyzer.getDuplicateSelectors(allRules);

                for (String duplicate : dups) {
                        buffer.append("Duplicate: " + duplicate + "\n");
                }

                return dups.size();
        }

        private int getUndefinedClasses(StringBuffer output) {
                output.append("========== UNDEFINED CSS CLASSES ==========\n");

                Set<String> undefinedClasses = new HashSet<String>();

                // for (ElementWithClass el : elementsWithNoClassDef) {
                for (String key : elementsWithNoClassDef.keySet()) {
                        output.append("State: " + key + "\n");
                        Set<ElementWithClass> set = elementsWithNoClassDef.get(key);
                        for (ElementWithClass e : set) {
                                for (String unmatched : e.getUnmatchedClasses()) {
                                        if (undefinedClasses.add(unmatched)) {

                                                output.append("Undefined class: ");
                                                output.append(" " + unmatched + "\n");
                                        }
                                }
                        }
                        output.append("\n");
                }

                return undefinedClasses.size();
        }

        private int getUnmatchedRules(StringBuffer buffer) {

                LOGGER.info("Reporting Unmatched CSS Rules...");
                buffer.append("========== UNMATCHED CSS RULES ==========\n");
                int counter = 0;
                for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {
                        List<MCssRule> rules = entry.getValue();

                        buffer.append("== UNMATCHED RULES IN: " + entry.getKey() + "\n");
                        for (MCssRule rule : rules) {
                                List<MSelector> selectors = rule.getUnmatchedSelectors();
                                counter += selectors.size();
                                if (selectors.size() > 0) {
                                        buffer.append("Unmatched: ");
                                        buffer.append("CSS rule: " + rule.getRule().getCssText() + "\n");
                                        buffer.append("at line: " + rule.getLocator().getLineNumber() + "\n");

                                        for (MSelector selector : selectors) {
                                                // ineffectivePropsSize+=selector.getSize();
                                                buffer.append(selector.toString() + "\n");
                                        }
                                }
                        }
                }

                return counter;
        }

        private int getUsedRules(StringBuffer buffer) {
                LOGGER.info("Reporting Matched CSS Rules...");
                buffer.append("========== MATCHED CSS RULES ==========\n");
                int counter = 0;
                for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {
                        List<MCssRule> rules = entry.getValue();

                        buffer.append("== MATCHED RULES IN: " + entry.getKey() + "\n");
                        for (MCssRule rule : rules) {
                                List<MSelector> selectors = rule.getMatchedSelectors();
                                counter += selectors.size();
                                if (selectors.size() > 0) {
                                        buffer.append("Matched: ");
                                        buffer.append("CSS rule: " + rule.getRule().getCssText() + "\n");
                                        buffer.append("at line: " + rule.getLocator().getLineNumber() + "\n");

                                        for (MSelector selector : selectors) {
                                                buffer.append(selector.toString() + "\n");
                                        }
                                }
                        }

                }
                return counter;
        }

        /************************************************************************************/
        /************************************************************************************/
        private int countTotalCssRules() {

                int totalCssRules = 0;
                for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {
                        totalCssRules += entry.getValue().size();
                }
                return (totalCssRules);

        }

        private int getTotalCssRulesSize() {
                for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {

                        for (MCssRule mrule : entry.getValue()) {
                                /*
* for(MSelector selec: mrule.getSelectors()){ totalCssRulesSize+=selec.getSize(); }
*
* } }
*/
                                totalCssRulesSize +=
                                 mrule.getRule().getCssText().trim().replace("{", "").replace("}", "")
                                 .replace(",", "").replace(" ", "").getBytes().length;

                        }

                }
                return totalCssRulesSize;

        }

        private int reducedSize() {
                boolean effective = false;
                boolean exit = false;
                int counter = 0;
                for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {

                        for (MCssRule mrule : entry.getValue()) {
                                List<MSelector> selector = mrule.getSelectors();
                                for (int i = 0; i < selector.size(); i++) {
                                        if (!selector.get(i).isIgnore()) {
                                                exit = true;
                                                List<MProperty> property = selector.get(i).getProperties();
                                                for (int j = 0; j < property.size(); j++) {
                                                        if (!property.get(j).isEffective()) {
                                                                effective = false;
                                                                for (int k = i + 1; k < selector.size(); k++) {
                                                                        if (!selector.get(k).isIgnore()) {
                                                                                if (selector.get(k).getProperties().get(j).isEffective()) {
                                                                                        effective = true;
                                                                                        break;
                                                                                }
                                                                        }
                                                                }
                                                                if (!effective) {
                                                                        counter++;
                                                                        ineffectivePropsSize += property.get(j).getsize();
                                                                }
                                                        }
                                                }

                                        }
                                        if (exit) {
                                                if (counter == selector.get(i).getProperties().size())
                                                        ineffectivePropsSize += selector.get(i).getSize();
                                                break;
                                        }
                                }
                        }
                }
                return ineffectivePropsSize;
        }
        
   
        private int getReactiveImportant(StringBuffer buffer){
         LOGGER.info("Reporting CSS Rules with reactive !important...");
         buffer.append("========== CSS RULES with REACTIVE !important ==========\n");
         //SpecificityCalculator sc = new SpecificityCalculator();
         int counter = 0;
         for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()){
         List<MCssRule> rules = entry.getValue();
         buffer.append("== RULES WITH REACTIVE !IMPORTANT IN: " + entry.getKey() + "\n");
         for (MCssRule rule : rules){
        
         //sc.reset();
         List<MSelector> selectors = rule.getReactiveImportant();
         counter += selectors.size();
         if (selectors.size() > 0) { //more than 3 !important is used
         buffer.append("!important used: ");
         buffer.append("CSS rule: " + rule.getRule().getCssText() + "\n");
         buffer.append("at line: " + rule.getLocator().getLineNumber() + "\n");

         for (MSelector selector : selectors) {
         // ineffectivePropsSize+=selector.getSize();
         buffer.append(selector.toString() + "\n");
         }
         }
        

         }
         }
         return counter;
        
         }

         private int getLazyRules(StringBuffer buffer){
         LOGGER.info("Reporting Lazy CSS Rules...");
         buffer.append("========== LAZY CSS RULES ==========\n");
         int counter = 0;
         for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()){
         List<MCssRule> rules = entry.getValue();
         buffer.append("== LAZY RULES IN: " + entry.getKey() + "\n");
         for (MCssRule rule : rules){
        
        
         List<MSelector> selectors = rule.getLazyRules();
         counter += selectors.size();
         if (selectors.size() > 0) {
         buffer.append("Lazy: ");
         buffer.append("CSS rule: " + rule.getRule().getCssText() + "\n");
         buffer.append("at line: " + rule.getLocator().getLineNumber() + "\n");

         for (MSelector selector : selectors) {
         // ineffectivePropsSize+=selector.getSize();
         buffer.append(selector.toString() + "\n");
         }
         }

         }
         }
         return counter;
        
         }
         private int getTooLongRules(StringBuffer buffer){
         LOGGER.info("Reporting Too Long CSS Rules...");
         buffer.append("========== TOO LONG CSS RULES ==========\n");
         int counter = 0;
         for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()){
         List<MCssRule> rules = entry.getValue();
         buffer.append("== TOO LONG RULES IN: " + entry.getKey() + "\n");
         for (MCssRule rule : rules){
        
        
         List<MSelector> selectors = rule.getTooLongRules();
         counter += selectors.size();
         if (selectors.size() > 0) {
         buffer.append("Long: ");
         buffer.append("CSS rule: " + rule.getRule().getCssText() + "\n");
         buffer.append("at line: " + rule.getLocator().getLineNumber() + "\n");

         for (MSelector selector : selectors) {
         // ineffectivePropsSize+=selector.getSize();
         buffer.append(selector.toString() + "\n");
         }
         }

         }
         }
         return counter;
        
         }

         private int getEmptyCatch(StringBuffer buffer){
         LOGGER.info("Reporting CSS Rules with Empty Catch...");
         buffer.append("========== CSS RULES with EMPTY CATCH ==========\n");
         int counter = 0;
         for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()){
         List<MCssRule> rules = entry.getValue();
         buffer.append("== RULES with EMPTY CATCH IN: " + entry.getKey() + "\n");
         for (MCssRule rule : rules){
        
        
         List<MSelector> selectors = rule.getEmptyCatch();
         counter += selectors.size();
         if (selectors.size() > 0) {
         buffer.append("Empty Catch: ");
         buffer.append("CSS rule: " + rule.getRule().getCssText() + "\n");
         buffer.append("at line: " + rule.getLocator().getLineNumber() + "\n");

         for (MSelector selector : selectors) {
         // ineffectivePropsSize+=selector.getSize();
         buffer.append(selector.toString() + "\n");
         }
         }

         }
         }
         return counter;
        
         }

         private int getUndoingStyle(StringBuffer buffer){
         LOGGER.info("Reporting CSS Rules with Overriding Properties...");
         buffer.append("========== CSS RULES with OVERRIDING PROPERTIES ==========\n");
         int counter = 0;
         for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()){
         List<MCssRule> rules = entry.getValue();
         buffer.append("== RULES with OVERRIDING PROPERTIES IN: " + entry.getKey() + "\n");
         for (MCssRule rule : rules){
        
        
         List<MSelector> selectors = rule.getUndoingStyle();
         counter += selectors.size();
         if (selectors.size() > 0) {
         buffer.append("Overriding: ");
         buffer.append("CSS rule: " + rule.getRule().getCssText() + "\n");
         buffer.append("at line: " + rule.getLocator().getLineNumber() + "\n");

         for (MSelector selector : selectors) {
         // ineffectivePropsSize+=selector.getSize();
         buffer.append(selector.toString() + "\n");
         }
         }

         }
         }
         return counter;
        
         }

         private int getIdWithClassOrElement(StringBuffer buffer){
         LOGGER.info("Reporting Selectors with ID and at Least One Class or Element...");
         buffer.append("========== Selectors with ID and at Least One Class or Element ==========\n");
         int counter = 0;
         for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()){
         List<MCssRule> rules = entry.getValue();
         buffer.append("== ID+ RULES IN: " + entry.getKey() + "\n");
         for (MCssRule rule : rules){
        
        
         List<MSelector> selectors = rule.getIdWithClassOrElement();
         counter += selectors.size();
         if (selectors.size() > 0) {
         buffer.append("ID+: ");
         buffer.append("CSS rule: " + rule.getRule().getCssText() + "\n");
         buffer.append("at line: " + rule.getLocator().getLineNumber() + "\n");

         for (MSelector selector : selectors) {
         // ineffectivePropsSize+=selector.getSize();
         buffer.append(selector.toString() + "\n");
         }
         }

         }
         }
         return counter;
        
         }

         private int getTooSpecificSelectors(StringBuffer buffer){
         LOGGER.info("Reporting CSS Rules with Too Specific Selectors Type I...");
         buffer.append("========== TOO SPECIFIC CSS RULES TYPE I ==========\n");
         //SpecificityCalculator sc = new SpecificityCalculator();
         int counter = 0;
         for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()){
         List<MCssRule> rules = entry.getValue();
         buffer.append("== TOO SPECIFIC RULES TYPE I IN: " + entry.getKey() + "\n");
         for (MCssRule rule : rules){
        
         //sc.reset();
         List<MSelector> selectors = rule.getTooSpecificSelectors();
         counter += selectors.size();
         if (selectors.size() > 0) {
         buffer.append("Too Specific Type I: ");
         buffer.append("CSS rule: " + rule.getRule().getCssText() + "\n");
         buffer.append("at line: " + rule.getLocator().getLineNumber() + "\n");

         for (MSelector selector : selectors) {
         // ineffectivePropsSize+=selector.getSize();
         buffer.append(selector.toString() + "\n");
         }
         }

         }
         }
         return counter;
        
         }
         
         private int getTooSpecificSelectors2(StringBuffer buffer){
             LOGGER.info("Reporting CSS Rules with Too Specific Selectors Type II...");
             buffer.append("========== TOO SPECIFIC CSS RULES TYPE II ==========\n");
             //SpecificityCalculator sc = new SpecificityCalculator();
             int counter = 0;
             for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()){
             List<MCssRule> rules = entry.getValue();
             buffer.append("== TOO SPECIFIC RULES TYPE II IN: " + entry.getKey() + "\n");
             for (MCssRule rule : rules){
            
             //sc.reset();
             List<MSelector> selectors = rule.getTooSpecificSelectors2();
             counter += selectors.size();
             if (selectors.size() > 0) {
             buffer.append("Too Specific Type II: ");
             buffer.append("CSS rule: " + rule.getRule().getCssText() + "\n");
             buffer.append("at line: " + rule.getLocator().getLineNumber() + "\n");

             for (MSelector selector : selectors) {
             // ineffectivePropsSize+=selector.getSize();
             buffer.append(selector.toString() + "\n");
             }
             }

             }
             }
             return counter;
            
             }


         private int getInnappFontSize(StringBuffer buffer){
         LOGGER.info("Reporting Selectors with Inappropriate Value for Font-size...");
         buffer.append("========== Selectors with Font-sie Property with Inappropriate Value ==========\n");
         int counter = 0;
         for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()){
         List<MCssRule> rules = entry.getValue();
         buffer.append("== Inappropriate Font-size RULES IN: " + entry.getKey() + "\n");
         for (MCssRule rule : rules){
        
        
         List<MSelector> selectors = rule.checkFontSize();
         counter += selectors.size();
         if (selectors.size() > 0) {
         buffer.append("Inappropriate Font-size: ");
         buffer.append("CSS rule: " + rule.getRule().getCssText() + "\n");
         buffer.append("at line: " + rule.getLocator().getLineNumber() + "\n");

         for (MSelector selector : selectors) {
         // ineffectivePropsSize+=selector.getSize();
         buffer.append(selector.toString() + "\n");
         }
         }

         }
         }
         return counter;
        
        
         }

         public int getNumEmbeddedRules(StringBuffer buffer){
        
         LOGGER.info("Reporting Embedded CSS Rules...");
         buffer.append("========== EMBEDDED CSS RULES ==========\n");
        
         int counter = allEmbeddedRules.size();
        
        
         return counter;
        
         }

         private int getDangerousSelectors(StringBuffer buffer){
         LOGGER.info("Reporting Dangerous Selectors...");
         buffer.append("========== Dangerous Selectors ==========\n");
         int counter = 0;
         for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()){
         List<MCssRule> rules = entry.getValue();
         buffer.append("== RULES WITH DANGEROUS SELECTORS IN: " + entry.getKey() + "\n");
         for (MCssRule rule : rules){
        
        
         List<MSelector> selectors = rule.getDangerousSelectors();
         counter += selectors.size();
         if (selectors.size() > 0) {
         buffer.append("Dangerous Selectors: ");
         buffer.append("CSS rule: " + rule.getRule().getCssText() + "\n");
         buffer.append("at line: " + rule.getLocator().getLineNumber() + "\n");

         for (MSelector selector : selectors) {
         // ineffectivePropsSize+=selector.getSize();
         buffer.append(selector.toString() + "\n");
         }
         }

         }
         }
         return counter;
        
        
         }
         // Invalid Syntax
         private int getInvalidSelectors(StringBuffer buffer){
             LOGGER.info("Reporting Selectors with Invalid Syntax...");
             buffer.append("========== Invalid Selectors ==========\n");
             int counter = 0;
             for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()){
             List<MCssRule> rules = entry.getValue();
             buffer.append("== RULES WITH INVALID SELECTORS IN: " + entry.getKey() + "\n");
             for (MCssRule rule : rules){
            
            
             List<MSelector> selectors = rule.getSelectorsWithInvalidSyntax();
             counter += selectors.size();
             if (selectors.size() > 0) {
             buffer.append("Invalid Selectors: ");
             buffer.append("CSS rule: " + rule.getRule().getCssText() + "\n");
             buffer.append("at line: " + rule.getLocator().getLineNumber() + "\n");

             for (MSelector selector : selectors) {
             // ineffectivePropsSize+=selector.getSize();
             buffer.append(selector.toString() + "\n");
             }
             }

             }
             }
             return counter;
            
            
             }

}