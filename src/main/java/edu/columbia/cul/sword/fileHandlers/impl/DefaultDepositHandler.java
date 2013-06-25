package edu.columbia.cul.sword.fileHandlers.impl;

import static org.fcrepo.common.Constants.MODEL;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.fcrepo.common.Constants;
import org.fcrepo.common.PID;
import org.fcrepo.common.rdf.SimpleURIReference;
import org.fcrepo.server.ReadOnlyContext;
import org.fcrepo.server.access.Access;
import org.fcrepo.server.access.RepositoryInfo;
import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.rest.DatastreamResource;
import org.fcrepo.server.rest.FedoraObjectsResource;
import org.fcrepo.server.storage.DOManager;
import org.fcrepo.server.storage.DOReader;
import org.fcrepo.server.storage.DOWriter;
import org.fcrepo.server.storage.types.DatastreamManagedContent;
import org.fcrepo.server.storage.types.DigitalObject;
import org.fcrepo.server.storage.types.MIMETypedStream;
import org.fcrepo.server.storage.types.Property;
import org.fcrepo.server.storage.types.RelationshipTuple;
import org.fcrepo.server.utilities.DCFields;
import org.jrdf.graph.PredicateNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.cul.fcrepo.Utils;
import edu.columbia.cul.sword.SWORDException;
import edu.columbia.cul.sword.SWORDResource;
import edu.columbia.cul.sword.SwordConstants;
import edu.columbia.cul.sword.fileHandlers.DepositHandler;
import edu.columbia.cul.sword.impl.DepositRequest;
import edu.columbia.cul.sword.impl.TemplateInputStream;
import edu.columbia.cul.sword.xml.entry.Entry;
import edu.columbia.cul.sword.xml.service.Collection;
import edu.columbia.cul.sword.xml.service.ServiceDocument;


public class DefaultDepositHandler implements DepositHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDepositHandler.class);	
	public static String DEFAULT_LABEL = "Object created via SWORD Deposit";	
//	protected String m_contentType = "";	
//	protected String m_packaging = null;	
	protected String m_contentType;	
	protected String m_packaging;	
	protected String m_namespace = "sword"; //default	
	//private DOManager m_mgmt;		
	private Set<String> m_collectionIds;
	private Set<String> m_rels; 	
	private Set<String> m_objectCModels = Collections.emptySet();
	private Access m_access;
	
