package com.zephyr.migration.service;

import com.atlassian.jira.rest.client.api.domain.Version;
import com.fasterxml.jackson.databind.JsonNode;

public interface VersionService {

    JsonNode getVersionsFromZephyrCloud(String projectId);

    Iterable<Version> getVersionsFromZephyrServer(Long projectId, String serverBaseUrl, String serverUserName, String serverUserPass);

    void createUnscheduledVersionInZephyrCloud(String projectId);

    JsonNode createVersionInZephyrCloud(Version jiraServerVersion, Long projectId);
}
