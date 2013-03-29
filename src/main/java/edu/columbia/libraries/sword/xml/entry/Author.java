package edu.columbia.libraries.sword.xml.entry;

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
    
    @Override
    public boolean equals(Object object) {
    	if (object instanceof Author) {
    		Author that = (Author)object;
    		return this.name.equals(that.name);
    	}
    	return false;
    }
}