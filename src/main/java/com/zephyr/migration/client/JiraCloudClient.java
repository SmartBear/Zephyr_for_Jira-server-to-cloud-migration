package com.zephyr.migration.client;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.zephyr.migration.dto.JiraIssueDTO;

import java.io.IOException;
import java.net.URI;

public class JiraCloudClient {

    private String accountId;
    private String accessKey;
    private String secretKey;
    private String zephyrBaseUrl;
    public String ZAPIACCESSKEY = "zapiAccessKey";

    public void createIssue(){
        String createUrl = zephyrBaseUrl + "/public/rest/api/2.0/issue/create";
        String jwt = createJWTToken(HttpMethod.POST, createUrl);

        JiraIssueDTO jiraIssueDTO = new JiraIssueDTO();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, jwt);
        headers.set(ZAPIACCESSKEY, accessKey);
        HttpEntity<String> request = new HttpEntity<String>(new Gson().toJson(jiraIssueDTO), headers);
        RestTemplate restTemplate = new RestTemplate();
        JsonNode responseNode = restTemplate.postForObject(createUrl, request, JsonNode.class);

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
