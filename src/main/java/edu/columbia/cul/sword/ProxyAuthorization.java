package edu.columbia.cul.sword;

import org.fcrepo.server.Context;
import org.fcrepo.server.errors.authorization.AuthzException;

public interface ProxyAuthorization {

	public void enforceProxy(
			Context context,
			String proxyiedId)
			throws AuthzException;

}
