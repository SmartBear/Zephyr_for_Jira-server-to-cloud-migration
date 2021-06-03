package com.zephyr.migration.service;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.zephyr.migration.client.JiraServerClient;

import java.util.List;

public interface IssueService {

    Issue getIssueFromJira(String issueKey);

    List<Issue> getIssueDetailsFromJira(String projectId, int startIndex, int limit);

    Integer getTotalTestCountPerProjectFromJira(String projectId);

}
