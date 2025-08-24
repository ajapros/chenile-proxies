package org.chenile.proxy.builder;

import org.chenile.core.context.HeaderCopier;
import org.chenile.proxy.invoker.LocalProxyInvoker;
import org.chenile.proxy.invoker.ProxyTypeRouter;
import org.chenile.proxy.invoker.RemoteProxyInvoker;
import org.chenile.proxy.utils.ProxyUtils;
import org.chenile.service.registry.model.ChenileRemoteOperationDefinition;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Creates a proxy for a chenile interface. A  proxy allows the
 * clients to avail the benefits of interception, transformation, caching etc.
 * The chain of execution includes a router that determine if a local or remote http
 * proxy must be used to invoke the actual service
 * 
 * @author Raja Shankar Kolluru
 *
 */
public class ProxyBuilder {
	public static final String REMOTE_URL_BASE = "REMOTE_URL_BASE";
	public static final String INVOCATION_METHOD = "INVOCATION_METHOD";
	/**
	 * Build proxy for an interface. The headers from the current request are copied using
	 * the headerCopier. The service name is the service which is being proxied
	 * @param <T> - the interface represented by a generic
	 * @param interfaceToProxy the interface that is implemented by the service
	 * @param serviceName service name of the chenile service
	 * @param headerCopier any special treatment of header parameters
	 * @return the mock proxy that implements the interface
	 */
	public <T> T buildProxy(Class<T> interfaceToProxy, String serviceName, HeaderCopier headerCopier,
							String baseUrl) {
		return buildProxy(interfaceToProxy,serviceName,headerCopier,ProxyMode.COMPUTE_DYNAMICALLY,baseUrl);
	}
	/**
	 * This is used for testing purposes. For production, please make sure that 
	 * proxy mode is set to COMPUTE_DYNAMICALLY
	 * @param <T> Interface to proxy class
	 * @param interfaceToProxy  the interface that needs to be proxied
	 * @param serviceName - name of the service
	 * @param headerCopier this copies headers from the source to the target CHenile exchange
	 * @param proxyMode - can be set to always proxy local or remote for testing purposes
	 * @return the proxy that can be used in lieu of the service
	 */
	public <T> T buildProxy(Class<T> interfaceToProxy, String serviceName, HeaderCopier headerCopier,
			ProxyMode proxyMode, String baseUrl) {
		ProxyClass proxyClass = new ProxyClass(interfaceToProxy,serviceName,headerCopier,proxyMode, baseUrl);
		@SuppressWarnings("unchecked")
		T proxy = (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
				new Class[] { interfaceToProxy }, proxyClass);
		return proxy;
	}
	
	public static enum ProxyMode {
		LOCAL, /* Always use local. testing only */
		REMOTE, /* Always use remote. testing only */
		COMPUTE_DYNAMICALLY /* Compute if it is local or remote - must be used in prod */
	}

	@Autowired
	private ProxyUtils proxyUtils;
	@Autowired
	ProxyTypeRouter proxyTypeRouter;
	@Autowired
	LocalProxyInvoker localProxyInvoker;
	@Autowired
	RemoteProxyInvoker remoteProxyInvoker;

	private class ProxyClass implements InvocationHandler {
		private final String serviceName;
		private final HeaderCopier headerCopier;
		private final Class<?> interfaceToProxy;
		private final ProxyMode proxyMode;
		private final String baseUrl;

		public ProxyClass(Class<?> interfaceToProxy, String serviceName, HeaderCopier headerCopier,
				ProxyMode proxyMode, String baseUrl) {
			this.headerCopier = headerCopier;
			this.serviceName = serviceName;
			this.interfaceToProxy = interfaceToProxy;
			this.proxyMode = proxyMode;
			this.baseUrl = baseUrl;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// invoke the methods from this class itself if they are not declared in the interface
			// that we are trying to proxy.
			if (!method.getDeclaringClass().equals(interfaceToProxy)){
				return method.invoke(this, args);
			}

			ChenileRemoteServiceDefinition serviceDefinition = proxyUtils.findService(serviceName);
			ChenileRemoteOperationDefinition operationDefinition = proxyUtils.
					findOperationInService(serviceDefinition,method.getName());
			if (proxyTypeRouter.isLocal(proxyMode,serviceDefinition)){
				return localProxyInvoker.invoke(serviceDefinition,operationDefinition,headerCopier,args);
			}else
				return remoteProxyInvoker.invoke(serviceDefinition,operationDefinition,
						headerCopier,method, args,baseUrl);
		}
		
		public String toString() {
			return "ProxyBuilder.Proxy." + serviceName ;
		}

	}


}
