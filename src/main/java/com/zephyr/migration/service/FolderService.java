package com.zephyr.migration.service;

import com.zephyr.migration.dto.FolderDTO;
import com.zephyr.migration.model.SearchRequest;
import com.zephyr.migration.model.ZfjCloudFolderBean;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public interface FolderService {

    ZfjCloudFolderBean createFolderInZephyrCloud(FolderDTO folderDTO, SearchRequest searchRequest);

    List<FolderDTO> fetchFoldersFromZephyrServer(Long cycleId, String projectId, String versionId, ArrayBlockingQueue<String> progressQueue);

}
