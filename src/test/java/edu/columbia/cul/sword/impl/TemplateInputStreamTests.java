package edu.columbia.cul.sword.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TemplateInputStreamTests {
  @Before
  public void setUp(){
	  
  }
  
  @After
  public void tearDown(){
	  
  }
  
  @Test
  public void testStuff() throws IOException{
	  InputStream src = TemplateInputStream.class.getResourceAsStream("/edu/columbia/libraries/sword/impl/foxml.xml");
	  if (src == null) fail("could not find foxml.xml resource");
	  String expected = readInput(src);
	  Pattern subPattern = Pattern.compile("\\#\\{(\\w.+)\\}");
	  Matcher matcher = subPattern.matcher(expected);
	  HashMap<String, String> props = new HashMap<String, String>();
	  props.put("PID", "changeme:1");
	  props.put("LABEL", "change this label");
	  props.put("OWNERID", "changeOwner");
	  while (matcher.find()){
		  expected = expected.replace(matcher.group(), props.get(matcher.group(1)));
	  }
      String actual = readInput(new TemplateInputStream(props.get("PID"), props.get("LABEL"), props.get("OWNERID")));
      assertEquals(expected, actual);
  }
  
  private static String readInput(InputStream in) throws IOException{
	  InputStreamReader rdr =  new InputStreamReader(in, Charset.forName("UTF-8"));
	  StringWriter writer = new StringWriter();
	  int c = -1;
	  while ((c = rdr.read()) != -1){ writer.write(c);}
	  return writer.toString();
  }
}
