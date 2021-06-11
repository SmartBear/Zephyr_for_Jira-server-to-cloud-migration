package com.zephyr.migration.service;



import com.zephyr.migration.model.Issue;

import java.util.List;

public interface IssueService {

    Issue getIssueFromJira(String issueKey);

    List<Issue> getIssueDetailsFromJira(String projectId, int startIndex, int limit);

    Integer getTotalTestCountPerProjectFromJira(String projectId);

}
