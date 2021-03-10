package com.zephyr.migration.service;

import com.atlassian.jira.rest.client.api.domain.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.zephyr.migration.dto.JiraVersionDTO;

import java.util.List;

public interface VersionService {

    JsonNode getVersionsFromZephyrCloud(String projectId);

    Iterable<Version> getVersionsFromZephyrServer(Long projectId, String serverBaseUrl, String serverUserName, String serverUserPass);

    void createUnscheduledVersionInZephyrCloud(String projectId);

    JsonNode createVersionInZephyrCloud(JiraVersionDTO jiraServerVersion, Long projectId);

    List<JiraVersionDTO> getVersionListFromServer(String projectId);
}
