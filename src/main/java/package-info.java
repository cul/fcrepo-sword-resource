@XmlSchema(namespace = "urn:example.com:foo",  
    xmlns = {   
        @XmlNs(namespaceURI = "http://www.w3.org/2005/Atom", prefix = "atom"),  
        @XmlNs(namespaceURI = "http://purl.org/net/sword/", prefix = "sword")  
    },  
    elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED)  
  
package edu.columbia.cul.sword.xml.entry;  
  
import javax.xml.bind.annotation.XmlNs;  
import javax.xml.bind.annotation.XmlSchema; 