package com.zephyr.migration.service.impl;

import com.atlassian.jira.rest.client.api.domain.Version;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

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
    MigrationMappingFileGenerationUtil migrationMappingFileGenerationUtil;

    @Value("${migrationFilePath}")
    private String migrationFilePath;

    @Override
    public void migrateSingleProject(Long projectId) throws Exception{

        final String SERVER_USER_NAME = configProperties.getConfigValue("zfj.server.username");
        final String SERVER_USER_PASS = configProperties.getConfigValue("zfj.server.password");
        final String SERVER_BASE_URL = configProperties.getConfigValue("zfj.server.baseUrl");

        Iterable<Version> versionsFromZephyrServer = versionService.getVersionsFromZephyrServer(projectId, SERVER_BASE_URL, SERVER_USER_NAME, SERVER_USER_PASS);

        if(Objects.nonNull(versionsFromZephyrServer)) {
            /*Just for sample validation*/
            versionsFromZephyrServer.forEach(version -> {
                log.info("Version Details : "+ version.getName());
            });
        }

        Path path = Paths.get(migrationFilePath, ApplicationConstants.VERSION_MAPPING_FILE_NAME + projectId + ".xls");
        if(Files.exists(path)){
            //TODO: Add logic to read the mapping file & validate whether corresponding cloud section exists.
            return;
        }else {
            versionService.createUnscheduledVersionInZephyrCloud(projectId.toString());
            JsonNode versionsFromZephyrCloud = versionService.getVersionsFromZephyrCloud(Long.toString(projectId));
            if(Objects.nonNull(versionsFromZephyrCloud)) {
                //TODO: Trigger project meta data
                migrationMappingFileGenerationUtil.generateVersionMappingReportExcel(migrationFilePath, Long.toString(projectId), versionsFromZephyrServer,versionsFromZephyrCloud);
            }else {
                log.warn("Version list from cloud is empty");
            }
        }

    }
}
