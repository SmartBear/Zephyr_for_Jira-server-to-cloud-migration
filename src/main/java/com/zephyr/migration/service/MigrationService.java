package com.zephyr.migration.service;

public interface MigrationService {

    void migrateSingleProject(Long projectId)  throws Exception;

    void createUnscheduledVersion(Long projectId)  throws Exception;
}
