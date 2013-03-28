package edu.columbia.libraries.sword.xml.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import edu.columbia.libraries.sword.xml.service.Workspace;

@XmlRootElement(name = "service", namespace="http://www.w3.org/2007/app")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceDocument {
	@XmlElement(name = "version", namespace = "http://purl.org/net/sword/")
	public String version;
	
	@XmlElementRef
	public Workspace workspace;
	
}
