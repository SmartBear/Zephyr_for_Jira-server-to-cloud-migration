package com.zephyr.migration.service.impl;

import com.google.common.collect.Lists;
import com.sun.jersey.api.client.ClientResponse;
import com.zephyr.migration.client.HttpClient;
import com.zephyr.migration.model.Issue;
import com.zephyr.migration.model.TestCaseBean;
import com.zephyr.migration.service.IssueService;
import com.zephyr.migration.utils.ApplicationConstants;
import com.zephyr.migration.utils.ConfigProperties;
import com.zephyr.migration.utils.JsonUtil;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class IssueServiceImpl implements IssueService {

    private static final Logger log = LoggerFactory.getLogger(IssueServiceImpl.class);

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    @Qualifier(value = "zapiHttpClient")
    private HttpClient zapiHttpClient;

    @Autowired
    @Qualifier(value = "jiraHttpClient")
    private HttpClient jiraHttpClient;

    private static final int count = 0;

    @Override
    public Issue getIssueFromJira(String issueKey) {

        jiraHttpClient.setResourceName(String.format(ApplicationConstants.JIRA_RESOURCE_ISSUE,issueKey));

        ClientResponse response = jiraHttpClient.get();

        TypeReference<Issue> reference = new TypeReference<Issue>() {};
        Issue issue;
        try {
            issue = JsonUtil.readValue(response.getEntity(String.class), reference);
            if(null != issue)
                return issue;

        } catch (IOException e) {
            log.error("Error occurred while fetching the issue details.",e.fillInStackTrace());
        }
        return null;
    }

    @Override
    public List<Issue> getIssueDetailsFromJira(String projectId, int startIndex, int limit) {
        String resourceName = String.format(ApplicationConstants.JIRA_RESOURCE_SEARCH_ISSUE_JQL,projectId,0);

        jiraHttpClient.setResourceName(ApplicationConstants.JIRA_RESOURCE_SEARCH_ISSUE + resourceName);

        ClientResponse response = jiraHttpClient.get();

        TypeReference<TestCaseBean> reference = new TypeReference<TestCaseBean>() {};
        TestCaseBean testCaseBean;
        try {
            testCaseBean = JsonUtil.readValue(response.getEntity(String.class), reference);
            if(null != testCaseBean)
                return testCaseBean.getIssues();

        } catch (IOException e) {
            log.error("Error occurred while fetching the test (issue) details from jira",e.fillInStackTrace());
        }

        return Lists.newArrayList();
    }

    @Override
    public Integer getTotalTestCountPerProjectFromJira(String projectId) {

        log.info("Serving --> {}", "getTotalTestCountPerProjectFromJira()");
        String resourceName = String.format(ApplicationConstants.JIRA_RESOURCE_SEARCH_ISSUE_JQL,projectId,0);

        jiraHttpClient.setResourceName(ApplicationConstants.JIRA_RESOURCE_SEARCH_ISSUE + resourceName);

        ClientResponse response = jiraHttpClient.get();

        TypeReference<TestCaseBean> reference = new TypeReference<TestCaseBean>() {};
        TestCaseBean testcaseBean;
        try {
            testcaseBean = JsonUtil.readValue(response.getEntity(String.class), reference);
            if(null != testcaseBean)
                return testcaseBean.getTotal();

        } catch (IOException e) {
            log.error("Error occurred while fetching the total test counts from jira",e.fillInStackTrace());
        }

        return count;
    }
}
