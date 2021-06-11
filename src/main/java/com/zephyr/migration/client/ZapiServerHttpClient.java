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
@Component(value = "zapiHttpClient")
public class ZapiServerHttpClient extends HttpClient {

    @Autowired
    private ConfigProperties configProperties;

    public ZapiServerHttpClient() {
    }

    public void setResourceName(String resourceName) {
        String API_URL = "rest/zapi/latest/";
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
