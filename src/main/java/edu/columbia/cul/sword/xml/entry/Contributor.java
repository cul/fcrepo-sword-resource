package edu.columbia.cul.sword.xml.entry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "contributor", namespace = "http://www.w3.org/2005/Atom")
public class Contributor {

	@XmlElement(name ="name")
    private String name;
    
    public Contributor(String name) {
    	this.name = name;
    }
    
    public Contributor() {
    	
    }
    
    public void setName(String name) {
    	this.name = name;
    }
    
    public String getName() {
    	return this.name;
    }
    
    @Override
    public boolean equals(Object object) {
    	if (object instanceof Contributor) {
    		Contributor that = (Contributor)object;
    		return this.name.equals(that.name);
    	}
    	return false;
    }
}
