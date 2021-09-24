package com.zephyr.migration.service;

import com.atlassian.jira.rest.client.api.domain.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.zephyr.migration.model.JiraVersion;

import java.util.List;

public interface VersionService {

    JsonNode getVersionsFromZephyrCloud(String projectId);

    Iterable<Version> getVersionsFromZephyrServer(Long projectId, String serverBaseUrl, String serverUserName, String serverUserPass);

    void createUnscheduledVersionInZephyrCloud(String projectId);

    JsonNode createVersionInZephyrCloud(JiraVersion jiraServerVersion, Long projectId);

    Iterable<JiraVersion> getVersionListFromServer(String projectId);

    JsonNode getVersionsByJiraFromZephyrCloud(String projectId);

    List<JiraVersion> getVersionListFromJiraServer(String projectId, int startIndex, int limit);

    Integer getTotalVersionCountPerProjectFromJira(String projectId);
}
