package com.zephyr.migration.service;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.zephyr.migration.dto.JiraIssueDTO;
import com.zephyr.migration.dto.TestStepResultDTO;
import com.zephyr.migration.model.ZfjCloudStepResultBean;

import java.util.List;
import java.util.Map;

public interface TestService {

    Issue getIssueDetailsFromServer(String issueKey);

    JiraIssueDTO createIssueInCloud(Issue issue);

    void createUnscheduledVersion(Long projectId)  throws Exception;

    void createVersionInJiraCloud(Long projectId);

    void fetchCyclesFromServer(Long projectId, Long versionId);

    void initializeHttpClientDetails();

    void triggerProjectMetaData(Long projectId);

    void importStepResultLevelAttachments(List<TestStepResultDTO> testStepResults, Map<Integer, ZfjCloudStepResultBean> stepResultBeanMap);

}
