package com.zephyr.migration.service;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.zephyr.migration.dto.JiraIssueDTO;

public interface TestService {

    Issue getIssueDetailsFromServer(String issueKey);

    JiraIssueDTO createIssueInCloud(Issue issue);
}
