package com.zephyr.migration.service;

import com.atlassian.jira.rest.client.api.domain.Issue;

public interface TestService {

    Issue getIssue(String issueKey);
}
