package edu.columbia.libraries.sword.xml.entry;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "feed", namespace = "http://www.w3.org/2005/Atom")
@XmlAccessorType(XmlAccessType.FIELD)
public class Feed {
	
	@XmlElement(name = "entry", namespace = "http://www.w3.org/2005/Atom")
    private List<Entry> entries;
	
	@XmlElement(name = "link", namespace ="http://www.w3.org/2005/Atom")
	private Link link;
	
	public Feed() {
		entries = new ArrayList<Entry>(10);
	}
	
	public void addEntry(Entry entry) {
		entries.add(entry);
	}
}
