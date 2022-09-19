package com.zephyr.migration.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.zephyr.migration.client.HttpClient;
import com.zephyr.migration.client.JiraCloudClient;
import com.zephyr.migration.dto.FolderDTO;
import com.zephyr.migration.model.SearchRequest;
import com.zephyr.migration.model.ZfjCloudFolderBean;
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
    public ZfjCloudFolderBean createFolderInZephyrCloud(FolderDTO folderDTO, SearchRequest searchFolderRequest) {
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

        HttpEntity<String> entity = new HttpEntity<>(new Gson().toJson(prepareRequestToCreateFolder(folderDTO, searchFolderRequest)), headers);
        JsonNode response;
        ZfjCloudFolderBean zfjCloudFolderBean = new ZfjCloudFolderBean();
        try {
            response = restTemplate.postForObject(createCloudFolderUrl, entity, JsonNode.class);
            //read the json node response & prepare folder bean object.
            if (response != null && !response.isEmpty()) {
                zfjCloudFolderBean.setName(response.findValue("name").asText());
                zfjCloudFolderBean.setId(response.findValue("id").asText());
                zfjCloudFolderBean.setProjectId(response.findValue("projectId").asLong());
                zfjCloudFolderBean.setVersionId(response.findValue("versionId").asLong());
                zfjCloudFolderBean.setCycleId(response.findValue("cycleId").asText());
                zfjCloudFolderBean.setCycleName(searchFolderRequest.getCycleName());
            }
            log.info("Create folder response from cloud endpoint: "+response);
        } catch (Exception e) {
            log.error("Error while creating folder in cloud " + e.getMessage());
            return null;
        }
        return zfjCloudFolderBean;
    }

    @Override
    public List<FolderDTO> fetchFoldersFromZephyrServer(Long cycleId, String projectId, String versionId, ArrayBlockingQueue<String> progressQueue) {

        TypeReference<List<FolderDTO>> reference = new TypeReference<List<FolderDTO>>() {};
        List<FolderDTO> folders = new ArrayList<>();
        try {
            ClientResponse response = zapiHttpClient.get(String.format(ApplicationConstants.SERVER_GET_FOLDERS_URL,cycleId,projectId,versionId,0,3000));
            String content = response.getEntity(String.class);
            folders = JsonUtil.readValue(content,reference);
        } catch (IOException | ClientHandlerException e) {
            log.error("Error while getting folder response from server -> ", e.fillInStackTrace());
        }
        return folders;
    }

    private ZfjCloudFolderBean prepareRequestToCreateFolder(FolderDTO folderDTO, SearchRequest searchFolderRequest) {
        ZfjCloudFolderBean folderCycleBean = new ZfjCloudFolderBean();
        folderCycleBean.setName(folderDTO.getFolderName());
        folderCycleBean.setDescription(folderDTO.getFolderDescription());
        folderCycleBean.setCycleId(searchFolderRequest.getCloudCycleId());
        folderCycleBean.setVersionId(Long.parseLong(searchFolderRequest.getCloudVersionId()));
        folderCycleBean.setProjectId(Long.parseLong(searchFolderRequest.getProjectId()));
        return folderCycleBean;
    }

}
