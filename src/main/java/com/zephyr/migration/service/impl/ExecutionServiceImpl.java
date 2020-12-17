package com.zephyr.migration.service.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.ClientResponse;
import com.zephyr.migration.client.HttpClient;
import com.zephyr.migration.dto.CycleDTO;
import com.zephyr.migration.dto.ExecutionDTO;
import com.zephyr.migration.dto.FolderDTO;
import com.zephyr.migration.service.ExecutionService;
import com.zephyr.migration.utils.ApplicationConstants;
import com.zephyr.migration.utils.ConfigProperties;
import com.zephyr.migration.utils.JsonUtil;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ExecutionServiceImpl implements ExecutionService {

    private static final Logger log = LoggerFactory.getLogger(ExecutionServiceImpl.class);

    @Autowired
    ConfigProperties configProperties;

    @Autowired
    @Qualifier(value = "zapiHttpClient")
    private HttpClient zapiHttpClient;

    @Override
    public List<ExecutionDTO> getExecutionsFromZFJByVersionAndCycleName(String projectId, String versionId, String cycleId, int offset, int maxRecords) {
        JsonParser parser = new JsonParser();
        TypeReference<List<ExecutionDTO>> executionReference = new TypeReference<List<ExecutionDTO>>() {};
        List<ExecutionDTO> executions = new ArrayList<>();
        try {
            zapiHttpClient.setResourceName(String.format(ApplicationConstants.ZAPI_RESOURCE_GET_EXECUTIONS, projectId, versionId, cycleId, offset, maxRecords));
            ClientResponse response = zapiHttpClient.get();
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
    public List<ExecutionDTO> getExecutionsFromZFJByVersionCycleAndFolderName(CycleDTO cycle, FolderDTO folder, int offset, int maxRecords) {
        JsonParser parser = new JsonParser();
        TypeReference<List<ExecutionDTO>> executionReference = new TypeReference<List<ExecutionDTO>>() {};
        List<ExecutionDTO> executions = new ArrayList<>();
        try {
            zapiHttpClient.setResourceName(String.format(ApplicationConstants.ZAPI_RESOURCE_GET_FOLDER_EXECUTIONS, cycle.getProjectId(), cycle.getVersionId(), cycle.getId(), folder.getFolderId(), offset, maxRecords));
            ClientResponse response = zapiHttpClient.get();
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
}
