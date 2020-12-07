package com.zephyr.migration.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;
import com.zephyr.migration.client.HttpClient;
import com.zephyr.migration.client.JiraCloudClient;
import com.zephyr.migration.dto.CycleDTO;
import com.zephyr.migration.dto.FolderDTO;
import com.zephyr.migration.model.ZfjCloudCycleBean;
import com.zephyr.migration.model.ZfjCloudFolderBean;
import com.zephyr.migration.service.CycleService;
import com.zephyr.migration.service.FolderService;
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
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

@Service
public class FolderServiceImpl implements FolderService {

    private static final Logger log = LoggerFactory.getLogger(FolderServiceImpl.class);

    @Autowired
    ConfigProperties configProperties;

    @Autowired
    @Qualifier(value = "zapiHttpClient")
    private HttpClient zapiHttpClient;

    @Override
    public ZfjCloudFolderBean createFolderInZephyrCloud(FolderDTO folderDTO) {
        log.info("Serving --> {}", "createFolderInZephyrCloud()");
        final String CLOUD_BASE_URL = configProperties.getConfigValue("zfj.cloud.baseUrl");
        final String CLOUD_ACCESS_KEY = configProperties.getConfigValue("zfj.cloud.accessKey");
        final String CLOUD_ACCOUNT_ID = configProperties.getConfigValue("zfj.cloud.accountId");
        final String CLOUD_SECRET_KEY = configProperties.getConfigValue("zfj.cloud.secretKey");
        JiraCloudClient jiraCloudClient = new JiraCloudClient(CLOUD_ACCOUNT_ID, CLOUD_ACCESS_KEY, CLOUD_SECRET_KEY, CLOUD_BASE_URL);
        String createCloudFolderUrl = CLOUD_BASE_URL + ApplicationConstants.CLOUD_CREATE_FOLDER_URL;
        String jwt = jiraCloudClient.createJWTToken(HttpMethod.POST, createCloudFolderUrl);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, jwt);
        headers.set(ApplicationConstants.ZAPI_ACCESS_KEY, CLOUD_ACCESS_KEY);

        HttpEntity<String> entity = new HttpEntity<>(new Gson().toJson(prepareRequestToCreateFolder(folderDTO)), headers);
        JsonNode response = null;
        ZfjCloudFolderBean zfjCloudFolderBean = new ZfjCloudFolderBean();
        try {
            response = restTemplate.postForObject(createCloudFolderUrl, entity, JsonNode.class);
            //read the json node response & prepare cycle bean object.
            if (response != null && !response.isEmpty()) {
                zfjCloudFolderBean.setName(response.findValue("name").asText());
                zfjCloudFolderBean.setId(response.findValue("id").asText());
                zfjCloudFolderBean.setProjectId(response.findValue("projectId").asLong());
                zfjCloudFolderBean.setVersionId(response.findValue("versionId").asLong());
                zfjCloudFolderBean.setCycleId(response.findValue("cycleId").asText());
            }
        } catch (Exception e) {
            log.error("Error while creating folder in cloud " + e.getMessage());
            return null;
        }
        return zfjCloudFolderBean;
    }

    private ZfjCloudFolderBean prepareRequestToCreateFolder(FolderDTO folderDTO) {
        ZfjCloudFolderBean folderCycleBean = new ZfjCloudFolderBean();
        folderCycleBean.setName(folderDTO.getName());
        folderCycleBean.setDescription(folderDTO.getDescription());
        folderCycleBean.setCycleId(folderDTO.getCycleId());
        folderCycleBean.setVersionId(Long.valueOf(folderDTO.getVersionId()));
        folderCycleBean.setProjectId(Long.valueOf(folderDTO.getProjectId()));
        //cloudCycleBean.setStartDate(new Date());
        //cloudCycleBean.setEndDate(new Date());
        return folderCycleBean;
    }

    @Override
    public List<FolderDTO> fetchFoldersFromZephyrServer(Long cycleId, String server_base_url,
                                                      String server_user_name, String server_user_pass) {

        TypeReference<Map<String,CycleDTO>> reference = new TypeReference<Map<String,CycleDTO>>() {};
        Map<String,CycleDTO> outputResponse = new HashMap<>();
        List<CycleDTO> cycles = new ArrayList<>();
        List<FolderDTO> folderDTOs = new ArrayList<>();
        try {
            String getCyclesServerUrl = ApplicationConstants.SERVER_GET_FOLDERS_URL;
            getCyclesServerUrl = String.format(getCyclesServerUrl, cycleId);
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
                /*try {
                    *//*if(Objects.nonNull(progressQueue)) {
                        progressQueue.put("fetched Cycle from server with data : "+ cycle.toString());
                    }*//*
                    return folderDTOs;
                    log.debug("Cycle DTO:: "+ cycle.toString());
                } catch (InterruptedException e) {
                    log.error("",e.fillInStackTrace());
                }*/
            }
        });
        return folderDTOs;
    }

}
