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
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    @Value("${project.threads.run.at.time:1}")
    private int projectsThreadSize;

    @GetMapping("/beginMigration")
    public String sendForm(Model model, MigrationRequest migrationRequest) {
    	//model.addAttribute("message", "");
        return "migrateProject";
    }

    @PostMapping("/beginMigration")
    public String processForm(Model model, MigrationRequest migrationRequest, @RequestParam("file") MultipartFile file) {
        try {
            List<Long> projectsIds = getInputProjects(migrationRequest, file);
            migrationService.initializeHttpClientDetails();
            projectsIds.forEach(s -> {
                migrationProgressService.initMigrationStatus(s);
            });
            log.info("Projects received for migration : {}", projectsIds);
            log.info("Projects projectsThreadSize : {} ", projectsThreadSize);
            if (projectsThreadSize > 1 && projectsIds != null && projectsIds.size() > 1) {
                log.info("Projects running parallel");
                ExecutorService executorService = Executors.newFixedThreadPool(projectsThreadSize);
                Set<Future<Boolean>> futureSet = new HashSet<Future<Boolean>>();
                for (Long projectId : projectsIds) {
                    futureSet.add(executorService.submit(() -> {
                        runMigrationForTheProject(projectId);
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
                log.info("Projects running one by one");
                for (Long projectId : projectsIds) {
                    runMigrationForTheProject(Long.valueOf(projectId));
                }
                log.info("*****************ALL PROJECTS MIGRATION COMPLETED*******************");
            }

        } catch (Exception e) {
            log.error("Error occurred while migrating the data.", e.fillInStackTrace());
        }
        //model.addAttribute("message", "Zephyr Server-Cloud Migration Triggered successfully.");
        //return "migrationSuccess";
        return "migrateProject";
    }

    private void runMigrationForTheProject(Long projectId) {
        try {
            MDC.put(ApplicationConstants.MDC_LOG_FILENAME, String.valueOf(projectId));
            log.info("***************** : Started migration for the project {} ", projectId);
            migrationProgressService.updateMigrationStatusAndStep(projectId, ProgressStatusLevel.IN_PROGRESS, "Migration started for the project....");
            migrationService.migrateSingleProject(projectId);
            migrationProgressService.updateMigrationStatusAndStep(projectId, ProgressStatusLevel.SUCCESS, "Migration completed for the project....");
        } catch (Exception exp) {
            migrationProgressService.updateMigrationStatusAndStep(projectId, ProgressStatusLevel.FAILED, "Migration failed for the project....");
            //migrationProgressService.updateMigrationStatusAndStep(projectId, ProgressStatusLevel.FAILED, "Migration failed for the project....");
            log.error("Exception while running migration for the project {} ", projectId, exp);
        } finally {
            migrationProgressService.addMigrationSteps(projectId, "----------------------------------------------------------------------------");
            log.info("***************** : Completed migration for the project {} ", projectId);
            MDC.remove(ApplicationConstants.MDC_LOG_FILENAME);
        }
    }

    @GetMapping("/viewLogs")
    public String viewLogs() {
        return "migrationViewLogs";
    }

    private List<Long> getInputProjects(MigrationRequest migrationRequest, MultipartFile file) {
        List<String> list = new ArrayList<>();
        List<Long> pIds = new ArrayList<>();
        String projectIds = "";
        try {
            if (file != null && !file.isEmpty()) {
                String content = new String(file.getBytes(), StandardCharsets.UTF_8);
                if (content != null) {
                    projectIds = content.replaceAll("/n", "").replaceAll("/r", "");
                }
            } else {
                projectIds = migrationRequest.getListOfProjects();
            }
            if (projectIds != null) {
                list = Arrays.asList(projectIds.split(",", -1));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (list != null && list.size() > 0) {
            list.stream().forEach(s -> {
                try {
                    pIds.add(Long.valueOf(s.trim()));
                } catch (Exception exp) {
                    log.error("Provided project id is not in correct format {} ", s, exp);
                }
            });
        }

        return pIds;
    }
}
