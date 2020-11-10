package com.zephyr.migration.service.impl;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.zephyr.migration.client.JiraServerClient;
import com.zephyr.migration.service.TestService;
import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl implements TestService {

    @Override
    public Issue getIssue(String issueKey) {

        JiraServerClient jiraServerClient = new JiraServerClient("user.name", "pass", "http://jira.company.com");

        Issue issue = jiraServerClient.getIssue(issueKey);
        System.out.println(issue.getDescription());
        return issue;
    }
}
