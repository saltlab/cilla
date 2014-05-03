package com.crawljax.plugins.cilla.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.css.sac.Locator;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;

import com.crawljax.plugins.cilla.CillaPlugin;
import com.crawljax.plugins.cilla.util.CssToXpathConverter;
import com.crawljax.plugins.cilla.util.specificity.SpecificityCalculator;
import com.steadystate.css.dom.CSSStyleRuleImpl;
import com.steadystate.css.userdata.UserDataConstants;

public class MCssRule {


        private CSSRule rule;
       private List<MSelector> selectors;
        
        private String ruleSelector;

        private static Set<String> ignorePseudoClasses = new HashSet<String>(Arrays.asList(":link",
         ":visited", ":hover", ":focus", ":active", ":target", ":lang", ":enabled",
         ":disabled", ":checked", ":indeterminate"));

        /*
* ":nth-child", ":nth-last-child", ":nth-of-type", ":nth-last-of-type", ":first-child",
* ":last-child", ":first-of-type", ":last-of-type", ":only-child", ":only-of-type", ":empty",
* ":contains", ":not", ":before", ":after", ":first-line", ":first-letter", ":selection")
*/

        /**
* Constructor.
*
* @param rule
* the CSS rule.
*/
        
        
        public MCssRule(CSSRule rule) {

                this.rule = rule;
                selectors = new ArrayList<MSelector>();
                setSelectors();
               
        }

        public CSSRule getRule() {
                return rule;
        }

        public List<MSelector> getSelectors() {
                return selectors;
        }

        private void setSelectors() {
                if (this.rule instanceof CSSStyleRule) {
                        CSSStyleRule styleRule = (CSSStyleRule) rule;

                        this.ruleSelector = styleRule.getSelectorText();
                        this.ruleSelector = this.ruleSelector.replace("*", " ");
                        //this.ruleSelector = CssToXpathConverter.removeChar(this.ruleSelector, '*');
                        // in case there are Grouping selectors: p, div, .news { }
                        List<MProperty> props = getProperties();
                        for (String sel : ruleSelector.split(",")) {
                                selectors.add(new MSelector(sel.trim(), props, shouldIgnore(sel)));
                        }
                }

        }

        private boolean shouldIgnore(String sel) {
                for (String ignore : ignorePseudoClasses) {
                        if (sel.contains(ignore)) {
                                return true;
                        }
                }
                return false;
        }

        public List<MProperty> getProperties() {
                CSSStyleDeclaration styleDeclaration = null;
                List<MProperty> properties = new ArrayList<MProperty>();

                if (this.rule instanceof CSSStyleRule) {
                        CSSStyleRule styleRule = (CSSStyleRule) rule;
                        styleDeclaration = styleRule.getStyle();

                        for (int j = 0; j < styleDeclaration.getLength(); j++) {
                                String property = styleDeclaration.item(j);
                                String value = styleDeclaration.getPropertyCSSValue(property).getCssText();
                                properties.add(new MProperty(property, value));
                        }

                }

                return properties;
        }

        /**
* @return the CSS Style declaration of this rule.
*/
        public CSSStyleDeclaration getStyleDeclaration() {
                CSSStyleDeclaration styleDeclaration = null;

                if (this.rule instanceof CSSStyleRule) {
                        CSSStyleRule styleRule = (CSSStyleRule) rule;
                        styleDeclaration = styleRule.getStyle();

                        for (int j = 0; j < styleDeclaration.getLength(); j++) {
                                String property = styleDeclaration.item(j);
                                System.out.println("property: " + property);
                                System.out.println("value: "
                                 + styleDeclaration.getPropertyCSSValue(property).getCssText());
                        }

                }

                return styleDeclaration;
        }

        public static List<MCssRule> convertToMCssRules(CSSRuleList ruleList) {

                List<MCssRule> mCssRules = new ArrayList<MCssRule>();

                for (int i = 0; i < ruleList.getLength(); i++) {
                        mCssRules.add(new MCssRule(ruleList.item(i)));
                }

                return mCssRules;
        }

