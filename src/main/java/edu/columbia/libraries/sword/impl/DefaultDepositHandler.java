package edu.columbia.libraries.sword.impl;

import java.io.InputStream;
import java.net.URI;
import java.util.Date;

import javax.ws.rs.core.UriInfo;

import org.fcrepo.common.Constants;
import org.fcrepo.server.Context;
import org.fcrepo.server.ReadOnlyContext;
import org.fcrepo.server.access.Access;
import org.fcrepo.server.access.RepositoryInfo;
import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.management.Management;
import org.fcrepo.server.rest.DatastreamResource;
import org.fcrepo.server.rest.FedoraObjectsResource;
import org.fcrepo.server.storage.DOManager;
import org.fcrepo.server.storage.DOWriter;
import org.fcrepo.server.storage.types.BasicDigitalObject;
import org.fcrepo.server.storage.types.Datastream;
import org.fcrepo.server.storage.types.DatastreamManagedContent;
import org.fcrepo.server.storage.types.DigitalObject;
import org.fcrepo.server.storage.types.MIMETypedStream;
import org.fcrepo.server.storage.types.Property;
import org.fcrepo.server.utilities.DCFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.libraries.sword.DepositHandler;
import edu.columbia.libraries.sword.SWORDException;
import edu.columbia.libraries.sword.SWORDResource;
import edu.columbia.libraries.sword.xml.entry.Content;
import edu.columbia.libraries.sword.xml.entry.Entry;
import edu.columbia.libraries.sword.xml.service.Collection;
import edu.columbia.libraries.sword.xml.service.ServiceDocument;


public class DefaultDepositHandler implements DepositHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultDepositHandler.class);
	
	public static String DEFAULT_LABEL = "Object created via SWORD Deposit";
	
	protected String m_contentType = "";
	
	protected String m_packaging = "";
	
	protected String m_namespace = "sword"; //default
	
	private DOManager m_mgmt;
	
	private UriInfo m_uriInfo;
	
	private Access m_access;
	
	public DefaultDepositHandler(DOManager mgmt, UriInfo uriInfo) throws ServerException {
		m_mgmt = mgmt;
		m_uriInfo = uriInfo;
	}
	
	public void setPIDNamespace(String namespace) {
		m_namespace = namespace;
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

	public Entry ingestDeposit(DepositRequest deposit,
			Context context) throws SWORDException {
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
				
				dObj.setExtProperty("org.purl.sword.slug", deposit.getSlug());
				DatastreamManagedContent ds = new DatastreamManagedContent();
				ds.putContentStream(
						new MIMETypedStream(
								deposit.getContentType(),
								deposit.getFile(),
								new Property[0],
								deposit.getContentLength()));
				ds.DatastreamID = DepositHandler.DEPOSIT_DSID;
				ds.DSChecksum = deposit.getMD5();
				ds.DSChecksumType = "MD5";
				ds.DSControlGrp = "M";
				ds.DSLabel = deposit.getFileName();
				ds.DSMIME = deposit.getContentType();
				ds.DSCreateDT = new Date();
				writer.addDatastream(ds, true);
				writer.commit(DEFAULT_LABEL);
				DCFields dcf = new DCFields(writer.GetDatastream("DC", null).getContentStream());
				result = new Entry(pid);
				result.treatment = DEFAULT_LABEL;
				result.setDCFields(dcf);
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

		
}
