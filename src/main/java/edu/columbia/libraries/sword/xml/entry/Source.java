package edu.columbia.libraries.sword.xml.entry;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "source", namespace = "http://www.w3.org/2005/Atom")
public class Source {
  @XmlElementRef
  public Generator generator;
}
