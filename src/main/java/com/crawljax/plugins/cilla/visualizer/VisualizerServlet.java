package com.crawljax.plugins.cilla.visualizer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.tools.view.VelocityViewServlet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.w3c.css.selectors.SelectorsList;
import org.w3c.dom.Node;







import com.crawljax.plugins.cilla.CillaPlugin;
import com.crawljax.plugins.cilla.analysis.ElementWithClass;
import com.crawljax.plugins.cilla.analysis.MCssRule;
import com.crawljax.plugins.cilla.analysis.MSelector;
import com.crawljax.plugins.cilla.examples.CillaRunner;
import com.google.common.base.Charsets;
import com.google.common.collect.SetMultimap;
import com.google.common.io.Files;
import com.google.common.io.Resources;


public class VisualizerServlet extends VelocityViewServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final File summaryHTML;
	private final File cssAnalysisHTML;
	private final File htmlAnalysisHTML;
	
private final File cssValidationHTML;
private final File cssLintHTML;
private final File statisticsHTML;

	private final File outputFolder = new File("output");
	


	private Template summaryTemplate;
	private Template cssAnalysisTemplate;
	private Template htmlAnalysisTemplate;
	
private Template cssValidationTemplate;
private Template cssLintTemplate;
private Template statisticsTemplate;

	private enum HighlightColor {
		NONE, UNMATCHED, INEFFECTIVE, EFFECTIVE
	}

	private Map<HighlightColor, String> highlightMap;

	private final String NoHighlight = "#fff";
	private final String UnmatchedHighlight = "#ccffff";
	private final String IneffectiveHighlight = "#ffcccc";
	private final String EffectiveHighlight = "#ccffcc";

	private Map<String, Map<String, Map<Integer, HighlightColor>>> unsortedMap;

	private String dateString;
	private String folderString;
	private String outputDir;
	private String crawledAddress;
	private VelocityEngine ve;

	public VisualizerServlet() {

		ve = new VelocityEngine();

		ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
		        "org.apache.velocity.runtime.log.NullLogChute");
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());

		SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
		dateString = sdf.format(new Date());

		sdf = new SimpleDateFormat("ddMMyy-hhmm");
		folderString = sdf.format(new Date());

		highlightMap = new HashMap<HighlightColor, String>();
		highlightMap.put(HighlightColor.NONE, NoHighlight);
		highlightMap.put(HighlightColor.UNMATCHED, UnmatchedHighlight);
		highlightMap.put(HighlightColor.INEFFECTIVE, IneffectiveHighlight);
		highlightMap.put(HighlightColor.EFFECTIVE, EffectiveHighlight);

		outputDir = outputFolder.getAbsolutePath() + folderString;
		File outputPath = new File(outputDir);
		outputPath.mkdir();
		summaryHTML = new File(outputDir + "/summary.html");
		cssAnalysisHTML = new File(outputDir + "/css-analysis.html");
		htmlAnalysisHTML = new File(outputDir + "/html-analysis.html");
		
