package com.crawljax.plugins.cilla;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.crawljax.plugins.cilla.analysis.MCssRule;
import com.crawljax.plugins.cilla.analysis.MProperty;
import com.crawljax.plugins.cilla.analysis.MSelector;
import com.crawljax.plugins.cilla.analysis.MatchedElements;
import com.crawljax.plugins.cilla.util.CSSDOMHelper;
import com.crawljax.plugins.cilla.util.CssParser;
import com.crawljax.plugins.cilla.visualizer.CillaVisualizer;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class CillaPlugin implements OnNewStatePlugin, PostCrawlingPlugin {
	private static final Logger LOGGER = Logger.getLogger(CillaPlugin.class.getName());

	private Map<String, List<MCssRule>> cssRules = new HashMap<String, List<MCssRule>>();

	public static final Set<String> cssEffectiveRuntime = new HashSet<String>();

	final SetMultimap<String, ElementWithClass> elementsWithNoClassDef = HashMultimap.create();

	private final SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy-hhmmss");

	private final File outputFile = new File("output/cilla"
	        + String.format("%s", sdf.format(new Date())) + ".txt");

	private final Random random = new Random();

	private int cssLOC;
	private int ineffectivePropsSize;
	private int totalCssRulesSize;

	public void onNewState(CrawlerContext context, StateVertex newState) {
		// if the external CSS files are not parsed yet, do so
		parseCssRules(context, newState);

		// now we have all the CSS rules neatly parsed in "rules"
		checkCssOnDom(newState);

		checkClassDefinitions(newState);

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
						cssRules.put(cssUrl, rules);
					}
				}
			}

			// get all the embedded <STYLE> rules, save per HTML page
			if (!cssRules.containsKey(url)) {
				String embeddedRules = CSSDOMHelper.getEmbeddedStyles(dom);
				cssLOC += countLines(embeddedRules);

				List<MCssRule> rules = CssParser.getMCSSRules(embeddedRules);
				if (rules != null && rules.size() > 0) {
					cssRules.put(url, rules);
				}
			}

		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

	}

	@Override
	public void postCrawling(CrawlSession session, ExitStatus exitReason) {

		int totalCssRules = 0;
		int totalCssSelectors = 0;
		for (Map.Entry<String, List<MCssRule>> entry : cssRules.entrySet()) {
			totalCssRules += entry.getValue().size();
			for (MCssRule mrule : entry.getValue()) {
				totalCssSelectors += mrule.getSelectors().size();

			}
		}

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

		StringBuffer ineffectiveBuffer = new StringBuffer();
		int ineffectiveInt = getIneffectiveSelectorsBasedOnProps(ineffectiveBuffer);

		output.append("Analyzed " + session.getConfig().getUrl() + " on "
		        + new SimpleDateFormat("dd/MM/yy-hh:mm:ss").format(new Date()) + "\n");

		output.append("-> Files with CSS code: " + cssRules.keySet().size() + "\n");
		for (String address : cssRules.keySet()) {
			output.append("    Address: " + address + "\n");
		}
		// output.append("Total CSS Size: " + getTotalCssRulesSize() + " bytes" + "\n");

		output.append("-> LOC (CSS): " + cssLOC + "\n");
		output.append("-> Total Defined CSS rules: " + totalCssRules + "\n");
		output.append("-> Total Defined CSS selectors: " + totalCssSelectors + " from which: \n");
		int ignored = totalCssSelectors - (unused + used);
		output.append("   -> Ignored (:link, :hover, etc):   " + ignored + "\n");
		output.append("   -> Unmatched: " + unused + "\n");
		output.append("   -> Matched:   " + used + "\n");
		output.append("   -> Ineffective: " + ineffectiveInt + "\n");
		output.append("   -> Effective: " + effectiveInt + "\n");
		output.append("-> Total Defined CSS Properties: " + countProperties() + "\n");
		output.append("   -> Ignored Properties: " + countIgnoredProperties() + "\n");
		output.append("   -> Unused Properties: " + countUnusedProps() + "\n");

		// output.append("-> Effective CSS Rules: " + CSSProxyPlugin.cssTraceSet.size() + "\n");

		// output.append("   -> Effective: " + effectiveInt + "\n");
		output.append("->> Undefined Classes: " + undefClasses + "\n");
		// output.append("->> Duplicate Selectors: " + duplicates + "\n\n");
		output.append("By deleting unused rules, css size reduced by: "
		        + Math.ceil((double) reducedSize() / getTotalCssRulesSize() * 100) + " percent"
		        + "\n");

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

		try {
			FileUtils.writeStringToFile(outputFile, output.toString());

		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

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
						output.append("  " + unmatched + "\n");
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

}
