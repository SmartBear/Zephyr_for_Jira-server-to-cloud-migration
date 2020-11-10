package com.zephyr.migration.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.zephyr.migration.dto.JiraIssueDTO;
import com.thed.zephyr.cloud.rest.ZFJCloudRestClient;
import com.thed.zephyr.cloud.rest.client.JwtGenerator;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

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
        JsonNode responseNode = restTemplate.postForObject(createUrl, request, JsonNode.class);

    }

    public String createJWTToken(HttpMethod method, String url) {
        String jwt = "";
        try {
            ZFJCloudRestClient client = ZFJCloudRestClient.restBuilder(zephyrBaseUrl, accessKey, secretKey, accountId).build();
            URI uri = new URI(url);
            JwtGenerator jwtGenerator = client.getJwtGenerator();
            jwt = jwtGenerator.generateJWT(method.name(), uri, 36000);
        }catch (Exception ex){
        }
        return jwt;
    }
}
