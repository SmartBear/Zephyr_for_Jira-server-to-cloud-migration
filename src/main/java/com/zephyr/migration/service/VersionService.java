package com.zephyr.migration.service;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.fasterxml.jackson.databind.JsonNode;
import com.zephyr.migration.dto.JiraIssueDTO;

public interface VersionService {

    public JsonNode getVersions(String projectId, String zephyrBaseUrl, String accessKey);
}
