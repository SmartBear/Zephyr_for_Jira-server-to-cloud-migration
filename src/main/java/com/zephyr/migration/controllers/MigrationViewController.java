package com.zephyr.migration.controllers;

import com.zephyr.migration.service.MigrationService;
import com.zephyr.migration.utils.ConfigProperties;
import com.zephyr.migration.vo.MigrationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class MigrationViewController {
    private static final Logger log = LoggerFactory.getLogger(MigrationController.class);

    @Autowired
    MigrationService migrationService;

    @Autowired
    ConfigProperties configProp;

    @GetMapping("/beginMigration")
    public String sendForm(MigrationRequest migrationRequest) {
        return "migrateProject";
    }

    @PostMapping("/beginMigration")
    public String processForm(MigrationRequest migrationRequest) {
        try {
            migrationService.initializeHttpClientDetails();
            migrationService.migrateSingleProject(migrationRequest.getProjectId());
        } catch (Exception e) {
            log.error("Error occurred while migrating the data.", e.fillInStackTrace());
        }
        return "migrationSuccess";
    }
}
