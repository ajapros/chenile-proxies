package org.chenile.proxy.interceptors.interpolations;

import org.chenile.owiz.Command;
import org.chenile.owiz.impl.InterpolationCommand;
import org.chenile.proxy.utils.ProxyUtils;
import org.chenile.service.registry.context.RemoteChenileExchange;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Interpolates all the operation specific processors on the client side for Chenile Proxy
 */
public class OperationSpecificClientProcessorsInterpolation extends InterpolationCommand<RemoteChenileExchange> {
	@Autowired
	ProxyUtils proxyUtils;
	@Override
	protected List<Command<RemoteChenileExchange>> fetchCommands(RemoteChenileExchange exchange) {
		return proxyUtils.makeInterceptorList(exchange.remoteOperationDefinition);
	}
}
