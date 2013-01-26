package com.crawljax.plugins.cilla.visualizer;

import java.util.List;
import java.util.Map;

import com.crawljax.plugins.cilla.analysis.ElementWithClass;
import com.crawljax.plugins.cilla.analysis.MCssRule;
import com.google.common.collect.SetMultimap;

/*
 * The com.crawljax.plugins.cilla.visualizer plug-in
 */
public interface VisualizerPlugin {
	/**
	 * 
	 * @param summary
	 * @param cssRules
	 * @param elementsWithNoClassDef
	 */
	void openVisualizer(String url, String summary, Map<String, List<MCssRule>> cssRules,
	        SetMultimap<String, ElementWithClass> elementsWithNoClassDef);

}
