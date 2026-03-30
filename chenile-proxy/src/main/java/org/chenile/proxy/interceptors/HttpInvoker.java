package org.chenile.proxy.interceptors;


import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map.Entry;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.chenile.base.exception.ErrorNumException;
import org.chenile.base.exception.ServerException;
import org.chenile.base.response.GenericResponse;
import org.chenile.core.context.ChenileExchange;
import org.chenile.core.context.HeaderUtils;
import org.chenile.owiz.Command;
import org.chenile.proxy.builder.ProxyBuilder;
import org.chenile.proxy.errorcodes.ErrorCodes;
import org.chenile.service.registry.context.RemoteChenileExchange;
import org.chenile.service.registry.model.ChenileRemoteOperationDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.restclient.RestTemplateBuilder;
import tools.jackson.databind.ObjectMapper;


/**
 * Invokes the remote service using HTTP
 */
public class HttpInvoker implements Command<RemoteChenileExchange>{
	private static final Logger log = LoggerFactory.getLogger(HttpInvoker.class);
	@Value("${server.servlet.context-path:}")
	private String contextPath;
	@Autowired RestTemplateBuilder restTemplateBuilder;
	private ObjectMapper objectMapper = new ObjectMapper();
	public HttpInvoker() {}	
	@Override
	@SuppressWarnings("unchecked")
	public void execute(RemoteChenileExchange exchange) throws Exception {
		ChenileRemoteOperationDefinition od = exchange.remoteOperationDefinition;
		HttpHeaders headers = extractHeaders(exchange);
		headers.setContentType(MediaType.APPLICATION_JSON); // Correct
		headers.set("Accept-Charset", "ISO-8859-1");
	    HttpEntity<Object> entity = new HttpEntity<Object>(exchange.getBody(),headers);
	      
	    String baseURI = (String)exchange.getHeader(ProxyBuilder.REMOTE_URL_BASE);
		String serviceOpName = exchange.remoteServiceDefinition.id + "." +
				exchange.remoteOperationDefinition.name;
		if (!baseURI.startsWith("http://")) baseURI = "http://" + baseURI;
		String requestUrl = baseURI + constructUrl(contextPath,od.url,exchange);
	    ResponseEntity<GenericResponse<?>> httpResponse = null;
	    RestTemplate restTemplate = getRestTemplate(exchange);
		log.debug("Invoking remote service {} with {} {}", serviceOpName, httpMethod(od), requestUrl);
		try {
			httpResponse = (ResponseEntity<GenericResponse<?>>)
					restTemplate.exchange(requestUrl,
							httpMethod(od), entity,exchange.getResponseBodyType());
		} catch (RestClientException e) {
			Object[] eArgs = new Object[]{baseURI, serviceOpName, e.getMessage()};
			log.error("HTTP invocation failed for {} at {}", serviceOpName, requestUrl, e);
			setCorrectException(exchange,eArgs,e);
			return;
		}
		log.debug("Remote service {} responded with status {}", serviceOpName, httpResponse.getStatusCode());
		populateResponse(httpResponse,baseURI,serviceOpName,exchange);
	}

	private void populateResponse(ResponseEntity<GenericResponse<?>> httpResponse, String baseURI,String serviceOpName, ChenileExchange exchange) {
		if (httpResponse.hasBody()) {
			GenericResponse<?> gr = httpResponse.getBody();
			exchange.setResponse(gr.getData());
		}else {
			log.warn("No response body returned for {} from {}. Response: {}", serviceOpName, baseURI, httpResponse);
			exchange.setException(new ServerException(ErrorCodes.MISSING_BODY.getSubError(), new Object[] {baseURI, serviceOpName}));
		}
	}

	private void setCorrectException(ChenileExchange exchange, Object[] eArgs, RestClientException e){
		if (exchange.getException() != null)
			return; // if this has already been handled by the error handler then
		// the exception has already been set. So we can return
		Throwable t = e.getCause();
		ErrorNumException exc;
		if (t instanceof ConnectException || t instanceof UnknownHostException ||
				t.getClass().getName().startsWith("java.net.")){
			exc = new ServerException(ErrorCodes.CANNOT_CONNECT.getSubError(), eArgs,e);
		}else {
			exc = new ServerException(ErrorCodes.CANNOT_INVOKE.getSubError(), eArgs,e);
		}
		exchange.setException(exc);
	}

	/**
	 * Constructs the URL taking care of path variables
	 * @param url - the url inclusive of path variable
	 * @param exchange - the chenile exchange
	 * @return the new URL with paths substituted if required
	 */
	private static String constructUrl(String contextPath, String url, RemoteChenileExchange exchange){
		while (url.contains("/{")) {
			int startIndex = url.indexOf("/{");
			int endIndex = url.indexOf("}");
			String pathVarName = url.substring(startIndex+2,endIndex);
			Object pathValue  = exchange.getHeader(pathVarName);
			url = url.substring(0,startIndex+1) + pathValue + url.substring(endIndex+1);
		}
		if (contextPath != null && !contextPath.isEmpty()){
			url = contextPath + url;
		}
		return url;
	}
	
	private HttpHeaders extractHeaders(ChenileExchange exchange) {
		HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
	    for (Entry<String, Object> entry: exchange.getHeaders().entrySet()) {
			String key = entry.getKey();
			Object obj = entry.getValue();
			headers.add(key, obj.toString());
			//Convert x headers into auth header basic auth rule
			if(key.equalsIgnoreCase(HeaderUtils.AUTH_X_TOKEN_HEADER)){
				headers.add(HeaderUtils.AUTH_TOKEN_HEADER,obj.toString());
			}
		}
	    return headers;
	}
	
	private HttpMethod httpMethod(ChenileRemoteOperationDefinition od) {
        return switch (od.httpMethod) {
            case GET -> HttpMethod.GET;
            case POST -> HttpMethod.POST;
            case DELETE -> HttpMethod.DELETE;
            case PUT -> HttpMethod.PUT;
            case PATCH -> HttpMethod.PATCH;
        };
    }
	
	protected RestTemplate getRestTemplate(RemoteChenileExchange chenileExchange) {
		ChenileResponseHandler responseErrorHandler = new ChenileResponseHandler(chenileExchange,objectMapper);

		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpComponentsClientHttpRequestFactory requestFactory =
				new HttpComponentsClientHttpRequestFactory(httpClient);

		//return RestClient.builder().build();

		return restTemplateBuilder
				.requestFactory(() -> requestFactory)
				.errorHandler(responseErrorHandler) // keep your custom error handler
				.build();
	}
}
