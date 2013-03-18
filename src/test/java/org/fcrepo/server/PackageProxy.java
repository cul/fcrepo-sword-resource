package org.fcrepo.server;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fcrepo.server.config.Parameter;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class PackageProxy {
    public static void setParameters(Parameterized target, List<Parameter> parameters) 
    		throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	Field field = Parameterized.class.getDeclaredField("m_parameters");
    	field.setAccessible(true);
    	Map<String, Parameter> m_parameters = (Map<String, Parameter>) field.get(target);
    	if (m_parameters == null) {
    		field.set(target, new HashMap<String, Parameter>());
    	}
    	target.setParameters(parameters);
    }
    
    public static void setApplicationContext(Server server, GenericApplicationContext context)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	Field field = Server.class.getDeclaredField("m_moduleContext");
    	field.setAccessible(true);
    	field.set(server, context);
    }
}
