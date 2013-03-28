package edu.columbia.libraries.sword.impl;

import java.io.InputStream;
import java.util.Date;

import org.fcrepo.common.Constants;
import org.fcrepo.server.Context;
import org.fcrepo.server.ReadOnlyContext;
import org.fcrepo.server.access.Access;
import org.fcrepo.server.access.RepositoryInfo;
import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.management.Management;
import org.fcrepo.server.storage.DOManager;
import org.fcrepo.server.storage.DOWriter;
import org.fcrepo.server.storage.types.BasicDigitalObject;
import org.fcrepo.server.storage.types.DigitalObject;
import org.fcrepo.server.utilities.DCFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.libraries.fcrepo.FedoraDeposit;
import edu.columbia.libraries.sword.DepositHandler;
import edu.columbia.libraries.sword.SWORDException;
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
	
	private Management m_management;
	
	private Access m_access;
	
	public DefaultDepositHandler(DOManager mgmt) {
		m_mgmt = mgmt;
	}
	
	public void setPIDNamespace(String namespace) {
		m_namespace = namespace;
	}

    public boolean handles(String contentType, String packaging) {
        return true;
    }

	public Entry ingestDeposit(FedoraDeposit deposit,
			ServiceDocument serviceDocument, Context context) throws SWORDException {
		BasicDigitalObject obj = new BasicDigitalObject();
		obj.setNew(true);
		Entry result;
		if (!deposit.isNoOp()) {
			try {
				String pid = m_mgmt.getNextPID(1, m_namespace)[0];
				while (m_mgmt.objectExists(pid)) {
					pid = m_mgmt.getNextPID(1, m_namespace)[0];
				}
				String ownerId = "fedoraAdmin;" + deposit.getOnBehalfOf();
				InputStream in = new TemplateInputStream(pid, DEFAULT_LABEL, ownerId);
				DOWriter writer = m_mgmt.getIngestWriter(false, context, in, Constants.FOXML1_1.uri, "UTF-8", pid);
				writer.addRelationship("info:fedora/" + pid, Constants.RELS_EXT.IS_MEMBER_OF.uri, "info:fedora/" + deposit.getDepositPid(), false, null);
				DigitalObject dObj = writer.getObject();
				dObj.setExtProperty("org.purl.sword.slug", deposit.getSlug());
				writer.commit("Ingest from SWORD");
				DCFields dcf = new DCFields(writer.GetDatastream("DC", null).getContentStream());
				result = new Entry(pid);
				result.setDCFields(dcf);
				String uri = "baseuri" + deposit.getDepositPid() + "/" + pid;
				result.addDescriptionLink(uri);
				result.addMediaLink(uri + "/content");
			} catch (ServerException e) {
				throw new SWORDException(e.getMessage(), e);
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
	protected Entry getSWORDEntry(final FedoraDeposit pDeposit,
			final ServiceDocument pServiceDocument,
			final DigitalObject pFedoraObj) throws SWORDException, ServerException {
		Entry tEntry = new Entry();
		Collection c = pServiceDocument.workspace.getCollection(pDeposit.getDepositPid());
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
