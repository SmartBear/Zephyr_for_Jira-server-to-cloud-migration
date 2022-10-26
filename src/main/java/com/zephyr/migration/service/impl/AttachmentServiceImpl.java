package com.zephyr.migration.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.ClientResponse;
import com.zephyr.migration.client.HttpClient;
import com.zephyr.migration.client.JiraCloudClient;
import com.zephyr.migration.dto.AttachmentDTO;
import com.zephyr.migration.model.ZfjCloudAttachmentBean;
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
    public List<AttachmentDTO> getAttachmentResponse(Integer executionId, ApplicationConstants.ENTITY_TYPE entityType) {
        List<AttachmentDTO> executionAttachmentList = new ArrayList<>(0);
        TypeReference<List<AttachmentDTO>> reference = new TypeReference<List<AttachmentDTO>>() {};
        try {
            ClientResponse response = zapiHttpClient.get(String.format(ApplicationConstants.ZAPI_RESOURCE_GET_ATTACHMENT, executionId, entityType.name()));
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
            log.error("Error while converting attachment json to object -> ", e.fillInStackTrace());
        } catch (Exception ex) {
            log.error("Error while fetching attachment json for execution id -> " + executionId, ex.fillInStackTrace());
        }
        return executionAttachmentList;
    }

    @Override
    public File downloadExecutionAttachmentFileFromZFJ(String fileId, String fileName) {
        ClientResponse response = zapiHttpClient.getWithNoContentType(String.format(ApplicationConstants.ZAPI_RESOURCE_GET_ATTACHMENT_FILE_FOR_EXECUTION, fileId));
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
    public ZfjCloudAttachmentBean addAttachmentInCloud(File attachment, String cloudExecutionId, String projectId, String entityName, String entityId) throws Exception {
        ZfjCloudAttachmentBean zfjCloudAttachmentBean = null;
        try{
            int filesize = FileUtils.getFileSizeInMB(attachment);
            if (filesize > 10) {
                log.info("file size for issue " + cloudExecutionId + "will be ignored as size is greater than allowed limits (10MB)");
                return zfjCloudAttachmentBean;
            }
            log.info("Serving --> {}", "addExecutionAttachmentInCloud()");
            final String CLOUD_BASE_URL = configProperties.getConfigValue("zfj.cloud.zapi.endpoint");
            final String CLOUD_ACCESS_KEY = configProperties.getConfigValue("zfj.cloud.accessKey");
            final String CLOUD_ACCOUNT_ID = configProperties.getConfigValue("zfj.cloud.accountId");
            final String CLOUD_SECRET_KEY = configProperties.getConfigValue("zfj.cloud.secretKey");
            JiraCloudClient jiraCloudClient = new JiraCloudClient(CLOUD_ACCOUNT_ID, CLOUD_ACCESS_KEY, CLOUD_SECRET_KEY, CLOUD_BASE_URL);
            String addAttachmentUrl = CLOUD_BASE_URL + ApplicationConstants.ADD_EXECUTION_ATTACHMENT_URL;
            String createUrl = addAttachmentUrl;
            String queryParams = null;
            createUrl = createUrl + "?";
            try {
                queryParams = URLEncoder.encode("entityName=" + entityName + "&entityId=" + entityId + "&projectId=" + projectId  + "&comment=updated by migration" + "&executionId=" + cloudExecutionId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            createUrl = createUrl + queryParams;
            String jwt = jiraCloudClient.createJWTToken(HttpMethod.POST, createUrl);
            addAttachmentUrl = addAttachmentUrl + "?entityName=" + entityName + "&entityId=" + entityId + "&projectId=" + projectId + "&comment=updated by migration" + "&executionId=" + cloudExecutionId ;
            log.debug("addAttachmentUrl :"+addAttachmentUrl);
            MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

            map.add("file", new FileSystemResource(attachment));
            map.add("attachmentFileName", attachment.getName());

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set(HttpHeaders.AUTHORIZATION, jwt);
            headers.set(ApplicationConstants.ZAPI_ACCESS_KEY, CLOUD_ACCESS_KEY);

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity(map, headers);
            ResponseEntity<JsonNode> response = restTemplate.exchange(addAttachmentUrl, HttpMethod.POST, entity, JsonNode.class);
            //read the json node response & prepare attachment bean object.
            zfjCloudAttachmentBean = new ZfjCloudAttachmentBean();
                if (response != null) {
                    JsonNode responseBody = response.getBody();
                    if (response.getBody() != null) {
                        zfjCloudAttachmentBean.setCloudExecutionId(responseBody.findValue("entityId").asText());
                        zfjCloudAttachmentBean.setCloudExecutionAttachmentId(responseBody.findValue("id").asText());
                    }
                }
            log.info("add attachment response is : " + response.getBody());
        }catch (Exception e){
            log.error("Exception while creating attachments.{}","Method: sendAttachments()",e);
            throw new Exception(e);
        }
        return zfjCloudAttachmentBean;
    }
}
