package edu.columbia.libraries.sword.xml;

import java.net.URI;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import edu.columbia.libraries.sword.xml.entry.Author;
import edu.columbia.libraries.sword.xml.entry.Generator;
import edu.columbia.libraries.sword.xml.entry.Link;

@XmlRootElement(name = "error", namespace = "http://purl.org/net/sword/")
public class SwordError {
	
	@XmlAttribute(name = "href")
	public URI reason;

	@XmlElement(name = "title", namespace = "http://www.w3.org/2005/Atom")
	public String title;
	
	@XmlElementRef
	public Author author;
	
	@XmlElement(name = "updated", namespace = "http://www.w3.org/2005/Atom")
	public String updated;
	
	@XmlElementRef
	public Generator generator;
	
	@XmlElement(name = "summary", namespace = "http://www.w3.org/2005/Atom")
	public String summary;
	
	@XmlElement(name = "treatment", namespace = "http://purl.org/net/sword/")
	public String treatment;
	
	@XmlElementRef
	public Link link;
	
	public void setUserAgent(String userAgent) {
		
	}
}
