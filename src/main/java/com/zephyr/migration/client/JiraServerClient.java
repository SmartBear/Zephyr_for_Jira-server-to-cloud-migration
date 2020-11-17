package com.zephyr.migration.client;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

public class JiraServerClient {

    private static final Logger log = LoggerFactory.getLogger(JiraServerClient.class);

    private String username;
    private String password;
    private String jiraUrl;
    private JiraRestClient restClient;

    public JiraServerClient(String username, String password, String jiraUrl) {
        this.username = username;
        this.password = password;
        this.jiraUrl = jiraUrl;
        this.restClient = getJiraRestClient();
    }

    private JiraRestClient getJiraRestClient() {
        log.info("Serving --> {}", "getJiraRestClient()");
        return new AsynchronousJiraRestClientFactory()
                .createWithBasicHttpAuthentication(getJiraUri(), this.username, this.password);
    }

    private URI getJiraUri() {
        log.info("Serving --> {}", "getJiraUri()");
        return URI.create(this.jiraUrl);
    }

    public String createIssue(String projectKey, Long issueType, String issueSummary) {
        log.info("Serving --> {}", "createIssue()");
        IssueRestClient issueClient = restClient.getIssueClient();

        IssueInput newIssue = new IssueInputBuilder(projectKey, issueType, issueSummary).build();

        return issueClient.createIssue(newIssue).claim().getKey();
    }

    public Issue getIssue(String issueKey) {
        log.info("Serving --> {}", "getIssue()");
        Issue issue = restClient.getIssueClient().getIssue(issueKey).claim();
        try {
            restClient.close();
        } catch (IOException e) {
            log.error("Failed to get issue " + e.getMessage());
            e.printStackTrace();
        }
        return issue;
    }
}
