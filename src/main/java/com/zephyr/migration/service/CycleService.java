package com.zephyr.migration.service;

import com.atlassian.jira.rest.client.api.domain.Version;
import com.fasterxml.jackson.databind.JsonNode;

public interface CycleService {

    JsonNode createCycleInZephyrCloud(Long projectId);
}
