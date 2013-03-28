package edu.columbia.libraries.sword.xml.service;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "workspace", namespace="http://www.w3.org/2007/app")
@XmlAccessorType(XmlAccessType.FIELD)
public class Workspace {
	
	@XmlElement(name = "title", namespace = "http://www.w3.org/2005/Atom")
	public String title;
	
	@XmlElementRef
    public List<Collection> collections;
	
	public Workspace(){
		this.collections = new ArrayList<Collection>(1);
	}
	
	public boolean addCollection(Collection collection) {
		return this.collections.add(collection);
	}
	
	public boolean removeCollection(Collection collection) {
		return this.collections.remove(collection);
	}
	
	public List<Collection> getCollections(){
		return this.collections;
	}
	
	public Collection getCollection(String id) {
		for (Collection c: collections) {
			if (c.href.endsWith(id)) return c; //TODO this should actually try to build the relevant href and compare
		}
		return null;
	}
}
