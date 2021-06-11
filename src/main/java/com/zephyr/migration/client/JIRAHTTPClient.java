package com.zephyr.migration.client;


import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.zephyr.migration.utils.ConfigProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component(value = "jiraHttpClient")
public class JIRAHTTPClient extends HttpClient{

    @Autowired
    private ConfigProperties configProperties;

    private static final String API_URL = "rest/api/2/";


    public JIRAHTTPClient() {
    }


    @Override
    public void setResourceName(String resourceName) {
        webResource = client.resource(configProperties.getConfigValue("zfj.server.baseUrl") + API_URL + resourceName);

    }

    @Override
    public void setLatestResourceName(String url) {
        setResourceName(url);
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
