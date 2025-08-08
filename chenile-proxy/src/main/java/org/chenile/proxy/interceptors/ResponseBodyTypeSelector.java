package org.chenile.proxy.interceptors;

import org.chenile.base.response.GenericResponse;
import org.chenile.core.context.ChenileExchange;
import org.chenile.core.interceptors.BaseChenileInterceptor;
import org.chenile.proxy.builder.ProxyBuilder;
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
		Method m = ex.getHeader(ProxyBuilder.INVOCATION_METHOD,Method.class);
		Type a = m.getGenericReturnType();
		ResolvableType rt1 = ResolvableType.forType(a);
		ResolvableType rt = ResolvableType.forClassWithGenerics(GenericResponse.class, rt1);
		ParameterizedTypeReference<?> ref = ParameterizedTypeReference.forType(rt.getType());
		ex.setResponseBodyType(ref);
	}
}
