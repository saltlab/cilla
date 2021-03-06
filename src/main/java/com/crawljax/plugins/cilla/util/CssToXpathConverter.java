package com.crawljax.plugins.cilla.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CssToXpathConverter {

	private static Pattern cssSelectorRegExp =
	        Pattern.compile("^(\\w+)?(#[a-zA-Z_0-9\u00C0-\uFFFF\\-\\_]+|(\\*))?((\\.[a-zA-Z_0-9\u00C0-\uFFFF\\-_]+)*)?((\\[\\w+(\\^|\\$|\\*|\\||~)?(=[a-zA-Z_0-9\u00C0-\uFFFF\\s\\-\\_\\.]+)?\\]+)*)?(((:\\w+[a-zA-Z_0-9\\-]*)(\\((odd|even|\\-?\\d*n?((\\+|\\-)\\d+)?|[a-zA-Z_0-9\u00C0-\uFFFF\\-_]+|((\\w*\\.[a-zA-Z_0-9\u00C0-\uFFFF\\-_]+)*)?|(\\[#?\\w+(\\^|\\$|\\*|\\||~)?=?[a-zA-Z_0-9\u00C0-\uFFFF\\s\\-\\_\\.]+\\]+)|(:\\w+[a-zA-Z_0-9\\-]*))\\))?)*)?(>|\\+|~)?");

	private static Pattern selectorSplitRegExp = Pattern
	        .compile("(?:\\[[^\\[]*\\]|\\(.*\\)|[^\\s\\+>~\\[\\(])+|[\\+>~]");

	static class SplitRule {

		public String tag;

		public String id;

		public String allClasses;

		public String allAttr;

		public String allPseudos;

		public String tagRelation;
	}

	private static boolean notNull(String s) {
		return s != null && !"".equals(s);
	}

	public static String convert(String selector) {
		/*
		 * if (selector.startsWith("*")) { selector = selector.substring(1, selector.length()); }
		 */
		selector = removeChar(selector, '*');
		String prefix = "";
		String[] cssRules = selector.replaceAll("\\s*(,)\\s*", "$1").split(",");
		String currentRule;
		boolean identical = false;
		String xPathExpression = ".";

		for (int i = 0; i < cssRules.length; i++) {
			currentRule = cssRules[i];

			if (i > 0) {
				identical = false;
				for (int x = 0, xl = i; x < xl; x++) {
					if (cssRules[i].equals(cssRules[x])) {
						identical = true;
						break;
					}
				}
				if (identical) {
					continue;
				}
			}

			ArrayList<String> cssSelectors = new ArrayList<String>();
			Matcher selm = selectorSplitRegExp.matcher(currentRule);
			while (selm.find()) {
				cssSelectors.add(selm.group(0));
			}

			Matcher cssSelector;
			for (int j = 0, jl = cssSelectors.size(); j < jl; j++) {
				cssSelector = cssSelectorRegExp.matcher(cssSelectors.get(j));
				if (cssSelector.matches()) {

					SplitRule splitRule = new SplitRule();
					splitRule.tag =
					        prefix
					                + ((!notNull(cssSelector.group(1)) || "*".equals(cssSelector
					                        .group(3))) ? "*" : cssSelector.group(1));
					splitRule.id =
					        (!"*".equals(cssSelector.group(3))) ? cssSelector.group(2) : null;
					splitRule.allClasses = cssSelector.group(4);
					splitRule.allAttr = cssSelector.group(6);
					splitRule.allPseudos = cssSelector.group(10);
					splitRule.tagRelation = cssSelector.group(22);
					if (notNull(splitRule.tagRelation)) {
						if (">".equals(splitRule.tagRelation)) {
							xPathExpression += "/child::";
						} else if ("+".equals(splitRule.tagRelation)) {
							xPathExpression += "/following-sibling::*[1]/self::";
						} else if ("~".equals(splitRule.tagRelation)) {
							xPathExpression += "/following-sibling::";
						}
					} else {
						xPathExpression +=
						        (j > 0 && cssSelectors.get(j - 1).matches("(>|\\+|~)")) ? splitRule.tag
						                : ("/descendant::" + splitRule.tag.toUpperCase());
					}

					if (notNull(splitRule.id)) {
						xPathExpression += "[@id = '" + splitRule.id.replaceAll("^#", "") + "']";
					}
					if (notNull(splitRule.allClasses)) {
						xPathExpression +=
						        splitRule.allClasses.replaceAll(
						                "\\.([a-zA-Z_0-9\u00C0 -\uFFFF\\-_]+)",
						                "[contains(concat(' ', @class, ' '), ' $1 ')]");
					}
					if (notNull(splitRule.allAttr)) {
						xPathExpression +=
						        attrToXPath(splitRule.allAttr,
						                "(\\w+)(\\^|\\$|\\*|\\||~)?=?([a-zA-Z_0-9\u00C0-\uFFFF\\s\\-_\\.]+)?");
					}
					if (notNull(splitRule.allPseudos)) {
						Pattern pseudoSplitRegExp =
						        Pattern.compile(":(\\w[a-zA-Z_0-9\\-]*)(\\(([^\\)]+)\\))?");
						Matcher m =
						        Pattern.compile("(:\\w+[a-zA-Z_0-9\\-]*)(\\([^\\)]+\\))?")
						                .matcher(splitRule.allPseudos);
						while (m.find()) {
							String str = m.group(0);
							Matcher pseudo = pseudoSplitRegExp.matcher(str == null ? "" : str);
							if (pseudo.matches()) {
								String pseudoClass =
								        notNull(pseudo.group(1)) ? pseudo.group(1).toLowerCase()
								                : null;
								String pseudoValue =
								        notNull(pseudo.group(3)) ? pseudo.group(3) : null;
								String xpath =
								        pseudoToXPath(splitRule.tag, pseudoClass, pseudoValue);
								if (notNull(xpath)) {
									xPathExpression += "[" + xpath + "]";
								}
							}
						}
					}
				}
			}
		}

		return xPathExpression;
	}

	static class Sequence {

		public int start;

		public int max;

		public int add;

		public int modVal;
	}

	private static String pseudoToXPath(String tag, String pseudoClass, String pseudoValue) {
		tag = pseudoClass.matches(".*\\-child$") ? "*" : tag;
		String xpath = "";
		String pseudo[] = pseudoClass.split("-");
		if ("first".equals(pseudo[0])) {
			xpath = "not(preceding-sibling::" + tag + ")";
		} else if ("last".equals(pseudo[0])) {
			xpath = "not(following-sibling::" + tag + ")";
		} else if ("only".equals(pseudo[0])) {
			xpath = "not(preceding-sibling::" + tag + " or following-sibling::" + tag + ")";
		} else if ("nth".equals(pseudo[0])) {
			if (!pseudoValue.matches("^n$")) {
				String position =
				        (("last".equals(pseudo[1])) ? "(count(following-sibling::"
				                : "(count(preceding-sibling::") + tag + ") + 1)";
				Sequence sequence = getSequence(pseudoValue);
				if (sequence != null) {
					if (sequence.start == sequence.max) {
						xpath = position + " = " + sequence.start;
					} else {
						xpath =
						        position
						                + " mod "
						                + sequence.add
						                + " = "
						                + sequence.modVal
						                + ((sequence.start > 1) ? " and " + position + " >= "
						                        + sequence.start : "")
						                + ((sequence.max > 0) ? " and " + position + " <= "
						                        + sequence.max : "");
					}
				}
			}
		} else if ("empty".equals(pseudo[0])) {
			xpath = "count(child::*) = 0 and string-length(text()) = 0";
		} else if ("contains".equals(pseudo[0])) {
			xpath = "contains(., '" + pseudoValue + "')";
		} else if ("enabled".equals(pseudo[0])) {
			xpath = "not(@disabled)";
		} else if ("disabled".equals(pseudo[0])) {
			xpath = "@disabled";
		} else if ("checked".equals(pseudo[0])) {
			xpath = "@checked='checked'"; // Doesn't work in Opera 9.24
		} else if ("not".equals(pseudo[0])) {
			if (pseudoValue.matches("^(:a-zA-Z_0-9+[a-zA-Z_0-9\\-]*)$")) {
				xpath = "not(" + pseudoToXPath(tag, pseudoValue.substring(1), "") + ")";
			} else {
				pseudoValue =
				        pseudoValue.replaceAll("^\\[#([a-zA-Z_0-9\u00C0-\uFFFF\\-\\_]+)\\]$",
				                "[id=$1]");
				String notSelector = pseudoValue.replaceFirst("^(a-zA-Z_0-9+)", "self::$1");
				notSelector =
				        notSelector.replaceAll("^\\.([a-zA-Z_0-9\u00C0-\uFFFF\\-_]+)",
				                "contains(concat(' ', @class, ' '), ' $1 ')");
				notSelector =
				        attrToXPath(notSelector,
				                "\\[(a-zA-Z_0-9+)(\\^|\\$|\\*|\\||~)?=?([a-zA-Z_0-9\u00C0-\uFFFF\\s\\-_\\.]+)?\\]");
				xpath = "not(" + notSelector + ")";
			}
		} else {
			xpath = "@" + pseudoClass + "='" + pseudoValue + "'";
		}
		return xpath;
	}

	private static String attrToXPath(String notSelector, String pattern) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(notSelector);
		m.reset();
		boolean result = m.find();
		if (result) {
			StringBuffer sb = new StringBuffer();
			do {
				String replacement;
				String p1 = m.group(1);
				String p2 = m.group(2);
				String p3 = m.group(3);
				if ("^".equals(p2)) {
					replacement = "starts-with(@" + p1 + ", '" + p3 + "')";
				} else if ("$".equals(p2)) {
					replacement =
					        "substring(@" + p1 + ", (string-length(@" + p1 + ") - "
					                + (p3.length() - 1) + "), " + p3.length() + ") = '" + p3
					                + "'";
				} else if ("*".equals(p2)) {
					replacement = "contains(concat(' ', @" + p1 + ", ' '), '" + p3 + "')";
				} else if ("|".equals(p2)) {
					replacement =
					        "(@" + p1 + "='" + p3 + "' or starts-with(@" + p1 + ", '" + p3
					                + "-'))";
				} else if ("~".equals(p2)) {
					replacement = "contains(concat(' ', @" + p1 + ", ' '), ' " + p3 + " ')";
				} else {
					replacement = "@" + p1 + (notNull(p3) ? "='" + p3 + "'" : "");
				}
				debug("p1=" + p1 + " p2=" + p2 + " p3=" + p3 + " replacement is " + replacement);
				m.appendReplacement(sb, replacement.replace("$", "\\$"));
				result = m.find();
			} while (result);
			m.appendTail(sb);
			return sb.toString();
		}
		return notSelector;
	}

	private static Sequence getSequence(String expression) {
		int start = 0, add = 2, max = -1, modVal = -1;
		Pattern expressionRegExp =
		        Pattern.compile("^((odd|even)|([1-9]\\d*)|((([1-9]\\d*)?)n([\\+\\-]\\d+)?)|(\\-(([1-9]\\d*)?)n\\+(\\d+)))$");
		Matcher pseudoValue = expressionRegExp.matcher(expression);
		if (!pseudoValue.matches()) {
			return null;
		} else {
			if (notNull(pseudoValue.group(2))) { // odd or even
				start = ("odd".equals(pseudoValue.group(2))) ? 1 : 2;
				modVal = (start == 1) ? 1 : 0;
			} else if (notNull(pseudoValue.group(3))) { // single digit
				start = Integer.parseInt(pseudoValue.group(3), 10);
				add = 0;
				max = start;
			} else if (notNull(pseudoValue.group(4))) { // an+b
				add = notNull(pseudoValue.group(6)) ? getInt(pseudoValue.group(6), 1) : 1;
				start = notNull(pseudoValue.group(7)) ? getInt(pseudoValue.group(7), 0) : 0;
				while (start < 1) {
					start += add;
				}
				modVal = (start > add) ? (start - add) % add : ((start == add) ? 0 : start);
			} else if (notNull(pseudoValue.group(8))) { // -an+b
				add =
				        notNull(pseudoValue.group(10)) ? Integer.parseInt(pseudoValue.group(10),
				                10) : 1;
				start = max = Integer.parseInt(pseudoValue.group(10), 10);
				while (start > add) {
					start -= add;
				}
				modVal = (max > add) ? (max - add) % add : ((max == add) ? 0 : max);
			}
		}
		Sequence s = new Sequence();
		s.start = start;
		s.add = add;
		s.max = max;
		s.modVal = modVal;
		return s;
	}

	private static int getInt(String s, int i) {
		try {
			if (s.startsWith("+")) {
				s = s.substring(1);
			}
			return Integer.parseInt(s);
		} catch (Exception e) {
			debug("error parsing Integer " + s);
			return i;
		}
	}

	private static void debug(String e) {
		System.out.println("Debug: " + e);
	}

	public static String removeChar(String s, char c) {

		String r = "";

		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) != c)
				r += s.charAt(i);
		}

		return r;
	}

}
