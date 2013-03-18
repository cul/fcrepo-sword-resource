package edu.columbia.libraries.fcrepo;

import java.io.InputStream;

import org.fcrepo.server.Context;
import org.purl.sword.base.Deposit;

public class FedoraDeposit extends Deposit {
    protected final String m_pid;
    protected final Deposit m_delegate;
    protected final Context m_authzContext;
    
    public FedoraDeposit(Deposit deposit, String pid, Context authzContext) {
    	m_pid = pid;
    	m_delegate = deposit;
    	m_authzContext = authzContext;
    }
    
    public Context getContext(){
    	return m_authzContext;
    }

	public int hashCode() {
		return m_delegate.hashCode();
	}

	public String getUsername() {
		return m_delegate.getUsername();
	}

	public void setUsername(String username) {
		m_delegate.setUsername(username);
	}

	public String getPassword() {
		return m_delegate.getPassword();
	}

	public void setPassword(String password) {
		m_delegate.setPassword(password);
	}

	public int getContentLength() {
		return m_delegate.getContentLength();
	}

	public void setContentLength(int contentLength) {
		m_delegate.setContentLength(contentLength);
	}

	public String getContentType() {
		return m_delegate.getContentType();
	}

	public void setContentType(String contentType) {
		m_delegate.setContentType(contentType);
	}

	public String getDepositID() {
		return m_delegate.getDepositID();
	}

	public void setDepositID(String depositID) {
		m_delegate.setDepositID(depositID);
	}

	public InputStream getFile() {
		return m_delegate.getFile();
	}

	public void setFile(InputStream file) {
		m_delegate.setFile(file);
	}

	public String getPackaging() {
		return m_delegate.getPackaging();
	}

	public void setPackaging(String packaging) {
		m_delegate.setPackaging(packaging);
	}

	public String getMd5() {
		return m_delegate.getMd5();
	}

	public void setMd5(String md5) {
		m_delegate.setMd5(md5);
	}

	public boolean isNoOp() {
		return m_delegate.isNoOp();
	}

	public void setNoOp(boolean noOp) {
		m_delegate.setNoOp(noOp);
	}

	public String getOnBehalfOf() {
		return m_delegate.getOnBehalfOf();
	}

	public void setOnBehalfOf(String onBehalfOf) {
		m_delegate.setOnBehalfOf(onBehalfOf);
	}

	public String getSlug() {
		return m_delegate.getSlug();
	}

	public void setSlug(String slug) {
		m_delegate.setSlug(slug);
	}

	public boolean isVerbose() {
		return m_delegate.isVerbose();
	}

	public void setVerbose(boolean verbose) {
		m_delegate.setVerbose(verbose);
	}

	public String getIPAddress() {
		return m_delegate.getIPAddress();
	}

	public void setIPAddress(String IPAddress) {
		m_delegate.setIPAddress(IPAddress);
	}

	public String getLocation() {
		return m_delegate.getLocation();
	}

	public void setLocation(String location) {
		m_delegate.setLocation(location);
	}

	public String getFilename() {
		return m_delegate.getFilename();
	}

	public void setContentDisposition(String disposition) {
		m_delegate.setContentDisposition(disposition);
	}

	public String getContentDisposition() {
		return m_delegate.getContentDisposition();
	}
	
	public String getDepositPid(){
		return m_pid;
	}
}
