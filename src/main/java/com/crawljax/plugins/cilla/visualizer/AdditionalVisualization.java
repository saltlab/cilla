package com.crawljax.plugins.cilla.visualizer;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.crawljax.plugins.cilla.CillaPlugin;

import com.crawljax.plugins.cilla.examples.CillaRunner;

// This class is for visualizing integrated results generated by W3C CSS Validator and CSS Lint and also CSS statistics in the report generated by CILLA

public class AdditionalVisualization extends VisualizerServlet {

private final File cssValidationHTML;
private Template cssValidationTemplate;
private final File cssLintHTML;
private Template cssLintTemplate;
private final File csscssHTML;
private Template csscssTemplate;
private final File statisticsHTML;
private Template statisticsTemplate;


public AdditionalVisualization(){

cssValidationHTML = new File(outputDir + "/css-validation.html");
cssValidationTemplate = ve.getTemplate("validation.vm");
cssLintHTML = new File(outputDir + "/css-lint.html");
cssLintTemplate = ve.getTemplate("lint.vm");
csscssHTML = new File(outputDir + "/csscss.html");
csscssTemplate = ve.getTemplate("csscss.vm");
statisticsHTML= new File(outputDir + "/statistics.html");
statisticsTemplate = ve.getTemplate("statistics.vm");

}

public void addValidation(String url){
         
crawledAddress = url;
VelocityContext context = new VelocityContext();
String template;
String cssValidationMsg;
try {
template = getTemplateAsString(cssValidationTemplate.getName());


//Document doc = Jsoup.connect("http://jigsaw.w3.org/css-validator/validator?uri=http%3A%2F%2F"+CillaRunner.b+"%2F&warning=2&profile=css2").get();

Document doc = Jsoup.connect("http://jigsaw.w3.org/css-validator/validator?uri="+CillaRunner.b+"&profile=css3&usermedium=all&warning=2&vextwarning=&lang=en").get();
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

Map<String, Map<String, String>> fileMap = new HashMap<String, Map<String, String>>();
String lintStr;
for(int i = 1; i< CillaPlugin.outputNum1; i++){
String filename = CillaPlugin.visualFilename[i];
Map<String, String> analysisMap = new HashMap<String, String>();

try {

File f = new File("C:/Users/Golnaz/cilla/CsslintReports/output"+CillaPlugin.outputNum+i+".txt");

FileInputStream fin = new FileInputStream(f);
if (!f.exists()) {
f.createNewFile();
}
byte[] buffer = new byte[(int) f.length()];
new DataInputStream(fin).readFully(buffer);
fin.close();
String s = new String(buffer, "UTF-8");
//System.out.println(s);


lintStr = s;
lintStr = lintStr.replace("\n", "<br> ");

analysisMap.put("", lintStr);
fileMap.put(filename, analysisMap);
VelocityContext context = new VelocityContext();
context.put("filemap", fileMap);
String template;
try {
template = getTemplateAsString(cssLintTemplate.getName());
FileWriter writer = new FileWriter(cssLintHTML);

ve.evaluate(context, writer, "CSS-Lint", template);
writer.flush();
writer.close();

} catch (IOException e) {
e.printStackTrace();
}
} catch (IOException e) {
e.printStackTrace();
}


}
}

public void addCssCss(){
	
	Map<String, Map<String, String>> fileMap = new HashMap<String, Map<String, String>>();
	String csscssStr;
	for(int i = 1; i< CillaPlugin.outputNum1; i++){
	String filename = CillaPlugin.visualFilename[i];
	Map<String, String> analysisMap = new HashMap<String, String>();

	try {

	File f = new File("D:/CssCssReports/output"+(CillaPlugin.outputNum-1)+i+".txt");

	FileInputStream fin = new FileInputStream(f);
	if (!f.exists()) {
	f.createNewFile();
	}
	byte[] buffer = new byte[(int) f.length()];
	new DataInputStream(fin).readFully(buffer);
	fin.close();
	String s = new String(buffer, "UTF-8");
	//System.out.println(s);


	csscssStr = s;
	csscssStr = csscssStr.replace("\n", "<br> ");

	analysisMap.put("", csscssStr);
	fileMap.put(filename, analysisMap);
	VelocityContext context = new VelocityContext();
	context.put("filemap", fileMap);
	String template;
	try {
	template = getTemplateAsString(csscssTemplate.getName());
	FileWriter writer = new FileWriter(csscssHTML);

	ve.evaluate(context, writer, "CSS-CSS", template);
	writer.flush();
	writer.close();

	} catch (IOException e) {
	e.printStackTrace();
	}
	} catch (IOException e) {
	e.printStackTrace();
	}


	}
	
}
public void addStatistics(){

VelocityContext context = new VelocityContext();
String template;
String statisticsMsg;


try {
template = getTemplateAsString(statisticsTemplate.getName());



String table = "table"+"tr"+"td"+"Measuring Number of Properties of One CSS Selector"+"\n"+"\td"+"td"+""+"\td"+"\tr"+"tr"+"td"+"Min"+"\n"+"\td"+"td"+(CillaPlugin.min)+
"\n"+"\td"+"\tr"+"tr"+"td"+"Mean"+"\n"+"\td"+"td"+(double)Math.round((CillaPlugin.Mean) * 100) / 100+
"\n"+"\td"+"\tr"+"tr"+"td"+"Median"+"\n"+"\td"+"td"+(CillaPlugin.Median)+
"\n"+"\td"+"\tr"+"tr"+"td"+"Max"+"\n"+"\td"+"td"+(CillaPlugin.max)+
"\n"+"\td"+"\tr"+"tr"+"td"+"\b"+"Measuring Number of Selector Types in One CSS Rule"+
"\n"+"\td"+"td"+""+"\td"+"\tr"+"tr"+"td"+"Min"+"\n"+"\td"+"td"+(CillaPlugin.minSelector)+
"\n"+"\td"+"\tr"+"tr"+"td"+"Mean"+"\n"+"\td"+"td"+(double)Math.round((CillaPlugin.meanSelector) * 100) / 100+
"\n"+"\td"+"\tr"+"tr"+"td"+"Median"+"\n"+"\td"+"td"+(CillaPlugin.medianSelector)+
"\n"+"\td"+"\tr"+"tr"+"td"+"Max"+"\n"+"\td"+"td"+(CillaPlugin.maxSelector)+
"\n"+"\td"+"\tr"+"tr"+"td"+"\b"+"Measuring CSS Code Quality"+
"\n"+"\td"+"td"+""+"\td"+"\tr"+"tr"+"td"+"Universality"+"\n"+"\td"+"td"+CillaPlugin.uni+
"\n"+"\td"+"\tr"+"tr"+"td"+"Average Scope"+"\n"+"\td"+"td"+CillaPlugin.AS+
"\n"+"\td"+"\tr"+"tr"+"td"+"*Abs"+"\n"+"\td"+"td"+CillaPlugin.abstFactor+
"\n"+"\td"+"\tr"+"tr"+"td"+"\b"+"Number of IDs"+"\n"+"\td"+"td"+""+"\td"+"\tr"+"tr"+"td"+"Total"+"\n"+"\td"+"td"+CillaPlugin.id+
"\n"+"\td"+"\tr"+"tr"+"td"+"Average (Total/NumOfSelectors)"+"\n"+"\td"+"td"+(double)Math.round((CillaPlugin.averageid) * 100) / 100+
"\n"+"\td"+"\tr"+"tr"+"td"+"\b"+"Number of Classes"+"\n"+"\td"+"td"+""+"\td"+"\tr"+"td"+"Total"+"\n"+"\td"+"td"+CillaPlugin.clas+
"\n"+"\td"+"\tr"+"tr"+"td"+"Average (Total/NumOfSelectors)"+"\n"+"\td"+"td"+(double)Math.round((CillaPlugin.averageclas) * 100) / 100+
"\n"+"\td"+"\tr"+"tr"+"td"+"\b"+"Number of Elements"+"\n"+"\td"+"td"+""+"\td"+"tr"+"td"+"Total"+"\n"+"\td"+"td"+CillaPlugin.element+
"\n"+"\td"+"\tr"+"tr"+"td"+"Average (Total/NumOfSelectors)"+"\n"+"\td"+"td"+(double)Math.round((CillaPlugin.averageelement) * 100) / 100+"\n"+"\td"+"\tr"+"\table"+"\n"+"*Abs = Abstractness Factor"+"\n"+"Number of Crawled DOM States: "+CillaPlugin.count;
statisticsMsg = table;
statisticsMsg = statisticsMsg.replace("table", "<table>");
statisticsMsg = statisticsMsg.replace("\table", "</table> ");
statisticsMsg = statisticsMsg.replace("tr", "<tr>");
statisticsMsg = statisticsMsg.replace("\tr", "</tr>");
statisticsMsg = statisticsMsg.replace("td", "<td>");
statisticsMsg = statisticsMsg.replace("\td", "</td>");
statisticsMsg = statisticsMsg.replace("\n", "<hr>");
statisticsMsg = statisticsMsg.replace("\b", "<br><br>");



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




}