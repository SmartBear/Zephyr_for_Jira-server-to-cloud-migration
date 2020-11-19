package com.zephyr.migration.service.impl;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.zephyr.migration.client.JiraCloudClient;
import com.zephyr.migration.client.JiraServerClient;
import com.zephyr.migration.dto.JiraIssueDTO;
import com.zephyr.migration.service.TestService;
import com.zephyr.migration.service.VersionService;
import com.zephyr.migration.utils.ApplicationConstants;
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

    @Autowired JiraCloudClient jiraCloudClient;

    private static final Logger log = LoggerFactory.getLogger(VersionServiceImpl.class);

    public JsonNode getVersions(String projectId, String zephyrBaseUrl, String accessKey) {
        log.info("Serving --> {}", "getVersions()");
        String getVersionsUrl = zephyrBaseUrl + ApplicationConstants.CLOUD_FETCH_VERSION_URL;
        String jwt = jiraCloudClient.createJWTToken(HttpMethod.POST, getVersionsUrl);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, jwt);
        headers.set(ApplicationConstants.ZAPI_ACCESS_KEY, accessKey);
        HttpEntity<String> entity = new HttpEntity<>(new Gson().toJson(projectId), headers);
        JsonNode response = null;
        try {
            response = restTemplate.postForObject(getVersionsUrl, entity, JsonNode.class);
        } catch (Exception e) {
            log.error("Error while fetching version list " + e.getMessage());
        }
        return response;
    }

}
