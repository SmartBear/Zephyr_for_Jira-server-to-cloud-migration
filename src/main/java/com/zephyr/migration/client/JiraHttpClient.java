package com.zephyr.migration.client;

import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.zephyr.migration.utils.ConfigProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>Handles http/https connection to Zapi API's.</p>
 * 
 * @author Harsh
 *
 */
@Component(value = "jiraHttpClient")
public class JiraHttpClient extends HttpClient {

    private static final String API_URL = "rest/api/2/";

    @Autowired
    ConfigProperties configProperties;

    public JiraHttpClient() {
    }

    public void setResourceName(String resourceName) {
        webResource = client.resource(configProperties.getConfigValue("zfj.server.baseUrl") + API_URL + resourceName);
    }
    public void setLatestResourceName(String resourceName) {
        setResourceName(resourceName);
    }
    public void init() {
    	super.init();
    	if (StringUtils.isNotBlank(configProperties.getConfigValue("zfj.server.username"))
                && StringUtils.isNotBlank(configProperties.getConfigValue("zfj.server.password"))) {
            HTTPBasicAuthFilter auth = new HTTPBasicAuthFilter(configProperties.getConfigValue("zfj.server.username"),
                    configProperties.getConfigValue("zfj.server.password"));
            client.addFilter(auth);
        }

    }

}
