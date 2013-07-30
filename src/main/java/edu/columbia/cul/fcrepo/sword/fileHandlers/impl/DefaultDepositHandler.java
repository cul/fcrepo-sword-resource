package edu.columbia.cul.fcrepo.sword.fileHandlers.impl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.fcrepo.common.Constants;
import org.fcrepo.common.PID;
import org.fcrepo.server.ReadOnlyContext;
import org.fcrepo.server.access.Access;
import org.fcrepo.server.access.RepositoryInfo;
import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.storage.DOManager;
import org.fcrepo.server.storage.DOWriter;
import org.fcrepo.server.storage.types.DatastreamManagedContent;
import org.fcrepo.server.storage.types.DigitalObject;
import org.fcrepo.server.storage.types.MIMETypedStream;
import org.fcrepo.server.storage.types.Property;
import org.fcrepo.server.storage.types.RelationshipTuple;
import org.fcrepo.server.utilities.DCFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.cul.fcrepo.Utils;
import edu.columbia.cul.fcrepo.sword.FedoraUtils;
import edu.columbia.cul.fcrepo.sword.fileHandlers.DepositHandler;
import edu.columbia.cul.sword.SwordConstants;
import edu.columbia.cul.sword.exceptions.SWORDException;
import edu.columbia.cul.sword.holder.SwordSessionStructure;
import edu.columbia.cul.sword.impl.DepositRequest;
import edu.columbia.cul.sword.impl.TemplateInputStream;
import edu.columbia.cul.sword.xml.entry.Entry;
import edu.columbia.cul.sword.xml.service.Collection;
import edu.columbia.cul.sword.xml.service.ServiceDocument;


public class DefaultDepositHandler implements DepositHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDepositHandler.class);	
	public static String DEFAULT_LABEL = "Object created via SWORD Deposit";	
	
	protected String m_contentType = "text/html";	
	protected String m_packaging;	
	protected String m_namespace = "sword"; //default		
	private Set<String> m_rels; 	
	private Set<String> m_objectCModels = Collections.emptySet();
	private Access m_access;
	
	public void setPIDNamespace(String namespace) {
		m_namespace = namespace;
	}
	
	public void setObjectCModels(Set<String> objectCModels) {
		m_objectCModels = objectCModels;
	}
    
    public String getContentType() {
    	return m_contentType;
    }
    
    public String getPackaging() {
    	return m_packaging;
    }
    
	public Entry ingestDeposit(SwordSessionStructure swordSession, DOManager doManager) throws SWORDException {
		
		Entry resultEntry = null;
		
//		if (!swordSession.noOp) {
			
			try {
				
				String pid = doManager.getNextPID(1, m_namespace)[0];

				while (doManager.objectExists(pid)) {
					pid = doManager.getNextPID(1, m_namespace)[0];
				}
				
				String ownerId = "fedoraAdmin;" + (swordSession.httpHeader.onBehalfOf != null ? " " + swordSession.httpHeader.onBehalfOf : "");
				
				InputStream in = new TemplateInputStream(pid, DEFAULT_LABEL, ownerId);
				
				DOWriter writer = doManager.getIngestWriter(false, swordSession.fedoraContext, in, Constants.FOXML1_1.uri, "UTF-8", pid);
				
				
				addRelationship(pid, writer);
				
				DigitalObject dObj = writer.getObject();
				
				Set<RelationshipTuple> rels = dObj.getRelationships(SwordConstants.SWORD.SLUG, null);
				
				if (rels.size() > 0) {
					for (RelationshipTuple rel: rels) {
						writer.purgeRelationship(rel.subject, rel.predicate, rel.object, rel.isLiteral, rel.datatype.toString());
					}
				}
				
				writer.getContentModels();
				String subject = PID.toURI(dObj.getPid());

				if (swordSession.httpHeader.slug != null) {
				  writer.addRelationship(
                      	subject,
                      	SwordConstants.SWORD.SLUG.uri,
                      	swordSession.httpHeader.slug, true, null);
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
                			PID.toURI(swordSession.collectionId),
                			false,
                			null);
                }
				
                DatastreamManagedContent ds = new DatastreamManagedContent();
				
                ds.putContentStream(
						new MIMETypedStream(
								swordSession.httpHeader.contentType,
								new FileInputStream(swordSession.tempFile),
								new Property[0],
								swordSession.contentLength));
                
				ds.DatastreamID = DepositHandler.DEPOSIT_DSID;
				ds.DSChecksum = swordSession.httpHeader.md5;
				if (ds.DSChecksum != null) ds.DSChecksumType = "MD5";
				ds.DSControlGrp = "M";
				ds.DSLabel = swordSession.fileName;
				ds.DSMIME = swordSession.httpHeader.contentType;
				ds.DSCreateDT = new Date();
				
				swordSession.depositId = pid;
				writer.addDatastream(ds, true);
				
				
				if(swordSession.noOp) {
					DCFields dcf = Utils.getDCFields(writer);
					resultEntry = FedoraUtils.makeEntry(swordSession, dcf);
				}else{
					
					writer.commit(DEFAULT_LABEL);
					resultEntry = FedoraUtils.makeEntry(swordSession, doManager);
				}

			} catch (Exception e) {
				throw new SWORDException(SWORDException.FEDORA_ERROR, e);
			}
//		} else {
//			resultEntry = new Entry("noOp");
//		}
		
		// do some stuff with link
		return resultEntry;
	}    


	protected void addRelationship(String pid, DOWriter writer) throws ServerException {
		
		if(m_contentType != null) {
			writer.addRelationship("info:fedora/" + pid, SwordConstants.SWORD.CONTENT_TYPE.uri, "contentType:" + m_contentType, false, null);
		}
		
		if(m_packaging != null) {
			writer.addRelationship("info:fedora/" + pid, SwordConstants.SWORD.PACKAGING.uri, m_packaging, false, null);
		}
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


	public void setRels(Set<String> rels){
		this.m_rels = rels;
	}
	
	
} // ================================================ //
