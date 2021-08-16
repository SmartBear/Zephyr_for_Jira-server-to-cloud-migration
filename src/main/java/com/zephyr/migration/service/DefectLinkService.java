package com.zephyr.migration.service;

import com.zephyr.migration.client.JIRAHTTPClient;
import com.zephyr.migration.client.ZapiServerHttpClient;
import com.zephyr.migration.model.Defect;
import com.zephyr.migration.model.Issue;

import java.util.List;

public interface DefectLinkService {

    List<Issue> getExecutionLevelDefectFromServer(Integer executionId, JIRAHTTPClient jirahttpClient, ZapiServerHttpClient zapiHttpClient);

    List<Issue> getStepLevelDefectFromZfj(Integer executionId, JIRAHTTPClient jirahttpClient, ZapiServerHttpClient zapiHttpClient);
    
    void createExecutionLevelDefectInZephyrCloud(String executionId, List<Defect> defects);

    void createStepResultLevelDefectInZephyrCloud(String executionId, String stepResultId, List<Defect> defects);
}
