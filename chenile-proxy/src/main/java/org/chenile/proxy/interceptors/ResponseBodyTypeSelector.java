package org.chenile.proxy.interceptors;

import org.chenile.base.response.GenericResponse;
import org.chenile.core.context.ChenileExchange;
import org.chenile.core.interceptors.BaseChenileInterceptor;
import org.chenile.proxy.builder.ProxyBuilder;
import org.chenile.service.registry.context.RemoteChenileExchange;
import org.chenile.service.registry.model.ChenileRemoteOperationDefinition;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * This class calculates the correct type for the response body. It will be of type GenericResponse<T> where
 * T is the return type of the underlying service. The underlying service return type can be represented as
 * a ParameterizedType of as a Class object, The treatment differs depending on what got specified
 */
public class ResponseBodyTypeSelector extends BaseChenileInterceptor {

	@Override
	protected void doPreProcessing(ChenileExchange ex) {
		RemoteChenileExchange exchange = (RemoteChenileExchange) ex;
		ChenileRemoteOperationDefinition od = exchange.remoteOperationDefinition;
		if (od.outputAsParameterizedReference != null) {
			ParameterizedTypeReference<?> ref = od.outputAsParameterizedReference;
			ResolvableType rt1 = ResolvableType.forType(ref);
			ResolvableType rt = ResolvableType.forClassWithGenerics(GenericResponse.class, rt1);
			ref = ParameterizedTypeReference.forType(rt.getType());
			exchange.setResponseBodyType(ref);
		} else if (od.output != null) {
            ResolvableType rt = null;
            try {
                rt = ResolvableType.forClassWithGenerics(GenericResponse.class, Class.forName(od.output));
				ParameterizedTypeReference<?> ref = ParameterizedTypeReference.forType(rt.getType());
				exchange.setResponseBodyType(ref);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

		}
	}
}
