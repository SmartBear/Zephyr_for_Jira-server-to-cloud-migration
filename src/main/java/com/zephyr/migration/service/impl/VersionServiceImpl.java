package com.zephyr.migration.service.impl;

import com.atlassian.jira.rest.client.api.domain.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.zephyr.migration.client.JiraCloudClient;
import com.zephyr.migration.client.JiraServerClient;
import com.zephyr.migration.service.VersionService;
import com.zephyr.migration.utils.ApplicationConstants;
import com.zephyr.migration.utils.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class VersionServiceImpl implements VersionService {

    private static final Logger log = LoggerFactory.getLogger(VersionServiceImpl.class);

    @Autowired
    ConfigProperties configProperties;

    public JsonNode getVersionsFromZephyrCloud(String projectId) {
        log.info("Serving --> {}", "getVersions()");
        final String CLOUD_BASE_URL = configProperties.getConfigValue("zfj.cloud.baseUrl");
        final String CLOUD_ACCESS_KEY = configProperties.getConfigValue("zfj.cloud.accessKey");
        final String CLOUD_ACCOUNT_ID = configProperties.getConfigValue("zfj.cloud.accountId");
        final String CLOUD_SECRET_KEY = configProperties.getConfigValue("zfj.cloud.secretKey");

        JiraCloudClient jiraCloudClient = new JiraCloudClient(CLOUD_ACCOUNT_ID, CLOUD_ACCESS_KEY, CLOUD_SECRET_KEY, CLOUD_BASE_URL);
        String getVersionsUrl = CLOUD_BASE_URL + ApplicationConstants.CLOUD_FETCH_VERSION_URL;
        String jwt = jiraCloudClient.createJWTToken(HttpMethod.POST, getVersionsUrl);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, jwt);
        headers.set(ApplicationConstants.ZAPI_ACCESS_KEY, CLOUD_ACCESS_KEY);
        HttpEntity<String> entity = new HttpEntity<>(new Gson().toJson(projectId), headers);
        JsonNode response = null;
        try {
            response = restTemplate.postForObject(getVersionsUrl, entity, JsonNode.class);
        } catch (Exception e) {
            log.error("Error while fetching version list " + e.getMessage());
        }
        return response;
    }

    @Override
    public Iterable<Version> getVersionsFromZephyrServer(Long projectId, String serverBaseUrl, String serverUserName, String serverUserPass) {
        JiraServerClient jiraServerClient = new JiraServerClient(serverUserName, serverUserPass, serverBaseUrl);
        return jiraServerClient.getVersions(projectId);
    }

    @Override
    public String createUnscheduledVersionInZephyrCloud(String projectId) {
        log.info("Serving --> {}", "createUnscheduledVersionInZephyrCloud()");
        final String CLOUD_BASE_URL = configProperties.getConfigValue("zfj.cloud.baseUrl");
        final String CLOUD_ACCESS_KEY = configProperties.getConfigValue("zfj.cloud.accessKey");
        final String CLOUD_ACCOUNT_ID = configProperties.getConfigValue("zfj.cloud.accountId");
        final String CLOUD_SECRET_KEY = configProperties.getConfigValue("zfj.cloud.secretKey");

        JiraCloudClient jiraCloudClient = new JiraCloudClient(CLOUD_ACCOUNT_ID, CLOUD_ACCESS_KEY, CLOUD_SECRET_KEY, CLOUD_BASE_URL);
        String createUnscheduledVersionsUrl = CLOUD_BASE_URL + ApplicationConstants.CLOUD_CREATE_UNSCHEDULED_VERSION_URL;
        String jwt = jiraCloudClient.createJWTToken(HttpMethod.POST, createUnscheduledVersionsUrl);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, jwt);
        headers.set(ApplicationConstants.ZAPI_ACCESS_KEY, CLOUD_ACCESS_KEY);
        HttpEntity<String> entity = new HttpEntity<>(new Gson().toJson(projectId), headers);
        String response = null;
        try {
            response = restTemplate.postForObject(createUnscheduledVersionsUrl, entity, String.class);
        } catch (Exception e) {
            log.error("Error while fetching create unscheduled version " + e.getMessage());
        }
        return response;
    }
}
