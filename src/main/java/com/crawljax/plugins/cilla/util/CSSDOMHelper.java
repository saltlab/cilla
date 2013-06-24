package com.crawljax.plugins.cilla.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.crawljax.plugins.cilla.analysis.ElementWithClass;
import com.crawljax.util.UrlUtils;
import com.crawljax.util.XPathHelper;

public class CSSDOMHelper {
	private static final Logger LOGGER = Logger.getLogger(CSSDOMHelper.class.getName());

	/**
	 * Get all the elements that have a 'class' attribute equal to the given classname.
	 * 
	 * @param classname
	 *            the class name.
	 * @param dom
	 *            the Document object.
	 * @return
	 */
	public List<Element> getElementsByClassname(String tagname, String classname, Document dom) {

		List<Element> matchedElements = new ArrayList<Element>();

		NodeList elements;

		// get all elements on the DOM
		if (tagname == null || "".equals(tagname.trim())) {
			elements = dom.getElementsByTagName("*");
		} else {
			elements = dom.getElementsByTagName(tagname);
		}

		for (int i = 0; i < elements.getLength(); i++) {
			NamedNodeMap attributes = elements.item(i).getAttributes();
			if (attributes != null) {
				for (int j = 0; j < attributes.getLength(); j++) {
					final Attr attr = (Attr) attributes.item(j);

					// if the element has an attribute of "class" and its value
					// is equal to the value given, we have a match.
					if ("class".equalsIgnoreCase(attr.getNodeName())
					        && attr.getNodeValue().contains(classname.trim())) {

						// classvalue could be a list of class names:
						// e.g. class='news article home'
						String[] classValues = attr.getNodeValue().split(" ");

						for (int k = 0; k < classValues.length; k++) {
							if (classValues[k].equalsIgnoreCase(classname.trim())) {
								matchedElements.add((Element) elements.item(i));
								break;
							}
						}

					}
				}
			}

		}

		return matchedElements;
	}

	public static List<String> extractCssFilenames(Document dom) {

		List<String> cssFileNames = new ArrayList<String>();

		NodeList linkTags = dom.getElementsByTagName("link");

		if (linkTags != null) {
			for (int i = 0; i < linkTags.getLength(); i++) {
				Node linkNode = linkTags.item(i);
				Node rel = linkNode.getAttributes().getNamedItem("rel");

				if (rel != null && rel.getNodeValue().toString().equalsIgnoreCase("stylesheet")) {
					Node href = linkNode.getAttributes().getNamedItem("href");

					if (href != null) {
						cssFileNames.add(href.getNodeValue().toString());
					}
				}

			}
		}

		return cssFileNames;
	}

	/**
	 * @param location
	 *            the URL location of the page (http://www.global.com).
	 * @param relUrl
	 *            the (relative) URL of the file (e.g ../../world/news.css).
	 * @return the absolute path of the file.
	 */
	public static String getAbsPath(String location, String relUrl) {

		if (relUrl.startsWith("http")) {
			return relUrl;
		}

		// Example: /default.css
		if (relUrl.startsWith("/")) {
			return UrlUtils.getBaseUrl(location) + relUrl;
		}

		// it is relative, example: ../../default.css
		String loc = location.substring(0, location.lastIndexOf('/'));

		while (relUrl.contains("../")) {
			relUrl = relUrl.substring(3);
			loc = loc.substring(0, loc.lastIndexOf('/'));
		}

		return loc + '/' + relUrl;
	}

	/**
	 * @param url
	 *            The URL.
	 * @return the content (string) of resource.
	 * @throws HttpException
	 * @throws IOException
	 */
	public static String getURLContent(String url) throws HttpException, IOException {

		GetMethod method = new GetMethod(url);

		int returnCode = new HttpClient().executeMethod(method);
		if (returnCode == 200) {
			return method.getResponseBodyAsString();
		}

		return "";
	}

	/**
	 * @param dom
	 *            the document object.
	 * @return the content of all the embedded css rules that are defined inside <STYLE> elements.
	 */
	public static String getEmbeddedStyles(Document dom) {
		NodeList styles = dom.getElementsByTagName("style");

		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < styles.getLength(); i++) {
			Node style = styles.item(i);

			buffer.append(style.getTextContent() + "\n");
		}

		return buffer.toString();
	}

	public static List<ElementWithClass> getElementWithClass(String stateName, Document dom)
	        throws XPathExpressionException {

		final List<ElementWithClass> results = new ArrayList<ElementWithClass>();
		final NodeList nodes =
		        XPathHelper.evaluateXpathExpression(dom, "./descendant::*[@class != '']");

		for (int i = 0; i < nodes.getLength(); i++) {
			Element node = (Element) nodes.item(i);
			String classvalue = node.getAttributeNode("class").getValue();

			results.add(new ElementWithClass(stateName, node,
			        Arrays.asList(classvalue.split(" "))));

		}

		return results;
	}
}
