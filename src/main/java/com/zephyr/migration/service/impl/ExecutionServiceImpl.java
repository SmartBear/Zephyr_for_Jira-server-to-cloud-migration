package com.zephyr.migration.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.*;
import com.sun.jersey.api.client.ClientResponse;
import com.zephyr.migration.client.HttpClient;
import com.zephyr.migration.client.JiraCloudClient;
import com.zephyr.migration.dto.ExecutionDTO;
import com.zephyr.migration.model.ZfjCloudExecutionBean;
import com.zephyr.migration.service.ExecutionService;
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ExecutionServiceImpl implements ExecutionService {

    private static final Logger log = LoggerFactory.getLogger(ExecutionServiceImpl.class);

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    @Qualifier(value = "zapiHttpClient")
    private HttpClient zapiHttpClient;

    @Override
    public List<ExecutionDTO> getExecutionsFromZFJByVersionAndCycleName(String projectId, String versionId, String cycleId, int offset, int maxRecords) {
        JsonParser parser = new JsonParser();
        TypeReference<List<ExecutionDTO>> executionReference = new TypeReference<List<ExecutionDTO>>() {};
        List<ExecutionDTO> executions = new ArrayList<>();
        try {
            ClientResponse response = zapiHttpClient.get(String.format(ApplicationConstants.ZAPI_RESOURCE_GET_EXECUTIONS_WITHOUT_OFFSET, projectId, versionId, cycleId));
            String content = response.getEntity(String.class);
            JsonElement element = parser.parse(content);
            JsonObject object = element.getAsJsonObject();
            JsonArray executionsArray = object.getAsJsonArray("executions");
            if (Objects.nonNull(executionsArray)) {
                executions = JsonUtil.readValue(executionsArray.toString(), executionReference);
            }
        } catch (UnsupportedEncodingException e) {
            log.error("Error occurred during encoding of executions.",e.fillInStackTrace());
        } catch (Exception e) {
            log.error("Error occurred during fetching executions from ZFJ",e.fillInStackTrace());
        }
        return executions;
    }


    @Override
    public List<ExecutionDTO> getExecutionsFromZFJByVersionCycleAndFolderName(String projectId, String versionId, String cycleId, String folderId, int offset, int maxRecords) {
        JsonParser parser = new JsonParser();
        TypeReference<List<ExecutionDTO>> executionReference = new TypeReference<List<ExecutionDTO>>() {};
        List<ExecutionDTO> executions = new ArrayList<>();
        try {
            ClientResponse response = zapiHttpClient.get(String.format(ApplicationConstants.ZAPI_RESOURCE_GET_FOLDER_EXECUTIONS_WITHOUT_OFFSET, projectId, versionId, cycleId, folderId));
            String content = response.getEntity(String.class);
            JsonElement element = parser.parse(content);
            JsonObject object = element.getAsJsonObject();
            JsonArray executionsArray = object.getAsJsonArray("executions");
            if (Objects.nonNull(executionsArray)) {
                executions = JsonUtil.readValue(executionsArray.toString(), executionReference);
                log.info("executionsArray string for folder ::" + folderId + "---"+ executionsArray.toString());
                log.info("size of executions list  for folder ::" + folderId + "---"+ executions.size());
            }
        } catch (UnsupportedEncodingException e) {
            log.error("Error occurred during encoding of executions.",e.fillInStackTrace());
        } catch (Exception e) {
            log.error("Error occurred during fetching executions from ZFJ",e.fillInStackTrace());
        }
        return executions;
    }

    @Override
    public ZfjCloudExecutionBean createExecutionInJiraCloud(ZfjCloudExecutionBean zfjCloudExecutionBean) {
        log.info("Serving --> {}", "createExecutionInJiraCloud()");
        final String CLOUD_BASE_URL = configProperties.getConfigValue("zfj.cloud.zapi.endpoint");
        final String CLOUD_ACCESS_KEY = configProperties.getConfigValue("zfj.cloud.accessKey");
        final String CLOUD_ACCOUNT_ID = configProperties.getConfigValue("zfj.cloud.accountId");
        final String CLOUD_SECRET_KEY = configProperties.getConfigValue("zfj.cloud.secretKey");
        JiraCloudClient jiraCloudClient = new JiraCloudClient(CLOUD_ACCOUNT_ID, CLOUD_ACCESS_KEY, CLOUD_SECRET_KEY, CLOUD_BASE_URL);
        String createCloudExecutionUrl = CLOUD_BASE_URL + ApplicationConstants.CLOUD_CREATE_EXECUTION_URL;
        String jwt = jiraCloudClient.createJWTToken(HttpMethod.POST, createCloudExecutionUrl);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, jwt);
        headers.set(ApplicationConstants.ZAPI_ACCESS_KEY, CLOUD_ACCESS_KEY);

        HttpEntity<String> entity = new HttpEntity<>(new Gson().toJson(zfjCloudExecutionBean), headers);
        JsonNode response;
        try {
            log.info("request to cloud for create execution ::: "+ zfjCloudExecutionBean.toString());
            response = restTemplate.postForObject(createCloudExecutionUrl, entity, JsonNode.class);
            //read the json node response & prepare cycle bean object.
            if (response != null && !response.isEmpty()) {
                zfjCloudExecutionBean.setId(response.findValue("id").asText());
            }
            log.info("Create execution response from cloud endpoint: "+response);
        } catch (Exception e) {
            log.error("Error while creating execution in cloud " + e.fillInStackTrace());
        }
        return zfjCloudExecutionBean;
    }
}
