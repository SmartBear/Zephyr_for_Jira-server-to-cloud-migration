package com.zephyr.migration.service.impl;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.zephyr.migration.client.JiraCloudClient;
import com.zephyr.migration.client.JiraServerClient;
import com.zephyr.migration.dto.JiraIssueDTO;
import com.zephyr.migration.service.TestService;
import com.zephyr.migration.service.VersionService;
import com.zephyr.migration.utils.ConfigProperties;
import com.zephyr.migration.utils.MigrationMappingFileGenerationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class TestServiceImpl implements TestService {

    private static final Logger log = LoggerFactory.getLogger(TestServiceImpl.class);

    @Autowired
    VersionService versionService;

    @Autowired
    ConfigProperties configProperties;

    @Autowired
    MigrationMappingFileGenerationUtil migrationMappingFileGenerationUtil;

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
        Iterable<Version> versionsFromZephyrServer = versionService.getVersionsFromZephyrServer(projectId, SERVER_BASE_URL, SERVER_USER_NAME, SERVER_USER_PASS);
        Map<String, Long> serverCloudVersionMapping = new HashMap<>();

        versionsFromZephyrServer.forEach(version -> {
            JsonNode versionNode = versionService.createVersionInZephyrCloud(version,projectId);
            try {
                log.info("created version in cloud : " + new ObjectMapper().writeValueAsString(versionNode));

                if(Objects.nonNull(versionNode) && versionNode.has("id")) {
                    String versionId = Objects.nonNull(version.getId()) ? Long.toString(version.getId()) : null;
                    Long cloudVersionId = versionNode.findValue("id").asLong();
                    serverCloudVersionMapping.put(versionId, cloudVersionId);
                }

            } catch (JsonProcessingException e) {
                log.error("Error occurred while creating version in jira cloud.", e.fillInStackTrace());
            }
        });

        migrationMappingFileGenerationUtil.updateVersionMappingFile(projectId, migrationFilePath, serverCloudVersionMapping);
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
