package com.zephyr.migration.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.zephyr.migration.dto.JiraIssueDTO;
import com.thed.zephyr.cloud.rest.ZFJCloudRestClient;
import com.thed.zephyr.cloud.rest.client.JwtGenerator;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

public class JiraCloudClient {

    private String accountId;
    private String accessKey;
    private String secretKey;
    private String zephyrBaseUrl;
    private static final String ZAPIACCESSKEY = "zapiAccessKey";

    public JiraCloudClient(String accountId, String accessKey, String secretKey, String zephyrBaseUrl) {
        this.accountId = accountId;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.zephyrBaseUrl = zephyrBaseUrl;
    }

    public JiraIssueDTO createIssue(JiraIssueDTO requestObject){
        String createUrl = zephyrBaseUrl + "/public/rest/api/2.0/issue/create";
        String jwt = createJWTToken(HttpMethod.POST, createUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, jwt);
        headers.set(ZAPIACCESSKEY, this.accessKey);
        HttpEntity<String> request = new HttpEntity<String>(new Gson().toJson(requestObject), headers);
        RestTemplate restTemplate = new RestTemplate();
        JsonNode responseNode = restTemplate.postForObject(createUrl, request, JsonNode.class);

        return requestObject;
    }

    public String createJWTToken(HttpMethod method, String url) {
        String jwt = "";
        try {
            ZFJCloudRestClient client = ZFJCloudRestClient.restBuilder(this.zephyrBaseUrl, this.accessKey, this.secretKey,
                    this.accountId).build();
            URI uri = new URI(url);
            JwtGenerator jwtGenerator = client.getJwtGenerator();
            jwt = jwtGenerator.generateJWT(method.name(), uri, 36000);
        }catch (Exception ex){
        }
        return jwt;
    }
}
