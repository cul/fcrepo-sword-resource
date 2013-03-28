package edu.columbia.libraries.sword.xml.entry;

import static edu.columbia.libraries.sword.utils.DateUtils.getAtomDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.fcrepo.server.utilities.DCField;
import org.fcrepo.server.utilities.DCFields;

import edu.columbia.libraries.fcrepo.FedoraDeposit;
import edu.columbia.libraries.sword.impl.DepositRequest;
import edu.columbia.libraries.sword.xml.entry.Author;
import edu.columbia.libraries.sword.xml.entry.Content;
import edu.columbia.libraries.sword.xml.entry.Contributor;
import edu.columbia.libraries.sword.xml.entry.Link;
import edu.columbia.libraries.sword.xml.entry.Generator;

@XmlRootElement(name = "entry", namespace = "http://www.w3.org/2005/Atom")
@XmlAccessorType(XmlAccessType.FIELD)
public class Entry {
    @XmlElement(name = "title")
    public String title;

    @XmlElement(name = "id", namespace = "http://www.w3.org/2005/Atom")
    public String id;
    
    @XmlElement(name = "published", namespace = "http://www.w3.org/2005/Atom")
    private String published;
    
    @XmlElement(name = "updated", namespace = "http://www.w3.org/2005/Atom")
    private String updated;
    
    @XmlElementRef
    private Set<Author> authors;
    
    @XmlElementRef
    private Set<Contributor> contributors;
    
	@XmlElement(name = "userAgent", namespace = "http://purl.org/net/sword/")
	public String userAgent;
	
	@XmlElement
	public Content content;
	
	@XmlElementRef
	List<Link> links;
	
	@XmlElementRef
	Source source;
	
	public Entry() {
		this.links = new ArrayList<Link>(2);
		this.contributors = new HashSet<Contributor>(1);
		this.authors = new HashSet<Author>(1);
	}
	
	public Entry(String id) {
		this();
		this.id = id;
	}
	
	public void addDescriptionLink(String href) {
		links.add(Link.getDescriptionLink(href));
	}
	
	public void addMediaLink(String href) {
		links.add(Link.getMediaLink(href));
	}
	
	public List<Link> getLinks() {
		return links;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId(){
		return this.id;
	}

	@XmlElement(name = "treatment", namespace = "http://purl.org/net/sword/")
	public String treatment;
	
	public void setGenerator(String generator, String version) {
		this.source = new Source();
		this.source.generator = new Generator(generator, version);
	}
    
    public void setPublished(Date updatedDate) {
    	this.updated = getAtomDate(updatedDate);
    }

    public void setUpdated(Date updatedDate) {
    	this.updated = getAtomDate(updatedDate);
    }
    
    public boolean addAuthor(String author) {
    	return authors.add(new Author(author));
    }
    
    public boolean removeAuthor(String author) {
    	return authors.remove(new Author(author));
    }

    public boolean addContributor(String contributor) {
    	return contributors.add(new Contributor(contributor));
    }

    public boolean removeContributor(String contributor) {
    	return contributors.remove(new Contributor(contributor));
    }
    
    public void setDepositInfo(FedoraDeposit deposit){
		//if (deposit.getPackaging() != null && deposit.getPackaging().trim().length() != 0) {
		//	this.setPackaging(deposit.getPackaging());
		//}	
		// this.setNoOp(deposit.isNoOp());
		
		this.addAuthor(deposit.getUsername());
		if (deposit.getOnBehalfOf() != null) {
			this.addContributor(deposit.getOnBehalfOf());
		}
		Content content = new Content();
		content.type = deposit.getContentType();
		content.src = deposit.getLocation();
		this.content = content;
        this.id = deposit.getDepositPid();
    }
        
    public void setDCFields(DCFields dcf) {
    	for (DCField field :dcf.subjects()) {
    		//TODO implement
    		//this.addCategory(field.getValue());
    	}
    	for (DCField field: dcf.rights()) {
    		//TODO implement rights on atom:source
    		//this.addCollectionPolicy(field.getValue());
    	}
    	for (DCField field: dcf.descriptions()) {
    		//TODO implement summary on this
    		//this.appendSummary(field.getValue());
    	}
    	for (DCField field: dcf.titles()) {
    		this.title += " " + field.getValue();
    	}
    	
    }
}
