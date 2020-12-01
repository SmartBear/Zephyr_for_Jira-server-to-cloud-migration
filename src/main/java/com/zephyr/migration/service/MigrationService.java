package com.zephyr.migration.service;

import java.util.List;

public interface MigrationService {

    void migrateSingleProject(Long projectId)  throws Exception;

    List<String> getProgressDetails();

    void initializeHttpClientDetails();
}
