package com.crawljax.plugins.cilla.analysis;

import org.apache.log4j.Logger;

public class TraceData {
	private static final Logger LOGGER = Logger.getLogger(TraceData.class.getName());
	private String trace;
	private String selector;
	private String url;
	private int lineNumber;

	public TraceData(String trace) {

		this.trace = trace;
		parse();

	}

	private void parse() {
		LOGGER.debug("PARSING: " + trace);

		this.selector = trace.substring(trace.indexOf('{') + 1, trace.indexOf('}'));
		// this.selector = selector.replaceAll("\\_", " ").replaceAll("\\*", "");
		this.selector = selector.replaceAll("\\*", "");
		this.lineNumber =
		        Integer.parseInt(trace.substring(trace.indexOf('[') + 1, trace.indexOf(']')));
		this.url = trace.substring(0, trace.indexOf('['));

	}

	public String getTrace() {
		return trace;
	}

	public String getSelector() {
		return selector;
	}

	public String getUrl() {
		return url;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("Selector " + this.selector + "\n");
		buffer.append("Linenumber " + this.lineNumber + "\n");

		return buffer.toString();
	}

}