cssValidationHTML = new File(outputDir + "/css-validation.html");
cssLintHTML = new File(outputDir + "/css-lint.html");
statisticsHTML= new File(outputDir + "/statistics.html");


		try {
			Velocity.init();
		} catch (Exception e1) {
			System.err.println("Could not initialize Apache Velocity.");
		}

		try {
			summaryTemplate = ve.getTemplate("summary.vm");
			cssAnalysisTemplate = ve.getTemplate("css-analysis.vm");
			// htmlAnalysisTemplate = ve.getTemplate("html-analysis.vm");
			
cssValidationTemplate = ve.getTemplate("validation.vm");
cssLintTemplate = ve.getTemplate("lint.vm");
statisticsTemplate = ve.getTemplate("statistics.vm");


			Files.write(Resources.toString(
			        VisualizerServlet.class.getResource("/visualizer.css"), Charsets.UTF_8),
			        new File(outputPath + "/visualizer.css"), Charsets.UTF_8);

		} catch (ResourceNotFoundException rnfe) {
			// couldn't find the template
			System.err.println("Could not find the template.");
		} catch (ParseErrorException pee) {
			// syntax error: problem parsing the template
			System.err.println("Problem parsing the template.");
		} catch (MethodInvocationException mie) {
			// something invoked in the template
			// threw an exception
			System.err.println("Something invoked in the template threw an exception.");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * The Servlet
	 */
	public Template handleRequest(HttpServletRequest request, HttpServletResponse response,
	        Context ctx) {
		Template temp = null;

		try {
			temp = ve.getTemplate("summary.vm");
		} catch (ParseErrorException pee) {
			System.err.println("VisualizerServlet : parse error for template " + pee);
		} catch (ResourceNotFoundException rnfe) {
			System.err.println("VisualizerServlet : template not found " + rnfe);
		} catch (Exception e) {
			System.err.println("Error " + e);
		}

		return temp;
	}

	public void addSummary(String url, String summary) {
		crawledAddress = url;
		VelocityContext context = new VelocityContext();
		String template;
		try {
			template = getTemplateAsString(summaryTemplate.getName());

			summary = summary.replace("\n", "$br");
			summary =
			        summary.replace("   ->", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
			summary = summary.replace("->", "&nbsp;");

			context.put("summary", summary);
			context.put("url", url);
			context.put("date", dateString);

			/*
			 * StringWriter sw = new StringWriter(); try { summaryTemplate.merge( context, sw ); }
			 * catch (ResourceNotFoundException e) {
			 * System.err.println("Could not find the summary template."); } catch
			 * (ParseErrorException e) { System.err.println("summaryTemplate: parse error"); } catch
			 * (MethodInvocationException e) { System.err.println("summaryTemplate: " +
			 * "something invoked in the template threw an exception"); }
			 */

			FileWriter writer = new FileWriter(summaryHTML);

			ve.evaluate(context, writer, "Summary", template);
			writer.flush();
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
public void addValidation(String url){
		
crawledAddress = url;
VelocityContext context = new VelocityContext();
String template;
String cssValidationMsg;
		try {
			template = getTemplateAsString(cssValidationTemplate.getName());

			//Document doc = Jsoup.connect("http://jigsaw.w3.org/css-validator/validator?uri=http%3A%2F%2Fwww.ece.ubc.ca/~amesbah/exp%2F&warning=2&profile=css2").get();
			Document doc = Jsoup.connect("http://jigsaw.w3.org/css-validator/validator?uri=http%3A%2F%2F"+CillaRunner.b+"%2F&warning=2&profile=css2").get();
		
			
			Elements table = doc.select("tr"); 
			

			cssValidationMsg = table.toString();
			
			cssValidationMsg = cssValidationMsg.replace("\n", "<br> ");
			
			
			context.put("summary", cssValidationMsg);
			context.put("url", url);
			context.put("date", dateString);
			
			FileWriter writer = new FileWriter(cssValidationHTML);

			ve.evaluate(context, writer, "CSS Validation", template);
			writer.flush();
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

public void addCssLint(){
	//crawledAddress = url;
	VelocityContext context = new VelocityContext();
	String template;
	String cssLintMsg;
			try {
				template = getTemplateAsString(cssLintTemplate.getName());

				File f = new File("C:/Users/Golnaz/cilla/CsslintReports/output"+CillaPlugin.i+".txt");
				//File f = new File("D:/Output.txt");
			    FileInputStream fin = new FileInputStream(f);
			    byte[] buffer = new byte[(int) f.length()];
			    new DataInputStream(fin).readFully(buffer);
			    fin.close();
			    String s = new String(buffer, "UTF-8");
			    //System.out.println(s);
				
		
				cssLintMsg = s;
				cssLintMsg = cssLintMsg.replace("\n", "<br> ");
				
				
				
				context.put("summary", cssLintMsg);
				//context.put("url", url);
				context.put("date", dateString);
				
				FileWriter writer = new FileWriter(cssLintHTML);

				ve.evaluate(context, writer, "CSS Lint", template);
				writer.flush();
				writer.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
			
	
}

public void addStatistics(){

	VelocityContext context = new VelocityContext();
	String template;
	String statisticsMsg;
			try {
				template = getTemplateAsString(statisticsTemplate.getName());
				
				String s = "The average number of properties used in one CSS rule in this web site: "+ String.valueOf(CillaPlugin.Mean)+
						"\n"+"The median of number of properties used in one CSS rule in this web site: "+ String.valueOf(CillaPlugin.Median)+"\n"+"The minimum number of properties in one CSS rule: "+String.valueOf(CillaPlugin.min)+"\n"+"The maximum number of properties used in one CSS rule: "+String.valueOf(CillaPlugin.max)+"\n"+"The average number of selector types used in one CSS rule: "+CillaPlugin.meanSelector+"\n"+"The median of selector types used in one CSS rule: "+CillaPlugin.medianSelector+"\n"+"The minimum number of selector types in one CSS rule: "+CillaPlugin.minSelector+"\n"+"The maximum number of selector types in one CSS rule: "+CillaPlugin.maxSelector; 
				statisticsMsg= s;

				statisticsMsg = statisticsMsg.replace("\n", "<br><br> ");
				
				
				context.put("summary", statisticsMsg);
				
				context.put("date", dateString);
				
				FileWriter writer = new FileWriter(statisticsHTML);

				ve.evaluate(context, writer, "Statistics", template);
				writer.flush();
				writer.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
			
	
}


	public void addSortedOutput(Map<String, List<MCssRule>> cssRules,
	        SetMultimap<String, ElementWithClass> elementsWithNoClassDef) {

		Map<String, Map<String, String>> fileMap = new HashMap<String, Map<String, String>>();

		String filename = new String();
		StringBuffer unmatchedBuffer;
		StringBuffer ineffectiveBuffer;
		StringBuffer effectiveBuffer;
		StringBuffer undefClassBuffer;
		
StringBuffer tooSpecific;
StringBuffer tooLazy;
StringBuffer tooLong;
StringBuffer emptyCatch;
StringBuffer undoingStyle;
StringBuffer idWithClassOrElement;
StringBuffer reactiveImportant;
StringBuffer inappFontSize;

		// Look through the files and format the sorted output
		for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {
			Map<String, String> analysisMap = new HashMap<String, String>();
			List<MCssRule> rules = entry.getValue();

			// Get the file name
			filename = entry.getKey();

			// Initialize the headings
			unmatchedBuffer = new StringBuffer();
			ineffectiveBuffer = new StringBuffer();
			effectiveBuffer = new StringBuffer();
			
			
tooSpecific = new StringBuffer();
tooLazy = new StringBuffer();
tooLong = new StringBuffer();
emptyCatch = new StringBuffer();
undoingStyle = new StringBuffer();
idWithClassOrElement = new StringBuffer();
reactiveImportant = new StringBuffer();
inappFontSize = new StringBuffer();



			// Loop through the rules
			for (MCssRule rule : rules) {

				// First print out the unmatched rules
				List<MSelector> unmatched = rule.getUnmatchedSelectors();
				if (unmatched.size() > 0) {
					unmatchedBuffer.append("CSS rule: " + rule.getRule().getCssText() + "<br>");
					unmatchedBuffer.append("at line: " + rule.getLocator().getLineNumber()
					        + "<br><br>");

					updateUnsortedMap(filename, rule.getLocator().getLineNumber(), rule.getRule()
					        .getCssText(), HighlightColor.UNMATCHED);
				} else {
					// unmatchedBuffer.append("(none) <br><br>");
				}

				// Second handle the ineffective and effective rules
				List<MSelector> matched = rule.getMatchedSelectors();
				if (matched.size() > 0) {
					for (MSelector sel : matched) {
						if (!sel.hasEffectiveProperties() && !sel.isIgnore()) {
							ineffectiveBuffer.append("CSS rule: " + rule.getRule().getCssText()
							        + "<br>");

							ineffectiveBuffer.append("at line: "
							        + rule.getLocator().getLineNumber() + "<br>");
							ineffectiveBuffer.append(" Selector: " + sel.getCssSelector()
							        + "<br><br>");

							updateUnsortedMap(filename, rule.getLocator().getLineNumber(), rule
							        .getRule().getCssText(), HighlightColor.INEFFECTIVE);
						} else if (sel.hasEffectiveProperties()) {
							effectiveBuffer.append("CSS rule: " + rule.getRule().getCssText()
							        + "<br>");
							effectiveBuffer.append("at line: "
							        + rule.getLocator().getLineNumber() + "<br>");
							effectiveBuffer.append(" Selector: " + sel.getCssSelector()
							        + "<br><br>");

							updateUnsortedMap(filename, rule.getLocator().getLineNumber(), rule
							        .getRule().getCssText(), HighlightColor.EFFECTIVE);
						}

					}
				} else {
					// ineffectiveBuffer.append("(none)<br><br>");
					// effectiveBuffer.append("(none)<br><br>");
				}
				
List<MSelector> tooSpecificc = rule.getTooSpecificSelectors();
if(tooSpecificc.size()>0){
					for (MSelector sel : tooSpecificc){
						tooSpecific.append("CSS rule: " + rule.getRule().getCssText()
						        + "<br>");
						tooSpecific.append("at line: "
						        + rule.getLocator().getLineNumber() + "<br>");
						tooSpecific.append(" Selector: " + sel.getCssSelector()
						        + "<br><br>");
					//	updateUnsortedMap(filename, rule.getLocator().getLineNumber(), rule
					//	        .getRule().getCssText(), HighlightColor.TOOSPECIFIC);
						
					}
					
				}

List<MSelector> tooLazyy = rule.getLazyRules();
	if(tooLazyy.size()>0){
					for (MSelector sel : tooLazyy){
						tooLazy.append("CSS rule: " + rule.getRule().getCssText()
						        + "<br>");
						tooLazy.append("at line: "
						        + rule.getLocator().getLineNumber() + "<br>");
						tooLazy.append(" Selector: " + sel.getCssSelector()
						        + "<br><br>");

						//	updateUnsortedMap(filename, rule.getLocator().getLineNumber(), rule
							//        .getRule().getCssText(), HighlightColor.LAZY);	
					
					}
					
				}

List<MSelector> tooLongg = rule.getTooLongRules();
	if(tooLongg.size()>0){
					for (MSelector sel : tooLongg){
						tooLong.append("CSS rule: " + rule.getRule().getCssText()
						        + "<br>");
						tooLong.append("at line: "
						        + rule.getLocator().getLineNumber() + "<br>");
						tooLong.append(" Selector: " + sel.getCssSelector()
						        + "<br><br>");
						
						//updateUnsortedMap(filename, rule.getLocator().getLineNumber(), rule
						        //.getRule().getCssText(), HighlightColor.TOOLONG);
						
					}
					
				}

List<MSelector> empcat = rule.getEmptyCatch();
	if(empcat.size()>0){
					for (MSelector sel : empcat){
						emptyCatch.append("CSS rule: " + rule.getRule().getCssText()
						        + "<br>");
						emptyCatch.append("at line: "
						        + rule.getLocator().getLineNumber() + "<br>");
						emptyCatch.append(" Selector: " + sel.getCssSelector()
						        + "<br><br>");
						
					//	updateUnsortedMap(filename, rule.getLocator().getLineNumber(), rule
						 //       .getRule().getCssText(), HighlightColor.EMPTYCATCH);
						
					}
					
				}

List<MSelector> undsty = rule.getEmptyCatch();
	if(undsty.size()>0){
					for (MSelector sel : undsty){
						emptyCatch.append("CSS rule: " + rule.getRule().getCssText()
						        + "<br>");
						emptyCatch.append("at line: "
						        + rule.getLocator().getLineNumber() + "<br>");
						emptyCatch.append(" Selector: " + sel.getCssSelector()
						        + "<br><br>");
						
					//	updateUnsortedMap(filename, rule.getLocator().getLineNumber(), rule
					//	        .getRule().getCssText(), HighlightColor.OVERRIDING);
						
					}
					
				}

List<MSelector> IDWithh = rule.getIdWithClassOrElement();
	if(IDWithh.size()>0){
					for (MSelector sel : IDWithh){
						idWithClassOrElement.append("CSS rule: " + rule.getRule().getCssText()
						        + "<br>");
						idWithClassOrElement.append("at line: "
						        + rule.getLocator().getLineNumber() + "<br>");
						idWithClassOrElement.append(" Selector: " + sel.getCssSelector()
						        + "<br><br>");
						
					//	updateUnsortedMap(filename, rule.getLocator().getLineNumber(), rule
						//        .getRule().getCssText(), HighlightColor.IDPLUS);
						
					}
					
				}

List<MSelector> reactive = rule.getReactiveImportant();
	if(reactive.size()>0){
					for (MSelector sel : reactive){
						reactiveImportant.append("CSS rule: " + rule.getRule().getCssText()
						        + "<br>");
						reactiveImportant.append("at line: "
						        + rule.getLocator().getLineNumber() + "<br>");
						reactiveImportant.append(" Selector: " + sel.getCssSelector()
						        + "<br><br>");
					//	updateUnsortedMap(filename, rule.getLocator().getLineNumber(), rule
					//	        .getRule().getCssText(), HighlightColor.IMPORTANT);
						
					}
					
				}

List<MSelector> inappfont = rule.checkFontSize();
	if(inappfont.size()>0){
					for (MSelector sel : inappfont){
						inappFontSize.append("CSS rule: " + rule.getRule().getCssText()
						        + "<br>");
						inappFontSize.append("at line: "
						        + rule.getLocator().getLineNumber() + "<br>");
						inappFontSize.append(" Selector: " + sel.getCssSelector()
						        + "<br><br>");
					//	updateUnsortedMap(filename, rule.getLocator().getLineNumber(), rule
					//	        .getRule().getCssText(), HighlightColor.IMPORTANT);
						
					}
					
				}


			} // for rules

			// Third append the Undefined Classes
			undefClassBuffer = new StringBuffer();
			// undefClassBuffer.append("<h2> Undefined CSS Classes </h2>");
			Set<String> undefinedClasses = new HashSet<String>();
			for (String key : elementsWithNoClassDef.keySet()) {
				// output.append("State: " + key + "\n");
				Set<ElementWithClass> set = elementsWithNoClassDef.get(key);
				for (ElementWithClass e : set) {
					for (String unmatched : e.getUnmatchedClasses()) {
						if (undefinedClasses.add(unmatched)) {

							// output.append("Undefined class: ");
							undefClassBuffer.append("&nbsp;" + unmatched + ",");
						}
					}
				}
			}
			
			
			









				
						

			// Replace asterisk (*) and pound (#) with HTML symbol for parsing
			String unmatchedStr = unmatchedBuffer.toString().replaceAll("#", "&#35");
			unmatchedStr = unmatchedStr.replaceAll("\\*", "&#42");
			String ineffectiveStr = ineffectiveBuffer.toString().replaceAll("#", "&#35");
			ineffectiveStr = ineffectiveStr.replaceAll("\\*", "&#42");
			String effectiveStr = effectiveBuffer.toString().replaceAll("#", "&#35");
			effectiveStr = effectiveStr.replaceAll("\\*", "&#42");
			String undefinedStr = undefClassBuffer.toString().replaceAll("#", "&#35");
			undefinedStr = undefinedStr.replaceAll("\\*", "&#42");
			
			
String tooSpecificStr = tooSpecific.toString().replaceAll("#", "&#35");
tooSpecificStr = tooSpecificStr.replaceAll("\\*", "&#42");
String tooLazyStr = tooLazy.toString().replaceAll("#", "&#35");
tooLazyStr = tooLazyStr.replaceAll("\\*", "&#42");
String tooLongStr = tooLong.toString().replaceAll("#", "&#35");
tooLongStr = tooLongStr.replaceAll("\\*", "&#42");
String empCatStr = emptyCatch.toString().replaceAll("#", "&#35");
empCatStr = empCatStr.replaceAll("\\*", "&#42");
String undoStr = undoingStyle.toString().replaceAll("#", "&#35");
undoStr = undoStr.replaceAll("\\*", "&#42");
String idWithStr = idWithClassOrElement.toString().replaceAll("#", "&#35");
idWithStr = idWithStr.replaceAll("\\*", "&#42");
String reactiveStr = reactiveImportant.toString().replaceAll("#", "&#35");
reactiveStr = reactiveStr.replaceAll("\\*", "&#42");
String inappFontStr = inappFontSize.toString().replaceAll("#", "&#35");
inappFontStr = inappFontStr.replaceAll("\\*", "&#42");

			analysisMap.put("Unmatched CSS Rules", unmatchedStr);
			analysisMap.put("Matched & Ineffective CSS Rules", ineffectiveStr);
			analysisMap.put("Matched & Effective CSS Rules", effectiveStr);
			analysisMap.put("Undefined CSS Classes", undefinedStr);
			
			
// prints CSS smells in css analysis tab, sorted.
analysisMap.put("CSS Rules with Too Specific Selectors", tooSpecificStr);
analysisMap.put("Lazy CSS Rules", tooLazyStr);
analysisMap.put("Too Long CSS Rules", tooLongStr);
analysisMap.put("CSS Rules with Empty Catch", empCatStr);
analysisMap.put("CSS Rules with Overriding Properties", undoStr);
analysisMap.put("Selectors with ID and at Least One Class or Element", idWithStr);
analysisMap.put("Rules with !important in their Declaration", reactiveStr);
analysisMap.put("Rules with Inappropriate Font-size Value", inappFontStr);

			fileMap.put(filename, analysisMap);

		} // for entry set

		VelocityContext context = new VelocityContext();
		context.put("filemap", fileMap);
		/*
		 * StringWriter sw = new StringWriter(); try { cssAnalysisTemplate.merge( context, sw ); }
		 * catch (ResourceNotFoundException e) {
		 * System.err.println("Could not find the css-com.crawljax.plugins.cilla.analysis template."
		 * ); } catch (ParseErrorException e) {
		 * System.err.println("cssAnalysisTemplate: parse error"); } catch
		 * (MethodInvocationException e) { System.err.println("cssAnalysisTemplate: " +
		 * "something invoked in the template threw an exception"); }
		 */

		context.put("filemap2", getVTLReference());
		context.put("date", dateString);
		context.put("url", crawledAddress);
		context.put("unmatchedColor", UnmatchedHighlight);
		context.put("ineffectiveColor", IneffectiveHighlight);
		context.put("effectiveColor", EffectiveHighlight);

		String template;
		try {
			template = getTemplateAsString(cssAnalysisTemplate.getName());
			FileWriter writer = new FileWriter(cssAnalysisHTML);
			ve.evaluate(context, writer, "CSS-Analysis", template);
			writer.flush();
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void constructRuleMap(Map<String, List<MCssRule>> cssRules) {
		int line;
		String parsedRule;
		String filename;
		unsortedMap = new HashMap<String, Map<String, Map<Integer, HighlightColor>>>();
		Map<String, Map<Integer, HighlightColor>> ruleMap;
		Map<Integer, HighlightColor> colorMap;

		try {
			for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {
				ruleMap = new HashMap<String, Map<Integer, HighlightColor>>();
				filename = entry.getKey();
				List<MCssRule> rules = entry.getValue();
				for (MCssRule rule : rules) {
					if (null != rule.getLocator()) {
						colorMap = new HashMap<Integer, HighlightColor>();
						line = rule.getLocator().getLineNumber();
						System.err.println("LOCATOR FOUND: " + rule.getLocator().getLineNumber());
						parsedRule = rule.getRule().getCssText();
						colorMap.put(line, HighlightColor.NONE);
						ruleMap.put(parsedRule, colorMap);
					} else {
						System.err.println("CANNOT FIND LOCATOR");
					}
				}
				unsortedMap.put(filename, ruleMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateUnsortedMap(String filename, int lineNumber, String ruleStr,
	        HighlightColor hc) {
		Map<Integer, HighlightColor> colorMap = new HashMap<Integer, HighlightColor>();
		colorMap.put(lineNumber, hc);
		Map<String, Map<Integer, HighlightColor>> ruleMap = unsortedMap.get(filename);
		ruleMap.put(ruleStr, colorMap);
		unsortedMap.put(filename, ruleMap);
	}

	private Map<String, List<String>> getVTLReference() {
		Map<String, List<String>> retMap = new HashMap<String, List<String>>();
		try {
			ArrayList<String> formattedRules;
			String currentString;

			String filename;
			Integer lineNumber;
			String rule;

			for (Map.Entry<String, Map<String, Map<Integer, HighlightColor>>> fileEntry : unsortedMap
			        .entrySet()) {
				formattedRules = new ArrayList<String>();
				filename = fileEntry.getKey();
				for (Map.Entry<String, Map<Integer, HighlightColor>> ruleEntry : fileEntry
				        .getValue().entrySet()) {
					rule = ruleEntry.getKey();
					for (Map.Entry<Integer, HighlightColor> colorEntry : ruleEntry.getValue()
					        .entrySet()) {
						lineNumber = colorEntry.getKey();
						currentString = lineNumber.toString() + ". " + rule;
						currentString =
						        "<div style = \"background-color:"
						                + highlightMap.get(colorEntry.getValue()) + "\">"
						                + currentString + "</div>";
						formattedRules.add(currentString);
					}
				}

				Collections.sort(formattedRules, new LineComparator());
				retMap.put(filename, formattedRules);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return retMap;
	}

	private class LineComparator implements Comparator<String> {

		public int compare(String o1, String o2) {

			int lineObj1 = Integer.parseInt(o1.substring(o1.indexOf('>') + 1, o1.indexOf('.')));
			int lineObj2 = Integer.parseInt(o2.substring(o2.indexOf('>') + 1, o2.indexOf('.')));
			int result = lineObj1 - lineObj2;

			if (result < 0) // o1 < o2
			{
				return -1;
			} else if (result == 0) // o1 = o2
			{
				return 0;
			} else // o1 >o2
			{
				return 1;
			}
		}
	}

	public File getWelcomePage() {
		return summaryHTML.getAbsoluteFile();
	}

	/**
	 * Retrieves the content of the filename. Also reads from JAR Searches for the resource in the
	 * root folder in the jar
	 * 
	 * @param fname
	 *            Filename.
	 * @return The contents of the file.
	 * @throws IOException
	 *             On error.
	 */
	private static String getTemplateAsString(String fname) throws IOException {
		// in .jar file
		String fnameJar = getFileNameInPath(fname);
		InputStream inStream = VisualizerServlet.class.getResourceAsStream("/" + fnameJar);
		if (inStream == null) {
			// try to find file normally
			File f = new File(fname);
			if (f.exists()) {
				inStream = new FileInputStream(f);
			} else {
				throw new IOException("Cannot find " + fname + " or " + fnameJar);
			}
		}

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inStream));
		String line;
		StringBuilder stringBuilder = new StringBuilder();

		while ((line = bufferedReader.readLine()) != null) {
			stringBuilder.append(line + "\n");
		}

		bufferedReader.close();
		return stringBuilder.toString();
	}

	/**
	 * Returns the filename in a path. For example with path = "foo/bar/crawljax.txt" returns
	 * "crawljax.txt"
	 * 
	 * @param path
	 * @return the filename from the path
	 */
	private static String getFileNameInPath(String path) {
		String fname;
		if (path.indexOf("/") != -1) {
			fname = path.substring(path.lastIndexOf("/") + 1);
		} else {
			fname = path;
		}
		return fname;
	}

}
