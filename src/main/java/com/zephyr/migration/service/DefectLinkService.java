package com.zephyr.migration.service;

import com.zephyr.migration.model.Defect;
import com.zephyr.migration.model.Issue;

import java.util.List;
import java.util.Map;

public interface DefectLinkService {

    List<Issue> getExecutionLevelDefectFromServer(Integer executionId, Map<String, Issue> processedIssueMap);

    Map<String, List<Issue>> getStepLevelDefectFromZfj(Integer executionId, Map<String, Issue> processedIssueMap);
    
    void createExecutionLevelDefectInZephyrCloud(String executionId, List<Defect> defects);

    void createStepResultLevelDefectInZephyrCloud(String executionId, String stepResultId, List<Defect> defects);
}
