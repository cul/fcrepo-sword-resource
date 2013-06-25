package edu.columbia.cul.sword.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;

public class TemplateInputStream  extends ByteArrayInputStream {

	private static final Charset utf8 = Charset.forName("UTF-8");
	private static final byte[][] templateParts = getTemplateParts();

	public TemplateInputStream(String pid, String label, String ownerId) {
		super(getBytes(pid, label, ownerId));
	}

	private static byte[] getBytes(String pid, String label, String ownerId) {
		byte [] pidBytes = pid.getBytes(utf8);
		byte [] ownerBytes = ownerId.getBytes(utf8);
		byte [] labelBytes = label.getBytes(utf8);
		int byteLen = templateParts[0].length + templateParts[1].length + templateParts[2].length
				+ templateParts[3].length + pidBytes.length + ownerBytes.length + labelBytes.length;
		byte [] bytes = new byte[byteLen];
		int offset = 0;
		System.arraycopy(templateParts[0], 0, bytes, offset, templateParts[0].length);
		offset += templateParts[0].length;
		System.arraycopy(pidBytes, 0, bytes, offset, pidBytes.length);
		offset += pidBytes.length;
		System.arraycopy(templateParts[1], 0, bytes, offset, templateParts[1].length);
		offset += templateParts[1].length;
		System.arraycopy(labelBytes, 0, bytes, offset, labelBytes.length);
		offset += labelBytes.length;
		System.arraycopy(templateParts[2], 0, bytes, offset, templateParts[2].length);
		offset += templateParts[2].length;
		System.arraycopy(ownerBytes, 0, bytes, offset, ownerBytes.length);
		offset += ownerBytes.length;
		System.arraycopy(templateParts[3], 0, bytes, offset, templateParts[3].length);
		return bytes;
	}

	private static byte[][] getTemplateParts() {
		try{
			InputStream in = TemplateInputStream.class.getResourceAsStream("foxml.xml");
			InputStreamReader rdr =  new InputStreamReader(in, Charset.forName("UTF-8"));
			StringWriter writer = new StringWriter();
			int c = -1;
			while ((c = rdr.read()) != -1){ writer.write(c);}
			String src = writer.toString();
			String[] partStrings = new String[4];
			int offset = 0;
			int end = src.indexOf("#{PID}");
			partStrings[0] = src.substring(offset,end);
			offset = end + "#{PID}".length();
			end = src.indexOf("#{LABEL}");
			partStrings[1] = src.substring(offset,end);
			offset = end + "#{LABEL}".length();
			end = src.indexOf("#{OWNERID}");
			partStrings[2] = src.substring(offset,end);
			offset = end + "#{OWNERID}".length();
			partStrings[3] = src.substring(offset);

			return new byte[][]{partStrings[0].getBytes(utf8),partStrings[1].getBytes(utf8),partStrings[2].getBytes(utf8),partStrings[3].getBytes(utf8)};
		} catch (Throwable t) {
			throw new RuntimeException(t.getMessage(),t);
		}
	}



}
