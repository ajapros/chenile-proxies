package org.chenile.proxy.invoker;

import org.chenile.core.context.ChenileExchange;
import org.chenile.core.context.EntryPointSubType;
import org.chenile.owiz.OrchExecutor;
import org.chenile.proxy.builder.ProxyBuilder;
import org.chenile.proxy.utils.ProxyUtils;
import org.chenile.service.registry.context.RemoteChenileExchange;
import org.chenile.service.registry.model.ChenileRemoteOperationDefinition;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RemoteProxyInvokerTest {

	@Test
	public void invokeMarksExchangeAsRemoteProxyBeforeExecutingProxyFlow() throws Exception {
		RemoteProxyInvoker invoker = new RemoteProxyInvoker();
		StubProxyUtils proxyUtils = new StubProxyUtils();
		RecordingOrchExecutor orchExecutor = new RecordingOrchExecutor();
		ChenileRemoteServiceDefinition serviceDefinition = new ChenileRemoteServiceDefinition();
		ChenileRemoteOperationDefinition operationDefinition = new ChenileRemoteOperationDefinition();
		operationDefinition.name = "increment";
		operationDefinition.params = java.util.List.of();
		Method method = SampleService.class.getMethod("increment");

		ReflectionTestUtils.setField(invoker, "proxyUtils", proxyUtils);
		ReflectionTestUtils.setField(invoker, "chenileProxyOrchExecutor", orchExecutor);

		invoker.invoke(serviceDefinition, operationDefinition, null, method, null, "localhost:8090");

		assertEquals(EntryPointSubType.REMOTE_PROXY, orchExecutor.exchange.getEntryPointSubType());
		assertTrue(orchExecutor.exchange.isProxyInvocation());
		assertEquals("localhost:8090", orchExecutor.exchange.getHeader(ProxyBuilder.REMOTE_URL_BASE));
		assertEquals(method, orchExecutor.exchange.getHeader(ProxyBuilder.INVOCATION_METHOD));
	}

	private interface SampleService {
		void increment();
	}

	private static class StubProxyUtils extends ProxyUtils {
		private final RemoteChenileExchange exchange = new RemoteChenileExchange();

		@Override
		public RemoteChenileExchange makeRemoteExchange(ChenileRemoteServiceDefinition serviceDefinition,
				ChenileRemoteOperationDefinition operationDefinition, org.chenile.core.context.HeaderCopier headerCopier) {
			exchange.remoteServiceDefinition = serviceDefinition;
			exchange.remoteOperationDefinition = operationDefinition;
			return exchange;
		}

		@Override
		public void populateArgs(ChenileExchange exchange, Object[] args, ChenileRemoteOperationDefinition od) {
		}
	}

	private static class RecordingOrchExecutor implements OrchExecutor<ChenileExchange> {
		private ChenileExchange exchange;

		@Override
		public void execute(ChenileExchange input) {
			exchange = input;
		}

		@Override
		public void execute(String flowName, ChenileExchange input) {
			exchange = input;
		}
	}
}
