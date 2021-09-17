package com.zephyr.migration.service.impl;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.zephyr.migration.client.HttpClient;
import com.zephyr.migration.client.JiraCloudClient;
import com.zephyr.migration.client.JiraServerClient;
import com.zephyr.migration.dto.AttachmentDTO;
import com.zephyr.migration.dto.CycleDTO;
import com.zephyr.migration.dto.JiraIssueDTO;
import com.zephyr.migration.dto.TestStepResultDTO;
import com.zephyr.migration.model.JiraVersion;
import com.zephyr.migration.model.ZfjCloudAttachmentBean;
import com.zephyr.migration.model.ZfjCloudStepResultBean;
import com.zephyr.migration.service.AttachmentService;
import com.zephyr.migration.service.CycleService;
import com.zephyr.migration.service.TestService;
import com.zephyr.migration.service.VersionService;
import com.zephyr.migration.utils.ApplicationConstants;
import com.zephyr.migration.utils.ConfigProperties;
import com.zephyr.migration.utils.MigrationMappingFileGenerationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class TestServiceImpl implements TestService {

    private static final Logger log = LoggerFactory.getLogger(TestServiceImpl.class);

    @Autowired
    private VersionService versionService;

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private MigrationMappingFileGenerationUtil migrationMappingFileGenerationUtil;

    @Autowired
    private CycleService cycleService;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    @Qualifier(value = "zapiHttpClient")
    private HttpClient zapiHttpClient;

    @Value("${migrationFilePath}")
    private String migrationFilePath;

    @Override
    public Issue getIssueDetailsFromServer(String issueKey) {
        log.info("Serving --> {}", "getIssueDetailsFromServer()");
        log.info("Getting issue details from server of issue key is : " + issueKey);
        JiraServerClient jiraServerClient = new JiraServerClient("admin", "password", "http://15.207.184.119:8089/");
        Issue issue = jiraServerClient.getIssue(issueKey);
        System.out.println(issue.getDescription());
        return issue;
    }

    @Override
    public JiraIssueDTO createIssueInCloud(Issue issue) {
        log.info("Serving --> {}", "createIssueInCloud()");
        /*JiraCloudClient jiraCloudClient = new JiraCloudClient("5f1041fcfe23000022438a56",
                "NzYxNjE2ZGEtYmJhNi0zZGQ0LWIwN2EtNTkwNDRiNTkwNjQ0IDVmMTA0MWZjZmUyMzAwMDAyMjQzOGE1NiBVU0VSX0RFRkFVTFRfTkFNRQ",
                "LsVFf5upvbINJm-__48Y7jFlIjkS8UCWm3KEbeLaF04",
                "https://harshcloud.ngrok.io");*/

        JiraCloudClient jiraCloudClient = new JiraCloudClient("5cdd254faee3080dc2f62ac4",
                "ZTI1YjE1YjctNzBiYi0zNzdkLTg5OGEtYmI4ZDdiYjg0ODU2IDVjZGQyNTRmYWVlMzA4MGRjMmY2MmFjNCBVU0VSX0RFRkFVTFRfTkFNRQ",
                "YxWFgOChDt9y3eOxVijVLkYkr32V39Tj6AJ5Pf31U0w",
                "https://himanshuconnect.ngrok.io");

        return jiraCloudClient.createIssue(prepareRequestObject(issue));
    }


    @Override
    public void createUnscheduledVersion(Long projectId) {
        versionService.createUnscheduledVersionInZephyrCloud(projectId.toString());
    }

    @Override
    public void createVersionInJiraCloud(Long projectId) {
        final String SERVER_USER_NAME = configProperties.getConfigValue("zfj.server.username");
        final String SERVER_USER_PASS = configProperties.getConfigValue("zfj.server.password");
        final String SERVER_BASE_URL = configProperties.getConfigValue("zfj.server.baseUrl");
        Iterable<JiraVersion> versionsFromZephyrServer = versionService.getVersionListFromServer(projectId+"");
        Map<String, Long> serverCloudVersionMapping = new HashMap<>();

        versionsFromZephyrServer.forEach(version -> {
            JsonNode versionNode = versionService.createVersionInZephyrCloud(version,projectId);
            try {
                log.info("created version in cloud : " + new ObjectMapper().writeValueAsString(versionNode));

                if(Objects.nonNull(versionNode) && versionNode.has("id")) {
                    String versionId = Objects.nonNull(version.getId()) ? version.getId() : null;
                    Long cloudVersionId = versionNode.findValue("id").asLong();
                    serverCloudVersionMapping.put(versionId, cloudVersionId);
                }

            } catch (JsonProcessingException e) {
                log.error("Error occurred while creating version in jira cloud.", e.fillInStackTrace());
            }
        });

        migrationMappingFileGenerationUtil.updateVersionMappingFile(projectId, migrationFilePath, serverCloudVersionMapping);
    }

    @Override
    public void fetchCyclesFromServer(Long projectId, Long versionId) {
        final String SERVER_USER_NAME = configProperties.getConfigValue("zfj.server.username");
        final String SERVER_USER_PASS = configProperties.getConfigValue("zfj.server.password");
        final String SERVER_BASE_URL = configProperties.getConfigValue("zfj.server.baseUrl");
        //List<CycleDTO> cycles = cycleService.fetchCyclesFromZephyrServer(projectId, versionId+"", SERVER_BASE_URL, SERVER_USER_NAME, SERVER_USER_PASS, null);

        List<String> listOfServerVersions = new ArrayList<>();
        listOfServerVersions.add("-1");
        listOfServerVersions.add("10000");
        listOfServerVersions.add("10001");
        listOfServerVersions.add("10002");
        listOfServerVersions.add("10100");

        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        Map<String, List<CycleDTO>> zephyrServerCyclesMap1 = new HashMap<>();
        listOfServerVersions.parallelStream().forEachOrdered(version -> {
            log.info("List of cycles for version :: "+version);
            List<CycleDTO> cycles = cycleService.fetchCyclesFromZephyrServer(projectId, version, null);
            zephyrServerCyclesMap1.put(version, cycles);
            log.info("Size of cycles for version :: "+cycles.size());
        });


        log.info("Size of map1 of cycles per version :: "+zephyrServerCyclesMap1.size());
    }

    @Override
    public void initializeHttpClientDetails() {
        zapiHttpClient.init();
    }

    @Override
    public void triggerProjectMetaData(Long projectId) {
        log.info("Serving --> {}", "triggerProjectMetaReindex()");
        final String CLOUD_BASE_URL = configProperties.getConfigValue("zfj.cloud.baseUrl");
        final String CLOUD_ACCESS_KEY = configProperties.getConfigValue("zfj.cloud.accessKey");
        final String CLOUD_ACCOUNT_ID = configProperties.getConfigValue("zfj.cloud.accountId");
        final String CLOUD_SECRET_KEY = configProperties.getConfigValue("zfj.cloud.secretKey");

        JiraCloudClient jiraCloudClient = new JiraCloudClient(CLOUD_ACCOUNT_ID, CLOUD_ACCESS_KEY, CLOUD_SECRET_KEY, CLOUD_BASE_URL);
        String triggerProjectMetaReindexUrl = CLOUD_BASE_URL + ApplicationConstants.CLOUD_PROJECT_META_REINDEX_URL;
        String jwt = jiraCloudClient.createJWTToken(HttpMethod.POST, triggerProjectMetaReindexUrl);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, jwt);
        headers.set(ApplicationConstants.ZAPI_ACCESS_KEY, CLOUD_ACCESS_KEY);
        HttpEntity<String> entity = new HttpEntity<>(new Gson().toJson(projectId), headers);
        try {
            String response = restTemplate.postForObject(triggerProjectMetaReindexUrl, entity, String.class);
        } catch (Exception ex) {
            log.error("Error while calling project meta reindex api call " + ex.fillInStackTrace());
        }
    }

    @Override
    public void importStepResultLevelAttachments(List<TestStepResultDTO> testStepResults, Map<Integer, ZfjCloudStepResultBean> stepResultBeanMap) {

        List<TestStepResultDTO> testStepResultNewList = testStepResults.stream()
                .filter(Objects::nonNull).collect(Collectors.toList());
        testStepResultNewList.forEach(testStepResult -> {
            try {
                List<AttachmentDTO> attachmentList = attachmentService.getAttachmentResponse(testStepResult.getId(), ApplicationConstants.ENTITY_TYPE.TESTSTEPRESULT);
                if(attachmentList != null && attachmentList.size() > 0) {
                    List<AttachmentDTO> stepResultsAttachmentList = attachmentList.stream()
                            .filter(Objects::nonNull).collect(Collectors.toList());
                    List<File> filesToDelete = new ArrayList<>();
                    stepResultsAttachmentList.forEach(stepResultAttachment -> {
                        try {
                            File testStepAttachmentFile = attachmentService.downloadExecutionAttachmentFileFromZFJ(stepResultAttachment.getFileId(), stepResultAttachment.getFileName());
                            if(testStepAttachmentFile != null) {
                                filesToDelete.add(testStepAttachmentFile);
                            }
                        } catch (Exception e) {
                            log.error("Error while downloading the step result attachment for step result -> " + stepResultAttachment.getFileId() + " ->  for step result: " + testStepResult.getId(), e);
                        }
                    });
                    if (!filesToDelete.isEmpty()) {
                        ZfjCloudStepResultBean zfjCloudStepResultBean = stepResultBeanMap.get(testStepResult.getOrderId());
                        int fileCount = 0;
                        for (File file : filesToDelete) {
                            if (file.exists()) {
                                try {
                                    ZfjCloudAttachmentBean zfjCloudAttachmentBean = attachmentService.addAttachmentInCloud(file, zfjCloudStepResultBean.getExecutionId(), "10000", ApplicationConstants.STEP_RESULT_ENTITY, zfjCloudStepResultBean.getId());
                                   if(null != zfjCloudAttachmentBean) {
                                       log.info("file uploaded successfully.");
                                   }
                                } catch (Exception e) {
                                    log.error("Error while adding attachment for issue", e);
                                }
                                file.delete();
                                ++fileCount;
                            }
                        }

                    }
                }
            } catch (Exception e) {
                log.error("Error while uploading the Step Result Attachment for Step Result: " + testStepResult.getId(), e);
            }
        });

    }

    @Override
    public void getVersionsFromJiraCloud(Long projectId) {
        JsonNode response = versionService.getVersionsByJiraFromZephyrCloud(projectId+"");
        if(null != response){
            log.info("Versions from Jira :: "+ response.toString());
        }
        response = versionService.getVersionsFromZephyrCloud(projectId+"");
        if(null != response){
            log.info("Versions from ES :: "+ response);
        }
    }

    private JiraIssueDTO prepareRequestObject(Issue issue) {
        log.info("Serving --> {}", "prepareRequestObject()");
        JiraIssueDTO jiraIssue = new JiraIssueDTO();
        jiraIssue.setSummary(issue.getSummary());
        jiraIssue.setDescription(issue.getDescription());
        //jiraIssue.setProject(ImmutableMap.of("id", issue.getProject().getId()));
        jiraIssue.setProject(ImmutableMap.of("id", 10026));
        jiraIssue.setIssuetype(ImmutableMap.of("id", 10005));
        /*jiraIssue.setProject(ImmutableMap.of("id", 10000));
        jiraIssue.setIssuetype(ImmutableMap.of("id", 10007));*/
        return jiraIssue;
    }
}
