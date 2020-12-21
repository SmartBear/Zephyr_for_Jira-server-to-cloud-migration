package com.zephyr.migration.service.impl;

import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.zephyr.migration.client.HttpClient;
import com.zephyr.migration.client.JiraCloudClient;
import com.zephyr.migration.dto.CycleDTO;
import com.zephyr.migration.dto.ExecutionDTO;
import com.zephyr.migration.dto.FolderDTO;
import com.zephyr.migration.executors.ExecutionCreationTask;
import com.zephyr.migration.model.SearchRequest;
import com.zephyr.migration.model.ZfjCloudCycleBean;
import com.zephyr.migration.model.ZfjCloudExecutionBean;
import com.zephyr.migration.model.ZfjCloudFolderBean;
import com.zephyr.migration.service.*;
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
import java.util.concurrent.*;


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
     FolderService folderService;

    @Autowired
    ExecutionService executionService;

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
            if(migrateCycles) {
                boolean migrateFolders = beginFolderMigration(projectId, SERVER_BASE_URL, SERVER_USER_NAME, SERVER_USER_PASS, progressQueue);
            }
            if (migrateCycles) {

            }
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

        Path path = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_VERSION_FILE_NAME + projectId + ApplicationConstants.XLS);
        if(Files.exists(path)){
            //Logic to read the mapping file & validate whether corresponding cloud & server section exists.
            log.debug("Version Mapping file exists for the given project.");
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
                            createUnmappedCycleInCloud(mappedServerToCloudVersionMap, cyclesListFromServer, projectId, finalProjectName, migrationFilePath);
                        }
                    } catch (Exception ex) {
                        log.error("", ex.fillInStackTrace());
                    }
                });
                if (!zephyrServerCloudCycleMappingMap.isEmpty()) {
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
    private boolean beginFolderMigration(Long projectId, String server_base_url, String server_user_name, String server_user_pass, ArrayBlockingQueue<String> progressQueue) throws IOException {
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
                    listOfServerCycles.parallelStream().forEachOrdered(serverCycleId -> {
                        try {
                            progressQueue.put("fetching folders from zephyr server instance for cycle :: "+ serverCycleId);
                            log.info("Fetching folders from server for cycleId :: "+ serverCycleId);
                            SearchRequest searchFolderRequest = mappedServerToCloudCycleMap.get(serverCycleId);
                            if(null != searchFolderRequest) {
                                log.info("Fetching cycles from server with details :: "+ searchFolderRequest.toString());
                                List<FolderDTO> foldersListFromServer = folderService.fetchFoldersFromZephyrServer(Long.parseLong(serverCycleId),
                                        searchFolderRequest.getProjectId(), searchFolderRequest.getVersionId(), progressQueue);
                                zephyrServerCycleFolderMap.put(serverCycleId, foldersListFromServer);
                                log.info("Fetched cycles from server for version :: "+ serverCycleId);
                            }
                        } catch (Exception ex) {
                            log.error("", ex.fillInStackTrace());
                        }
                    });

                String finalProjectName = projectName;
                mappedServerToCloudCycleMap.forEach((serverCycleId, searchFolderRequest) -> {
                        try {
                            progressQueue.put("creating folders from zephyr server instance for cycle :: "+ serverCycleId);
                            List<FolderDTO> foldersListFromServer = zephyrServerCycleFolderMap.get(serverCycleId);
                            if (!Files.exists(folderMappedFile)) {
                                foldersListFromServer.forEach(folderDTO -> {
                                    try{
                                        progressQueue.put("creating folder in zephyr cloud instance with name :: "+ folderDTO.getFolderName());
                                        progressQueue.put("creating folder in zephyr cloud instance for cycle :: "+ searchFolderRequest.getCloudCycleId());
                                        log.info("creating folder in zephyr cloud instance with name :: "+ folderDTO.getFolderName());
                                        log.info("creating folder in zephyr cloud instance for cycle :: "+ searchFolderRequest.getCloudCycleId());
                                    }catch (InterruptedException ex) {
                                        log.error("",ex.fillInStackTrace());
                                    }
                                    ZfjCloudFolderBean cloudFolderBean = folderService.createFolderInZephyrCloud(folderDTO, searchFolderRequest);
                                    if (Objects.nonNull(cloudFolderBean)) {
                                        zephyrServerCloudFolderMappingMap.put(folderDTO, cloudFolderBean);
                                    }
                                });
                            }else {
                                createUnmappedFolderInCloud(mappedServerToCloudCycleMap, foldersListFromServer, projectId, finalProjectName,migrationFilePath);
                            }
                        } catch (Exception ex) {
                            log.error("", ex.fillInStackTrace());
                        }
                    });

                    if (!zephyrServerCloudFolderMappingMap.isEmpty()) {
                        migrationMappingFileGenerationUtil.generateFolderMappingReportExcel(zephyrServerCloudFolderMappingMap, projectId.toString(), finalProjectName, migrationFilePath);
                        return true;
                    }

            }
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
            String response = restTemplate.postForObject(triggerProjectMetaReindexUrl, entity, String.class);
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
        if(!foldersListFromServer.isEmpty()) {
            Map<FolderDTO, ZfjCloudFolderBean> zephyrServerCloudFolderMappingMap = new HashMap<>();
            foldersListFromServer.forEach(folderDTO -> {
                SearchRequest searchFolderRequest = mappedServerToCloudCycleMap.get(folderDTO.getCycleId());
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
    private boolean beginExecutionMigration(Long projectId, String server_base_url, String server_user_name, String server_user_pass, ArrayBlockingQueue<String> progressQueue) throws InterruptedException, IOException {
        Path path = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_EXECUTION_FILE_NAME + projectId + ApplicationConstants.XLS);
        Project project = projectService.getProject(projectId, server_base_url,server_user_name,server_user_pass);
        String projectName = null;
        if (project != null) {
            projectName = project.getName();
        }

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        List<Future<Map<ExecutionDTO, ZfjCloudExecutionBean>>> futures = new ArrayList<>();
        Map<ExecutionDTO, ZfjCloudExecutionBean> finalResponse = new HashMap<>();

        Path cycleMappedFile = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_CYCLE_FILE_NAME + projectId + ApplicationConstants.XLS);

        if (Files.exists(cycleMappedFile)) {
            Path folderMappedFile = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_FOLDER_FILE_NAME + projectId + ApplicationConstants.XLS);

            Map<String, SearchRequest> mappedServerToCloudCycleMap = FileUtils.readCycleMappingFile(migrationFilePath, ApplicationConstants.MAPPING_CYCLE_FILE_NAME + projectId + ApplicationConstants.XLS);

            //create cycle level executions
            mappedServerToCloudCycleMap.forEach((serverCycleId, searchRequest) -> {
                List<ExecutionDTO> executionList = executionService.getExecutionsFromZFJByVersionAndCycleName(searchRequest.getProjectId(), searchRequest.getVersionId(), serverCycleId, 0, 500);
                // submit this list to executor service
                futures.add(executorService.submit(new ExecutionCreationTask(executionList, searchRequest)));

                if (!Files.exists(folderMappedFile)) return;

                List<String> mappedServerFolderIds = FileUtils.getServerCloudFolderMapping(migrationFilePath, ApplicationConstants.MAPPING_FOLDER_FILE_NAME + projectId + ApplicationConstants.XLS,
                        searchRequest.getCloudCycleId(), serverCycleId);

                if (!((mappedServerFolderIds != null) && (mappedServerFolderIds.size() > 0))) return;

                // submit this list to executor service
                mappedServerFolderIds.stream().map(serverFolderId -> executionService.getExecutionsFromZFJByVersionCycleAndFolderName(searchRequest.getProjectId(), searchRequest.getVersionId(),
                        serverCycleId, serverFolderId, 0, 500)).map(folderExecutionList -> executorService.submit(new ExecutionCreationTask(folderExecutionList, searchRequest))).forEach(futures::add);
            });

            futures.forEach(mapFuture -> {
                try {
                    finalResponse.putAll(mapFuture.get());
                } catch (InterruptedException | ExecutionException e) {
                   log.error("error :: ", e.fillInStackTrace());
                }
            });

        }
        return true;
    }
}
