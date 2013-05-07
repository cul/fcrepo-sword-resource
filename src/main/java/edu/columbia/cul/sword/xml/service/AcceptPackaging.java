package edu.columbia.libraries.sword.xml.service;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "acceptPackaging", namespace="http://purl.org/net/sword/")
@XmlAccessorType(XmlAccessType.FIELD)
public class AcceptPackaging {
	
	@XmlAttribute(name = "q")
	private String qRating;
	
    @XmlValue
    public URI packaging;
    
    public AcceptPackaging() {
    	
    }
    
    public AcceptPackaging(String packaging) {
    	this(URI.create(packaging));
    }
    
    public AcceptPackaging(URI packaging) {
    	this(packaging, 1.0F);
    }

    public AcceptPackaging(String packaging, float q) {
    	this(URI.create(packaging), q);
    }
    
    public AcceptPackaging(URI packaging, float q) {
    	this.packaging = packaging;
    	setQ(q);
    }
    
    public void setQ(float q) {
    	if (q < 0 || q > 1) q = 1.0F;
    	String qRating = Float.toString(q);
    	int pos = qRating.indexOf('.');
    	if (pos == -1) {
    		qRating += ".0";
    	} else {
    		if (qRating.length() > (pos + 4)) {
    			qRating = qRating.substring(0, pos+4);
    		}
    	}
    	this.qRating = qRating;
    }
    
    public String getQ() {
    	return this.qRating;
    }
    
    @Override
    public boolean equals(Object other) {
    	if (! (other instanceof AcceptPackaging) ) return false;
    	AcceptPackaging that = (AcceptPackaging)other;
    	return this.packaging.equals(that.packaging) && this.qRating.equals(that.qRating);
    }
}
