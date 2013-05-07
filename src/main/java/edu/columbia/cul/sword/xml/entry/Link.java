package edu.columbia.libraries.sword.xml.entry;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "link", namespace = "http://www.w3.org/2005/Atom")
@XmlAccessorType(XmlAccessType.FIELD)
public class Link {

	@XmlAttribute(name = "rel")
	private String rel;
	
	@XmlAttribute(name = "href")
	private URI href;
	
	@XmlAttribute(name = "type")
	private String type;
	
	public Link(URI href, String rel) {
		this.href = href;
		this.rel = rel;
	}
	
	public Link() {
		
	}
	
	public boolean isDescription() {
		return rel.equals("edit");
	}
	
	public URI getHref(){
		return href;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}
	
	public static Link getDescriptionLink(String href) {
		return new Link(URI.create(href), "edit");
	}

	public static Link getMediaLink(String href) {
		return new Link(URI.create(href), "edit-media");
	}
}
