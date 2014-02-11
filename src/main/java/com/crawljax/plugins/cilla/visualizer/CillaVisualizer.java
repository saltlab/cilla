package com.crawljax.plugins.cilla.visualizer;

import java.util.List;
import java.util.Map;

import com.crawljax.plugins.cilla.analysis.ElementWithClass;
import com.crawljax.plugins.cilla.analysis.MCssRule;
import com.google.common.collect.SetMultimap;


public class CillaVisualizer implements VisualizerPlugin {

        @Override
        public void openVisualizer(String url,
                                                                String summary,
                                                                Map<String, List<MCssRule>> cssRules,
                                                                SetMultimap<String, ElementWithClass> elementsWithNoClassDef) {
                
                // Call the Visualizer Servlet
                VisualizerServlet vs = new VisualizerServlet();
AdditionalVisualization av = new AdditionalVisualization();                
                vs.addSummary(url, summary);
                vs.constructRuleMap(cssRules);
                vs.addSortedOutput(cssRules, elementsWithNoClassDef);
                

av.addValidation(url);
av.addStatistics();


                
                // Call the open com.crawljax.plugins.cilla to open to welcome page
                OpenBrowser.open(vs.getWelcomePage());
        }

}