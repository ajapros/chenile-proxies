package org.chenile.proxy.invoker;

import org.chenile.base.response.GenericResponse;
import org.chenile.core.context.ChenileExchange;
import org.chenile.core.context.ChenileExchangeBuilder;
import org.chenile.core.context.ContextContainer;
import org.chenile.core.context.HeaderCopier;
import org.chenile.core.entrypoint.ChenileEntryPoint;
import org.chenile.proxy.utils.ProxyUtils;
import org.chenile.service.registry.model.ChenileRemoteOperationDefinition;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Invokes the Chenile Entry point locally since this service is local.
 * @author Raja Shankar Kolluru
 *
 */
public class LocalProxyInvoker{
	@Autowired
	ChenileExchangeBuilder exchangeBuilder;
	@Autowired
	private ChenileEntryPoint chenileEntryPoint;
	@Autowired
	ProxyUtils proxyUtils;
	@Autowired
	ContextContainer contextContainer;

	public Object invoke(ChenileRemoteServiceDefinition serviceDefinition,
					   ChenileRemoteOperationDefinition operationDefinition,
					   HeaderCopier headerCopier,
					   Object[] args) throws Exception {
		ContextContainer.ContextSnapshot parentContext = contextContainer.snapshot();
		ChenileExchange exchange = exchangeBuilder.makeExchange(serviceDefinition.serviceId,
				operationDefinition.name,headerCopier);
		exchange.setLocalInvocation(true);
		proxyUtils.populateArgs(exchange,args,operationDefinition);
		try {
			chenileEntryPoint.execute(exchange);
			if (exchange.getException() != null) {
				throw exchange.getException();
			}
		} finally {
			contextContainer.restore(parentContext);
		}
		GenericResponse<?> resp = (GenericResponse<?>)exchange.getResponse();
		return resp.getData();
	}
}
