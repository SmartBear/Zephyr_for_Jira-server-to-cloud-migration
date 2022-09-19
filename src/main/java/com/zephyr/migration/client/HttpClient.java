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
 */
public abstract class HttpClient {

    protected ClientConfig clientConfig;

    protected Client client;

    public void init() {
        org.slf4j.bridge.SLF4JBridgeHandler.removeHandlersForRootLogger();
        org.slf4j.bridge.SLF4JBridgeHandler.install();
        if (Objects.isNull(clientConfig) && Objects.isNull(client)) {
            clientConfig = new DefaultClientConfig();
            clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.FALSE);
            client = Client.create(clientConfig);
            client.addFilter(new LoggingFilter());
        }
    }

    public abstract WebResource getResourceName(String url);


    public ClientResponse get(String url) {
        WebResource webResource = getWebResource(url);
        ClientResponse response = webResource.accept("application/json")
                .type(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);

        return checkStatus(response);
    }

    public ClientResponse post(String url, String content) {
        WebResource webResource = getWebResource(url);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, content);

        return checkStatus(response);
    }

    public ClientResponse getWithNoContentType(String url) {
        WebResource webResource = getWebResource(url);

        ClientResponse response = webResource.get(ClientResponse.class);

        return checkStatus(response);
    }


    public ClientResponse put(String url, String content) {
        WebResource webResource = getWebResource(url);

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

    private WebResource getWebResource(String url) {
        WebResource webResource = getResourceName(url);
        if (Objects.isNull(webResource)) {
            throw new IllegalStateException("webResource is not Initialized. call setResourceName() method ");
        }
        return webResource;
    }

}
