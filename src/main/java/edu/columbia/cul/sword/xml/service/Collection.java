package edu.columbia.cul.sword.xml.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.fcrepo.server.utilities.DCField;
import org.fcrepo.server.utilities.DCFields;

@XmlRootElement(name = "collection", namespace="http://www.w3.org/2007/app")
@XmlAccessorType(XmlAccessType.FIELD)
public class Collection {
	
	@XmlAttribute(name = "href")
	public String href;
	
	@XmlElement(name = "title", namespace = "http://www.w3.org/2005/Atom")
	public List<String> title;
	
	@XmlElement(name = "accept", namespace="http://www.w3.org/2007/app")
	Set<String> accepts;
	
	@XmlElement(name = "collectionPolicy", namespace = "http://purl.org/net/sword/")
	public List<String> collectionPolicy;

	@XmlElement(name = "mediation", namespace = "http://purl.org/net/sword/")
	public boolean mediation;

	@XmlElement(name = "treatment", namespace = "http://purl.org/net/sword/")
	public String treatment;

	@XmlElement(name = "abstract", namespace = "http://purl.org/dc/terms/")
	public List<String> dcAbstract;
	
	@XmlElementRef
	List<AcceptPackaging> acceptPackagings;
	
	@XmlElement(name = "service", namespace = "http://purl.org/net/sword/")
	String service;
	
	public Collection() {
		this.accepts = new HashSet<String>();
		this.title = new ArrayList<String>(1);
		this.dcAbstract = new ArrayList<String>(1);
		this.collectionPolicy = new ArrayList<String>(1);
		this.acceptPackagings = new ArrayList<AcceptPackaging>();
	}
	
	public boolean addAcceptableMimeType(String mimeType) {
		return this.accepts.add(mimeType);
	}
	
	public boolean removeAcceptableMimeType(String mimeType) {
		return this.accepts.remove(mimeType);
	}
	
	public Set<String> getAcceptableMimeTypes() {
		return this.accepts;
	}
	
	public boolean addAcceptablePackaging(URI packaging) {
		removeAcceptablePackaging(packaging);
		return this.acceptPackagings.add(new AcceptPackaging(packaging));
	}
	
	public boolean addAcceptablePackaging(URI packaging, float q) {
		removeAcceptablePackaging(packaging);
		return this.acceptPackagings.add(new AcceptPackaging(packaging, q));
	}
	
	public boolean removeAcceptablePackaging(URI packaging) {
		AcceptPackaging [] accepts = new AcceptPackaging[0];
		accepts = this.acceptPackagings.toArray(accepts);
		boolean result = false;
		for (AcceptPackaging accept:accepts) {
			if (packaging.equals(accept.packaging)){
				result = this.acceptPackagings.remove(packaging);
			}
		}
		return result;
	}
	
	public List<AcceptPackaging> getAcceptablePackagings() {
		return this.acceptPackagings;
	}
	
    @Override
	public boolean equals(Object other) {
    	if (!(other instanceof Collection)) return false;
    	Collection that = (Collection)other;
    	if (this.href == null) return that.href == null;
    	return this.href.equals(that.href);
    }
    
    public void setDCFields(DCFields dcf) {
		List<DCField> fields;
		fields = dcf.titles();
		if (fields.size() == 0){
			title.add( "FCRepo Collection " + href);
		}
		fields = dcf.descriptions();
		for (DCField field: fields) this.dcAbstract.add(field.getValue());
		fields = dcf.rights();
		for (DCField field: fields) this.collectionPolicy.add(field.getValue());
    }

    
}
