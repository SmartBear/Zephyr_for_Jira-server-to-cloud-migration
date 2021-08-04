package com.zephyr.migration.service.impl;

import com.atlassian.jira.rest.client.api.domain.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;
import com.zephyr.migration.client.HttpClient;
import com.zephyr.migration.client.JiraCloudClient;
import com.zephyr.migration.client.JiraServerClient;
import com.zephyr.migration.dto.VersionDTO;
import com.zephyr.migration.model.JiraVersion;
import com.zephyr.migration.model.VersionWrapper;
import com.zephyr.migration.service.VersionService;
import com.zephyr.migration.utils.ApplicationConstants;
import com.zephyr.migration.utils.ConfigProperties;
import com.zephyr.migration.utils.JsonUtil;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class VersionServiceImpl implements VersionService {

    private static final Logger log = LoggerFactory.getLogger(VersionServiceImpl.class);

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    @Qualifier(value = "jiraHttpClient")
    private HttpClient jiraHttpClient;

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
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
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
    public void createUnscheduledVersionInZephyrCloud(String projectId) {
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
        try {
            String response = restTemplate.postForObject(createUnscheduledVersionsUrl, entity, String.class);
            log.info("Response received from cloud endpoint: "+response);
        } catch (Exception e) {
            log.error("Error while creating unscheduled version " + e.getMessage());
        }
    }

    @Override
    public JsonNode createVersionInZephyrCloud(JiraVersion jiraServerVersion, Long projectId) {
        log.info("Serving --> {}", "createVersionInZephyrCloud()");
        final String CLOUD_BASE_URL = configProperties.getConfigValue("zfj.cloud.baseUrl");
        final String CLOUD_ACCESS_KEY = configProperties.getConfigValue("zfj.cloud.accessKey");
        final String CLOUD_ACCOUNT_ID = configProperties.getConfigValue("zfj.cloud.accountId");
        final String CLOUD_SECRET_KEY = configProperties.getConfigValue("zfj.cloud.secretKey");

        JiraCloudClient jiraCloudClient = new JiraCloudClient(CLOUD_ACCOUNT_ID, CLOUD_ACCESS_KEY, CLOUD_SECRET_KEY, CLOUD_BASE_URL);
        String createCloudVersionsUrl = CLOUD_BASE_URL + ApplicationConstants.CLOUD_CREATE_VERSION_URL;
        String jwt = jiraCloudClient.createJWTToken(HttpMethod.POST, createCloudVersionsUrl);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, jwt);
        headers.set(ApplicationConstants.ZAPI_ACCESS_KEY, CLOUD_ACCESS_KEY);
        VersionDTO versionDTO = new VersionDTO();
        versionDTO.setDescription(jiraServerVersion.getDescription());
        versionDTO.setName(jiraServerVersion.getName());
        versionDTO.setProjectId(projectId);
        HttpEntity<String> entity = new HttpEntity<>(new Gson().toJson(versionDTO), headers);
        JsonNode response = null;
        try {
            response = restTemplate.postForObject(createCloudVersionsUrl, entity, JsonNode.class);
            log.info("Version creation response from cloud endpoint:: "+response);
        } catch (Exception e) {
            log.error("Error while creating version in cloud " + e.getMessage());
        }
        return response;
    }

    @Override
    public Iterable<JiraVersion> getVersionListFromServer(String projectId) {

        jiraHttpClient.setResourceName(String.format(ApplicationConstants.JIRA_RESOURCE_VERSION, projectId));

        ClientResponse response = jiraHttpClient.get();

        TypeReference<VersionWrapper> ref = new TypeReference<VersionWrapper>() {};
        VersionWrapper versions;
        Iterable<JiraVersion> versionList = null;
        try {
            versions = JsonUtil.readValue(response.getEntity(String.class), ref);
            versionList = versions.getValues();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return versionList;
    }
}
