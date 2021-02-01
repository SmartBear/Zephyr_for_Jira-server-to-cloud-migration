package com.zephyr.migration.controllers;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.zephyr.migration.dto.ExecutionAttachmentDTO;
import com.zephyr.migration.dto.TestStepResultDTO;
import com.zephyr.migration.model.ZfjCloudExecutionBean;
import com.zephyr.migration.dto.FolderDTO;
import com.zephyr.migration.dto.JiraIssueDTO;
import com.zephyr.migration.model.SearchRequest;
import com.zephyr.migration.model.ZfjCloudStepResultBean;
import com.zephyr.migration.service.*;
import com.zephyr.migration.utils.ApplicationConstants;
import com.zephyr.migration.utils.FileUtils;
import com.zephyr.migration.utils.MigrationMappingFileGenerationUtil;
import com.zephyr.migration.utils.ConfigProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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

    @Autowired
    FolderService folderService;

    @Autowired
    ExecutionService executionService;

    @Autowired
    AttachmentService attachmentService;

    @Autowired
    TestStepService testStepService;

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
        Path path = Paths.get(migrationFilePath, ApplicationConstants.MAPPING_VERSION_FILE_NAME+projectId+".xls");
        if(Files.exists(path)){
            List vList = FileUtils.readFile(migrationFilePath, ApplicationConstants.MAPPING_VERSION_FILE_NAME + projectId + ".xls");
            return "false";
        }
        //migrationMappingFileGenerationUtil.generateVersionMappingReportExcel(migrationFilePath, projectId, response);
        return "true";
    }

    @GetMapping("/create/jira/{projectId}")
    public String createVersionInJiraCloud(@PathVariable Long projectId) {
        testService.createVersionInJiraCloud(projectId);
        return String.format("Hello Unscheduled version has been created for project %s!", projectId);
    }

    @GetMapping("/fetch/server/cycles/{projectId}/{versionId}")
    public String fetchCyclesFromServer(@PathVariable Long projectId, @PathVariable Long versionId) {
        testService.initializeHttpClientDetails();
        testService.fetchCyclesFromServer(projectId,versionId);
        return String.format("Hello Unscheduled version has been created for project %s!", projectId);
    }

    @GetMapping("/triggerProjectMetaData/{projectId}")
    public String triggerProjectMetaData(@PathVariable Long projectId) {
        testService.triggerProjectMetaData(projectId);
        return String.format("Trigger project meta data for project %s!", projectId);
    }

    @GetMapping("/create/folder/{projectId}")
    public String createFolderInJiraCloud(@PathVariable Long projectId) {
        FolderDTO folderDTO = new FolderDTO();
        folderDTO.setCycleId("0b619922-e085-4173-bf81-6c3e7f33bf97");
        folderDTO.setFolderName("tata");
        folderDTO.setProjectId("10000");
        folderDTO.setVersionId("-1");
        folderService.createFolderInZephyrCloud(folderDTO, new SearchRequest());
        return String.format("Hello Unscheduled version has been created for project %s!", projectId);
    }

    @GetMapping("/fetch/server-folder/{projectId}")
    public String fetchFolderFromJiraServer(@PathVariable Long projectId) {
        final String SERVER_USER_NAME = configProp.getConfigValue("zfj.server.username");
        final String SERVER_USER_PASS = configProp.getConfigValue("zfj.server.password");
        final String SERVER_BASE_URL = configProp.getConfigValue("zfj.server.baseUrl");
        folderService.fetchFoldersFromZephyrServer(39L, "SERVER_BASE_URL", "SERVER_USER_NAME",new ArrayBlockingQueue<>(10000));
        return String.format("Hello Unscheduled version has been created for project %s!", projectId);
    }

    @GetMapping("/create/execution")
    public String createExecutionInJiraCloud() {
        ZfjCloudExecutionBean executionDTO = new ZfjCloudExecutionBean();
        executionDTO.setProjectId(10000);
        executionDTO.setVersionId(-1);
        executionDTO.setComment("testing comment");
        executionDTO.setCycleId("b08568d9-237f-420f-84e5-3b7e7d28855a");
        executionDTO.setIssueId(10005);
        executionService.createExecutionInJiraCloud(executionDTO);
        return String.format("Hello execution has been created for cycle %s!", executionDTO.getCycleId());
    }

    @GetMapping("/downloadFile")
    public String downloadFile(@RequestParam("executionId") Integer executionId, HttpServletRequest request) {

        testService.initializeHttpClientDetails();
        if(executionId != null) {
            File tempFile;
            AtomicReference<File> file = new AtomicReference<>();
            try {

                List<ExecutionAttachmentDTO> attachmentList = attachmentService.getAttachmentResponse(executionId, ApplicationConstants.ENTITY_TYPE.EXECUTION);
                if(attachmentList != null && attachmentList.size() > 0) {
                    List<ExecutionAttachmentDTO> executionAttachments = attachmentList.stream()
                            .filter(Objects::nonNull).collect(Collectors.toList());
                    List<File> filesToDelete = new ArrayList<>();
                    executionAttachments.forEach(attachment -> {
                        if(attachment != null) {
                            try {
                                File executionAttachmentFile = attachmentService.downloadExecutionAttachmentFileFromZFJ(attachment.getFileId(), attachment.getFileName());
                                if(executionAttachmentFile != null) {
                                    filesToDelete.add(executionAttachmentFile);
                                    file.set(executionAttachmentFile);
                                }
                            } catch (Exception e) {
                                log.error("Error while downloading the Testcase Execution Attachment for Execution -> " + attachment.getFileId(), e);
                            }
                        }
                    });
                    if (!filesToDelete.isEmpty()) {
                        filesToDelete.forEach(f -> {
                            if(f.exists())
                                f.delete();
                        });
                    }
                }
            } catch (Exception ex) {
                log.error("Error while creating attachment for issue", ex);
            }

            return String.format("File name is  %s!", file.get().getName());
        } else {
            return "Not Found";
        }
    }

    @GetMapping("/uploadFileForStepResults")
    public String uploadFileForStepResults(@RequestParam("executionId") String executionId, @RequestParam("issueId") Integer issueId) {

        testService.initializeHttpClientDetails();
        if(executionId != null) {
            if(issueId == null) {
                issueId = 10003;
            }
            List<ZfjCloudStepResultBean> stepResultBeans = testStepService.getTestStepResultsFromZFJCloud(executionId);
            log.info("step result beans "+stepResultBeans.toString());

            String serverExecutionId = "757";
            List<TestStepResultDTO> testStepResults = testStepService.getTestStepsResultFromZFJ(serverExecutionId);
            if(CollectionUtils.isNotEmpty(testStepResults)) {
                TestStepResultDTO testStepResultDTO = testStepResults.get(0);
                Map<Integer, ZfjCloudStepResultBean> stepResultBeanMap = stepResultBeans.stream().collect(Collectors.toMap(ZfjCloudStepResultBean::getOrderId, c -> c));
                testService.importStepResultLevelAttachments(testStepResults,stepResultBeanMap);
            }
            return "Step results found";
        }
        return "Not Found";
    }
}
