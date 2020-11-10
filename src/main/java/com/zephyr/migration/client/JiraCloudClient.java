package com.zephyr.migration.client;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import java.io.IOException;
import java.net.URI;

public class JiraCloudClient {

    private String accountId;
    private String accessKey;
    private String secretKey;
    private String zephyrBaseUrl;

    public void createIssue(){
        String createUrl = zephyrBaseUrl + "/public/rest/api/2.0/issue/create";
        String jwt = createJWTToken(HttpMethod.POST, createUrl);


    }

    public void createJWTToken(HttpMethod method, String url) {
        String jwt = "";
        try {
            ZFJCloudRestClient client = ZFJCloudRestClient.restBuilder(zephyrBaseUrl, accessKey, secretKey, accountId).build();
            URI uri = new URI(url);
            JwtGenerator jwtGenerator = client.getJwtGenerator();
            jwt = jwtGenerator.generateJWT(method.name(), uri, 36000);
        }catch (Exception ex){
        }
    }
}
