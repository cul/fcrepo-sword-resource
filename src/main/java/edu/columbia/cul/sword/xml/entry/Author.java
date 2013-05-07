package edu.columbia.cul.sword.xml.entry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "author", namespace = "http://www.w3.org/2005/Atom")
public class Author {
    @XmlElement(name ="name")
    private String name;
    
    public Author(String name) {
    	this.name = name;
    }
    
    public Author() {
    	
    }
    
    public void setName(String name) {
    	this.name = name;
    }
    
    public String getName() {
    	return this.name;
    }
    
    @Override
    public boolean equals(Object object) {
    	if (object instanceof Author) {
    		Author that = (Author)object;
    		return this.name.equals(that.name);
    	}
    	return false;
    }
}
