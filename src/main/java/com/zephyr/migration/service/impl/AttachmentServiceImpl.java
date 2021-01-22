package com.zephyr.migration.service.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.ClientResponse;
import com.zephyr.migration.client.HttpClient;
import com.zephyr.migration.dto.ExecutionAttachmentDTO;
import com.zephyr.migration.service.AttachmentService;
import com.zephyr.migration.utils.ApplicationConstants;
import com.zephyr.migration.utils.JsonUtil;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class AttachmentServiceImpl implements AttachmentService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentServiceImpl.class);

    @Autowired
    @Qualifier(value = "zapiHttpClient")
    private HttpClient zapiHttpClient;

    @Override
    public List<ExecutionAttachmentDTO> getAttachmentResponse(Integer executionId, ApplicationConstants.ENTITY_TYPE entityType) {
        zapiHttpClient.setResourceName(String.format(ApplicationConstants.ZAPI_RESOURCE_GET_ATTACHMENT, executionId, entityType.name()));
        List<ExecutionAttachmentDTO> executionAttachmentList = new ArrayList<>(0);
        TypeReference<List<ExecutionAttachmentDTO>> reference = new TypeReference<List<ExecutionAttachmentDTO>>() {};
        try {
            ClientResponse response = zapiHttpClient.get();
            String content = response.getEntity(String.class);
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(content);
            if(!element.isJsonNull() && element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if(!object.isJsonNull()) {
                    JsonArray attachmentArray = object.getAsJsonArray("data");
                    if (Objects.nonNull(attachmentArray)) {
                        executionAttachmentList = JsonUtil.readValue(attachmentArray.toString(), reference);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Error while converting attachment json to object -> ", e);
        }
        return executionAttachmentList;
    }

    @Override
    public File downloadExecutionAttachmentFileFromZFJ(String fileId, String fileName) {
        zapiHttpClient.setResourceName(String.format(ApplicationConstants.ZAPI_RESOURCE_GET_ATTACHMENT_FILE_FOR_EXECUTION, fileId));
        ClientResponse response = zapiHttpClient.getWithNoContentType();
        InputStream inputStream = response.getEntityInputStream();
        return getFileFromStream(fileName, inputStream);
    }

    private File getFileFromStream(String fileName, InputStream inputStream) {
        File file = new File(fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            IOUtils.copyLarge(inputStream,fos);
        } catch (IOException e) {
            log.error("Error occurred while reading the file -> " + file.getAbsolutePath(), e);
        } catch (Exception e) {
            log.error("Exception writing File. File Path:" + file.getAbsolutePath() + " with Error::", e);
        } finally {
            if(Objects.nonNull(fos)) {
                try {
                    fos.close();
                } catch(IOException e) {
                    log.error("Error occurred while closing the file.");
                }
            }
        }
        return file;
    }
}
