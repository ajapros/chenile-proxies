package org.chenile.proxy.utils;

import org.chenile.core.context.ChenileExchange;
import org.chenile.core.context.HeaderCopier;
import org.chenile.owiz.Command;
import org.chenile.service.registry.context.RemoteChenileExchange;
import org.chenile.service.registry.model.ChenileRemoteOperationDefinition;
import org.chenile.service.registry.model.ChenileRemoteParamDefinition;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.chenile.service.registry.service.ServiceRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Builds an exchange from a service name and an operation name
 */
public class ProxyUtils {
	@Autowired
	ServiceRegistryService serviceRegistry;
	@Autowired
	ApplicationContext applicationContext;

	public ChenileRemoteServiceDefinition findService(String serviceName) {
		return serviceRegistry.retrieveById(serviceName);
	}
	
	public ChenileRemoteOperationDefinition findOperationInService(ChenileRemoteServiceDefinition serviceDefinition, String opName) {
		for (ChenileRemoteOperationDefinition od: serviceDefinition.operations) {
			if (od.name.equals(opName)){
				return od;
			}
		}
		return null;
	}
	
	public RemoteChenileExchange makeRemoteExchange(ChenileRemoteServiceDefinition serviceDefinition,
				  ChenileRemoteOperationDefinition operationDefinition, HeaderCopier headerCopier) {
		RemoteChenileExchange exchange = new RemoteChenileExchange();
		exchange.remoteServiceDefinition = serviceDefinition;
		exchange.remoteOperationDefinition = operationDefinition;
		if (headerCopier != null) headerCopier.copy(exchange);
		return exchange;
	}


	@SuppressWarnings("unchecked")
	public void populateArgs(ChenileExchange exchange, Object[] args, ChenileRemoteOperationDefinition od) {
		if(args == null) return;
		exchange.setApiInvocation(Arrays.asList(args));
		// Size of the args and od.getParams() will be identical
		for (int index = 0; index < od.params.size(); index++) {
			ChenileRemoteParamDefinition pd = od.params.get(index);
			Object arg = args[index];
			switch (pd.type) {
				case HEADER:
					exchange.setHeader(pd.name, arg);
					break;
				case BODY:
					exchange.setBody(arg);
					break;
				case HEADERS:
					exchange.setHeaders((Map<String, Object>)arg);
					break;
				default:
					break;
			}
		}
	}

	public List<Command<RemoteChenileExchange>> makeInterceptorList(ChenileRemoteOperationDefinition od){
		if (od.clientInterceptors != null) {
			return  od.clientInterceptors;
		}
		List<Command<RemoteChenileExchange>> list = makeInterceptorList(od.clientInterceptorNames);
		od.clientInterceptors = list;
		return list;
	}

	public List<Command<RemoteChenileExchange>> makeInterceptorList(ChenileRemoteServiceDefinition sd){
		if (sd.clientInterceptors != null) {
			return sd.clientInterceptors;
		}
		List<Command<RemoteChenileExchange>> list = makeInterceptorList(sd.clientInterceptorNames);
		sd.clientInterceptors = list;
		return list;
	}

	@SuppressWarnings("unchecked")
	public List<Command<RemoteChenileExchange>> makeInterceptorList(List<String> componentNames){
		if (componentNames == null) return List.of();
		List<Command<RemoteChenileExchange>> commands = new ArrayList<>();
		for (String componentName: componentNames){
			commands.add((Command<RemoteChenileExchange>)applicationContext.getBean(componentName));
		}
		return commands;
	}

}
