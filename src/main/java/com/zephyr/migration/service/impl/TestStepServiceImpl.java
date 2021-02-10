package com.zephyr.migration.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.*;
import com.sun.jersey.api.client.ClientResponse;
import com.zephyr.migration.client.HttpClient;
import com.zephyr.migration.client.JiraCloudClient;
import com.zephyr.migration.dto.TestStepDTO;
import com.zephyr.migration.dto.TestStepResultDTO;
import com.zephyr.migration.model.ZfjCloudStepResultBean;
import com.zephyr.migration.service.TestStepService;
import com.zephyr.migration.utils.ApplicationConstants;
import com.zephyr.migration.utils.ConfigProperties;
import com.zephyr.migration.utils.JsonUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONArray;
import org.json.JSONObject;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class TestStepServiceImpl implements TestStepService {

    private static final Logger log = LoggerFactory.getLogger(TestStepServiceImpl.class);

    @Autowired
    @Qualifier(value = "zapiHttpClient")
    private HttpClient zapiHttpClient;

    @Autowired
    ConfigProperties configProperties;

    @Override
    public List<TestStepResultDTO> getTestStepsResultFromZFJ(String executionId) {
        List<TestStepResultDTO> responseList = new ArrayList<>();
        TypeReference<List<TestStepResultDTO>> reference = new TypeReference<List<TestStepResultDTO>>() {};
        try {
            zapiHttpClient.setResourceName(String.format(ApplicationConstants.ZAPI_RESOURCE_FETCH_TEST_STEP_RESULT_BY_EXECUTION_ID,executionId));
            ClientResponse response = zapiHttpClient.get();
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
            zapiHttpClient.setResourceName(String.format(ApplicationConstants.ZAPI_RESOURCE_GET_TEST_STEP,issueId));
            ClientResponse response = zapiHttpClient.get();
            String content = response.getEntity(String.class);
            JSONArray array = new JSONArray(content);
            TypeReference<TestStepDTO> ref1 = new TypeReference<TestStepDTO>(){};
            for(int i=0; i<array.length(); i++){
                JSONObject json = (JSONObject)array.get(i);
                TestStepDTO step = JsonUtil.readValue(json.toString(), ref1);
                testStepDTOS.add(step);
            }
        } catch (IOException e) {
            log.warn("Exception occurred while fetching test steps from server :", e.fillInStackTrace());
        }
        return testStepDTOS;
    }
}
