package com.zephyr.migration.service;

import com.atlassian.jira.rest.client.api.domain.Project;

public interface ProjectService {

    Project getProject(Long projectId, String serverBaseUrl, String serverUserName, String serverUserPass);
}
