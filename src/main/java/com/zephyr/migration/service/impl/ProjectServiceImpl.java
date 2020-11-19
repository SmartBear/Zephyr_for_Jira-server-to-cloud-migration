package com.zephyr.migration.service.impl;

import com.atlassian.jira.rest.client.api.domain.Project;
import com.zephyr.migration.client.JiraServerClient;
import com.zephyr.migration.service.ProjectService;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Override
    public Project getProject(Long projectId, String serverBaseUrl, String serverUserName, String serverUserPass) {
        JiraServerClient jiraServerClient = new JiraServerClient(serverUserName, serverUserPass, serverBaseUrl);
        return jiraServerClient.getProject(Long.toString(projectId));
    }
}
