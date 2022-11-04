package com.zephyr.migration.service.impl;

import com.zephyr.migration.service.MigrationProgressService;
import com.zephyr.migration.utils.EntityCounts;
import com.zephyr.migration.utils.MigrationProgress;
import com.zephyr.migration.utils.ProgressStatusLevel;
import com.zephyr.migration.utils.ProjectMigrationStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MigrationProgressServiceImpl implements MigrationProgressService {
    private List<String> entities = Arrays.asList("versions", "cycles", "folders", "executions");

    @Override
    public void initMigrationStatus(final Long projectId) {
        ProjectMigrationStatus status = MigrationProgress.statusMap.get(projectId);
        if (status == null) {
            status = new ProjectMigrationStatus();
            status.status = ProgressStatusLevel.READY;
            status.statusSteps = new ArrayList<>();

            //status.counts = getEntities();

            MigrationProgress.statusMap.put(projectId, status);
        } else {
            status.status = ProgressStatusLevel.READY;
            //status.counts = getEntities();
        }
    }

    @Override
    public void addMigrationSteps(final Long projectId, final String step) {
        ProjectMigrationStatus status = MigrationProgress.statusMap.get(projectId);
        status.statusSteps.add(step + " " + new Date());
    }

    @Override
    public void updateMigrationStatus(final Long projectId, final ProgressStatusLevel status) {
        ProjectMigrationStatus statusBean = MigrationProgress.statusMap.get(projectId);
        statusBean.status = status;
    }

    @Override
    public void updateMigrationStatusAndStep(final Long projectId, final ProgressStatusLevel status, final String step) {
        ProjectMigrationStatus statusBean = MigrationProgress.statusMap.get(projectId);
        statusBean.status = status;
        statusBean.statusSteps.add(step + " " + new Date());
    }

    @Override
    public void setOrUpdateMigrationEntityTotalCount(final Long projectId, final String entity, final Long count) {
        ProjectMigrationStatus statusBean = MigrationProgress.statusMap.get(projectId);
        //statusBean.counts.get(entity).total = statusBean.counts.get(entity).total + count;
    }

    @Override
    public void setOrUpdateMigrationEntityProcessCount(final Long projectId, final String entity, final Long count) {
        ProjectMigrationStatus statusBean = MigrationProgress.statusMap.get(projectId);
        //statusBean.counts.get(entity).processed = statusBean.counts.get(entity).processed + count;
    }

    @Override
    public void setOrUpdateMigrationEntityFailedCount(final Long projectId, final String entity, final Long count) {
        ProjectMigrationStatus statusBean = MigrationProgress.statusMap.get(projectId);
        //statusBean.counts.get(entity).failed = statusBean.counts.get(entity).failed + count;
    }

    private Map<String, EntityCounts> getEntities() {
        Map<String, EntityCounts> map = new HashMap<>();
        entities.forEach(s -> {
            map.put(s, new EntityCounts());
        });
        return map;
    }
}
