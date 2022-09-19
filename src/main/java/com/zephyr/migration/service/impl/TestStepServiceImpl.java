package com.zephyr.migration.service.impl;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.sun.jersey.api.client.ClientResponse;
import com.zephyr.migration.client.HttpClient;
import com.zephyr.migration.client.JiraCloudClient;
import com.zephyr.migration.dto.JiraCloudTestStepDTO;
import com.zephyr.migration.dto.TestStepDTO;
import com.zephyr.migration.dto.TestStepResultDTO;
import com.zephyr.migration.model.ZfjCloudStepResultBean;
import com.zephyr.migration.model.ZfjCloudStepResultUpdateBean;
import com.zephyr.migration.service.TestStepService;
import com.zephyr.migration.utils.ApplicationConstants;
import com.zephyr.migration.utils.ConfigProperties;
import com.zephyr.migration.utils.JsonUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

@Service
public class TestStepServiceImpl implements TestStepService {

    private static final Logger log = LoggerFactory.getLogger(TestStepServiceImpl.class);
    private static final int TEST_STEP_PARTITION_LIST_SIZE = 50;

    @Autowired
    @Qualifier(value = "zapiHttpClient")
    private HttpClient zapiHttpClient;

    @Autowired
    private ConfigProperties configProperties;

    @Override
    public List<TestStepResultDTO> getTestStepsResultFromZFJ(String executionId) {
        List<TestStepResultDTO> responseList = new ArrayList<>();
        TypeReference<List<TestStepResultDTO>> reference = new TypeReference<List<TestStepResultDTO>>() {};
        try {
            ClientResponse response = zapiHttpClient.get(String.format(ApplicationConstants.ZAPI_RESOURCE_FETCH_TEST_STEP_RESULT_BY_EXECUTION_ID,executionId));
            String content = response.getEntity(String.class);
            if(StringUtils.isNotBlank(content)) {
                log.debug("Test Step Content=="+content);
                responseList = JsonUtil.readValue(content, reference);
            }
        } catch (IOException ex) {
            log.error("Exception occurred while fetching the test step results :", ex);
        }
        if(CollectionUtils.isNotEmpty(responseList))
            Collections.sort(responseList);
        return responseList;
    }

