package com.zephyr.migration.service;

import com.zephyr.migration.utils.ProgressStatusLevel;

public interface MigrationProgressService {
    void initMigrationStatus(Long projectId);

    void addMigrationSteps(Long projectId, String step);

    void updateMigrationStatus(Long projectId, ProgressStatusLevel status);

    void updateMigrationStatusAndStep(Long projectId, ProgressStatusLevel status, String step);

    void setOrUpdateMigrationEntityTotalCount(Long projectId, String entity, Long count);

    void setOrUpdateMigrationEntityProcessCount(Long projectId, String entity, Long count);

    void setOrUpdateMigrationEntityFailedCount(Long projectId, String entity, Long count);
}
