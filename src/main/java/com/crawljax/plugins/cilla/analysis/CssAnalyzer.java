package com.crawljax.plugins.cilla.analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.fishtank.css.selectors.NodeSelector;
import se.fishtank.css.selectors.NodeSelectorException;
import se.fishtank.css.selectors.dom.DOMNodeSelector;

import com.crawljax.plugins.cilla.CillaPlugin;
import com.crawljax.util.XPathHelper;

public class CssAnalyzer {
public static int[][] s;
public static double totaldescendants;
public static double[] descendantArray;
int j = 0;
        private static final Logger LOGGER = Logger.getLogger(CssAnalyzer.class.getName());

        public static List<MCssRule> checkCssSelectorRulesOnDom(String stateName, Document dom,
         List<MCssRule> mRules) throws NodeSelectorException {
  
  

                for (MCssRule mRule : mRules) {
                        List<MSelector> selectors = mRule.getSelectors();
                        
                        for (MSelector selec : selectors) {

                                String cssSelector = selec.getCssSelector();

                                NodeSelector selector = new DOMNodeSelector(dom);
                                Set<Node> result = selector.querySelectorAll(cssSelector);
int[] numberOfChildren = new int[result.size()];
int i = 0;
                                for (Node node : result) {
 System.out.println("hello");
System.out.println(node.getChildNodes());
System.out.println(node.getChildNodes().getLength());
numberOfChildren[i] = node.getChildNodes().getLength();
i++;

 
                                        if (node instanceof Document) {
                                                LOGGER.debug("CSS rule returns the whole document!!!");
                                                selec.setMatched(true);
                                        } else {
                                                ElementWrapper ew = new ElementWrapper(stateName, (Element) node);
                                                selec.addMatchedElement(ew);
                                                MatchedElements.setMatchedElement(ew, selec);
                                        }
                                }
   int sum = 0;
for(int m = 0; m< numberOfChildren.length;m++){
	sum+= numberOfChildren[m];
	
}

totaldescendants += sum;


                        }
                  

                }
                
    
    //totaldescendants: number of elements in scope of selectors

                return mRules;
 
        }

        public static List<MCssRule> checkCssRulesOnDom(String stateName, Document dom,
         List<MCssRule> mRules) {

                for (MCssRule mRule : mRules) {
                        List<MSelector> selectors = mRule.getSelectors();
                        for (MSelector selec : selectors) {

                                String xpath = selec.getXpathSelector();
                                try {
                                        NodeList nodeList = XPathHelper.evaluateXpathExpression(dom, xpath);

                                        for (int i = 0; i < nodeList.getLength(); i++) {

                                                Node node = nodeList.item(i);
                                                if (node instanceof Document) {
                                                        LOGGER.debug("CSS rule returns the whole document!!!");
                                                        selec.setMatched(true);
                                                } else {
                                                        ElementWrapper ew = new ElementWrapper(stateName, (Element) node);
                                                        selec.addMatchedElement(ew);
                                                        MatchedElements.setMatchedElement(ew, selec);
                                                }
                                        }
                                } catch (XPathExpressionException e) {
                                        LOGGER.error("Css rule: " + selec.getCssSelector() + " xpath: " + xpath);
                                        LOGGER.error(e.getMessage(), e);
                                }

                        }

                }

                return mRules;
        }

        public static Set<String> getDuplicateSelectors(List<MCssRule> mRules) {
                Set<String> set = new HashSet<String>();
                Set<String> duplicates = new HashSet<String>();

                for (MCssRule mRule : mRules) {
                        List<MSelector> selectors = mRule.getSelectors();
                        for (MSelector selec : selectors) {
                                if (!set.add(selec.getCssSelector())) {
                                        duplicates.add(selec.getCssSelector());
                                }
                        }
                }

                return duplicates;
        }

}