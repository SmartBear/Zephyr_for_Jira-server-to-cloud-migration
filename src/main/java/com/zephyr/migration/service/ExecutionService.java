package com.zephyr.migration.service;

import com.zephyr.migration.dto.CycleDTO;
import com.zephyr.migration.model.ZfjCloudExecutionBean;
import com.zephyr.migration.dto.ExecutionDTO;
import com.zephyr.migration.dto.FolderDTO;

import java.util.List;

public interface ExecutionService {

    List<ExecutionDTO> getExecutionsFromZFJByVersionAndCycleName(String projectId, String versionId, String cycleId, int offset, int maxRecords);

    List<ExecutionDTO> getExecutionsFromZFJByVersionCycleAndFolderName(CycleDTO cycle, FolderDTO folder, int offset, int maxRecords);

    ZfjCloudExecutionBean createExecutionInJiraCloud(ZfjCloudExecutionBean zfjCloudExecutionBean);
}
