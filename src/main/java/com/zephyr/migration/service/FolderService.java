package com.zephyr.migration.service;

import com.zephyr.migration.dto.CycleDTO;
import com.zephyr.migration.dto.FolderDTO;
import com.zephyr.migration.model.ZfjCloudCycleBean;
import com.zephyr.migration.model.ZfjCloudFolderBean;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public interface FolderService {

    ZfjCloudFolderBean createFolderInZephyrCloud(FolderDTO folderDTO);

    List<FolderDTO> fetchFoldersFromZephyrServer(Long cycleId, String server_base_url, String server_user_name, String server_user_pass);


}
