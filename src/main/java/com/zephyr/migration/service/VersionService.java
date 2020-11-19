package com.zephyr.migration.service;

import com.atlassian.jira.rest.client.api.domain.Version;
import com.fasterxml.jackson.databind.JsonNode;

public interface VersionService {

    JsonNode getVersionsFromZephyrCloud(String projectId, String zephyrBaseUrl, String accessKey);

    Iterable<Version> getVersionsFromZephyrServer(Long projectId, String serverBaseUrl, String serverUserName, String serverUserPass);
}
