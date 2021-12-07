package com.zephyr.migration.service.impl;

import com.atlassian.jira.rest.client.api.domain.Project;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.zephyr.migration.client.HttpClient;
import com.zephyr.migration.client.JiraCloudClient;
import com.zephyr.migration.dto.*;
import com.zephyr.migration.model.*;
import com.zephyr.migration.service.*;
import com.zephyr.migration.utils.ApplicationConstants;
import com.zephyr.migration.utils.ConfigProperties;
import com.zephyr.migration.utils.FileUtils;
import com.zephyr.migration.utils.MigrationMappingFileGenerationUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@Service
public class MigrationServiceImpl implements MigrationService {

    private static final Logger log = LoggerFactory.getLogger(MigrationServiceImpl.class);
    private static final String UNEXECUTED_STATUS = "-1";

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private VersionService versionService;

    @Autowired
    private CycleService cycleService;

    @Autowired
    private FolderService folderService;

    @Autowired
    private ExecutionService executionService;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private TestStepService testStepService;

    @Autowired
    private IssueService issueService;

    @Autowired
    private DefectLinkService defectLinkService;

    @Autowired
    private MigrationMappingFileGenerationUtil migrationMappingFileGenerationUtil;

    @Value("${migrationFilePath}")
    private String migrationFilePath;

    @Autowired
    @Qualifier(value = "zapiHttpClient")
    private HttpClient zapiHttpClient;

