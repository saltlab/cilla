
package com.crawljax.plugins.cilla.examples;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class GetUrls {
	
	private static final Logger LOG = LoggerFactory.getLogger(GetUrls.class);

	public static String[] getArray(String path, int size) {
		List<String> readLines = null;
		try {
			readLines = Files.readLines(new File(path), Charsets.UTF_8);
		} catch (IOException e) {
			LOG.error("Could not get list of web sites",e);
		}
		return readLines.toArray(new String[readLines.size()]);
	}
}