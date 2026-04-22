package org.chenile.proxy.invoker;

import org.chenile.base.response.GenericResponse;
import org.chenile.core.context.ChenileExchange;
import org.chenile.core.context.ChenileExchangeBuilder;
import org.chenile.core.context.ContextContainer;
import org.chenile.core.context.EntryPointSubType;
import org.chenile.core.context.HeaderCopier;
import org.chenile.core.entrypoint.ChenileEntryPoint;
import org.chenile.proxy.utils.ProxyUtils;
import org.chenile.service.registry.model.ChenileRemoteOperationDefinition;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LocalProxyInvokerTest {

	@Test
	public void invokeMarksExchangeAsLocalBeforeExecutingEntryPoint() throws Exception {
		LocalProxyInvoker invoker = new LocalProxyInvoker();
		StubExchangeBuilder exchangeBuilder = new StubExchangeBuilder();
		RecordingEntryPoint chenileEntryPoint = new RecordingEntryPoint();
		NoOpProxyUtils proxyUtils = new NoOpProxyUtils();
		ContextContainer contextContainer = ContextContainer.getInstance();
		ChenileRemoteServiceDefinition serviceDefinition = new ChenileRemoteServiceDefinition();
		serviceDefinition.serviceId = "fooService";
		ChenileRemoteOperationDefinition operationDefinition = new ChenileRemoteOperationDefinition();
		operationDefinition.name = "increment";
		operationDefinition.params = java.util.List.of();
		GenericResponse<String> response = new GenericResponse<>();
		response.setData("ok");
		exchangeBuilder.exchange.setResponse(response);

		ReflectionTestUtils.setField(invoker, "exchangeBuilder", exchangeBuilder);
		ReflectionTestUtils.setField(invoker, "chenileEntryPoint", chenileEntryPoint);
		ReflectionTestUtils.setField(invoker, "proxyUtils", proxyUtils);
		ReflectionTestUtils.setField(invoker, "contextContainer", contextContainer);

		try {
			Object result = invoker.invoke(serviceDefinition, operationDefinition, null, null);
			assertTrue(chenileEntryPoint.recordedExchange.isLocalInvocation());
			assertEquals(EntryPointSubType.LOCAL_PROXY, chenileEntryPoint.recordedExchange.getEntryPointSubType());
			assertTrue(chenileEntryPoint.recordedExchange.isProxyInvocation());
			assertEquals("ok", result);
		} finally {
			contextContainer.clear();
		}
	}

	private static class StubExchangeBuilder extends ChenileExchangeBuilder {
		private final ChenileExchange exchange = new ChenileExchange();

		@Override
		public ChenileExchange makeExchange(String serviceName, String operationName, HeaderCopier headerCopier) {
			return exchange;
		}
	}

	private static class RecordingEntryPoint extends ChenileEntryPoint {
		private ChenileExchange recordedExchange;

		@Override
		public void execute(ChenileExchange exchange) {
			recordedExchange = exchange;
		}
	}

	private static class NoOpProxyUtils extends ProxyUtils {
		@Override
		public void populateArgs(ChenileExchange exchange, Object[] args, ChenileRemoteOperationDefinition od) {
		}
	}
}
