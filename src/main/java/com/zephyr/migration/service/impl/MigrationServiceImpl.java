package com.zephyr.migration.service.impl;

import com.atlassian.jira.rest.client.api.domain.Version;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.zephyr.migration.client.HttpClient;
import com.zephyr.migration.client.JiraCloudClient;
import com.zephyr.migration.dto.CycleDTO;
import com.zephyr.migration.model.ZfjCloudCycleBean;
import com.zephyr.migration.service.CycleService;
import com.zephyr.migration.service.MigrationService;
import com.zephyr.migration.service.ProjectService;
import com.zephyr.migration.service.VersionService;
import com.zephyr.migration.utils.ApplicationConstants;
import com.zephyr.migration.utils.ConfigProperties;
import com.zephyr.migration.utils.FileUtils;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

@Service
public class MigrationServiceImpl implements MigrationService {

    private static final Logger log = LoggerFactory.getLogger(MigrationServiceImpl.class);

    @Autowired
    ConfigProperties configProperties;

    @Autowired
    ProjectService projectService;

    @Autowired
    VersionService versionService;

    @Autowired
    CycleService cycleService;

    @Autowired
    MigrationMappingFileGenerationUtil migrationMappingFileGenerationUtil;

    @Value("${migrationFilePath}")
    private String migrationFilePath;

    @Autowired
    @Qualifier(value = "zapiHttpClient")
    private HttpClient zapiHttpClient;

    private final ArrayBlockingQueue<String> progressQueue = new ArrayBlockingQueue<>(10000);

    @Override
    public void migrateSingleProject(Long projectId) throws Exception{

        final String SERVER_USER_NAME = configProperties.getConfigValue("zfj.server.username");
        final String SERVER_USER_PASS = configProperties.getConfigValue("zfj.server.password");
        final String SERVER_BASE_URL = configProperties.getConfigValue("zfj.server.baseUrl");

        progressQueue.put("########### ########### ########### ########### ###########  ");
        progressQueue.put("Started Migration For project : -> project id: " + projectId + ", date/Time -> " + new Date());

        boolean migrateVersions = beginVersionMigration(projectId, SERVER_BASE_URL, SERVER_USER_NAME, SERVER_USER_PASS, progressQueue);

        if(migrateVersions) {
            boolean migrateCycles = beginCycleMigration(projectId, SERVER_BASE_URL, SERVER_USER_NAME, SERVER_USER_PASS, progressQueue);
        }

        progressQueue.put("Migration of project [" + projectId+ "] completed.");
        log.info("Migration of project [" + projectId+ "] completed.");
        progressQueue.put("########### ########### ########### ########### ###########");
    }

    @Override
    public List<String> getProgressDetails() {
        List<String> progressDetails = new ArrayList<>();
        progressQueue.drainTo(progressDetails);
        return progressDetails;
    }

    @Override
    public void initializeHttpClientDetails() {
        zapiHttpClient.init();
    }

    ///////////////////////////////// Private method goes below ///////////////////////////////////////////////////////

    /**
     * version migration
     */
    private boolean beginVersionMigration(Long projectId, String server_base_url, String server_user_name, String server_user_pass, ArrayBlockingQueue<String> progressQueue) throws IOException, InterruptedException {

        Iterable<Version> versionsFromZephyrServer = versionService.getVersionsFromZephyrServer(projectId, server_base_url, server_user_name, server_user_pass);

        Path path = Paths.get(migrationFilePath, ApplicationConstants.VERSION_MAPPING_FILE_NAME + projectId + ApplicationConstants.XLS);
        if(Files.exists(path)){
            //Logic to read the mapping file & validate whether corresponding cloud & server section exists.
            log.debug("Version Mapping file exists for the given project.");
            progressQueue.put("Version Mapping file exists for the given project.");
            List<String> mappedServerToCloudVersionList = FileUtils.readFile(migrationFilePath, ApplicationConstants.VERSION_MAPPING_FILE_NAME + projectId + ApplicationConstants.XLS);
            if (!mappedServerToCloudVersionList.contains(ApplicationConstants.CLOUD_UNSCHEDULED_VERSION_ID)) {
                log.info("Unscheduled version is not created for this project. Going to create it now !!");
                progressQueue.put("Unscheduled version is not created for this project. Going to create it now !!");
                versionService.createUnscheduledVersionInZephyrCloud(projectId.toString());
                migrationMappingFileGenerationUtil.doEntryOfUnscheduledVersionInExcel(projectId.toString(), migrationFilePath);
            }
            createUnmappedVersionInCloud(versionsFromZephyrServer, mappedServerToCloudVersionList, projectId, migrationFilePath);
            return true;
        }else {
            versionService.createUnscheduledVersionInZephyrCloud(projectId.toString());
            JsonNode versionsFromZephyrCloud = versionService.getVersionsFromZephyrCloud(Long.toString(projectId));
            if(Objects.nonNull(versionsFromZephyrCloud)) {
                /*
                 * 1. Validate the version from server & cloud. If id matches then it's a like copy.
                 * 2. If the server has version which doesn't match in cloud then create as part of unmapped version
                 * call in cloud.
                 * 3. Update the xls mapping file.
                 * 4. trigger the project meta data.
                 */
                migrationMappingFileGenerationUtil.generateVersionMappingReportExcel(migrationFilePath, Long.toString(projectId), versionsFromZephyrServer,versionsFromZephyrCloud);
                List<String> mappedServerToCloudVersionList = FileUtils.readFile(migrationFilePath, ApplicationConstants.VERSION_MAPPING_FILE_NAME + projectId + ApplicationConstants.XLS);
                createUnmappedVersionInCloud(versionsFromZephyrServer, mappedServerToCloudVersionList, projectId, migrationFilePath);
                triggerProjectMetaReindex(projectId);
                return true;
            }else {
                progressQueue.put("Version list from cloud is empty");
                log.warn("Version list from cloud is empty");
                return false;
            }
        }
    }