//	public DefaultDepositHandler(DOManager mgmt, Set<String> collectionIds) throws ServerException {
//		m_mgmt = mgmt;
//		m_collectionIds = collectionIds;
//		m_rels = new HashSet<String>(1);
//		m_rels.add(Constants.RELS_EXT.IS_MEMBER_OF.uri);
//	}
//	
//	public DefaultDepositHandler(DOManager mgmt, Set<String> collectionIds, Set<String> membershipRels) throws ServerException {
//		m_mgmt = mgmt;
//		m_collectionIds = collectionIds;
//		m_rels = membershipRels;
//		m_rels.add(Constants.RELS_EXT.IS_MEMBER_OF.uri);
//	}
	
	
	public void setPIDNamespace(String namespace) {
		m_namespace = namespace;
	}
	
	public void setObjectCModels(Set<String> objectCModels) {
		m_objectCModels = objectCModels;
	}

    public boolean handles(String contentType, String packaging) {
        return true;
    }
    
    public String getContentType() {
    	return m_contentType;
    }
    
    public String getPackaging() {
    	return m_packaging;
    }

	public Entry ingestDeposit(DepositRequest deposit, org.fcrepo.server.Context context, DOManager m_mgmt) throws SWORDException {
		
		Entry result;
		
		if (!deposit.isNoOp()) {
			
			try {
				
				String pid = m_mgmt.getNextPID(1, m_namespace)[0];
				
				while (m_mgmt.objectExists(pid)) {
					pid = m_mgmt.getNextPID(1, m_namespace)[0];
				}
				
				String collection = deposit.getCollection();
				String ownerId = "fedoraAdmin;" + deposit.getOnBehalfOf();
				InputStream in = new TemplateInputStream(pid, DEFAULT_LABEL, ownerId);
				
				DOWriter writer = m_mgmt.getIngestWriter(false, context, in, Constants.FOXML1_1.uri, "UTF-8", pid);
				
				writer.addRelationship("info:fedora/" + pid, Constants.RELS_EXT.IS_MEMBER_OF.uri, "info:fedora/" + collection, false, null);
				DigitalObject dObj = writer.getObject();
				
				Set<RelationshipTuple> rels = dObj.getRelationships(SwordConstants.SWORD.SLUG, null);
				
				if (rels.size() > 0) {
					for (RelationshipTuple rel: rels) {
						writer.purgeRelationship(rel.subject, rel.predicate, rel.object, rel.isLiteral, rel.datatype.toString());
					}
				}
				
				writer.getContentModels();
				String subject = PID.toURI(dObj.getPid());

				if (deposit.getSlug() != null) {
				  writer.addRelationship(
                      	subject,
                      	SwordConstants.SWORD.SLUG.uri,
                      	deposit.getSlug(), true, null);
				}

				// add any configured object models to the created object
                for (String cmodel: m_objectCModels) {
                	writer.addRelationship(
                			subject,
                			Constants.MODEL.HAS_MODEL.uri,
                			cmodel,
                			false,
                			null);
                }
                
                // and add whatever membership-indicating rels for the collection
                for (String memberOf: m_rels) {
                	writer.addRelationship(
                			subject,
                			memberOf,
                			PID.toURI(collection),
                			false,
                			null);
                }
				
                DatastreamManagedContent ds = new DatastreamManagedContent();
				
                ds.putContentStream(
						new MIMETypedStream(
								deposit.getContentType(),
								deposit.getFile(),
								new Property[0],
								deposit.getContentLength()));
                
				ds.DatastreamID = DepositHandler.DEPOSIT_DSID;
				ds.DSChecksum = deposit.getMD5();
				if (ds.DSChecksum != null) ds.DSChecksumType = "MD5";
				ds.DSControlGrp = "M";
				ds.DSLabel = deposit.getFileName();
				ds.DSMIME = deposit.getContentType();
				ds.DSCreateDT = new Date();
				
				writer.addDatastream(ds, true);
				writer.commit(DEFAULT_LABEL);
				
				DCFields dcf = Utils.getDCFields(writer);
				result = new Entry(pid);
				result.treatment = DEFAULT_LABEL;
				result.setDCFields(dcf);
				result.setPackaging(m_packaging);
				UriInfo baseUri = deposit.getBaseUri();
				
				URI contentUri =
						baseUri.getBaseUriBuilder().path(FedoraObjectsResource.class, "getObjectProfile")
						.build(pid);
				
				URI descUri = 
						baseUri.getBaseUriBuilder().path(SWORDResource.class, "getDepositEntry")
						.build(collection, pid);
				
				URI mediaUri =
						baseUri.getBaseUriBuilder().path(DatastreamResource.class, "getDatastream")
						.build(pid, DepositHandler.DEPOSIT_DSID);
				
				result.addEditLink(descUri.toString());
				result.addEditMediaLink(mediaUri.toString());
				result.setContent(contentUri.toString(), "text/html");
				
			} catch (ServerException e) {
				throw new SWORDException(SWORDException.FEDORA_ERROR, e);
			}
		} else {
			
			result = new Entry("noOp");
		}
		
		// do some stuff with link
		return result;
	}
	
	/** 
	 * This method is the general method that converts the service document and deposit into a SWORD entry. This is the overall method
	 * so if you want complete control on how the SWORDEntry is created override this method otherwise override the other SWORD Entry methods.
    * 
	 * @param DepositCollection the deposit and its associated collection
	 * @param ServiceDocument the service document associated with this request
	 * @param FedoraObject the object that has been ingested
	 * @throws ServerException 
	 */ 
	protected Entry getSWORDEntry(final DepositRequest pDeposit,
			final ServiceDocument pServiceDocument,
			final DigitalObject pFedoraObj) throws SWORDException, ServerException {
		Entry tEntry = new Entry();
		Collection c = pServiceDocument.workspace.getCollection(pDeposit.getCollection());
		if (c != null) {
			tEntry.treatment = pServiceDocument.workspace.getCollections().get(0).treatment; //TODO find the deposit's collection
		}
        RepositoryInfo repositoryInfo = m_access.describeRepository(ReadOnlyContext.EMPTY);
		tEntry.setDepositInfo(pDeposit);
		Date currentDate = new Date();
		tEntry.setPublished(currentDate);
		tEntry.setUpdated(currentDate);

		tEntry.setGenerator(repositoryInfo.repositoryBaseURL, repositoryInfo.repositoryVersion);
		if (pDeposit.isVerbose()) {
			//tEntry.setVerboseDescription("Your deposit was added to the repository with identifier " + pFedoraObj.getPid() + ". Thank you for depositing.");
		}

		return tEntry;
	}

	@Override
	public Entry getEntry(DepositRequest deposit,
			org.fcrepo.server.Context context,
			DOManager m_mgmt) throws SWORDException {
		try {
			String collectionId = deposit.getCollection();
			String depositId = deposit.getDepositId();
			if (!m_collectionIds.contains(collectionId)) {
				throw new SWORDException(SWORDException.ERROR_REQUEST);
			}
			if (!m_mgmt.objectExists(depositId)) {
				throw new SWORDException(SWORDException.FEDORA_NO_OBJECT);
			}
			DOReader reader = m_mgmt.getReader(false, context, depositId);
			String packaging = Utils.getSwordPackaging(reader);
			Set<RelationshipTuple> rels = reader.getRelationships();
			boolean collectionFound = false;
			String collectionUri = "info:fedora/" + collectionId;
			for (RelationshipTuple rel: rels) {
				if (m_rels.contains(rel.predicate)) {
					collectionFound = (collectionFound || rel.object.equals(collectionUri));
				}
			}
			if (!collectionFound) {
				throw new SWORDException(SWORDException.FEDORA_NO_OBJECT);
			}
			Entry entry = new Entry();
			if (packaging != null) {
				entry.setPackaging(packaging);
			}
			entry.setUpdated(reader.getLastModDate());
			entry.setPublished(reader.getCreateDate());
			DCFields dcf = Utils.getDCFields(reader);
			entry.setDCFields(dcf);
			entry.setId(reader.GetObjectPID());
			URI contentUri =
					deposit.getBaseUri().getBaseUriBuilder().path(FedoraObjectsResource.class, "getObjectProfile")
					.build(depositId);
			URI descUri = 
					deposit.getBaseUri().getBaseUriBuilder().path(SWORDResource.class, "getDepositEntry")
					.build(collectionId, depositId);
			URI mediaUri =
					deposit.getBaseUri().getBaseUriBuilder().path(DatastreamResource.class, "getDatastream")
					.build(depositId, DepositHandler.DEPOSIT_DSID);
			entry.addEditLink(descUri.toString());
			entry.addEditMediaLink(mediaUri.toString());
			entry.setContent(contentUri.toString(), "text/html");

			//TODO Treatment?
			return entry;
		} catch (ServerException e) {
			throw new SWORDException(SWORDException.FEDORA_ERROR, e);
		}
		}

		
}
