package edu.columbia.cul.sword.xml.entry;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "generator", namespace = "http://www.w3.org/2005/Atom")
@XmlAccessorType(XmlAccessType.FIELD)
public class Generator {
    @XmlAttribute(name = "uri")
    private URI uri;
    
    @XmlAttribute(name = "version")
    private String version;
    
    @XmlValue
    private String content;
    
    public Generator(String generator, String version) {
    	
    	System.out.println("============== LogCheckPoint-3 inside Generator: " + generator + ", " + version);
    	
    	this.uri = URI.create(generator);
    	this.version = (version != null)? version : "1.0";
    }
    
    public Generator() {
    	
    }
    
    public void setUri(URI uri) {
    	this.uri = uri;
    }
    
    public URI getUri() {
    	return this.uri;
    }
    
    public void setVersion(String version) {
    	this.version = version;
    }
    
    public String getVersion() {
    	return this.version;
    }
    
}
