package com.zephyr.migration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.zephyr.migration.dto.CycleDTO;
import com.zephyr.migration.model.ZfjCloudCycleBean;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public interface CycleService {

    ZfjCloudCycleBean createCycleInZephyrCloud(CycleDTO cycleDTO);

    List<CycleDTO> fetchCyclesFromZephyrServer(Long projectId, String serverVersionId, String server_base_url, String server_user_name, String server_user_pass, ArrayBlockingQueue<String> progressQueue);
}
