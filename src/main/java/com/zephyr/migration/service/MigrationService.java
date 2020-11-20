package com.zephyr.migration.service;

public interface MigrationService {

    void migrateSingleProject(Long projectId)  throws Exception;
}