    @Autowired
    @Qualifier(value = "jiraHttpClient")
    private HttpClient jiraHttpClient;

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
            if(migrateCycles) {
                boolean migrateFolders = beginFolderMigration(projectId, SERVER_BASE_URL, SERVER_USER_NAME, SERVER_USER_PASS, progressQueue);

                if (migrateFolders) {
                    beginExecutionMigration(projectId,SERVER_BASE_URL,SERVER_USER_NAME,SERVER_USER_PASS,progressQueue);
                    beginAttachmentsEntityMigration(projectId,SERVER_BASE_URL,SERVER_USER_NAME,SERVER_USER_PASS,progressQueue);
                }
            }
        }

        final String MIGRATE_EXECUTION_LEVEL_DEFECT_FLAG = configProperties.getConfigValue("migrate.execution.level.defect");
        boolean isMigrateExecutionLevelDefect = Boolean.parseBoolean(MIGRATE_EXECUTION_LEVEL_DEFECT_FLAG);

        if(isMigrateExecutionLevelDefect) {
            beginExecutionLevelDefectMigration(projectId,SERVER_BASE_URL,SERVER_USER_NAME,SERVER_USER_PASS);
        }

        final String MIGRATE_STEP_RESULT_LEVEL_DEFECT_FLAG = configProperties.getConfigValue("migrate.step.result.level.defect");
        boolean isMigrateStepResultLevelDefect = Boolean.parseBoolean(MIGRATE_STEP_RESULT_LEVEL_DEFECT_FLAG);

        if(isMigrateStepResultLevelDefect) {
            beginStepResultLevelDefectMigration(projectId,SERVER_BASE_URL,SERVER_USER_NAME,SERVER_USER_PASS);
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
        jiraHttpClient.init();
    }

    ///////////////////////////////// Private method goes below ///////////////////////////////////////////////////////

    /**
     * version migration
     */
    private boolean beginVersionMigration(Long projectId, String server_base_url, String server_user_name, String server_user_pass, ArrayBlockingQueue<String> progressQueue) throws IOException, InterruptedException {

       // Iterable<Version> versionsFromZephyrServer = versionService.getVersionsFromZephyrServer(projectId, server_base_url, server_user_name, server_user_pass);

        Iterable<JiraVersion> versionsFromZephyrServer = versionService.getVersionListFromServer(String.valueOf(projectId));

        versionsFromZephyrServer.forEach(v -> {
            log.info("Version name:: "+v.getName() + " id:"+v.getId());
        });


        Path path = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_VERSION_FILE_NAME + projectId + ApplicationConstants.XLS);
        if(Files.exists(path)){
            //Logic to read the mapping file & validate whether corresponding cloud & server section exists.
            log.info("Version Mapping file exists for the given project.");
            progressQueue.put("Version Mapping file exists for the given project.");
            List<String> mappedServerToCloudVersionList = FileUtils.readFile(migrationFilePath, ApplicationConstants.MAPPING_VERSION_FILE_NAME + projectId + ApplicationConstants.XLS);
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
                List<String> mappedServerToCloudVersionList = FileUtils.readFile(migrationFilePath, ApplicationConstants.MAPPING_VERSION_FILE_NAME + projectId + ApplicationConstants.XLS);
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

        Project project = projectService.getProject(projectId, server_base_url,server_user_name,server_user_pass);
        String projectName = null;
        if (project != null) {
            projectName = project.getName();
        }

        Path path = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_VERSION_FILE_NAME + projectId + ApplicationConstants.XLS);

        if(Files.exists(path)){
            //Logic to read the mapping file & validate whether corresponding cloud & server section exists.
            log.debug("Version Mapping file exists for the given project.");
            progressQueue.put("Version Mapping file exists for the given project.");
            Map<String, String> mappedServerToCloudVersionMap = FileUtils.readVersionMappingFile(migrationFilePath, ApplicationConstants.MAPPING_VERSION_FILE_NAME + projectId + ApplicationConstants.XLS);

            if(mappedServerToCloudVersionMap.size() > 0) {
                List<String> listOfServerVersions = new ArrayList<>(mappedServerToCloudVersionMap.keySet());
                Map<String, List<CycleDTO>> zephyrServerCyclesMap = new HashMap<>();
                Map<CycleDTO, ZfjCloudCycleBean> zephyrServerCloudCycleMappingMap = new HashMap<>();
                listOfServerVersions.parallelStream().forEachOrdered(serverVersionId -> {
                    try {
                        progressQueue.put("Fetching cycles from server for version :: "+ serverVersionId);
                        log.info("Fetching cycles from server for version :: "+ serverVersionId);
                        List<CycleDTO> cyclesListFromServer = cycleService.fetchCyclesFromZephyrServer(projectId, serverVersionId, progressQueue);
                        zephyrServerCyclesMap.put(serverVersionId, cyclesListFromServer);
                        progressQueue.put("Fetched cycles from server for version :: "+ serverVersionId);
                        log.info("Fetched cycles from server for version :: "+ serverVersionId);
                    } catch (Exception ex) {
                        log.error("", ex.fillInStackTrace());
                    }
                });

                Path cycleMappedFile = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_CYCLE_FILE_NAME + projectId + ApplicationConstants.XLS);

                String finalProjectName = projectName;
                mappedServerToCloudVersionMap.forEach((serverVersionId, cloudVersionId) -> {
                    try {
                        progressQueue.put("Creating cycles in zephyr cloud instance for version :: "+ serverVersionId);
                        List<CycleDTO> cyclesListFromServer = zephyrServerCyclesMap.get(serverVersionId);
                        if (!Files.exists(cycleMappedFile)) {
                            cyclesListFromServer.forEach(cycleDTO -> {
                                if (!cycleDTO.getId().equalsIgnoreCase(ApplicationConstants.AD_HOC_CYCLE_ID)) {
                                    log.info("version info :: " + mappedServerToCloudVersionMap.get(cycleDTO.getVersionId()));
                                    cycleDTO.setCloudVersionId(mappedServerToCloudVersionMap.get(cycleDTO.getVersionId()));
                                    ZfjCloudCycleBean cloudCycleBean = cycleService.createCycleInZephyrCloud(cycleDTO);
                                    if (Objects.nonNull(cloudCycleBean)) {
                                        zephyrServerCloudCycleMappingMap.put(cycleDTO, cloudCycleBean);
                                    }
                                } else {
                                    /*Add adhoc cycle for mapping file*/
                                    zephyrServerCloudCycleMappingMap.put(cycleDTO, prepareAdhocCycleResponse(projectId,mappedServerToCloudVersionMap.get(cycleDTO.getVersionId())));
                                }
                            });
                        }else {
                            progressQueue.put("Cycle mapping file exists, going to create the unmapped cycles.");
                            createUnmappedCycleInCloud(mappedServerToCloudVersionMap, cyclesListFromServer, projectId, finalProjectName, migrationFilePath);
                        }
                    } catch (Exception ex) {
                        log.error("", ex.fillInStackTrace());
                    }
                });
                if (!zephyrServerCloudCycleMappingMap.isEmpty()) {
                    progressQueue.put("Creating the mapping file for cycle migration.");
                    migrationMappingFileGenerationUtil.generateCycleMappingReportExcel(zephyrServerCloudCycleMappingMap,
                            projectId.toString(), projectName, migrationFilePath);
                    return true;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * folder migration
     */
    private boolean beginFolderMigration(Long projectId, String server_base_url, String server_user_name, String server_user_pass, ArrayBlockingQueue<String> progressQueue) throws IOException, InterruptedException {
        Project project = projectService.getProject(projectId, server_base_url,server_user_name,server_user_pass);
        String projectName = null;
        if (project != null) {
            projectName = project.getName();
        }

        Path cycleMappedFile = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_CYCLE_FILE_NAME + projectId + ApplicationConstants.XLS);
        if (Files.exists(cycleMappedFile)) {
            Map<FolderDTO, ZfjCloudFolderBean> zephyrServerCloudFolderMappingMap = new HashMap<>();
            Path folderMappedFile = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_FOLDER_FILE_NAME + projectId + ApplicationConstants.XLS);
            Map<String, SearchRequest> mappedServerToCloudCycleMap = FileUtils.readCycleMappingFile(migrationFilePath, ApplicationConstants.MAPPING_CYCLE_FILE_NAME + projectId + ApplicationConstants.XLS);
            if(mappedServerToCloudCycleMap.size() > 0) {
                    List<String> listOfServerCycles = new ArrayList<>(mappedServerToCloudCycleMap.keySet());
                    Map<String, List<FolderDTO>> zephyrServerCycleFolderMap = new HashMap<>();
                    listOfServerCycles.parallelStream().forEachOrdered(cycleId -> {
                        try {
                            String serverCycleId = "";
                            if(cycleId.contains("_")) {
                                serverCycleId = cycleId.substring(0,cycleId.indexOf("_"));
                                progressQueue.put("fetching folders from zephyr server instance for cycle :: " + serverCycleId);
                                log.info("Fetching folders from server for cycleId :: " + serverCycleId);
                                SearchRequest searchFolderRequest = mappedServerToCloudCycleMap.get(cycleId);
                                if(null != searchFolderRequest) {
                                    log.info("Fetching cycles from server with details :: "+ searchFolderRequest.toString());
                                    if(!serverCycleId.equalsIgnoreCase(ApplicationConstants.AD_HOC_CYCLE_ID)) {
                                        List<FolderDTO> foldersListFromServer = folderService.fetchFoldersFromZephyrServer(Long.parseLong(serverCycleId),
                                                searchFolderRequest.getProjectId(), searchFolderRequest.getVersionId(), progressQueue);
                                        zephyrServerCycleFolderMap.put(serverCycleId, foldersListFromServer);
                                        log.info("Fetched folders from server for version :: "+ serverCycleId);
                                        progressQueue.put("Fetched folders from server for version -> "+serverCycleId);
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            log.error("", ex.fillInStackTrace());
                        }
                    });

                String finalProjectName = projectName;
                mappedServerToCloudCycleMap.forEach((cycleId, searchFolderRequest) -> {
                        try {
                            String serverCycleId = "";
                            if(cycleId.contains("_")) {
                                serverCycleId = cycleId.substring(0, cycleId.indexOf("_"));
                                if(!serverCycleId.equalsIgnoreCase(ApplicationConstants.AD_HOC_CYCLE_ID)) {

                                    progressQueue.put("creating folders from zephyr server instance for cycle :: "+ serverCycleId);
                                    log.info("fetching folder list from map for cycle id :: "+ serverCycleId);

                                    List<FolderDTO> foldersListFromServer = zephyrServerCycleFolderMap.get(serverCycleId);
                                    log.info("fetching folder list from map for cycle id with size :: "+ foldersListFromServer.size());
                                    if (!Files.exists(folderMappedFile)) {
                                        foldersListFromServer.parallelStream().forEachOrdered(folderDTO -> {
                                            try {
                                                progressQueue.put("creating folder in zephyr cloud instance with name :: " + folderDTO.getFolderName());
                                                progressQueue.put("creating folder in zephyr cloud instance for cycle :: " + searchFolderRequest.getCloudCycleId());
                                                log.info("creating folder in zephyr cloud instance with name :: " + folderDTO.getFolderName());
                                                log.info("creating folder in zephyr cloud instance for cycle :: " + searchFolderRequest.getCloudCycleId());
                                            } catch (InterruptedException ex) {
                                                log.error("InterruptedException ", ex.fillInStackTrace());
                                            }
                                            ZfjCloudFolderBean cloudFolderBean = folderService.createFolderInZephyrCloud(folderDTO, searchFolderRequest);
                                            if (Objects.nonNull(cloudFolderBean)) {
                                                zephyrServerCloudFolderMappingMap.put(folderDTO, cloudFolderBean);
                                            }
                                        });
                                    }else {
                                        progressQueue.put("Folder mapping file exists for the project, creating unmapped folders in cloud.");
                                        createUnmappedFolderInCloud(mappedServerToCloudCycleMap, foldersListFromServer, projectId, finalProjectName,migrationFilePath);
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            log.error("", ex.fillInStackTrace());
                        }
                    });

                    if (!zephyrServerCloudFolderMappingMap.isEmpty()) {
                        progressQueue.put("Creating the mapping file for folder migration.");
                        migrationMappingFileGenerationUtil.generateFolderMappingReportExcel(zephyrServerCloudFolderMappingMap, projectId.toString(), finalProjectName, migrationFilePath);
                        return true;
                    }
            }
        }
        return true;
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
            String response = restTemplate.postForObject(triggerProjectMetaReindexUrl, entity, String.class);
            log.info("Trigger project meta reindex call is completed.");
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
    private void createUnmappedVersionInCloud(Iterable<JiraVersion> versionsFromZephyrServer, List<String> mappedServerToCloudVersionList, Long projectId, String migrationFilePath) throws InterruptedException {
        if(Objects.nonNull(versionsFromZephyrServer)) {
            progressQueue.put("Got the versions from JIRA Server.");
            Map<String, Long> serverCloudVersionMapping = new HashMap<>();
            versionsFromZephyrServer.forEach(jiraServerVersion -> {
                try {
                    progressQueue.put("Version Details : "+ jiraServerVersion.getName());
                    String versionId = Objects.nonNull(jiraServerVersion.getId()) ? jiraServerVersion.getId() : null;
                    if(!mappedServerToCloudVersionList.contains(versionId)) {
                        log.info("Version Details doesn't exist in cloud, creating the version in cloud instance:"+ jiraServerVersion.getName());
                        progressQueue.put("Version Details doesn't exist in cloud, creating the version in cloud instance: "+ jiraServerVersion.getName());
                        JsonNode versionCreatedInCloud = versionService.createVersionInZephyrCloud(jiraServerVersion, projectId);
                        if(Objects.nonNull(versionCreatedInCloud) && versionCreatedInCloud.has("id")) {

                            Long cloudVersionId = versionCreatedInCloud.findValue("id").asLong();
                            log.info("Version successfully created in cloud instance: "+ new ObjectMapper().writeValueAsString(versionCreatedInCloud));
                            progressQueue.put("Version successfully created in cloud instance: "+ new ObjectMapper().writeValueAsString(versionCreatedInCloud));
                            serverCloudVersionMapping.put(versionId, cloudVersionId);
                        }
                    }
                } catch (InterruptedException | JsonProcessingException e) {
                    log.info("Version creation failed in cloud instance: "+e.fillInStackTrace());
                    log.error("", e.fillInStackTrace());
                }
            });
            //Update the mapping file.
            migrationMappingFileGenerationUtil.updateVersionMappingFile(projectId, migrationFilePath, serverCloudVersionMapping);
        }
    }


    /**
     *
     * @param projectId
     * @param versionId
     * @return
     */
    private ZfjCloudCycleBean prepareAdhocCycleResponse(Long projectId, String versionId) {
        ZfjCloudCycleBean adhocCycle = new ZfjCloudCycleBean();
        adhocCycle.setId(ApplicationConstants.AD_HOC_CYCLE_ID);
        adhocCycle.setProjectId(projectId);
        adhocCycle.setVersionId(Long.parseLong(versionId));
        adhocCycle.setName(ApplicationConstants.AD_HOC_CYCLE_NAME);
        return adhocCycle;
    }


    /**
     *
     * @param mappedServerToCloudVersionMap
     * @param cyclesListFromServer
     * @param projectId
     * @param migrationFilePath
     * @throws IOException
     * @throws InterruptedException
     */
    private void createUnmappedCycleInCloud(Map<String, String> mappedServerToCloudVersionMap, List<CycleDTO> cyclesListFromServer, Long projectId, String projectName, String migrationFilePath)  {
        if(!cyclesListFromServer.isEmpty()) {
            Map<CycleDTO, ZfjCloudCycleBean> zephyrServerCloudCycleMappingMap = new HashMap<>();
                cyclesListFromServer.forEach(cycleDTO -> {
                    if (!cycleDTO.getId().equalsIgnoreCase(ApplicationConstants.AD_HOC_CYCLE_ID)) {
                        String cloudVersionIdFromVersionMappingFile = mappedServerToCloudVersionMap.get(cycleDTO.getVersionId());
                        try {
                            Boolean cycleMoved = FileUtils.readCycleMappingFile(migrationFilePath, ApplicationConstants.MAPPING_CYCLE_FILE_NAME + projectId + ApplicationConstants.XLS, cloudVersionIdFromVersionMappingFile, cycleDTO.getId());
                            if (!cycleMoved) {
                                log.info("version info :: " + mappedServerToCloudVersionMap.get(cycleDTO.getVersionId()));
                                cycleDTO.setCloudVersionId(mappedServerToCloudVersionMap.get(cycleDTO.getVersionId()));
                                ZfjCloudCycleBean cloudCycleBean = cycleService.createCycleInZephyrCloud(cycleDTO);
                                if (Objects.nonNull(cloudCycleBean)) {
                                    zephyrServerCloudCycleMappingMap.put(cycleDTO, cloudCycleBean);
                                }
                            }
                        }catch (Exception ex) {
                            log.error("Error occurred while creating cycle.", ex.fillInStackTrace());
                        }
                    }
                });
            if (!zephyrServerCloudCycleMappingMap.isEmpty()) {
                //Update the cycle mapping file.
                migrationMappingFileGenerationUtil.updateCycleMappingFile(projectId, projectName, migrationFilePath, zephyrServerCloudCycleMappingMap);
            }
        }
    }

    private void createUnmappedFolderInCloud(Map<String, SearchRequest> mappedServerToCloudCycleMap, List<FolderDTO> foldersListFromServer, Long projectId, String finalProjectName, String migrationFilePath) {
        if(foldersListFromServer != null && !foldersListFromServer.isEmpty()) {
            Map<FolderDTO, ZfjCloudFolderBean> zephyrServerCloudFolderMappingMap = new HashMap<>();
            foldersListFromServer.forEach(folderDTO -> {
                log.info("search folder request for folder. [" + folderDTO.getCycleId());
                String mapKey = folderDTO.getCycleId() + "_" +folderDTO.getVersionId();
                log.info("search folder request with key. [" + mapKey);
                SearchRequest searchFolderRequest = mappedServerToCloudCycleMap.get(mapKey);
                try {
                    Boolean folderMoved = FileUtils.readFolderMappingFile(migrationFilePath, ApplicationConstants.MAPPING_FOLDER_FILE_NAME + projectId + ApplicationConstants.XLS,
                            searchFolderRequest.getCloudCycleId(), folderDTO.getFolderId());
                    if (!folderMoved) {
                        ZfjCloudFolderBean zfjCloudFolderBean = folderService.createFolderInZephyrCloud(folderDTO, searchFolderRequest);
                        if (Objects.nonNull(zfjCloudFolderBean)) {
                            log.info("Adding the folder to map.");
                            zephyrServerCloudFolderMappingMap.put(folderDTO, zfjCloudFolderBean);
                        }
                    }
                }catch (Exception ex) {
                    log.error("Error occurred while creating folder.", ex.fillInStackTrace());
                }
            });
            if (!zephyrServerCloudFolderMappingMap.isEmpty()) {
                //Update the cycle mapping file.
                log.info("Updating the mapping file for folder.");
                migrationMappingFileGenerationUtil.updateFolderMappingFile(projectId, finalProjectName, migrationFilePath, zephyrServerCloudFolderMappingMap);
            }
        }
    }

    /**
     * execution migration
     */
    private Map<Integer, List<TestStepDTO>> beginExecutionMigration(Long projectId, String server_base_url, String server_user_name, String server_user_pass, ArrayBlockingQueue<String> progressQueue) throws IOException, InterruptedException {
        Project project = projectService.getProject(projectId, server_base_url, server_user_name, server_user_pass);
        String projectName = null;
        if (project != null) {
            projectName = project.getName();
        }

        Map<ExecutionDTO, ZfjCloudExecutionBean> finalResponse = new HashMap<>();
        Map<Integer, List<TestStepDTO>> fetchedTestStepsFromServer = new HashMap<>();

        Path cycleMappedFile = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_CYCLE_FILE_NAME + projectId + ApplicationConstants.XLS);

        if (Files.exists(cycleMappedFile)) {
            Path folderMappedFile = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_FOLDER_FILE_NAME + projectId + ApplicationConstants.XLS);

            Path executionMappedFile = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_EXECUTION_FILE_NAME + projectId + ApplicationConstants.XLSX);

            Map<String, SearchRequest> mappedServerToCloudCycleMap = FileUtils.readCycleMappingFile(migrationFilePath, ApplicationConstants.MAPPING_CYCLE_FILE_NAME + projectId + ApplicationConstants.XLS);
            final String cloudAccountId = configProperties.getConfigValue("zfj.cloud.accountId");
            log.info("cloudAccountId ::: ["+cloudAccountId+"]");

            Set<String> uniqueVersionIds = new HashSet<>();
            Map<String,String> uniqueVersionMap = new HashMap<>();

            //create cycle level executions
            mappedServerToCloudCycleMap.forEach((cycleId, searchRequest) -> {
                log.info("Fetching executions for cycle with id:: "+cycleId + " for version ::"+searchRequest.getVersionId());

                if(!uniqueVersionMap.containsKey(searchRequest.getVersionId())) {
                    uniqueVersionMap.put(searchRequest.getVersionId(),searchRequest.getCloudVersionId());
                }

                String serverCycleId = "";
                if(cycleId.contains("_")) {
                    serverCycleId = cycleId.substring(0,cycleId.indexOf("_"));
                }

                if(StringUtils.isNotBlank(serverCycleId) && !serverCycleId.equals(ApplicationConstants.AD_HOC_CYCLE_ID)) {
                    List<ExecutionDTO> executionList = executionService.getExecutionsFromZFJByVersionAndCycleName(searchRequest.getProjectId(), searchRequest.getVersionId(), serverCycleId, 0, 3000);

                    if (Files.exists(executionMappedFile)) {
                        if (executionList != null && !executionList.isEmpty()) {
                            List<ExecutionDTO> finalExecutionsListToBeProcessed = FileUtils.readExecutionMappingFile(migrationFilePath, ApplicationConstants.MAPPING_EXECUTION_FILE_NAME + projectId + ApplicationConstants.XLSX,
                                    searchRequest.getCloudCycleId(), null, executionList, ApplicationConstants.CYCLE_LEVEL_EXECUTION);
                            log.info("Final list to be processed for serverCycleId : "+ serverCycleId + " is " + finalExecutionsListToBeProcessed.size() +" executions.");
                            if(finalExecutionsListToBeProcessed.size() >0) {
                                finalExecutionsListToBeProcessed.forEach(serverExecution -> {

                                    if(!fetchedTestStepsFromServer.containsKey(serverExecution.getIssueId())) {
                                        List<TestStepDTO> testStepDTOList = testStepService.fetchTestStepsFromZFJ(serverExecution.getIssueId());
                                        createTestStepInJiraCloud(projectId, serverExecution.getIssueId(), testStepDTOList);
                                        // add the created steps in map
                                        fetchedTestStepsFromServer.put(serverExecution.getIssueId(), testStepDTOList);

                                    }
                                    ZfjCloudExecutionBean zfjCloudExecutionBean = executionService.createExecutionInJiraCloud(prepareRequestForCloud(serverExecution, searchRequest, cloudAccountId));
                                    if(Objects.nonNull(zfjCloudExecutionBean)) {
                                        if(zfjCloudExecutionBean.getId() != null) {
                                            finalResponse.put(serverExecution,zfjCloudExecutionBean);
                                        }
                                    }
                                });
                            }
                        }
                    }else {
                        if(null != executionList && executionList.size() >0) {
                            executionList.forEach(serverExecution -> {

                                if(!fetchedTestStepsFromServer.containsKey(serverExecution.getIssueId())) {
                                    List<TestStepDTO> testStepDTOList = testStepService.fetchTestStepsFromZFJ(serverExecution.getIssueId());
                                    createTestStepInJiraCloud(projectId, serverExecution.getIssueId(), testStepDTOList);
                                    // add the created steps in map
                                    fetchedTestStepsFromServer.put(serverExecution.getIssueId(), testStepDTOList);
                                }

                                ZfjCloudExecutionBean zfjCloudExecutionBean = executionService.createExecutionInJiraCloud(prepareRequestForCloud(serverExecution, searchRequest, cloudAccountId));
                                if(Objects.nonNull(zfjCloudExecutionBean)) {
                                    if(zfjCloudExecutionBean.getId() != null) {
                                        finalResponse.put(serverExecution,zfjCloudExecutionBean);
                                    }
                                }
                            });
                        }
                    }
                }
            });

            if(!uniqueVersionMap.isEmpty()) {
                //create execution for Adhoc cycle per version.
                uniqueVersionMap.forEach((serverVersionId,cloudVersionId) -> {
                    List<ExecutionDTO> executionList = executionService.getExecutionsFromZFJByVersionAndCycleName(projectId.toString(), serverVersionId, ApplicationConstants.AD_HOC_CYCLE_ID, 0, 3000);
                    Map<ExecutionDTO, ZfjCloudExecutionBean> adhocCycleData = createExecutionsForAdhocCycle(executionList,projectId,cloudVersionId,fetchedTestStepsFromServer,executionMappedFile);
                    log.info("Adhoc cycle executions size for version : "+serverVersionId + " is "+adhocCycleData.size());
                    finalResponse.putAll(adhocCycleData);
                });
            }


            if (Files.exists(folderMappedFile)) {
                mappedServerToCloudCycleMap.forEach((cycleId, searchRequest) -> {
                    String serverCycleId = "";
                    if(cycleId.contains("_")) {
                        serverCycleId = cycleId.substring(0,cycleId.indexOf("_"));

                        Map<String,String> mappedServerCloudFolderIds = FileUtils.getServerCloudFolderMapping(migrationFilePath, ApplicationConstants.MAPPING_FOLDER_FILE_NAME + projectId + ApplicationConstants.XLS,
                                searchRequest.getCloudCycleId(), serverCycleId);

                        if (!((mappedServerCloudFolderIds != null) && (mappedServerCloudFolderIds.size() > 0))) return;

                        final String server_CycleId = cycleId.substring(0,cycleId.indexOf("_"));;
                        // submit this list to executor service
                        mappedServerCloudFolderIds.forEach((serverFolderId, cloudFolderId) -> {
                            log.info("Fetching executions from server with folder id:: ["+ serverFolderId + "]" + " belongs to Cycle "+server_CycleId);
                            searchRequest.setCloudFolderId(cloudFolderId);
                            List<ExecutionDTO> folderExecutionList = executionService.getExecutionsFromZFJByVersionCycleAndFolderName(searchRequest.getProjectId(), searchRequest.getVersionId(),
                                    server_CycleId, serverFolderId, 0, 3000);
                            // futures.add(scheduledExecutorService.schedule(new ExecutionCreationTask(folderExecutionList, searchRequest, cloudAccountId, executionService),5,TimeUnit.SECONDS));

                            if (Files.exists(executionMappedFile)) {
                                if (folderExecutionList != null && !folderExecutionList.isEmpty()) {
                                    List<ExecutionDTO> finalExecutionsListToBeProcessed = FileUtils.readExecutionMappingFile(migrationFilePath, ApplicationConstants.MAPPING_EXECUTION_FILE_NAME + projectId + ApplicationConstants.XLSX,
                                            null, searchRequest.getCloudFolderId(), folderExecutionList, ApplicationConstants.FOLDER_LEVEL_EXECUTION);
                                    log.info("Final list to be processed for serverFolderId : "+ serverFolderId + " is " + finalExecutionsListToBeProcessed.size() +" executions.");
                                    if(finalExecutionsListToBeProcessed.size() >0) {
                                        finalExecutionsListToBeProcessed.forEach(serverExecution -> {

                                            if(!fetchedTestStepsFromServer.containsKey(serverExecution.getIssueId())) {
                                                List<TestStepDTO> testStepDTOList = testStepService.fetchTestStepsFromZFJ(serverExecution.getIssueId());
                                                // create steps in zephyr cloud.
                                                createTestStepInJiraCloud(projectId, serverExecution.getIssueId(), testStepDTOList);
                                                // add the created steps in map
                                                fetchedTestStepsFromServer.put(serverExecution.getIssueId(), testStepDTOList);
                                            }

                                            ZfjCloudExecutionBean zfjCloudExecutionBean = executionService.createExecutionInJiraCloud(prepareRequestForCloud(serverExecution, searchRequest, cloudAccountId));
                                            if(Objects.nonNull(zfjCloudExecutionBean)) {
                                                if(zfjCloudExecutionBean.getId() != null) {
                                                    finalResponse.put(serverExecution,zfjCloudExecutionBean);
                                                }
                                            }
                                        });
                                    }
                                }
                            } else {
                                if(null != folderExecutionList && folderExecutionList.size() >0) {
                                    folderExecutionList.forEach(serverExecution -> {

                                        if(!fetchedTestStepsFromServer.containsKey(serverExecution.getIssueId())) {
                                            List<TestStepDTO> testStepDTOList = testStepService.fetchTestStepsFromZFJ(serverExecution.getIssueId());
                                            createTestStepInJiraCloud(projectId, serverExecution.getIssueId(), testStepDTOList);
                                            // add the created steps in map
                                            fetchedTestStepsFromServer.put(serverExecution.getIssueId(), testStepDTOList);
                                        }

                                        ZfjCloudExecutionBean zfjCloudExecutionBean = executionService.createExecutionInJiraCloud(prepareRequestForCloud(serverExecution, searchRequest, cloudAccountId));
                                        if(Objects.nonNull(zfjCloudExecutionBean)) {
                                            if(zfjCloudExecutionBean.getId() != null) {
                                                finalResponse.put(serverExecution,zfjCloudExecutionBean);
                                            }
                                        }
                                    });
                                }
                            }
                        });

                    }
                });
            }

            if (Files.exists(executionMappedFile)) {
                progressQueue.put("Updating the mapping file for execution migration for project : "+projectId);
                migrationMappingFileGenerationUtil.updateExecutionMappingFile(projectId+"", projectName, migrationFilePath, finalResponse);
               // return fetchedTestStepsFromServer;
            }else if(finalResponse.size() > 0) {
                progressQueue.put("Creating the mapping file for execution migration for project : "+projectId);
                migrationMappingFileGenerationUtil.generateExecutionMappingReportExcel(projectId+"", projectName,migrationFilePath,finalResponse);
                // return fetchedTestStepsFromServer;
            }

            try{
                beginTestStepsMigration(projectId,fetchedTestStepsFromServer);
            }catch (Exception ex) {
                log.info("Error occurred while migrating test steps.");
            }
        }
        return fetchedTestStepsFromServer;
    }

    private void beginAttachmentsEntityMigration(Long projectId, String server_base_url, String server_user_name, String server_user_pass, ArrayBlockingQueue<String> progressQueue) throws IOException, InterruptedException{
        try {
            Path executionMappedFile = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_EXECUTION_FILE_NAME + projectId + ApplicationConstants.XLSX);

            if (Files.exists(executionMappedFile)) {
                Map<String, String> mappedServerToCloudExecutionIdMap = FileUtils.readExecutionMappingFile(migrationFilePath, ApplicationConstants.MAPPING_EXECUTION_FILE_NAME + projectId + ApplicationConstants.XLSX);
                if (!mappedServerToCloudExecutionIdMap.isEmpty()) {
                    Project project = projectService.getProject(projectId, server_base_url, server_user_name, server_user_pass);
                    String projectName = null;
                    if (project != null) {
                        projectName = project.getName();
                    }
                    List<ZfjCloudAttachmentBean> zfjCloudAttachmentBeanList = new ArrayList<>();
                    Path executionAttachmentMappedFile = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_EXECUTION_ATTACHMENT_FILE_NAME + projectId + ApplicationConstants.XLS);
                    List<String> mappedServerToCloudExecutionAttachmentMap = new ArrayList<>();
                    if (Files.exists(executionAttachmentMappedFile)) {
                        try {
                            mappedServerToCloudExecutionAttachmentMap = FileUtils.readExecutionAttachmentMappingFile(migrationFilePath, ApplicationConstants.MAPPING_EXECUTION_ATTACHMENT_FILE_NAME + projectId + ApplicationConstants.XLS);
                        } catch (IOException e) {
                            log.error("Error while reading execution attachment mapping file", e);
                        }
                    }

                    if (!mappedServerToCloudExecutionIdMap.isEmpty()) {
                        final List<String> finalMappedServerToCloudExecutionAttachmentMap = mappedServerToCloudExecutionAttachmentMap;
                        mappedServerToCloudExecutionIdMap.forEach((serverExecutionId, cloudExecutionId) -> {
                            //Read the execution mapping file and start processing it.
                            List<AttachmentDTO> attachmentList = attachmentService.getAttachmentResponse(Integer.parseInt(serverExecutionId), ApplicationConstants.ENTITY_TYPE.EXECUTION);
                            List<AttachmentDTO> finalAttachmentList = new ArrayList<>();
                            if (attachmentList != null && attachmentList.size() > 0) {
                                if (Files.exists(executionAttachmentMappedFile)) {
                                    try {
                                        if (!finalMappedServerToCloudExecutionAttachmentMap.isEmpty()) {
                                            for (AttachmentDTO executionAttachment : attachmentList) {
                                                if (!finalMappedServerToCloudExecutionAttachmentMap.contains(executionAttachment.getFileId())) {
                                                    finalAttachmentList.add(executionAttachment);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        log.error("Error while reading execution attachment mapping file", e);
                                    }
                                } else {
                                    finalAttachmentList.addAll(attachmentList);
                                }
                                List<AttachmentDTO> executionAttachments = finalAttachmentList.stream()
                                        .filter(Objects::nonNull).collect(Collectors.toList());
                                List<File> filesToDelete = new ArrayList<>();
                                List<String> attachmentFileId = new ArrayList<>();
                                executionAttachments.forEach(attachment -> {
                                    if (attachment != null) {
                                        try {
                                            File executionAttachmentFile = attachmentService.downloadExecutionAttachmentFileFromZFJ(attachment.getFileId(), attachment.getFileName());
                                            if (executionAttachmentFile != null) {
                                                filesToDelete.add(executionAttachmentFile);
                                                attachmentFileId.add(attachment.getFileId());
                                            }
                                        } catch (Exception e) {
                                            log.error("Error while downloading the Testcase Execution Attachment for Execution -> " + attachment.getFileId(), e);
                                        }
                                    }
                                });
                                if (!filesToDelete.isEmpty()) {
                                    int fileCount = 0;
                                    for (File file : filesToDelete) {
                                        if (file.exists()) {
                                            try {
                                                ZfjCloudAttachmentBean zfjCloudAttachmentBean = attachmentService.addAttachmentInCloud(file, cloudExecutionId, projectId + "", ApplicationConstants.EXECUTION_ENTITY, cloudExecutionId);
                                                if (zfjCloudAttachmentBean != null) {
                                                    zfjCloudAttachmentBean.setServerExecutionId(serverExecutionId);
                                                    zfjCloudAttachmentBean.setServerExecutionAttachmentId(attachmentFileId.get(fileCount));
                                                    zfjCloudAttachmentBeanList.add(zfjCloudAttachmentBean);
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
                        });
                    }

                    if (Files.exists(executionAttachmentMappedFile) && !zfjCloudAttachmentBeanList.isEmpty()) {
                        progressQueue.put("Updating the mapping file for execution attachment migration for project : " + projectId);
                        migrationMappingFileGenerationUtil.updateExecutionAttachmentMappingFile(projectId + "", projectName, migrationFilePath, zfjCloudAttachmentBeanList);
                    } else {
                        if (!zfjCloudAttachmentBeanList.isEmpty()) {
                            progressQueue.put("Creating the mapping file for execution attachment migration for project : " + projectId);
                            migrationMappingFileGenerationUtil.generateExecutionAttachmentMappingReportExcel(projectId + "", projectName, migrationFilePath, zfjCloudAttachmentBeanList);
                        }
                    }

                    if (!mappedServerToCloudExecutionIdMap.isEmpty()) {
                        migrateStepResultLevelAttachments(mappedServerToCloudExecutionIdMap, projectId+"", projectName);
                    }

                }
            } else {
                log.warn("Execution mapping file either not created or empty.");
            }

        }catch (Exception ex) {
            log.error("Error while creating attachment for issue", ex);
        }
    }

    private void migrateStepResultLevelAttachments(Map<String, String> mappedServerToCloudExecutionIdMap, String projectId, String projectName) {
        List<String> serverStepResultAttachmentList = new ArrayList<>();
        try {
            Path stepResultAttachmentMappedFile = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_STEP_RESULT_ATTACHMENT_FILE_NAME + projectId + ApplicationConstants.XLS);
            if (Files.exists(stepResultAttachmentMappedFile)) {
                serverStepResultAttachmentList = FileUtils.readStepResultAttachmentMappingFileAndReturnList(migrationFilePath, ApplicationConstants.MAPPING_STEP_RESULT_ATTACHMENT_FILE_NAME + projectId + ApplicationConstants.XLS);
            }
        }catch ( IOException ex) {
            log.error("Error while reading the step result attachment mapping file for processing. -> ", ex);
        }

        AtomicReference<String> dummyCloudExecutionId = new AtomicReference<>();
        final String MIGRATE_UPDATE_STEP_RESULTS_FLAG = configProperties.getConfigValue("migrate.update.step.results");
        boolean isMigrateStepResultsAndAttachment = Boolean.parseBoolean(MIGRATE_UPDATE_STEP_RESULTS_FLAG);

        log.info("Step results update migration flag set to : "+isMigrateStepResultsAndAttachment);

        if(isMigrateStepResultsAndAttachment) {
            List<String> finalServerStepResultAttachmentList = serverStepResultAttachmentList;
            mappedServerToCloudExecutionIdMap.forEach((serverExecutionId, cloudExecutionId) -> {
                List<TestStepResultDTO> testStepResults = testStepService.getTestStepsResultFromZFJ(serverExecutionId);
                if(CollectionUtils.isNotEmpty(testStepResults) && testStepResults.size() > 0) {
                    importStepResultLevelAttachmentsAndResults(testStepResults, projectId, projectName, cloudExecutionId, finalServerStepResultAttachmentList);
                    dummyCloudExecutionId.set(cloudExecutionId);
                }
            });
        }

        try {
            final String MIGRATE_TEST_STEPS_ATTACHMENT_FLAG = configProperties.getConfigValue("migrate.test.steps.attachment");
            boolean isMigrateTestStepsAttachment = Boolean.parseBoolean(MIGRATE_TEST_STEPS_ATTACHMENT_FLAG);
            log.info("Test steps level attachment flag set to : "+isMigrateTestStepsAttachment);

            if(isMigrateTestStepsAttachment) {
                importTestStepsAttachmentMigration(projectId,projectName,dummyCloudExecutionId.get(),progressQueue);
            }
        } catch (Exception ex) {
            log.error("Error while creating attachment for test steps", ex.fillInStackTrace());
        }

    }

    private void importStepResultLevelAttachmentsAndResults(List<TestStepResultDTO> testStepResults, String projectId, String projectName, String cloudExecutionId, List<String> finalServerStepResultAttachmentList) {
        List<ZfjCloudAttachmentBean> zfjCloudAttachmentBeanList = new ArrayList<>();
        Path stepResultAttachmentMappedFile = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_STEP_RESULT_ATTACHMENT_FILE_NAME + projectId + ApplicationConstants.XLS);
        Path stepResultsMigrationMappedFile = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_STEP_RESULTS_FILE_NAME + projectId + ApplicationConstants.XLSX);

        List<TestStepResultDTO> testStepResultNewList = testStepResults.stream().filter(Objects::nonNull).collect(Collectors.toList());
        Map<Integer, ZfjCloudStepResultBean> stepResultBeanMap = new HashMap<>();

        List<ZfjCloudStepResultBean> cloudStepResultList = testStepService.getTestStepResultsFromZFJCloud(cloudExecutionId);
        if(!CollectionUtils.isEmpty(cloudStepResultList)) {
            stepResultBeanMap = cloudStepResultList.stream().collect(Collectors.toMap(ZfjCloudStepResultBean::getOrderId, c -> c));
        }
        final String MIGRATE_STEP_RESULTS_ATTACHMENT_FLAG = configProperties.getConfigValue("migrate.step.results.attachment");
        boolean isMigrateStepResultsAttachment = Boolean.parseBoolean(MIGRATE_STEP_RESULTS_ATTACHMENT_FLAG);
        log.info("Step level attachment flag set to : "+isMigrateStepResultsAttachment);

        List<StepResultFileResponseBean> stepResultFileResponseBeanList = new LinkedList<>();

        for (TestStepResultDTO testStepResult : testStepResultNewList) {
            try {
                //Update the step result data in cloud instance.
                ZfjCloudStepResultBean zfjCloudStepResultBean = stepResultBeanMap.get(testStepResult.getOrderId());
                if(Objects.nonNull(zfjCloudStepResultBean)) {
                    if(StringUtils.isNotBlank(testStepResult.getStatus()) &&
                            !testStepResult.getStatus().equalsIgnoreCase(UNEXECUTED_STATUS)) {
                        log.info("Step level status to update:: "+ testStepResult.getStatus());
                        log.info("Step level status for id:: "+ testStepResult.getId());
                        testStepService.updateStepResult(prepareRequestForStepResult(zfjCloudStepResultBean,testStepResult));
                        StepResultFileResponseBean responseBean = new StepResultFileResponseBean(String.valueOf(testStepResult.getExecutionId()),
                                zfjCloudStepResultBean.getExecutionId(), String.valueOf(testStepResult.getId()), zfjCloudStepResultBean.getId());
                        stepResultFileResponseBeanList.add(responseBean);
                    }
                }
                if(isMigrateStepResultsAttachment) {
                    List<String> stepResultFileId = new ArrayList<>();
                    List<String> stepResultFileName = new ArrayList<>();
                    List<AttachmentDTO> attachmentList = attachmentService.getAttachmentResponse(testStepResult.getId(), ApplicationConstants.ENTITY_TYPE.TESTSTEPRESULT);
                    if (attachmentList != null && attachmentList.size() > 0) {

                        List<AttachmentDTO> stepResultsAttachmentList = attachmentList.stream().filter(Objects::nonNull).collect(Collectors.toList());
                        List<File> filesToDelete = new ArrayList<>();
                        stepResultsAttachmentList.forEach(stepResultAttachment -> {

                            try {
                                if (!finalServerStepResultAttachmentList.contains(stepResultAttachment.getFileId())) {
                                    File testStepAttachmentFile = attachmentService.downloadExecutionAttachmentFileFromZFJ(stepResultAttachment.getFileId(), stepResultAttachment.getFileName());
                                    if (testStepAttachmentFile != null) {
                                        filesToDelete.add(testStepAttachmentFile);
                                        stepResultFileId.add(stepResultAttachment.getFileId());
                                        stepResultFileName.add(stepResultAttachment.getFileName());
                                    }
                                }
                            } catch (Exception e) {
                                log.error("Error while downloading the step result attachment for step result -> " + stepResultAttachment.getFileId() + " ->  for step result: " + testStepResult.getId(), e);
                            }
                        });
                        if (!filesToDelete.isEmpty()) {
                            int fileCount = 0;
                            ZfjCloudStepResultBean zfjCloudStepResult = stepResultBeanMap.get(testStepResult.getOrderId());
                            if(Objects.nonNull(zfjCloudStepResult)) {
                                for (File file : filesToDelete) {
                                    if (file.exists()) {
                                        try {
                                            ZfjCloudAttachmentBean zfjCloudAttachmentBean = attachmentService.addAttachmentInCloud(file, zfjCloudStepResultBean.getExecutionId(), projectId, ApplicationConstants.STEP_RESULT_ENTITY, zfjCloudStepResult.getId());
                                            if (null != zfjCloudAttachmentBean) {
                                                log.info("file uploaded successfully.");
                                                zfjCloudAttachmentBean.setServerStepResultId(testStepResult.getId());
                                                zfjCloudAttachmentBean.setFileId(stepResultFileId.get(fileCount));
                                                zfjCloudAttachmentBean.setFileName(stepResultFileName.get(fileCount));
                                                zfjCloudAttachmentBeanList.add(zfjCloudAttachmentBean);
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
                    }
                }
            } catch (Exception e) {
                log.error("Error while uploading the Step Result Attachment for Step Result: " + testStepResult.getId(), e);
            }
        }
        if (!zfjCloudAttachmentBeanList.isEmpty() && Files.exists(stepResultAttachmentMappedFile)) {
            migrationMappingFileGenerationUtil.updateStepResultAttachmentMappingFile(projectId + "", projectName, migrationFilePath, zfjCloudAttachmentBeanList);
        }
        if (!zfjCloudAttachmentBeanList.isEmpty() && !Files.exists(stepResultAttachmentMappedFile)) {
            migrationMappingFileGenerationUtil.generateStepResultAttachmentMappingReportExcel(projectId + "", projectName, migrationFilePath, zfjCloudAttachmentBeanList);
        }
        if (!stepResultFileResponseBeanList.isEmpty() && Files.exists(stepResultsMigrationMappedFile)) {
            migrationMappingFileGenerationUtil.updateStepResultsMigrationMappingFile(projectId + "", projectName, migrationFilePath, stepResultFileResponseBeanList);
        }else if (!stepResultFileResponseBeanList.isEmpty() && !Files.exists(stepResultsMigrationMappedFile)) {
            migrationMappingFileGenerationUtil.generateStepResultsMigrationMappingFile(projectId + "", projectName, migrationFilePath, stepResultFileResponseBeanList);
        }
    }

    private void importTestStepsAttachmentMigration(String projectId, String projectName, String cloudExecutionId, ArrayBlockingQueue<String> progressQueue) throws IOException, InterruptedException{
        try {
            Path testStepMappedFile = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_TEST_STEP_FILE_NAME + projectId + ApplicationConstants.XLSX);

            if (Files.exists(testStepMappedFile)) {
                Map<String, ArrayList<String>> mappedServerToCloudTestStepsIdMap = FileUtils.readTestStepIdsMappingFile(migrationFilePath, ApplicationConstants.MAPPING_TEST_STEP_FILE_NAME + projectId + ApplicationConstants.XLSX);
                if (!mappedServerToCloudTestStepsIdMap.isEmpty()) {
                    List<ZfjCloudAttachmentBean> zfjCloudAttachmentBeanList = new ArrayList<>();
                    Path testStepsAttachmentMappedFile = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_TEST_STEP_ATTACHMENT_FILE_NAME + projectId + ApplicationConstants.XLS);
                    if (!mappedServerToCloudTestStepsIdMap.isEmpty()) {
                        List<String> mappedServerToCloudTestStepAttachmentList = new ArrayList<>();
                        if (Files.exists(testStepsAttachmentMappedFile)) {
                            mappedServerToCloudTestStepAttachmentList = FileUtils.readTestStepsAttachmentMappingFile(migrationFilePath, ApplicationConstants.MAPPING_TEST_STEP_ATTACHMENT_FILE_NAME + projectId + ApplicationConstants.XLS);

                        }

                        final List<String> finalMappedServerToCloudTestStepAttachmentList = mappedServerToCloudTestStepAttachmentList;
                        mappedServerToCloudTestStepsIdMap.forEach((serverStepId, cloudRequestDetails) -> {
                            //Read the execution mapping file and start processing it.
                            List<AttachmentDTO> attachmentList = attachmentService.getAttachmentResponse(Integer.parseInt(serverStepId), ApplicationConstants.ENTITY_TYPE.TESTSTEP);
                            List<AttachmentDTO> finalAttachmentList = new ArrayList<>();
                            if (attachmentList != null && attachmentList.size() > 0) {
                                if (Files.exists(testStepsAttachmentMappedFile)) {
                                    try {
                                        if (!finalMappedServerToCloudTestStepAttachmentList.isEmpty()) {
                                            for (AttachmentDTO testStepAttachment : attachmentList) {
                                                if (!finalMappedServerToCloudTestStepAttachmentList.contains(testStepAttachment.getFileId())) {
                                                    finalAttachmentList.add(testStepAttachment);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        log.error("Error while reading execution attachment mapping file", e);
                                    }
                                } else {
                                    finalAttachmentList.addAll(attachmentList);
                                }
                                List<AttachmentDTO> testStepAttachments = finalAttachmentList.stream().filter(Objects::nonNull).collect(Collectors.toList());
                                List<File> filesToDelete = new ArrayList<>();
                                List<String> attachmentFileId = new ArrayList<>();
                                testStepAttachments.forEach(attachment -> {
                                    if (attachment != null) {
                                        try {
                                            File executionAttachmentFile = attachmentService.downloadExecutionAttachmentFileFromZFJ(attachment.getFileId(), attachment.getFileName());
                                            if (executionAttachmentFile != null) {
                                                filesToDelete.add(executionAttachmentFile);
                                                attachmentFileId.add(attachment.getFileId());
                                            }
                                        } catch (Exception e) {
                                            log.error("Error while downloading the Testcase Execution Attachment for Execution -> " + attachment.getFileId(), e);
                                        }
                                    }
                                });
                                if (!filesToDelete.isEmpty()) {
                                    int fileCount = 0;
                                    String cloudStepId = cloudRequestDetails.get(0);
                                    for (File file : filesToDelete) {
                                        if (file.exists()) {
                                            try {
                                                ZfjCloudAttachmentBean zfjCloudAttachmentBean = attachmentService.addAttachmentInCloud(file, cloudExecutionId, projectId + "", ApplicationConstants.TEST_STEP_ENTITY, cloudStepId);
                                                if (zfjCloudAttachmentBean != null) {
                                                    zfjCloudAttachmentBean.setServerTestStepId(serverStepId);
                                                    zfjCloudAttachmentBean.setServerExecutionAttachmentId(attachmentFileId.get(fileCount));
                                                    zfjCloudAttachmentBeanList.add(zfjCloudAttachmentBean);
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
                        });
                    }

                    if (Files.exists(testStepsAttachmentMappedFile) && !zfjCloudAttachmentBeanList.isEmpty()) {
                        progressQueue.put("Updating the mapping file for test steps attachment migration for project : " + projectId);
                        migrationMappingFileGenerationUtil.updateTestStepAttachmentMappingFile(projectId + "", projectName, migrationFilePath, zfjCloudAttachmentBeanList);
                    } else {
                        if (!zfjCloudAttachmentBeanList.isEmpty()) {
                            progressQueue.put("Creating the mapping file for test steps attachment migration for project : " + projectId);
                            migrationMappingFileGenerationUtil.generateTestStepAttachmentMappingReportExcel(projectId + "", projectName, migrationFilePath, zfjCloudAttachmentBeanList);
                        }
                    }
                }else {
                    log.info("Test steps mapped file not created.");
                }
            }

        }catch (Exception ex) {
            log.error("Error while creating attachment for issue", ex);
        }
    }

    private ZfjCloudExecutionBean prepareRequestForCloud(ExecutionDTO serverExecution, SearchRequest searchRequest, String assignedAccountId) {
        ZfjCloudExecutionBean zfjCloudExecutionBean = new ZfjCloudExecutionBean();

        zfjCloudExecutionBean.setProjectId(Integer.parseInt(searchRequest.getProjectId()));
        zfjCloudExecutionBean.setVersionId(Integer.parseInt(searchRequest.getCloudVersionId()));
        zfjCloudExecutionBean.setCycleId(searchRequest.getCloudCycleId());
        zfjCloudExecutionBean.setIssueId(serverExecution.getIssueId());
        zfjCloudExecutionBean.setExecutedByZapi(Boolean.TRUE);
        zfjCloudExecutionBean.setAssignedToAccountId(assignedAccountId);
        zfjCloudExecutionBean.setExecutedByAccountId(assignedAccountId);
        zfjCloudExecutionBean.setAssigneeType(ApplicationConstants.ASSIGNEE_TYPE);
        zfjCloudExecutionBean.setStatusId(serverExecution.getExecutionStatus());
        if(Objects.nonNull(searchRequest.getCloudFolderId())) {
            zfjCloudExecutionBean.setFolderId(searchRequest.getCloudFolderId());
        }
        if(StringUtils.isNotBlank(serverExecution.getComment())) {
            zfjCloudExecutionBean.setComment(serverExecution.getComment());
        }else {
            zfjCloudExecutionBean.setComment("");
        }

        try {
            if (StringUtils.isNotEmpty(serverExecution.getExecutedOn())) {
                zfjCloudExecutionBean.setExecutedOnStr(serverExecution.getExecutedOn());
            }
            if (StringUtils.isNotEmpty(serverExecution.getCreatedOn())) {
                zfjCloudExecutionBean.setCreationDateStr(serverExecution.getCreatedOn());
            }
        }catch (Exception ex) {
            log.info("Error while converting string to date");
        }
        return zfjCloudExecutionBean;
    }

    private void createTestStepInJiraCloud(Long projectId, Integer issueId, List<TestStepDTO> testStepDTOList) {
        List<JiraCloudTestStepDTO> createdCloudTestStepList = null;
        Path testStepMappedFile = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_TEST_STEP_FILE_NAME + projectId + ApplicationConstants.XLSX);
        if (Files.exists(testStepMappedFile)) {
            try {
                List<Integer> mappedTestStepId = FileUtils.readTestStepMappingFile(migrationFilePath, ApplicationConstants.MAPPING_TEST_STEP_FILE_NAME + projectId + ApplicationConstants.XLSX, issueId.toString());
                if (mappedTestStepId != null && mappedTestStepId.size() > 0) {
                    List<TestStepDTO> alreadyCreatedTestStep = new ArrayList<>();
                    for (TestStepDTO testStepDTO : testStepDTOList) {
                        if (testStepDTO != null && mappedTestStepId.contains(testStepDTO.getId())) {
                            alreadyCreatedTestStep.add(testStepDTO);
                        }
                    }
                    if (alreadyCreatedTestStep.size() > 0) {
                        testStepDTOList.removeAll(alreadyCreatedTestStep);
                    }
                }
            }catch (Exception ex) {
                log.info("Exception while fetching mapped test step id");
            }
        }
        // create steps in zephyr cloud.
        if (testStepDTOList != null && !testStepDTOList.isEmpty()) {
            createdCloudTestStepList = new ArrayList<JiraCloudTestStepDTO>();
            createdCloudTestStepList = testStepService.createTestStepInJiraCloud(testStepDTOList, issueId, projectId);
        }

        if (createdCloudTestStepList != null && !createdCloudTestStepList.isEmpty()) {
            if (!Files.exists(testStepMappedFile)) {
                migrationMappingFileGenerationUtil.generateTestStepMappingReportExcel(projectId + "", issueId.toString(),
                        migrationFilePath,testStepDTOList,createdCloudTestStepList);
            }else {
                migrationMappingFileGenerationUtil.updateTestStepMappingFile(projectId + "", issueId.toString(), migrationFilePath, testStepDTOList, createdCloudTestStepList);
            }
        }
    }


    private ZfjCloudStepResultUpdateBean prepareRequestForStepResult(ZfjCloudStepResultBean zfjCloudStepResultBean, TestStepResultDTO testStepResult) {
        ZfjCloudStepResultUpdateBean zfjCloudStepResultUpdateBean = new ZfjCloudStepResultUpdateBean();

        zfjCloudStepResultUpdateBean.setStepResultId(zfjCloudStepResultBean.getId());
        zfjCloudStepResultUpdateBean.setExecutionId(zfjCloudStepResultBean.getExecutionId());
        zfjCloudStepResultUpdateBean.setStatusId(testStepResult.getStatus());
        zfjCloudStepResultUpdateBean.setComment(testStepResult.getComment());

        return zfjCloudStepResultUpdateBean;
    }

    private Map<ExecutionDTO, ZfjCloudExecutionBean> createExecutionsForAdhocCycle(List<ExecutionDTO> executionList, Long projectId, String cloudVersionId, Map<Integer, List<TestStepDTO>> fetchedTestStepsFromServer, Path executionMappedFile) {
        Map<ExecutionDTO, ZfjCloudExecutionBean> createdExecutionResponse = new HashMap<>();
        final String cloudAccountId = configProperties.getConfigValue("zfj.cloud.accountId");

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setProjectId(projectId+"");
        searchRequest.setCloudVersionId(cloudVersionId);
        searchRequest.setCloudCycleId(ApplicationConstants.AD_HOC_CYCLE_ID);

        if (Files.exists(executionMappedFile)) {
            if (executionList != null && !executionList.isEmpty()) {
                List<ExecutionDTO> finalExecutionsListToBeProcessed = FileUtils.readExecutionMappingFile(migrationFilePath, ApplicationConstants.MAPPING_EXECUTION_FILE_NAME + projectId + ApplicationConstants.XLSX,
                        ApplicationConstants.AD_HOC_CYCLE_ID, null, executionList, ApplicationConstants.CYCLE_LEVEL_EXECUTION);
                log.info("Final list to be processed for serverCycleId : "+ ApplicationConstants.AD_HOC_CYCLE_ID + " is " + finalExecutionsListToBeProcessed.size() +" executions.");
                if(finalExecutionsListToBeProcessed.size() >0) {
                    finalExecutionsListToBeProcessed.forEach(serverExecution -> {

                        if(!fetchedTestStepsFromServer.containsKey(serverExecution.getIssueId())) {
                            List<TestStepDTO> testStepDTOList = testStepService.fetchTestStepsFromZFJ(serverExecution.getIssueId());
                            createTestStepInJiraCloud(projectId, serverExecution.getIssueId(), testStepDTOList);
                            // add the created steps in map
                            fetchedTestStepsFromServer.put(serverExecution.getIssueId(), testStepDTOList);

                        }
                        ZfjCloudExecutionBean zfjCloudExecutionBean = executionService.createExecutionInJiraCloud(prepareRequestForCloud(serverExecution, searchRequest, cloudAccountId));
                        if(Objects.nonNull(zfjCloudExecutionBean)) {
                            if(zfjCloudExecutionBean.getId() != null) {
                                createdExecutionResponse.put(serverExecution,zfjCloudExecutionBean);
                            }
                        }
                    });
                }
            }
        } else {
            if(null != executionList && executionList.size() >0) {
                executionList.forEach(serverExecution -> {

                    if(!fetchedTestStepsFromServer.containsKey(serverExecution.getIssueId())) {
                        List<TestStepDTO> testStepDTOList = testStepService.fetchTestStepsFromZFJ(serverExecution.getIssueId());
                        createTestStepInJiraCloud(projectId, serverExecution.getIssueId(), testStepDTOList);
                        // add the created steps in map
                        fetchedTestStepsFromServer.put(serverExecution.getIssueId(), testStepDTOList);
                    }

                    ZfjCloudExecutionBean zfjCloudExecutionBean = executionService.createExecutionInJiraCloud(prepareRequestForCloud(serverExecution, searchRequest, cloudAccountId));
                    if(Objects.nonNull(zfjCloudExecutionBean)) {
                        if(zfjCloudExecutionBean.getId() != null) {
                            createdExecutionResponse.put(serverExecution,zfjCloudExecutionBean);
                        }
                    }
                });
            }
        }
        return createdExecutionResponse;
    }

    private void beginTestStepsMigration(Long projectId, Map<Integer, List<TestStepDTO>> fetchedTestStepsFromServer) {
        //Get the total issue count using JQL
        //Fetch the records using based on total count.
        //create the test steps accordingly
        String _projectId = projectId.toString();
        Integer totalIssueCount = issueService.getTotalTestCountPerProjectFromJira(_projectId);
        log.info("Total issue count received from jira ::: "+totalIssueCount);
        Integer offset = 0;
        Integer limit = 50;
        AtomicInteger counter = new AtomicInteger(1);
        do{
            List<Issue> zephyrTests = issueService.getIssueDetailsFromJira(_projectId,offset,limit);
            zephyrTests.forEach(issue -> {
                int issueId = issue.getId();
                log.info("Fetching test steps for issue counter:: ["+counter.get()+"]");
                if(!fetchedTestStepsFromServer.containsKey(issueId)) {
                    try{
                        List<TestStepDTO> testStepDTOList = testStepService.fetchTestStepsFromZFJ(issueId);
                        createTestStepInJiraCloud(projectId,issueId,testStepDTOList);
                    }catch (Exception ex) {
                        log.error("Error occurred while migrating test steps from server to cloud for issue id ["+issueId+"]",ex.fillInStackTrace());
                    }
                }
                counter.addAndGet(1);
            });
            offset +=limit;
        }while (offset < totalIssueCount);

    }

    private void beginExecutionLevelDefectMigration(Long projectId, String server_base_url, String server_user_name, String server_user_pass) {
        try {
            Path executionMappedFile = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_EXECUTION_FILE_NAME + projectId + ApplicationConstants.XLSX);
            Path executionLevelDefectMappedFile = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_EXECUTION_LEVEL_DEFECT_MAPPING_FILE_NAME + projectId + ApplicationConstants.XLSX);

            if (Files.exists(executionMappedFile)) {
                Map<String, String> mappedServerToCloudExecutionIdMap = FileUtils.readExecutionMappingFile(migrationFilePath, ApplicationConstants.MAPPING_EXECUTION_FILE_NAME + projectId + ApplicationConstants.XLSX);
                if (!mappedServerToCloudExecutionIdMap.isEmpty()) {
                    Project project = projectService.getProject(projectId, server_base_url, server_user_name, server_user_pass);
                    String projectName = null;
                    if (project != null) {
                        projectName = project.getName();
                    }
                    Map<String,DefectLinkResponseBean> finalResponse = new ConcurrentHashMap<>();
                    Map<String,Issue> processedIssueMap = new ConcurrentHashMap<>();


                    mappedServerToCloudExecutionIdMap.entrySet().forEach((entry) -> {
                        String serverExecutionId = entry.getKey();
                        String cloudExecutionId = entry.getValue();
                        List<Issue> defectList = defectLinkService.getExecutionLevelDefectFromServer(Integer.parseInt(serverExecutionId),processedIssueMap);

                        if(CollectionUtils.isNotEmpty(defectList)) {
                            finalResponse.putAll(createExecutionLevelDefectInCloud(serverExecutionId,cloudExecutionId,defectList));
                        }
                    });

                    if(finalResponse.size() > 0 ) {
                        //call method to create file once the processing is completed.
                        if(Files.exists(executionLevelDefectMappedFile)) {
                            log.info("Execution level defect mapping file exists, updating the file.");
                             migrationMappingFileGenerationUtil.updateExecutionLevelDefectMigrationMappingFile(projectId + "", projectName, migrationFilePath, finalResponse);
                        }else {
                            log.info("Creating the execution level defect mapping after migration.");
                            migrationMappingFileGenerationUtil.generateExecutionLevelDefectMigrationMappingFile(projectId + "", projectName, migrationFilePath, finalResponse);
                        }
                    }
                }

            } else {
                    log.warn("Execution mapping file either not created or empty, hence execution level defect will not be migrated.");
            }

        }catch (Exception ex) {
                log.error("Error while migrating defect links for issue", ex);
        }
    }

    private void beginStepResultLevelDefectMigration(Long projectId, String server_base_url, String server_user_name, String server_user_pass) {
        try {
            Path stepResultsMappedFile = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_STEP_RESULTS_FILE_NAME + projectId + ApplicationConstants.XLSX);
            Path stepResultsDefectMappedFile = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_STEP_RESULTS_DEFECT_MAPPING_FILE_NAME + projectId + ApplicationConstants.XLSX);

            if (Files.exists(stepResultsMappedFile)) {
                Map<String, Map<String,StepResultFileResponseBean>> mappedServerToCloudStepResultsIdMap = FileUtils.readStepResultsMappingFile(migrationFilePath, ApplicationConstants.MAPPING_STEP_RESULTS_FILE_NAME + projectId + ApplicationConstants.XLSX);
                if (!mappedServerToCloudStepResultsIdMap.isEmpty()) {
                    Project project = projectService.getProject(projectId, server_base_url, server_user_name, server_user_pass);
                    String projectName = null;
                    if (project != null) {
                        projectName = project.getName();
                    }
                    Map<String,DefectLinkResponseBean> finalResponse = new ConcurrentHashMap<>();
                    Map<String,Issue> processedIssueMap = new ConcurrentHashMap<>();


                    mappedServerToCloudStepResultsIdMap.entrySet().forEach((entry) -> {
                        String serverExecutionId = entry.getKey();
                        Map<String,StepResultFileResponseBean> stepResultFileResponseBeanMap = entry.getValue();
                        Map<String, List<Issue>> stepLevelDefectMap = defectLinkService.getStepLevelDefectFromZfj
                                (Integer.parseInt(serverExecutionId), processedIssueMap);

                      if(stepLevelDefectMap.size() > 0) {
                            finalResponse.putAll(createStepLevelDefectInCloud(stepResultFileResponseBeanMap, stepLevelDefectMap));
                      }
                    });

                    if(finalResponse.size() > 0 ) {
                        //call method to create file once the processing is completed.
                        if(Files.exists(stepResultsDefectMappedFile)) {
                            log.info("Step results defect mapping file exists, updating the file.");
                            migrationMappingFileGenerationUtil.updateStepResultsDefectMigrationMappingFile(projectId + "", projectName, migrationFilePath, finalResponse);
                        }else {
                            log.info("Creating the step results defect mapping after migration.");
                            migrationMappingFileGenerationUtil.generateStepResultsDefectMigrationMappingFile(projectId + "", projectName, migrationFilePath, finalResponse);
                        }
                    }
                }

            } else {
                log.warn("Execution mapping file either not created or empty, hence execution level defect mapping is .");
            }

        }catch (Exception ex) {
            log.error("Error occurred while creating step level defects for issue", ex);
        }
    }

    private Map<String,DefectLinkResponseBean> createExecutionLevelDefectInCloud(String serverExecutionId, String cloudExecutionId, List<Issue> defectList) {
        Map<String,DefectLinkResponseBean> responseBeanMap = new HashMap<>();
        DefectLinkResponseBean responseBean = new DefectLinkResponseBean();
        List<Defect> cloudDefectCreationList = new ArrayList<>();
        StringBuffer sb = new StringBuffer();
        defectList.forEach(defectId -> {
            cloudDefectCreationList.add(new Defect(new Long(defectId.getId())));
            sb.append(defectId.getId()+",");
        });

        responseBean.setServerExecutionId(serverExecutionId);
        responseBean.setCloudExecutionId(cloudExecutionId);

        defectLinkService.createExecutionLevelDefectInZephyrCloud(cloudExecutionId,cloudDefectCreationList);

        responseBean.setServerDefectLinks(sb.toString());
        responseBean.setCloudDefectLinks(sb.toString());

        responseBeanMap.putIfAbsent(serverExecutionId,responseBean);

        return responseBeanMap;
    }

    private Map<String,DefectLinkResponseBean> createStepLevelDefectInCloud(Map<String, StepResultFileResponseBean> stepResultFileResponseBeanMap,
                                                                            Map<String, List<Issue>> stepLevelDefectMap) {
        Map<String,DefectLinkResponseBean> responseBeanMap = new ConcurrentHashMap<>();

        if(stepResultFileResponseBeanMap.size() > 0) {
            stepResultFileResponseBeanMap.entrySet().parallelStream().forEach(entry -> {
                String serverStepId = entry.getKey();
                StepResultFileResponseBean stepResultDetails = entry.getValue();
                List<Issue> defectList = stepLevelDefectMap.get(serverStepId);

                if(CollectionUtils.isNotEmpty(defectList)) {
                    DefectLinkResponseBean responseBean = new DefectLinkResponseBean();
                    List<Defect> cloudDefectCreationList = new ArrayList<>();
                    StringBuffer sb = new StringBuffer();
                    defectList.forEach(defectId -> {
                        cloudDefectCreationList.add(new Defect(new Long(defectId.getId())));
                        sb.append(defectId.getId()+",");
                    });

                    responseBean.setServerExecutionId(stepResultDetails.getServerExecutionId());
                    responseBean.setCloudExecutionId(stepResultDetails.getCloudExecutionId());

                    defectLinkService.createStepResultLevelDefectInZephyrCloud(stepResultDetails.getCloudExecutionId(),
                            stepResultDetails.getCloudStepResultId(), cloudDefectCreationList);

                    responseBean.setServerStepResultId(serverStepId);
                    responseBean.setCloudStepResultId(stepResultDetails.getCloudStepResultId());
                    responseBean.setServerDefectLinks(sb.toString());
                    responseBean.setCloudDefectLinks(sb.toString());

                    responseBeanMap.putIfAbsent(stepResultDetails.getServerExecutionId(),responseBean);
                }
            });
        }

        return responseBeanMap;
    }
}
