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
        /*JiraCloudClient jiraCloudClient = new JiraCloudClient("5f1041fcfe23000022438a56",
                "NzYxNjE2ZGEtYmJhNi0zZGQ0LWIwN2EtNTkwNDRiNTkwNjQ0IDVmMTA0MWZjZmUyMzAwMDAyMjQzOGE1NiBVU0VSX0RFRkFVTFRfTkFNRQ",
                "LsVFf5upvbINJm-__48Y7jFlIjkS8UCWm3KEbeLaF04",
                "https://harshcloud.ngrok.io");*/

        JiraCloudClient jiraCloudClient = new JiraCloudClient("5cdd254faee3080dc2f62ac4",
                "ZTI1YjE1YjctNzBiYi0zNzdkLTg5OGEtYmI4ZDdiYjg0ODU2IDVjZGQyNTRmYWVlMzA4MGRjMmY2MmFjNCBVU0VSX0RFRkFVTFRfTkFNRQ",
                "YxWFgOChDt9y3eOxVijVLkYkr32V39Tj6AJ5Pf31U0w",
                "https://himanshuconnect.ngrok.io");

        JiraIssueDTO issueDTO = jiraCloudClient.createIssue(prepareRequestObject(issue));
        return issueDTO;
    }

    private JiraIssueDTO prepareRequestObject(Issue issue) {
        JiraIssueDTO jiraIssue = new JiraIssueDTO();
        jiraIssue.setSummary(issue.getSummary());
        //jiraIssue.setProject(ImmutableMap.of("id", issue.getProject().getId()));
        jiraIssue.setProject(ImmutableMap.of("id", 10026));
        jiraIssue.setIssuetype(ImmutableMap.of("id", 10005));
        return jiraIssue;
    }
}
