package analysis;

import junit.framework.Assert;

import org.junit.Test;

import com.crawljax.plugins.cilla.analysis.TraceData;

public class TraceDataTest {

	@Test
	public void testTraceData() {

		String trace = "http://www.ece.ubc.ca:80/~amesbah/exp/default.css[11]{div_*.c1}";

		TraceData trData = new TraceData(trace);

		Assert.assertEquals("http://www.ece.ubc.ca:80/~amesbah/exp/default.css", trData.getUrl());

		Assert.assertEquals("div_.c1", trData.getSelector());

		Assert.assertEquals(11, trData.getLineNumber());

	}
}
