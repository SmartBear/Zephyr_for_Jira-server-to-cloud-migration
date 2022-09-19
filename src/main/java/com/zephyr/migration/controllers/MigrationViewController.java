package com.zephyr.migration.controllers;

import com.zephyr.migration.service.MigrationProgressService;
import com.zephyr.migration.service.MigrationService;
import com.zephyr.migration.utils.ApplicationConstants;
import com.zephyr.migration.utils.ConfigProperties;
import com.zephyr.migration.utils.ProgressStatusLevel;
import com.zephyr.migration.vo.MigrationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Controller
public class MigrationViewController {
    private static final Logger log = LoggerFactory.getLogger(MigrationController.class);

    @Autowired
    MigrationService migrationService;

    @Autowired
    ConfigProperties configProp;

    @Autowired
    MigrationProgressService migrationProgressService;

    @Value("${project.threads.run.at.time:10}")
    private int projectsThreadSize;

    @GetMapping("/beginMigration")
    public String sendForm(MigrationRequest migrationRequest) {
        return "migrateProject";
    }

    @PostMapping("/beginMigration")
    public String processForm(MigrationRequest migrationRequest) {
        try {
            migrationService.initializeHttpClientDetails();
            List<String> projectsIds = Arrays.asList(migrationRequest.getListOfProjects().split(",", -1));
            projectsIds.forEach(s -> {
                migrationProgressService.initMigrationStatus(Long.valueOf(s));
            });
            log.info("Projects received for migration : {}", projectsIds);
            log.info("Projects projectsThreadSize : {} ", projectsThreadSize);
            if (projectsThreadSize > 1 && projectsIds != null && projectsIds.size() > 1) {
                ExecutorService executorService = Executors.newFixedThreadPool(projectsThreadSize);
                Set<Future<Boolean>> futureSet = new HashSet<Future<Boolean>>();
                for (String projectId : projectsIds) {
                    futureSet.add(executorService.submit(() -> {
                        runMigrationForTheProject(Long.valueOf(projectId));
                        return true;
                    }));
                }

                boolean result = true;
                for (Future<Boolean> booleanFuture : futureSet) {
                    try {
                        if (!(booleanFuture.get())) {
                            result = false;
                        } else {
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        result = false;
                    }
                }
                executorService.shutdown();
                log.info("*****************ALL PROJECTS MIGRATION THREADS COMPLETED*******************");
            } else {
                for (String projectId : projectsIds) {
                    runMigrationForTheProject(Long.valueOf(projectId));
                }
                log.info("*****************ALL PROJECTS MIGRATION COMPLETED*******************");
            }

        } catch (Exception e) {
            log.error("Error occurred while migrating the data.", e.fillInStackTrace());
        }
        return "migrationSuccess";
    }

    private void runMigrationForTheProject(Long projectId) {
        try {
            MDC.put(ApplicationConstants.MDC_LOG_FILENAME, String.valueOf(projectId));
            log.info("***************** : Started migration for the project {} ", projectId);
            migrationProgressService.updateMigrationStatusAndStep(projectId, ProgressStatusLevel.IN_PROGRESS, "Migration started for the project....");
            migrationService.migrateSingleProject(projectId);
        } catch (Exception exp) {
            log.error("Exception while running migration for the project {} ", projectId, exp);
        } finally {
            log.info("***************** : Completed migration for the project {} ", projectId);
            MDC.remove(ApplicationConstants.MDC_LOG_FILENAME);
        }
    }

    @GetMapping("/viewLogs")
    public String viewLogs() {
        return "migrationViewLogs";
    }
}
