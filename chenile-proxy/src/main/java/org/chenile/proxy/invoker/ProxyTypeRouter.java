package org.chenile.proxy.invoker;

import org.chenile.core.model.ChenileConfiguration;
import org.chenile.proxy.builder.ProxyBuilder.ProxyMode;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Figures out if this should be a local invocation
 * @author Raja Shankar Kolluru
 *
 */
public class ProxyTypeRouter {
	@Autowired ChenileConfiguration chenileConfiguration;
	
	public boolean isLocal(ProxyMode mode, ChenileRemoteServiceDefinition serviceDefinition) {
		if (mode.equals(ProxyMode.COMPUTE_DYNAMICALLY))
			mode = computeDynamic(serviceDefinition);
		return mode.equals(ProxyMode.LOCAL);
	}
	
	private ProxyMode computeDynamic(ChenileRemoteServiceDefinition serviceDefinition) {
		if(chenileConfiguration.getModuleName().equals(serviceDefinition.moduleName))
			return ProxyMode.LOCAL;
		else
			return ProxyMode.REMOTE;
	}
}
