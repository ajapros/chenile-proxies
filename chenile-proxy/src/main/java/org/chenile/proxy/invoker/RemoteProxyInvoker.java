package org.chenile.proxy.invoker;

import org.chenile.core.context.ChenileExchange;
import org.chenile.core.context.HeaderCopier;
import org.chenile.owiz.OrchExecutor;
import org.chenile.proxy.builder.ProxyBuilder;
import org.chenile.proxy.utils.ProxyUtils;
import org.chenile.service.registry.context.RemoteChenileExchange;
import org.chenile.service.registry.model.ChenileRemoteOperationDefinition;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.reflect.Method;

public class RemoteProxyInvoker {
    @Autowired
    ProxyUtils proxyUtils;
    @Autowired @Qualifier("chenileProxyOrchExecutor")
    private OrchExecutor<ChenileExchange> chenileProxyOrchExecutor;

    public Object invoke(ChenileRemoteServiceDefinition serviceDefinition,
                       ChenileRemoteOperationDefinition operationDefinition,
                       HeaderCopier headerCopier,
                       Method method,
                       Object[] args,
                       String baseUrl) throws Exception {
        RemoteChenileExchange exchange = proxyUtils.makeRemoteExchange(
                serviceDefinition, operationDefinition, headerCopier);

        proxyUtils.populateArgs(exchange, args,operationDefinition);
        exchange.setHeader(ProxyBuilder.REMOTE_URL_BASE, baseUrl);
        exchange.setHeader(ProxyBuilder.INVOCATION_METHOD, method);
        chenileProxyOrchExecutor.execute(exchange);
        if (exchange.getException() != null) {
            throw exchange.getException();
        }
        return exchange.getResponse();
    }
}
