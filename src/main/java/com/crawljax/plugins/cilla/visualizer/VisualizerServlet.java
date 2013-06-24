package com.crawljax.plugins.cilla.visualizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import com.crawljax.plugins.cilla.analysis.ElementWithClass;
import com.crawljax.plugins.cilla.analysis.MCssRule;
import com.crawljax.plugins.cilla.analysis.MSelector;
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
	private final File outputFolder = new File("output");

	private Template summaryTemplate;
	private Template cssAnalysisTemplate;
	private Template htmlAnalysisTemplate;

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

		try {
			Velocity.init();
		} catch (Exception e1) {
			System.err.println("Could not initialize Apache Velocity.");
		}

		try {
			summaryTemplate = ve.getTemplate("summary.vm");
			cssAnalysisTemplate = ve.getTemplate("css-analysis.vm");
			// htmlAnalysisTemplate = ve.getTemplate("html-analysis.vm");

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

	public void addSortedOutput(Map<String, List<MCssRule>> cssRules,
	        SetMultimap<String, ElementWithClass> elementsWithNoClassDef) {

		Map<String, Map<String, String>> fileMap = new HashMap<String, Map<String, String>>();

		String filename = new String();
		StringBuffer unmatchedBuffer;
		StringBuffer ineffectiveBuffer;
		StringBuffer effectiveBuffer;
		StringBuffer undefClassBuffer;

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

			analysisMap.put("Unmatched CSS Rules", unmatchedStr);
			analysisMap.put("Matched & Ineffective CSS Rules", ineffectiveStr);
			analysisMap.put("Matched & Effective CSS Rules", effectiveStr);
			analysisMap.put("Undefined CSS Classes", undefinedStr);

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
