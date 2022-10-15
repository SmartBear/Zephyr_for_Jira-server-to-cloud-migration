package com.zephyr.migration.controllers;

import com.zephyr.migration.service.MigrationService;
import com.zephyr.migration.utils.ConfigProperties;
import com.zephyr.migration.utils.MigrationProgress;
import com.zephyr.migration.utils.ProjectMigrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@RestController
public class MigrationController {

    private static final Logger log = LoggerFactory.getLogger(MigrationController.class);

    @Autowired
    MigrationService migrationService;

    @Autowired
    ConfigProperties configProp;
    
    public static Map<String, String> logsMap = new LinkedHashMap<>();
    

	@GetMapping("/migrate/{projectId}")
    public String migrateVersion(@PathVariable Long projectId) throws Exception {
        /*change it to post method*/
        migrationService.initializeHttpClientDetails();
        migrationService.migrateSingleProject(projectId);
        return String.format("Hello Migration triggered for project %s!", projectId);
    }

    @GetMapping("/getProgressInformation")
    public String fetchProgressInformation() {
    	String lastLogs = logsMap.get("logs");
        List<String> progressDetails = migrationService.getProgressDetails();
        StringBuffer progressMessages = new StringBuffer();
        if (lastLogs != null) {
        	progressMessages.append(lastLogs).append("<br>");
        }else {
        	progressMessages.append("<h3>Zephyr Server-Cloud Migration Triggered successfully.</h3>").append("<br>");
        }
        progressDetails.forEach(progressMessage -> progressMessages.append(progressMessage).append("<br>"));
        logsMap.put("logs", progressMessages.toString());
        return progressMessages.toString();
    }
    

    @GetMapping("/migration/status")
    public Map<Long, ProjectMigrationStatus> migrationStatus() {
        return MigrationProgress.statusMap;
    }
}