        @Override
        public String toString() {

                StringBuffer buffer = new StringBuffer();
                Locator locator = getLocator();

                buffer.append("locator: line=" + locator.getLineNumber() + " col="
                 + locator.getColumnNumber() + "\n");
                buffer.append("Rule: " + rule.getCssText() + "\n");

                for (MSelector selector : this.selectors) {
                        buffer.append(selector.toString());
                }

                return buffer.toString();
        }

        /**
* @return the selectors that are not matched (not associated DOM elements have been detected).
*/
        public List<MSelector> getUnmatchedSelectors() {
                List<MSelector> unmatched = new ArrayList<MSelector>();

                for (MSelector selector : this.selectors) {
                        if (!selector.isMatched() && !selector.isIgnore()) {
                                unmatched.add(selector);
                        }
                }

                return unmatched;

        }

        /**
* @return the selectors that are effective (associated DOM elements have been detected).
*/
        public List<MSelector> getMatchedSelectors() {
                List<MSelector> effective = new ArrayList<MSelector>();

                for (MSelector selector : this.selectors) {
                        if (selector.isMatched() && !selector.isIgnore()) {
                                effective.add(selector);
                        }
                }

                return effective;

        }

        /**
* @return the Locator of this rule (line number, column).
*/
        public Locator getLocator() {
                if (this.rule instanceof CSSStyleRuleImpl) {
                        return (Locator) ((CSSStyleRuleImpl) this.rule)
                         .getUserData(UserDataConstants.KEY_LOCATOR);
                }

                return null;
        }

