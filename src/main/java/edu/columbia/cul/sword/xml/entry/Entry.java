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

import edu.columbia.libraries.sword.impl.DepositRequest;
import edu.columbia.libraries.sword.xml.entry.Author;
import edu.columbia.libraries.sword.xml.entry.Content;
import edu.columbia.libraries.sword.xml.entry.Contributor;
import edu.columbia.libraries.sword.xml.entry.Link;
import edu.columbia.libraries.sword.xml.entry.Generator;

@XmlRootElement(name = "entry", namespace = "http://www.w3.org/2005/Atom")
@XmlAccessorType(XmlAccessType.FIELD)
public class Entry {
    @XmlElement(name = "title", namespace = "http://www.w3.org/2005/Atom")
    List<String> title;

    @XmlElement(name = "id", namespace = "http://www.w3.org/2005/Atom")
    String id;
    
    @XmlElement(name = "published", namespace = "http://www.w3.org/2005/Atom")
    String published;
    
    @XmlElement(name = "updated", namespace = "http://www.w3.org/2005/Atom")
    String updated;
    
    @XmlElementRef
    Set<Author> authors;
    
    @XmlElementRef
    Set<Contributor> contributors;
    
	@XmlElement(name = "userAgent", namespace = "http://purl.org/net/sword/")
	String userAgent;
	
	@XmlElement(name = "summary", namespace = "http://purl.org/net/sword/")
	List<String> summary;
	
	@XmlElementRef
	Content content;
	
	@XmlElementRef
	List<Link> links;
	
	@XmlElementRef
	Source source;
	
	@XmlElement(name = "packaging", namespace = "http://purl.org/net/sword/")
	public String packaging;
	
	public boolean noOp;
	
	
	public Entry() {
		this.links = new ArrayList<Link>(2);
		this.contributors = new HashSet<Contributor>(1);
		this.authors = new HashSet<Author>(1);
		this.title = new ArrayList<String>();
		this.summary = new ArrayList<String>(1);
	}
	
	public Entry(String id) {
		this();
		this.id = id;
	}
	
	public void addEditLink(String href) {
		links.add(Link.getDescriptionLink(href));
	}
	
	public void addEditMediaLink(String href) {
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
	public String treatment = "Ingested into FCRepo";
	
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
    
    public Set<String> getAuthors() {
    	HashSet<String> authors = new HashSet<String>(this.authors.size());
    	for (Author author: this.authors) {
    		authors.add(author.getName());
    	}
    	return authors;
    }

    public boolean addContributor(String contributor) {
    	return contributors.add(new Contributor(contributor));
    }

    public boolean removeContributor(String contributor) {
    	return contributors.remove(new Contributor(contributor));
    }
    
    public Set<String> getContributors() {
    	HashSet<String> contributors = new HashSet<String>(this.contributors.size());
    	for (Contributor contributor: this.contributors) {
    		contributors.add(contributor.getName());
    	}
    	return contributors;
    }

    public void setPackaging(String packaging) {
    	this.packaging = packaging;
    }
    
    public String getPackaging() {
    	return this.packaging;
    }
    
    public boolean addTitle(String title) {
    	return this.title.add(title);
    }
    
    public boolean removeTitle(String title) {
    	return this.title.remove(title);
    }
    
    public List<String > getTitles() {
    	return this.title;
    }
    
    public boolean addSummary(String summary) {
    	return this.summary.add(summary);
    }
    
    public boolean removeSummary(String summary) {
    	return this.summary.remove(summary);
    }
    
    public List<String> getSummary(){
    	return this.summary;
    }
    
    public void setNoOp(boolean noOp) {
    	this.noOp = noOp;
    }
    
    public boolean getNoOp(){
    	return this.noOp;
    }
    
    public void setContent(String href, String contentType) {
    	Content content = new Content();
    	content.src = href;
    	content.type = contentType;
    	this.content = content;
    }
    
    public Content getContent() {
    	return this.content;
    }
    
    public void setDepositInfo(DepositRequest deposit){
    	
		if (deposit.getPackaging() != null) {
			String packaging = deposit.getPackaging().trim();
			if (packaging.length() > 0) setPackaging(packaging);
		}	
		this.setNoOp(deposit.isNoOp());
		
		this.addAuthor(deposit.getUserName());
		if (deposit.getOnBehalfOf() != null) {
			this.addContributor(deposit.getOnBehalfOf());
		}
    }
        
    public void setDCFields(DCFields dcf) {
    	for (DCField field :dcf.contributors()) {
    		this.addContributor(field.getValue());
    	}
    	for (DCField field: dcf.creators()) {
    		this.addAuthor(field.getValue());
    	}
    	for (DCField field: dcf.descriptions()) {
    		this.addSummary(field.getValue());
    	}
    	for (DCField field: dcf.titles()) {
    		this.addTitle(field.getValue());
    	}
    	
    }
    
    public DCFields getDCFields() {
    	DCFields result = new DCFields();
    	for (String title: this.title) {
    		result.titles().add(new DCField(title));
    	}
    	for (String creator: getAuthors()) {
    		result.creators().add(new DCField(creator));
    	}
    	for (String contrib: getContributors()) {
    		result.contributors().add(new DCField(contrib));
    	}
    	for (String summary: getSummary()) {
    		result.descriptions().add(new DCField(summary));
    	}
    	return result;
    }
    
}
