package com.zephyr.migration.service.impl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;
import com.zephyr.migration.client.JIRAHTTPClient;
import com.zephyr.migration.client.ZapiServerHttpClient;
import com.zephyr.migration.model.Issue;
import com.zephyr.migration.service.DefectLinkService;
import com.zephyr.migration.service.IssueService;
import com.zephyr.migration.utils.ApplicationConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
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
}