    /**
     * cycle migration
     */
    private boolean beginCycleMigration(Long projectId, String server_base_url, String server_user_name, String server_user_pass, ArrayBlockingQueue<String> progressQueue) throws InterruptedException, IOException {

        /*
        1. Read the mapping file & get the server-cloud mapping
        2. based on the version list, fetch the cycles from server.
        3. if the mapping file exists, prepare the mapping list. if corresponding server cycle exists in cloud then skip
        else create the unmapped cycles in cloud instance.
        4. if the mapping file doesn't exist then create the cycle data in cloud instance & update the mapping file.
         */

        Path path = Paths.get(migrationFilePath, ApplicationConstants.VERSION_MAPPING_FILE_NAME + projectId + ApplicationConstants.XLS);

        if(Files.exists(path)){
            //Logic to read the mapping file & validate whether corresponding cloud & server section exists.
            log.debug("Version Mapping file exists for the given project.");
            progressQueue.put("Version Mapping file exists for the given project.");
            Map<String, String> mappedServerToCloudVersionMap = FileUtils.readVersionMappingFile(migrationFilePath, ApplicationConstants.VERSION_MAPPING_FILE_NAME + projectId + ApplicationConstants.XLS);

            if(mappedServerToCloudVersionMap.size() > 0) {
                List<String> listOfServerVersions = new ArrayList<>(mappedServerToCloudVersionMap.keySet());
                Map<String, List<CycleDTO>> zephyrServerCyclesMap = new HashMap<>();
                Map<CycleDTO, ZfjCloudCycleBean> zephyrServerCloudCycleMappingMap = new HashMap<>();
                listOfServerVersions.parallelStream().forEach(serverVersionId -> {
                    try {
                        progressQueue.put("Fetching cycles from server for version :: "+ serverVersionId);
                        List<CycleDTO> cyclesListFromServer = cycleService.fetchCyclesFromZephyrServer(projectId, serverVersionId, server_base_url, server_user_name,server_user_pass,progressQueue);
                        zephyrServerCyclesMap.put(serverVersionId, cyclesListFromServer);
                    } catch (Exception ex) {
                        log.error("", ex.fillInStackTrace());
                    }
                });

                mappedServerToCloudVersionMap.forEach((serverVersionId, cloudVersionId) -> {
                    try {
                        progressQueue.put("Creating cycles in zephyr cloud instance for version :: "+ serverVersionId);
                        List<CycleDTO> cyclesListFromServer = zephyrServerCyclesMap.get(serverVersionId);
                        cyclesListFromServer.parallelStream().forEach(cycleDTO -> {
                            if(!cycleDTO.getId().equalsIgnoreCase(ApplicationConstants.AD_HOC_CYCLE_ID)){
                                ZfjCloudCycleBean cloudCycleBean = cycleService.createCycleInZephyrCloud(cycleDTO);
                                zephyrServerCloudCycleMappingMap.put(cycleDTO, cloudCycleBean);
                            }
                        });

                    } catch (Exception ex) {
                        log.error("", ex.fillInStackTrace());
                    }
                });
            }

            return true;
        }

        return false;
    }

    /**
     *
     * @param projectId
     * @throws InterruptedException
     */
    private void triggerProjectMetaReindex(Long projectId) throws InterruptedException {
        log.info("Serving --> {}", "triggerProjectMetaReindex()");
        progressQueue.put("Triggering project meta reindex in cloud.");
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
            restTemplate.postForObject(triggerProjectMetaReindexUrl, entity, String.class);
        } catch (Exception ex) {
            log.error("Error while calling project meta reindex api call " + ex.fillInStackTrace());
        }
    }

    /**
     *
     * @param versionsFromZephyrServer
     * @param mappedServerToCloudVersionList
     * @param projectId
     * @param migrationFilePath
     * @throws InterruptedException
     */
    private void createUnmappedVersionInCloud(Iterable<Version> versionsFromZephyrServer, List<String> mappedServerToCloudVersionList, Long projectId, String migrationFilePath) throws InterruptedException {
        if(Objects.nonNull(versionsFromZephyrServer)) {
            progressQueue.put("Got the versions from JIRA Server.");
            Map<String, Long> serverCloudVersionMapping = new HashMap<>();
            versionsFromZephyrServer.forEach(jiraServerVersion -> {
                try {
                    progressQueue.put("Version Details : "+ jiraServerVersion.getName());
                    String versionId = Objects.nonNull(jiraServerVersion.getId()) ? Long.toString(jiraServerVersion.getId()) : null;
                    if(!mappedServerToCloudVersionList.contains(versionId)) {
                        progressQueue.put("Version Details doesn't exist in cloud, creating the version in cloud instance: "+ jiraServerVersion.getName());
                        JsonNode versionCreatedInCloud = versionService.createVersionInZephyrCloud(jiraServerVersion, projectId);
                        if(Objects.nonNull(versionCreatedInCloud) && versionCreatedInCloud.has("id")) {

                            Long cloudVersionId = versionCreatedInCloud.findValue("id").asLong();
                            progressQueue.put("Version successfully created in cloud instance: "+ new ObjectMapper().writeValueAsString(versionCreatedInCloud));
                            serverCloudVersionMapping.put(versionId, cloudVersionId);
                        }

                    }
                } catch (InterruptedException | JsonProcessingException e) {
                    log.error("", e.fillInStackTrace());
                }
            });
            //TODO: Update the mapping file.
            migrationMappingFileGenerationUtil.updateVersionMappingFile(projectId, migrationFilePath, serverCloudVersionMapping);
        }
    }
}
