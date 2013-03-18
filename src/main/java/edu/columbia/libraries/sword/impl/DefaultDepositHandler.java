package edu.columbia.libraries.sword.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.fcrepo.common.Constants;
import org.fcrepo.server.Context;
import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.management.Management;
import org.fcrepo.server.storage.DOManager;
import org.fcrepo.server.storage.DOWriter;
import org.fcrepo.server.storage.types.BasicDigitalObject;
import org.fcrepo.server.storage.types.DigitalObject;
import org.purl.sword.base.SWORDEntry;
import org.purl.sword.base.SWORDException;
import org.purl.sword.base.ServiceDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.libraries.fcrepo.FedoraDeposit;
import edu.columbia.libraries.sword.DepositHandler;


public class DefaultDepositHandler implements DepositHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultDepositHandler.class);
	
	public static String DEFAULT_LABEL = "Object created via SWORD Deposit";
	
	protected String m_contentType = "";
	
	protected String m_packaging = "";
	
	protected String m_namespace = "sword"; //default
	
	private DOManager m_mgmt;
	
	private Management m_management;
	
	public DefaultDepositHandler(DOManager mgmt) {
		m_mgmt = mgmt;
	}
	
	public void setPIDNamespace(String namespace) {
		m_namespace = namespace;
	}

    public boolean handles(String contentType, String packaging) {
        return true;
    }

	public SWORDEntry ingestDeposit(FedoraDeposit deposit,
			ServiceDocument serviceDocument, Context context) throws SWORDException {
		BasicDigitalObject obj = new BasicDigitalObject();
		obj.setNew(true);
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
				dObj.
				writer.commit("Ingest from SWORD");
				
			} catch (ServerException e) {
				throw new SWORDException(e.getMessage(), e);
			}
		}
		SWORDEntry result = new SWORDEntry();
		// do some stuff
		result.addLink(link)
		return result;
	}
	
	public static String getCurrentDate(boolean millis){
		
		DateFormat df = (millis) ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
		                         : new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		return df.format(new Date());
	}
	
}
