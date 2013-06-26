package edu.columbia.cul.sword;

import org.fcrepo.common.rdf.RDFName;
import org.fcrepo.common.rdf.RDFNamespace;

public class SwordNamespace extends RDFNamespace {

	private static final long serialVersionUID = 1L;
	
	public final RDFName CONTENT_TYPE;
	
	public final RDFName MEDIATION;
	
	public final RDFName PACKAGING;
	
	public final RDFName PROXY;
	
	public final RDFName SLUG;
	
	public final RDFName ON_BEHALF_OF;

	public SwordNamespace() {

        uri = "http://purl.org/sword/";
        prefix = "sword";
        
        CONTENT_TYPE = new RDFName(this, "m_contentType");
        
        MEDIATION = new RDFName(this, "mediation");
        
        PACKAGING = new RDFName(this, "packaging");
        
        PROXY = new RDFName(this, "proxy");

        SLUG = new RDFName(this, "slug");
        
        ON_BEHALF_OF = new RDFName(this, "On-Behalf-Of");
        
    }

}
