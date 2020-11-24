package com.zephyr.migration.controllers;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.zephyr.migration.dto.JiraIssueDTO;
import com.zephyr.migration.service.TestService;
import com.zephyr.migration.utils.ApplicationConstants;
import com.zephyr.migration.utils.FileUtils;
import com.zephyr.migration.utils.MigrationMappingFileGenerationUtil;
import com.zephyr.migration.utils.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

/**
 * This controller is for testing a specific flow.
 */
@RestController
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    @Value("${migrationFilePath}")
    private String migrationFilePath;

    @Autowired
    TestService testService;

    @Autowired
    MigrationMappingFileGenerationUtil migrationMappingFileGenerationUtil;

    @Autowired
    ConfigProperties configProp;

    @GetMapping("/hello")
    public String sayHello(@RequestParam(value = "myName", defaultValue = "World") String name) {
        log.info("Serving --> {}", "sayHello()");
        log.error("Serving --> {}", "sayHello()");

        String portNumber = configProp.getConfigValue("server.port");
        return String.format("Hello %s!", name +portNumber);
    }

    @GetMapping("/getIssueDescription")
    public String getIssueDescription(@RequestParam(value = "issueKey", defaultValue = "World") String issueKey) {
        log.info("Serving --> {}", "getIssueDescription()");
        log.info("Getting issue description of issue key is : " + issueKey);
        String summary = "";
        try {
            Issue issue = testService.getIssueDetailsFromServer(issueKey);
            summary = issue.getSummary();
        }catch (Exception ex) {
            log.error("Failed to get issue description" + ex.getMessage());
        }
       /* if(Objects.nonNull(issue)) {
            JiraIssueDTO issueDTO = testService.createIssueInCloud(issue);
        }*/
        return String.format("Issue Summary ::: %s!", summary);
    }

    @GetMapping("/createIssueDescription")
    public String createIssueDescription(@RequestParam(value = "issueKey", defaultValue = "World") String issueKey) {
        log.info("Serving --> {}", "createIssueDescription()");
        log.info("Create issue at cloud of issue key is : " + issueKey);
        String summary = "";
        JiraIssueDTO issueDTO = null;
        try {
            Issue issue = testService.getIssueDetailsFromServer(issueKey);
            if(Objects.nonNull(issue)) {
                issueDTO = testService.createIssueInCloud(issue);
            }
            summary = issue.getSummary();
        }catch (Exception ex) {
            log.error("Failed to create issue at cloud" + ex.getMessage());
        }
        return String.format("Issue Summary from cloud ::: %s! & Issue Summary from server ::: %s!",
                null != issueDTO ? issueDTO.getSummary() : "", summary);

    }

    @GetMapping("/create/unscheduled/version/{projectId}")
    public String createUnscheduledVersion(@PathVariable Long projectId) throws Exception{
        testService.createUnscheduledVersion(projectId);
        return String.format("Hello Unscheduled version has been created for project %s!", projectId);
    }

    @GetMapping("migrationMappingFile")
    public String versionMigrationFile(@RequestParam(value = "projectId", defaultValue = "World") String projectId,
                                       HttpServletResponse response) throws Exception {
        log.info("migration file path is : " + migrationFilePath);
        Path path = Paths.get(migrationFilePath, ApplicationConstants.VERSION_MAPPING_FILE_NAME+projectId+".xls");
        if(Files.exists(path)){
            List vList = FileUtils.readFile(migrationFilePath, ApplicationConstants.VERSION_MAPPING_FILE_NAME + projectId + ".xls");
            return "false";
        }
        //migrationMappingFileGenerationUtil.generateVersionMappingReportExcel(migrationFilePath, projectId, response);
        return "true";
    }
}
