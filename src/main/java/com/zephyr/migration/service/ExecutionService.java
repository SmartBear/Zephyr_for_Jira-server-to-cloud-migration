package com.zephyr.migration.service;

import com.zephyr.migration.dto.CycleDTO;
import com.zephyr.migration.dto.ExecutionCloudDTO;
import com.zephyr.migration.dto.ExecutionDTO;
import com.zephyr.migration.dto.FolderDTO;

import java.util.List;

public interface ExecutionService {

    List<ExecutionDTO> getExecutionsFromZFJByVersionAndCycleName(CycleDTO cycle, int offset, int maxRecords);

    List<ExecutionDTO> getExecutionsFromZFJByVersionCycleAndFolderName(CycleDTO cycle, FolderDTO folder, int offset, int maxRecords);

    void createExecutionInJiraCloud(ExecutionCloudDTO executionDTO);
}
