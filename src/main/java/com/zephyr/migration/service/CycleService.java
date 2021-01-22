package com.zephyr.migration.service;

import com.zephyr.migration.dto.CycleDTO;
import com.zephyr.migration.model.ZfjCloudCycleBean;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public interface CycleService {

    ZfjCloudCycleBean createCycleInZephyrCloud(CycleDTO cycleDTO);

    List<CycleDTO> fetchCyclesFromZephyrServer(Long projectId, String serverVersionId, ArrayBlockingQueue<String> progressQueue);
}
