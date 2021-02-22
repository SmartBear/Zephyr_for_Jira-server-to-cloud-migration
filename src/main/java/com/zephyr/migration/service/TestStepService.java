package com.zephyr.migration.service;

import com.zephyr.migration.dto.JiraCloudTestStepDTO;
import com.zephyr.migration.dto.TestStepResultDTO;
import com.zephyr.migration.dto.TestStepDTO;
import com.zephyr.migration.model.ZfjCloudExecutionBean;
import com.zephyr.migration.model.ZfjCloudStepResultBean;

import java.util.List;

public interface TestStepService {

    List<TestStepResultDTO> getTestStepsResultFromZFJ(String executionId);

    List<ZfjCloudStepResultBean> getTestStepResultsFromZFJCloud(String cloudExecutionId);

    List<TestStepDTO> fetchTestStepsFromZFJ(Integer issueId);

    List<JiraCloudTestStepDTO> createTestStepInJiraCloud(List<TestStepDTO> testSteps, Integer issueId, Long projectId);

    String updateStepResult(TestStepDTO testStepDTO);

}
