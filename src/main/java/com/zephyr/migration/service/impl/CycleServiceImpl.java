package com.zephyr.migration.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;
import com.zephyr.migration.client.HttpClient;
import com.zephyr.migration.client.JiraCloudClient;
import com.zephyr.migration.dto.CycleDTO;
import com.zephyr.migration.dto.VersionDTO;
import com.zephyr.migration.model.ZfjCloudCycleBean;
import com.zephyr.migration.service.CycleService;
import com.zephyr.migration.utils.ApplicationConstants;
import com.zephyr.migration.utils.ConfigProperties;
import com.zephyr.migration.utils.JsonUtil;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

@Service
public class CycleServiceImpl implements CycleService {

    private static final Logger log = LoggerFactory.getLogger(CycleServiceImpl.class);

    @Autowired
    ConfigProperties configProperties;

    @Autowired
    @Qualifier(value = "zapiHttpClient")
    private HttpClient zapiHttpClient;

    @Override
    public ZfjCloudCycleBean createCycleInZephyrCloud(CycleDTO cycleDTO) {
        log.info("Serving --> {}", "createCycleInZephyrCloud()");
        final String CLOUD_BASE_URL = configProperties.getConfigValue("zfj.cloud.baseUrl");
        final String CLOUD_ACCESS_KEY = configProperties.getConfigValue("zfj.cloud.accessKey");
        final String CLOUD_ACCOUNT_ID = configProperties.getConfigValue("zfj.cloud.accountId");
        final String CLOUD_SECRET_KEY = configProperties.getConfigValue("zfj.cloud.secretKey");
        JiraCloudClient jiraCloudClient = new JiraCloudClient(CLOUD_ACCOUNT_ID, CLOUD_ACCESS_KEY, CLOUD_SECRET_KEY, CLOUD_BASE_URL);
        String createCloudVersionsUrl = CLOUD_BASE_URL + ApplicationConstants.CLOUD_CREATE_CYCLE_URL;
        String jwt = jiraCloudClient.createJWTToken(HttpMethod.POST, createCloudVersionsUrl);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, jwt);
        headers.set(ApplicationConstants.ZAPI_ACCESS_KEY, CLOUD_ACCESS_KEY);

        HttpEntity<String> entity = new HttpEntity<>(new Gson().toJson(""), headers);
        JsonNode response = null;
        String res = null;
        try {
            response = restTemplate.postForObject(createCloudVersionsUrl, entity, JsonNode.class);
        } catch (Exception e) {
            log.error("Error while creating cycle in cloud " + e.getMessage());
        }
        //read the json node response & prepare cycle bean object.
        ZfjCloudCycleBean zfjCloudCycleBean = new ZfjCloudCycleBean();
        return zfjCloudCycleBean;
    }

    @Override
    public List<CycleDTO> fetchCyclesFromZephyrServer(Long projectId, String serverVersionId, String server_base_url,
                                                      String server_user_name, String server_user_pass, ArrayBlockingQueue<String> progressQueue) {

        TypeReference<Map<String,CycleDTO>> reference = new TypeReference<Map<String,CycleDTO>>() {};
        Map<String,CycleDTO> outputResponse = new HashMap<>();
        List<CycleDTO> cycles = new ArrayList<>();
        try {
            String getCyclesServerUrl = ApplicationConstants.SERVER_GET_CYCLES_URL;
            getCyclesServerUrl = String.format(getCyclesServerUrl, projectId, serverVersionId);
            zapiHttpClient.setResourceName(getCyclesServerUrl);

            ClientResponse response = zapiHttpClient.get();
            String content = response.getEntity(String.class);
            outputResponse = JsonUtil.readValue(content,reference);

        } catch (IOException e) {
            log.error("Error while converting cycle json to object -> ", e.fillInStackTrace());
        }
        outputResponse.forEach((key, cycle) -> {
            if (Objects.nonNull(cycle.getName())) {
                cycle.setId(key);
                cycles.add(cycle);
                try {
                    if(Objects.nonNull(progressQueue)) {
                        progressQueue.put("fetched Cycle from server with data : "+ cycle.toString());
                    }
                    log.debug("Cycle DTO:: "+ cycle.toString());
                } catch (InterruptedException e) {
                    log.error("",e.fillInStackTrace());
                }
            }
        });
        return cycles;
    }

}
