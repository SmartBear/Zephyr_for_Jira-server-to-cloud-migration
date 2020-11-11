package com.zephyr.migration.service.impl;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.google.common.collect.ImmutableMap;
import com.zephyr.migration.client.JiraCloudClient;
import com.zephyr.migration.client.JiraServerClient;
import com.zephyr.migration.dto.JiraIssueDTO;
import com.zephyr.migration.service.TestService;
import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl implements TestService {

    @Override
    public Issue getIssueDetailsFromServer(String issueKey) {

        JiraServerClient jiraServerClient = new JiraServerClient("admin", "password", "http://15.207.184.119:8089/");
        Issue issue = jiraServerClient.getIssue(issueKey);
        System.out.println(issue.getDescription());
        return issue;
    }

    @Override
    public JiraIssueDTO createIssueInCloud(Issue issue) {
        JiraCloudClient jiraCloudClient = new JiraCloudClient("5f1041fcfe23000022438a56",
                "NzYxNjE2ZGEtYmJhNi0zZGQ0LWIwN2EtNTkwNDRiNTkwNjQ0IDVmMTA0MWZjZmUyMzAwMDAyMjQzOGE1NiBVU0VSX0RFRkFVTFRfTkFNRQ",
                "LsVFf5upvbINJm-__48Y7jFlIjkS8UCWm3KEbeLaF04",
                "https://harshcloud.ngrok.io");

        JiraIssueDTO issueDTO = jiraCloudClient.createIssue(prepareRequestObject(issue));
        return issueDTO;
    }

    private JiraIssueDTO prepareRequestObject(Issue issue) {
        JiraIssueDTO jiraIssue = new JiraIssueDTO();
        jiraIssue.setSummary(issue.getSummary());
        jiraIssue.setProject(ImmutableMap.of("id", issue.getProject().getId()));
        return jiraIssue;
    }
}
