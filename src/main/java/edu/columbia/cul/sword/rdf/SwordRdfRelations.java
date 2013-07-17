package edu.columbia.cul.sword.rdf;

import org.fcrepo.common.rdf.FedoraRelsExtNamespace;
import org.fcrepo.common.rdf.RDFName;
import org.fcrepo.common.rdf.RDFNamespace;

public enum SwordRdfRelations {//extends RDFNamespace {
	
	CONTENT_TYPE("contentType"), PACKAGING("packaging");
	
	private String baseUri = uri = "info:fedora/fedora-system:def/relations-external#";
	private String uri;
	
	private SwordRdfRelations(String uri){
		this.uri = baseUri + uri;
	}
	
	public String getUri(){
		return uri;
	}
	
	
	
//    public static final FedoraRelsExtNamespace RELS_EXT =
//            new FedoraRelsExtNamespace();
//	
//	
//    private static final long serialVersionUID = 1L;
//
//
//    public final RDFName IS_MEMBER_OF;
//
//
//    public SwordRdfRelations() {
//
//        uri = "info:fedora/fedora-system:def/relations-external#";
//        prefix = "rel";
//
//        // Properties
//        IS_MEMBER_OF = new RDFName(this, "isMemberOf");
//
//        // Values
//
//        // Types
//
//    }

} // =========================================== //
