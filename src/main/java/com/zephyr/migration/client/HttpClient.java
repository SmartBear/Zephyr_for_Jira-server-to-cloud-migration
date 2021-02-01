package com.zephyr.migration.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.json.JSONConfiguration;

import javax.ws.rs.core.MediaType;
import java.util.Objects;

/**
 * <p>Handles http/https connections.</p>
 * 
 * @author Harsh
 *
 */
public abstract class HttpClient {
		
	protected ClientConfig clientConfig;
	
	protected Client client;
	
	protected WebResource webResource;

	public void init() {
		org.slf4j.bridge.SLF4JBridgeHandler.removeHandlersForRootLogger();
        org.slf4j.bridge.SLF4JBridgeHandler.install();        
        if(Objects.isNull(clientConfig) && Objects.isNull(client)) {
        	clientConfig = new DefaultClientConfig();
            clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.FALSE);
            client = Client.create(clientConfig);
            client.addFilter(new LoggingFilter());
        }
	}
	
    public abstract void setResourceName(String url);
    public abstract void setLatestResourceName(String url);

    public ClientResponse get() {
        if (Objects.isNull(webResource)) {
            throw new IllegalStateException("webResource is not Initialized. call setResourceName() method ");
        }

        ClientResponse response = webResource.accept("application/json")
                .type(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);

        return checkStatus(response);
    }

    public ClientResponse post(String content) {
        if (Objects.isNull(webResource)) {
            throw new IllegalStateException("webResource is not Initialized. call setResourceName() method ");
        }

        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, content);

        return checkStatus(response);
    }

    public ClientResponse getWithNoContentType() {
        if (Objects.isNull(webResource)) {
            throw new IllegalStateException("webResource is not Initialized. call setResourceName() method ");
        }

        ClientResponse response = webResource.get(ClientResponse.class);

        return checkStatus(response);
    }

    
    public ClientResponse put(String content) {
        if (Objects.isNull(webResource)) {
            throw new IllegalStateException("webResource is not Initialized. call setResourceName() method ");
        }

        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .put(ClientResponse.class, content);

        return checkStatus(response);
    }

    protected ClientResponse checkStatus(ClientResponse response) {
        if (response.getStatus() != Status.OK.getStatusCode() && response.getStatus() != Status.CREATED.getStatusCode()) {
            throw new ClientHandlerException("Failed : HTTP error code : " + response.getStatus());
        }
        return response;
    }

}
