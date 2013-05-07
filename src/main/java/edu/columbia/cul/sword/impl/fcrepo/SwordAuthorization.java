package edu.columbia.cul.sword.impl.fcrepo;

import java.util.Map;

import org.fcrepo.common.Constants;
import org.fcrepo.server.Context;
import org.fcrepo.server.MultiValueMap;
import org.fcrepo.server.Server;
import org.fcrepo.server.errors.ModuleInitializationException;
import org.fcrepo.server.errors.authorization.AuthzException;
import org.fcrepo.server.errors.authorization.AuthzOperationalException;
import org.fcrepo.server.security.DefaultAuthorization;
import org.fcrepo.server.security.PolicyEnforcementPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.cul.sword.ProxyAuthorization;
import edu.columbia.cul.sword.SwordConstants;

public class SwordAuthorization 
       extends DefaultAuthorization
       implements ProxyAuthorization {

	private static final Logger LOGGER = LoggerFactory.getLogger(SwordAuthorization.class);
    // Unfortunately, the reference to the PEP bean in the superclass is private,
	// so it must be shadowed here
	private PolicyEnforcementPoint xacmlPep;

    public SwordAuthorization(Map<String, String> moduleParameters,
			Server server, String role) throws ModuleInitializationException {
		super(moduleParameters, server, role);
	}
	
	@Override
	public void postInitModule() throws ModuleInitializationException {
		super.postInitModule();
        xacmlPep = getServer().getBean(PolicyEnforcementPoint.class.getName(), PolicyEnforcementPoint.class);
	}

	public final void enforceProxy(Context context,
			String proxyiedId)
					throws AuthzException {
		try {
			LOGGER.debug("Entered enforceProxy: {} attempting to proxy for {}",
					context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri),
					proxyiedId);
			String target = SwordConstants.SWORD.PROXY.uri;
            MultiValueMap actionAttributes = new MultiValueMap();
            String name = "";

            try {
                name = actionAttributes
                        .setReturn(SwordConstants.SWORD.ON_BEHALF_OF.uri,
                        		proxyiedId);
                context.setActionAttributes(actionAttributes);
            } catch (Exception e) {
                context.setActionAttributes(null);
                throw new AuthzOperationalException(target + " couldn't set "
                                                    + name, e);
            }
			context.setResourceAttributes(null);
			xacmlPep
			.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri),
					target,
					Constants.ACTION.APIM.uri,
					"",
					"",
					context);
		} finally {
			LOGGER.debug("Exiting enforceProxy");
		}
	}


}