    @Override
    public List<ZfjCloudStepResultBean> getTestStepResultsFromZFJCloud(String cloudExecutionId) {
        log.info("Serving --> {}", "getTestStepResultsFromZFJCloud()");

        List<ZfjCloudStepResultBean> responseList = new ArrayList<>();
        TypeReference<List<ZfjCloudStepResultBean>> reference = new TypeReference<List<ZfjCloudStepResultBean>>() {};
        JsonParser parser = new JsonParser();

        final String CLOUD_BASE_URL = configProperties.getConfigValue("zfj.cloud.baseUrl");
        final String CLOUD_ACCESS_KEY = configProperties.getConfigValue("zfj.cloud.accessKey");
        final String CLOUD_ACCOUNT_ID = configProperties.getConfigValue("zfj.cloud.accountId");
        final String CLOUD_SECRET_KEY = configProperties.getConfigValue("zfj.cloud.secretKey");
        JiraCloudClient jiraCloudClient = new JiraCloudClient(CLOUD_ACCOUNT_ID, CLOUD_ACCESS_KEY, CLOUD_SECRET_KEY, CLOUD_BASE_URL);
        String getTestStepResultsUrl = CLOUD_BASE_URL + ApplicationConstants.CLOUD_GET_TEST_STEP_RESULTS_URL;
        String createJwtUrl;
        String queryParams = null;
        createJwtUrl = getTestStepResultsUrl + "?";
        try {
            queryParams = URLEncoder.encode("executionId=" + cloudExecutionId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        createJwtUrl = createJwtUrl + queryParams;

        String jwt = jiraCloudClient.createJWTToken(HttpMethod.GET, createJwtUrl);

        getTestStepResultsUrl = getTestStepResultsUrl + "?&executionId=" + cloudExecutionId;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, jwt);
        headers.set(ApplicationConstants.ZAPI_ACCESS_KEY, CLOUD_ACCESS_KEY);

        try {
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(getTestStepResultsUrl, HttpMethod.GET, entity, String.class);

            //read the json node response & prepare bean object.
            JsonElement element = parser.parse(Objects.requireNonNull(response.getBody()));
            JsonObject object = element.getAsJsonObject();
            JsonArray stepResultsArray = object.getAsJsonArray("stepResults");
            if (Objects.nonNull(stepResultsArray)) {
                responseList = JsonUtil.readValue(stepResultsArray.toString(), reference);
                log.info("step results array for executionId ::" + cloudExecutionId + " ---"+ stepResultsArray.toString());
            }
        } catch (Exception e) {
            log.error("Error while getting step results from cloud " + e.fillInStackTrace());
        }
        return responseList;
    }

    @Override
    public List<TestStepDTO> fetchTestStepsFromZFJ(Integer issueId) {
        List<TestStepDTO> testStepDTOS = new ArrayList<>();
        try {
             ClientResponse response = zapiHttpClient.get(String.format(ApplicationConstants.ZAPI_RESOURCE_GET_TEST_STEP,issueId));
            String content = response.getEntity(String.class);

            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(content);
            JsonObject object = element.getAsJsonObject();
            JsonArray testStepsArray = object.getAsJsonArray("stepBeanCollection");

            TypeReference<List<TestStepDTO>> reference = new TypeReference<List<TestStepDTO>>() {};
            if (Objects.nonNull(testStepsArray)) {
                testStepDTOS = JsonUtil.readValue(testStepsArray.toString(), reference);
            }
        } catch (IOException e) {
            log.warn("Exception occurred while fetching test steps from server :", e.fillInStackTrace());
        }
        return testStepDTOS;
    }

    @Override
    public List<JiraCloudTestStepDTO> createTestStepInJiraCloud(List<TestStepDTO> testSteps, Integer issueId, Long projectId) {
        log.info("Serving --> {}", "createTestStepInJiraCloud()");
        final String CLOUD_BASE_URL = configProperties.getConfigValue("zfj.cloud.baseUrl");
        final String CLOUD_ACCESS_KEY = configProperties.getConfigValue("zfj.cloud.accessKey");
        final String CLOUD_ACCOUNT_ID = configProperties.getConfigValue("zfj.cloud.accountId");
        final String CLOUD_SECRET_KEY = configProperties.getConfigValue("zfj.cloud.secretKey");
        JiraCloudClient jiraCloudClient = new JiraCloudClient(CLOUD_ACCOUNT_ID, CLOUD_ACCESS_KEY, CLOUD_SECRET_KEY, CLOUD_BASE_URL);
        String createCloudBulkTestStepUrl = CLOUD_BASE_URL + ApplicationConstants.CLOUD_CREATE_BULK_TEST_STEP_URL;

        String createUrl = createCloudBulkTestStepUrl;
        String queryParams = null;
        createUrl = createUrl + "?";
        try {
            queryParams = URLEncoder.encode("projectId=" + projectId + "&issueId=" + issueId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        createUrl = createUrl + queryParams;
        String jwt = jiraCloudClient.createJWTToken(HttpMethod.POST, createUrl);

        createCloudBulkTestStepUrl = createCloudBulkTestStepUrl + "?projectId=" + projectId + "&issueId=" + issueId;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, jwt);
        headers.set(ApplicationConstants.ZAPI_ACCESS_KEY, CLOUD_ACCESS_KEY);
        headers.set(HttpHeaders.CONTENT_LENGTH,String.valueOf(1048576));
        List<JiraCloudTestStepDTO> finalProcessedTestStepList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(testSteps) && testSteps.size() > TEST_STEP_PARTITION_LIST_SIZE) {
            /**
             * Partition the list into size of 100 & process the list.
             */
            List<List<TestStepDTO>> partitionTestStepsList = Lists.partition(testSteps,TEST_STEP_PARTITION_LIST_SIZE);
            log.info("Size of partitionTestStepsList is :"+partitionTestStepsList.size() + " for issueId: "+issueId);

            int counter =1;
            for(List<TestStepDTO> testStepDTOList : partitionTestStepsList) {
                log.debug("processing sub list counter :"+counter++);

                HttpEntity<String> entity = new HttpEntity<>(new Gson().toJson(testStepDTOList), headers);
                String response;
                List<JiraCloudTestStepDTO> testStepList = new ArrayList<>();
                try {
                    log.info("request to cloud for create bulk test step for this issue id ::: "+ issueId.toString());
                    response = restTemplate.postForObject(createCloudBulkTestStepUrl, entity, String.class);
                    if (response != null && !response.isEmpty()) {
                        JsonParser parser = new JsonParser();
                        JsonElement element = parser.parse(response);
                        JsonObject object = element.getAsJsonObject();
                        JsonArray testStepsArray = object.getAsJsonArray("teststepList");

                        TypeReference<List<JiraCloudTestStepDTO>> reference = new TypeReference<List<JiraCloudTestStepDTO>>() {};
                        if (Objects.nonNull(testStepsArray)) {
                            testStepList = JsonUtil.readValue(testStepsArray.toString(), reference);
                            finalProcessedTestStepList.addAll(testStepList);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error while creating test step in cloud for" + issueId.toString() + "this issue id " + e.fillInStackTrace());
                }
            }
            return finalProcessedTestStepList;
        }else {

            HttpEntity<String> entity = new HttpEntity<>(new Gson().toJson(testSteps), headers);
            String response;
            List<JiraCloudTestStepDTO> testStepList = new ArrayList<>();
            try {
                log.info("request to cloud for create bulk test step for this issue id ::: "+ issueId.toString());
                response = restTemplate.postForObject(createCloudBulkTestStepUrl, entity, String.class);
                if (response != null && !response.isEmpty()) {
                    JsonParser parser = new JsonParser();
                    JsonElement element = parser.parse(response);
                    JsonObject object = element.getAsJsonObject();
                    JsonArray testStepsArray = object.getAsJsonArray("teststepList");

                    TypeReference<List<JiraCloudTestStepDTO>> reference = new TypeReference<List<JiraCloudTestStepDTO>>() {};
                    if (Objects.nonNull(testStepsArray)) {
                        testStepList = JsonUtil.readValue(testStepsArray.toString(), reference);
                    }
                }
            } catch (Exception e) {
                log.error("Error while creating test step in cloud for" + issueId.toString() + "this issue id " + e.fillInStackTrace());
            }
            return testStepList;
        }
    }

    @Override
    public void updateStepResult(ZfjCloudStepResultUpdateBean stepResultUpdateBean) {
        log.info("Serving --> {}", "updateStepResult()");
        final String CLOUD_BASE_URL = configProperties.getConfigValue("zfj.cloud.baseUrl");
        final String CLOUD_ACCESS_KEY = configProperties.getConfigValue("zfj.cloud.accessKey");
        final String CLOUD_ACCOUNT_ID = configProperties.getConfigValue("zfj.cloud.accountId");
        final String CLOUD_SECRET_KEY = configProperties.getConfigValue("zfj.cloud.secretKey");
        JiraCloudClient jiraCloudClient = new JiraCloudClient(CLOUD_ACCOUNT_ID, CLOUD_ACCESS_KEY, CLOUD_SECRET_KEY, CLOUD_BASE_URL);
        String updateStepResultUrl = CLOUD_BASE_URL + ApplicationConstants.CLOUD_UPDATE_STEP_RESULT_URL;
        String jwt = jiraCloudClient.createJWTToken(HttpMethod.POST, updateStepResultUrl);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, jwt);
        headers.set(ApplicationConstants.ZAPI_ACCESS_KEY, CLOUD_ACCESS_KEY);

        HttpEntity<String> entity = new HttpEntity<>(new Gson().toJson(stepResultUpdateBean), headers);
        try {
            String response = restTemplate.postForObject(updateStepResultUrl, entity, String.class);

        } catch (Exception e) {
            log.error("Error while updating step result in cloud " + e.getMessage());
        }
    }
}
