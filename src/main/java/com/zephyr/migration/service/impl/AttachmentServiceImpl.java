package com.zephyr.migration.service.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.ClientResponse;
import com.zephyr.migration.client.HttpClient;
import com.zephyr.migration.client.JiraCloudClient;
import com.zephyr.migration.dto.ExecutionAttachmentDTO;
import com.zephyr.migration.service.AttachmentService;
import com.zephyr.migration.utils.ApplicationConstants;
import com.zephyr.migration.utils.ConfigProperties;
import com.zephyr.migration.utils.FileUtils;
import com.zephyr.migration.utils.JsonUtil;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AttachmentServiceImpl implements AttachmentService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentServiceImpl.class);

    @Autowired
    @Qualifier(value = "zapiHttpClient")
    private HttpClient zapiHttpClient;

    @Autowired
    ConfigProperties configProperties;

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

    @Override
    public void addExecutionAttachmentInCloud(File attachment, String cloudExecutionId, String projectId) throws Exception {
        try{
            int filesize = FileUtils.getFileSizeInMB(attachment);
            if (filesize > 10) {
                log.info("file size for issue " + cloudExecutionId + "will be ignored as size is greater than allowed limits (10MB)");
                return;
            }
            log.info("Serving --> {}", "addExecutionAttachmentInCloud()");
            final String CLOUD_BASE_URL = configProperties.getConfigValue("zfj.cloud.baseUrl");
            final String CLOUD_ACCESS_KEY = configProperties.getConfigValue("zfj.cloud.accessKey");
            final String CLOUD_ACCOUNT_ID = configProperties.getConfigValue("zfj.cloud.accountId");
            final String CLOUD_SECRET_KEY = configProperties.getConfigValue("zfj.cloud.secretKey");
            JiraCloudClient jiraCloudClient = new JiraCloudClient(CLOUD_ACCOUNT_ID, CLOUD_ACCESS_KEY, CLOUD_SECRET_KEY, CLOUD_BASE_URL);
            String addAttachmentUrl = CLOUD_BASE_URL + ApplicationConstants.ADD_EXECUTION_ATTACHMENT_URL;
            String createUrl = addAttachmentUrl;
            String queryParams = null;
            createUrl = createUrl + "?";
            try {
                queryParams = URLEncoder.encode("entityName=execution&entityId=" + cloudExecutionId + "&projectId=" + projectId  + "&comment=updated by migration", "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            createUrl = createUrl + queryParams;
            String jwt = jiraCloudClient.createJWTToken(HttpMethod.POST, createUrl);
            addAttachmentUrl = addAttachmentUrl + "?entityName=execution&entityId=" + cloudExecutionId + "&projectId=" + projectId + "&comment=updated by migration";
            MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

            map.add("file", new FileSystemResource(attachment));
            map.add("attachmentFileName", attachment.getName());

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set(HttpHeaders.AUTHORIZATION, jwt);
            headers.set(ApplicationConstants.ZAPI_ACCESS_KEY, CLOUD_ACCESS_KEY);
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restTemplate.exchange(addAttachmentUrl, HttpMethod.POST, entity, String.class);
            log.info("add attachment response is : " + response.getBody());
        }catch (Exception e){
            log.error("Exception while creating attachments.{}","Method: sendAttachments()",e);
            throw new Exception(e);
        }
    }
}
