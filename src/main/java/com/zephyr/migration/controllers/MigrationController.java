package com.zephyr.migration.controllers;

import com.zephyr.migration.service.MigrationService;
import com.zephyr.migration.utils.ConfigProperties;
import com.zephyr.migration.vo.MigrationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MigrationController {

    private static final Logger log = LoggerFactory.getLogger(MigrationController.class);

    @Autowired
    MigrationService migrationService;

    @Autowired
    ConfigProperties configProp;

    @GetMapping("/migrate/{projectId}")
    public String migrateVersion(@PathVariable Long projectId) throws Exception{
        /*change it to post method*/
        migrationService.migrateSingleProject(projectId);
        return String.format("Hello Migration triggered for project %s!", projectId);
    }
}
