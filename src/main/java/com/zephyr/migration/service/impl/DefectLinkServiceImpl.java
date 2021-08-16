package com.zephyr.migration.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.ClientResponse;
import com.zephyr.migration.client.JIRAHTTPClient;
import com.zephyr.migration.client.JiraCloudClient;
import com.zephyr.migration.client.ZapiServerHttpClient;
import com.zephyr.migration.dto.JiraCloudTestStepDTO;
import com.zephyr.migration.dto.TestStepDTO;
import com.zephyr.migration.model.Defect;
import com.zephyr.migration.model.Issue;
import com.zephyr.migration.model.ZfjCloudFolderBean;
import com.zephyr.migration.service.DefectLinkService;
import com.zephyr.migration.service.IssueService;
import com.zephyr.migration.utils.ApplicationConstants;
import com.zephyr.migration.utils.ConfigProperties;
import com.zephyr.migration.utils.JsonUtil;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class DefectLinkServiceImpl implements DefectLinkService {

    private static final Logger log = LoggerFactory.getLogger(DefectLinkServiceImpl.class);

    private static final String STEP_DEFECTS_KEY = "stepDefects";
    private static final String KEY = "key";

    @Autowired
    IssueService issueService;
    
    @Autowired
    ConfigProperties configProperties;

    @Override
    public List<Issue> getExecutionLevelDefectFromServer(Integer executionId, JIRAHTTPClient jirahttpClient, ZapiServerHttpClient zapiHttpClient) {
        if (null == zapiHttpClient)
            throw new IllegalStateException("HTTP Client not Initialized");

        ClientResponse response;
        String content;
        Type defectList = new TypeToken<Map<String,Map<String, Map<String, String>>>>(){}.getType();
        List<Issue> issueList = new ArrayList<>();

        zapiHttpClient.setResourceName(String.format(ApplicationConstants.ZAPI_RESOURCE_GET_DEFECTS_BY_EXECUTION_ID, executionId));

        response = zapiHttpClient.get();
        content = response.getEntity(String.class);

        if(StringUtils.isNotBlank(content)) {
            Gson gson = new Gson();
            Map<String,Map<String, Map<String, String>>> defectMap = gson.fromJson(content,defectList);

            if(MapUtils.isNotEmpty(defectMap)) {
                for(Map.Entry entry : defectMap.entrySet()) {
                    Map<String, Map<String, String>> defects = (Map<String, Map<String, String>>) entry.getValue();
                    if(MapUtils.isNotEmpty(defectMap)) {
                        for(Map.Entry defect : defects.entrySet()) {
                            String issueKey = (String) defect.getKey();
                            Issue issue = issueService.getIssueFromJira(issueKey);
                            issueList.add(issue);
                        }
                    }
                }
            }
        }

        return issueList;
    }

    @Override
    public List<Issue> getStepLevelDefectFromZfj(Integer executionId, JIRAHTTPClient jirahttpClient, ZapiServerHttpClient zapiHttpClient) {
        if (null == zapiHttpClient)
            throw new IllegalStateException("HTTP Client not Initialized");

        ClientResponse response;
        String content;
        List<Issue> issueList = new ArrayList<>();

        zapiHttpClient.setResourceName(String.format(ApplicationConstants.ZAPI_RESOURCE_GET_STEP_DEFECTS_BY_EXECUTION_ID, executionId));

        response = zapiHttpClient.get();
        content = response.getEntity(String.class);
        if (StringUtils.isNotBlank(content)) {

            JSONObject jsonObject = new JSONObject(content);
            JSONObject stepDefects = jsonObject.getJSONObject(STEP_DEFECTS_KEY);
            List<String> issueKeys = new ArrayList<>();
            for (Object key : stepDefects.keySet()) {
                String stepId = (String) key;
                JSONObject stepDefect = (JSONObject) stepDefects.get(stepId);
                //Print key and value
                log.info("key: " + stepId + " value: " + stepDefect.toMap());

                if (Objects.nonNull(stepDefect) && StringUtils.contains(stepDefect.toString(), STEP_DEFECTS_KEY)) {
                    JSONArray jsonArray = stepDefect.getJSONArray(STEP_DEFECTS_KEY);

                    if (Objects.nonNull(jsonArray) && jsonArray.length() > 0) {

                        for (int i = 0, size = jsonArray.length(); i < size; i++) {
                            JSONObject defectObject = jsonArray.getJSONObject(i);
                            if (defectObject.toMap().containsKey(KEY)) {
                                issueKeys.add((String) defectObject.toMap().get(KEY));
                            }
                        }
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(issueKeys)) {
                issueKeys.parallelStream().forEach(issueKey -> {
                            Issue issue = issueService.getIssueFromJira(issueKey);
                            issueList.add(issue);
                        }
                );
            }
        }
        return issueList;
    }
    
    @Override
    public void createExecutionLevelDefectInZephyrCloud(String executionId, List<Defect> defects) {
    	log.info("Serving --> {}", "createExecutionLevelDefectInZephyrCloud()");
    	final String CLOUD_BASE_URL = configProperties.getConfigValue("zfj.cloud.baseUrl");
        final String CLOUD_ACCESS_KEY = configProperties.getConfigValue("zfj.cloud.accessKey");
        final String CLOUD_ACCOUNT_ID = configProperties.getConfigValue("zfj.cloud.accountId");
        final String CLOUD_SECRET_KEY = configProperties.getConfigValue("zfj.cloud.secretKey");
        JiraCloudClient jiraCloudClient = new JiraCloudClient(CLOUD_ACCOUNT_ID, CLOUD_ACCESS_KEY, CLOUD_SECRET_KEY, CLOUD_BASE_URL);
        String createCloudExecutionLevelDefectUrl = CLOUD_BASE_URL + ApplicationConstants.CLOUD_CREATE_EXECUTION_LEVEL_DEFECT_URL;

        String createUrl = createCloudExecutionLevelDefectUrl;
        String queryParams = null;
        createUrl = createUrl + "?";
        try {
            queryParams = URLEncoder.encode("executionId=" + executionId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        createUrl = createUrl + queryParams;
        String jwt = jiraCloudClient.createJWTToken(HttpMethod.POST, createUrl);

        createCloudExecutionLevelDefectUrl = createCloudExecutionLevelDefectUrl + "?executionId=" + executionId;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, jwt);
        headers.set(ApplicationConstants.ZAPI_ACCESS_KEY, CLOUD_ACCESS_KEY);
        headers.set(HttpHeaders.CONTENT_LENGTH,String.valueOf(1048576));
        
        HttpEntity<String> entity = new HttpEntity<>(new Gson().toJson(defects), headers);
        String response;
        try {
        	log.info("request to cloud for create execution level defect for this execution id ::: "+ executionId);
            response = restTemplate.postForObject(createCloudExecutionLevelDefectUrl, entity, String.class);
        } catch (Exception e) {
            log.error("Error while creating execution level defect in cloud for" + executionId + "this execution id " + e.fillInStackTrace());
        }
    }
    
    @Override
    public void createStepResultLevelDefectInZephyrCloud(String executionId, String stepResultId, List<Defect> defects) {
    	log.info("Serving --> {}", "createStepResultLevelDefectInZephyrCloud()");
    	final String CLOUD_BASE_URL = configProperties.getConfigValue("zfj.cloud.baseUrl");
        final String CLOUD_ACCESS_KEY = configProperties.getConfigValue("zfj.cloud.accessKey");
        final String CLOUD_ACCOUNT_ID = configProperties.getConfigValue("zfj.cloud.accountId");
        final String CLOUD_SECRET_KEY = configProperties.getConfigValue("zfj.cloud.secretKey");
        JiraCloudClient jiraCloudClient = new JiraCloudClient(CLOUD_ACCOUNT_ID, CLOUD_ACCESS_KEY, CLOUD_SECRET_KEY, CLOUD_BASE_URL);
        String createCloudStepResultLevelDefectUrl = CLOUD_BASE_URL + ApplicationConstants.CLOUD_CREATE_STEP_RESULT_LEVEL_DEFECT_URL;

        String createUrl = createCloudStepResultLevelDefectUrl;
        String queryParams = null;
        createUrl = createUrl + "?";
        try {
            queryParams = URLEncoder.encode("stepResultId=" + stepResultId + "&executionId=" + executionId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        createUrl = createUrl + queryParams;
        String jwt = jiraCloudClient.createJWTToken(HttpMethod.POST, createUrl);

        createCloudStepResultLevelDefectUrl = createCloudStepResultLevelDefectUrl + "stepResultId=" + stepResultId + "&executionId=" + executionId;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, jwt);
        headers.set(ApplicationConstants.ZAPI_ACCESS_KEY, CLOUD_ACCESS_KEY);
        headers.set(HttpHeaders.CONTENT_LENGTH,String.valueOf(1048576));
        
        HttpEntity<String> entity = new HttpEntity<>(new Gson().toJson(defects), headers);
        String response;
        try {
        	log.info("request to cloud for create step result level defect for this execution id ::: "+ executionId);
            response = restTemplate.postForObject(createCloudStepResultLevelDefectUrl, entity, String.class);
        } catch (Exception e) {
            log.error("Error while creating step result level defect in cloud for" + executionId + "this execution id " + e.fillInStackTrace());
        }
    }
}
