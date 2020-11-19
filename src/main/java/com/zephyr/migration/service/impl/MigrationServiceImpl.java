package com.zephyr.migration.service.impl;

import com.atlassian.jira.rest.client.api.domain.Version;
import com.zephyr.migration.service.MigrationService;
import com.zephyr.migration.service.ProjectService;
import com.zephyr.migration.service.VersionService;
import com.zephyr.migration.utils.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public void migrateSingleProject(Long projectId) {

        final String SERVER_USER_NAME = configProperties.getConfigValue("zfj.server.username");
        final String SERVER_USER_PASS = configProperties.getConfigValue("zfj.server.password");
        final String SERVER_BASE_URL = configProperties.getConfigValue("zfj.server.baseUrl");

        final String CLOUD_BASE_URL = configProperties.getConfigValue("zfj.cloud.baseUrl");
        final String CLOUD_ACCESS_KEY = configProperties.getConfigValue("zfj.cloud.accessKey");

        Iterable<Version> versionsFromZephyrServer = versionService.getVersionsFromZephyrServer(projectId, SERVER_BASE_URL, SERVER_USER_NAME, SERVER_USER_PASS);

        if(Objects.nonNull(versionsFromZephyrServer)) {
            versionsFromZephyrServer.forEach(version -> {
                log.info("Version Details : "+ version.getName());
            });
        }

        //JsonNode versions = versionService.getVersionsFromZephyrCloud(Long.toString(projectId), CLOUD_BASE_URL, CLOUD_ACCESS_KEY);

    }
}
