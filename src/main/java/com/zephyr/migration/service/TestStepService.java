package com.zephyr.migration.service;

import com.zephyr.migration.dto.TestStepResultDTO;
import com.zephyr.migration.model.ZfjCloudStepResultBean;

import java.util.List;

public interface TestStepService {

    List<TestStepResultDTO> getTestStepsResultFromZFJ(String executionId);

    List<ZfjCloudStepResultBean> getTestStepResultsFromZFJCloud(String cloudExecutionId);

}
