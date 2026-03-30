package org.chenile.proxy.interceptors;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;

import org.chenile.base.exception.ErrorNumException;
import org.chenile.base.exception.ServerException;
import org.chenile.base.response.GenericResponse;
import org.chenile.core.context.ChenileExchange;
import org.chenile.service.registry.context.RemoteChenileExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * This communicates with SpringRestTemplate to find the errors from the returned response.
 */
public class ChenileResponseHandler extends DefaultResponseErrorHandler{
	private static final Logger log = LoggerFactory.getLogger(ChenileResponseHandler.class);

	private final RemoteChenileExchange chenileExchange;
	private final ObjectMapper objectMapper;
	public ChenileResponseHandler(RemoteChenileExchange exchange, ObjectMapper objectMapper) {
		this.chenileExchange = exchange;
		this.objectMapper = objectMapper;
	}
	
	@Override
	public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
		return !(httpResponse.getStatusCode().is2xxSuccessful());
	}

	@Override
	public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
		HttpStatus statusCode = HttpStatus.resolve(response.getStatusCode().value());
		if (statusCode != null) {
			byte[] body = getResponseBody(response);
			log.warn("Remote service {} returned {} {} for {} {}", getEndpointName(),
					response.getStatusCode().value(), response.getStatusText(), method, url);
			if (body.length == 0) {
				RuntimeException e1 = new ServerException("Error happened in invoking " + getEndpointName() +
		            response.getStatusCode().value() + response.getStatusText() + " "  +
						response.getBody());
				log.warn("Remote service {} returned an empty error body for {} {}", getEndpointName(), method, url);
				chenileExchange.setException(e1);
				return;
			}
			parseBody(body,response);
			return;
		}		
	}
	
	protected void parseBody(byte[] body,ClientHttpResponse response) throws IOException{
		try {
			GenericResponse<?> gr = (GenericResponse<?>) objectMapper.readValue(body, getTypeReference());
			ErrorNumException e1 = new ErrorNumException(gr.getCode(),gr.getSubErrorCode(),
					gr.getDescription());
			log.warn("Remote service {} returned business error code={} subErrorCode={}",
					getEndpointName(), gr.getCode(), gr.getSubErrorCode());
			chenileExchange.setException(e1);
			return;
		}catch(Exception e) {
			ErrorNumException e1 = new ServerException("Error happened in invoking " + getEndpointName() +
		            response.getStatusCode().value() + response.getStatusText() );
			log.error("Failed to parse error response for {}", getEndpointName(), e);
			chenileExchange.setException(e1);
		}
	}
	
	protected TypeReference<?> getTypeReference(){
		return new TypeReference<Object>() {
			@Override
		    public  Type getType() {
		        return chenileExchange.getResponseBodyType().getType();
		    }
		};
	}
	
	protected String getEndpointName() {
		return chenileExchange.remoteServiceDefinition.id + "." +
				chenileExchange.remoteOperationDefinition.name;
	}	
}