        public String getRuleSelector() {
                return ruleSelector;
        }
        
        
        
        
  
public List<MSelector> getTooSpecificSelectors(){
SpecificityCalculator sc = new SpecificityCalculator();

List<MSelector> tooSpecific = new ArrayList<MSelector>();
                        
                for (MSelector selector : this.selectors){
                        sc.reset();

        String s = sc.getSpecificity(selector.getCssSelector()).toString();
        s = s.replaceAll("\\D+","");
        /*
        int a = Integer.parseInt(s.substring(1, 2));
      
        int b = Integer.parseInt(s.substring(4, 5));
     
        int c = Integer.parseInt(s.substring(7, 8));
        
        int d = Integer.parseInt(s.substring(10, 11));
       */
        int a = Integer.parseInt(s.substring(0, 1));
        
        int b = Integer.parseInt(s.substring(1, 2));
     
        int c = Integer.parseInt(s.substring(2, 3));
        
        int d = Integer.parseInt(s.substring(3, 4));
                                                
                                                if(b+c+d> 4){
                                                        tooSpecific.add(selector);
                                                        
                                                }
                   
        
                                 }
                                        
                                
                        return tooSpecific;
                }

public List<MSelector> getTooSpecificSelectors2(){
SpecificityCalculator sc = new SpecificityCalculator();

List<MSelector> tooSpecific2 = new ArrayList<MSelector>();
                        
                for (MSelector selector : this.selectors){
                        sc.reset();

        String s = sc.getSpecificity(selector.getCssSelector()).toString();
        s = s.replaceAll("\\D+","");
        /*
        int a = Integer.parseInt(s.substring(1, 2));
      
        int b = Integer.parseInt(s.substring(4, 5));
     
        int c = Integer.parseInt(s.substring(7, 8));
        
        int d = Integer.parseInt(s.substring(10, 11));
        */
        int a = Integer.parseInt(s.substring(0, 1));
        
        int b = Integer.parseInt(s.substring(1, 2));
     
        int c = Integer.parseInt(s.substring(2, 3));
        
        int d = Integer.parseInt(s.substring(3, 4));
       
                                                
                                                if((b> 1 || c> 2 || d> 3)){
                                                        tooSpecific2.add(selector);
                                                }
                                            
                                
                                        }
                                        
                                
                        return tooSpecific2;
                }
                        

                     
public List<MSelector> getIdWithClassOrElement(){
SpecificityCalculator sc = new SpecificityCalculator();

List<MSelector> idWith = new ArrayList<MSelector>();
                        
                        for (MSelector selector : this.selectors){
                                        sc.reset();

                                                String s = sc.getSpecificity(selector.getCssSelector()).toString();
                                                s = s.replaceAll("\\D+","");
                                                /*
                                                int b = Integer.parseInt(s.substring(4, 5));
                                                int c = Integer.parseInt(s.substring(7, 8));
                                                int d = Integer.parseInt(s.substring(10, 11));
                                                */
                                                int b = Integer.parseInt(s.substring(1, 2));
                                                int c = Integer.parseInt(s.substring(2, 3));
                                                int d = Integer.parseInt(s.substring(3, 4));
                                                if(b!=0){
                                                        if(c!=0 || d!=0){
                                                                
                                                                idWith.add(selector);
                                                                
                                                                
                                                        }
                                                }
                                
                                        }
                                        
                                
                        return idWith;
                }
                        

public List<MSelector> getDangerousSelectors(){
SpecificityCalculator sc = new SpecificityCalculator();
List<MSelector> dangerousSelectors = new ArrayList<MSelector>();
    
    for (MSelector selector : this.selectors){
     sc.reset();

        String s = sc.getSpecificity(selector.getCssSelector()).toString();
        s = s.replaceAll("\\D+","");
        /*
        int b = Integer.parseInt(s.substring(4, 5));
        int c = Integer.parseInt(s.substring(7, 8));
        int d = Integer.parseInt(s.substring(10, 11));
        */
        int b = Integer.parseInt(s.substring(1, 2));
        int c = Integer.parseInt(s.substring(2, 3));
        int d = Integer.parseInt(s.substring(3, 4));
        if(b==0 && c==0 && d == 1){
       //  if(selector.toString().contains("div") || selector.toString().contains("header") || selector.toString().contains("aside") || selector.toString().contains("ul") || selector.toString().contains("body")|| selector.toString().contains("title")){
        	if(selector.getCssSelector().contains("div") || selector.getCssSelector().contains("header") || selector.getCssSelector().contains("aside") || selector.getCssSelector().contains("ul") || selector.getCssSelector().contains("body")|| selector.getCssSelector().contains("title") || selector.getCssSelector().contains("head") || selector.getCssSelector().contains("html") || selector.getCssSelector().contains("DIV") || selector.getCssSelector().contains("HEADER") || selector.getCssSelector().contains("ASIDE") || selector.getCssSelector().contains("UL") || selector.getCssSelector().contains("BODY")|| selector.getCssSelector().contains("TITLE") || selector.getCssSelector().contains("HEAD") || selector.getCssSelector().contains("HTML")){
         dangerousSelectors.add(selector);
         }
        
        }
        
    
    }
return dangerousSelectors;	
}

public List<MSelector> getSelectorsWithInvalidSyntax(){

List<MSelector> invalidSyntaxSelectors = new ArrayList<MSelector>();

for (MSelector selector : this.selectors){

//String s = selector.toString();
String[] parts = selector.getCssSelector().split(" ");
for (String part : parts) {
int numIdSign = 0;
int numClassSign = 0; //dot
for(int i = 0; i< part.length(); i++){
if(part.charAt(i) == '#'){
numIdSign++;

}
if(part.charAt(i) == '.'){
numClassSign++;

}
}
// There must be space between 2 classes, 2 IDs or one class and one ID.
if(numIdSign > 1 || numClassSign > 1 || numIdSign+numClassSign >1){
invalidSyntaxSelectors.add(selector);
continue;
}

}
//There must be no space between an element and a class
if(parts.length > 1){
for(int i = 1; i< parts.length; i++){
if(!parts[i].isEmpty()){
if(parts[i].charAt(0) == '.' && !parts[i-1].isEmpty() && (!parts[i-1].contains(".") || !parts[i-1].contains("#"))){
invalidSyntaxSelectors.add(selector);
continue;
}
//There must be no space between an element and an ID
if(parts[i].charAt(0) == '#' && !parts[i-1].isEmpty() && (!parts[i-1].contains(".") || !parts[i-1].contains("#"))){
invalidSyntaxSelectors.add(selector);
continue;
}
}
}
}

}

return invalidSyntaxSelectors;

}

public List<MSelector> getUniversalSelectors(){

List<MSelector> universalSelectors = new ArrayList<MSelector>();
for (MSelector selector : this.selectors){
	if(selector.getCssSelector().contains("*")){
	
		universalSelectors.add(selector);
	
	}
}

return universalSelectors;
}


}