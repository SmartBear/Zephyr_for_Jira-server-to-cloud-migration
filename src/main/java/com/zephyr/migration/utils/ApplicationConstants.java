package com.zephyr.migration.utils;

import org.springframework.stereotype.Component;

/**
 * Created by Himanshu
 */
@Component
public class ApplicationConstants {

    /******* CLOUD API endpoint URL *********/
    public static final String CLOUD_FETCH_VERSION_URL ="/public/rest/api/1.0/migration/fetch/versions";
    public static final String CLOUD_CREATE_UNSCHEDULED_VERSION_URL ="/public/rest/api/1.0/migration/create/unscheduled/version";
    public static final String CLOUD_CREATE_VERSION_URL ="/public/rest/api/1.0/create/version";
    public static final String CLOUD_PROJECT_META_REINDEX_URL = "/public/rest/api/1.0/migration/reindexProjectMetaData";
    public static final String CLOUD_CREATE_CYCLE_URL = "/public/rest/api/1.0/migration/create/cycle";
    public static final String CLOUD_CREATE_FOLDER_URL = "/public/rest/api/1.0/migration/create/folder";
    public static final String CLOUD_CREATE_EXECUTION_URL = "/public/rest/api/1.0/migration/create/execution";
    public static final String ADD_EXECUTION_ATTACHMENT_URL = "/public/rest/api/1.0/migration/add/attachment";
    public static final String CLOUD_GET_TEST_STEP_RESULTS_URL = "/public/rest/api/1.0/migration/getStepResults";
    public static final String CLOUD_CREATE_BULK_TEST_STEP_URL = "/public/rest/api/1.0/migration/create/bulk/teststep";
    public static final String CLOUD_UPDATE_STEP_RESULT_URL = "/public/rest/api/1.0/migration/update/stepResult";
    public static final String CLOUD_FETCH_VERSION_BY_JIRA_URL = "/public/rest/api/1.0/migration/fetch/versionsFromJira";
    public static final String CLOUD_FETCH_VERSION_BY_JIRA_URL_V2 = "/public/rest/api/2.0/migration/fetch/versions/jira";

    /******* General constants *********/
    public static final String XLS = ".xls";
    public static final String XLSX = ".xlsx";
    public static final String CLOUD_UNSCHEDULED_VERSION_ID = "-1";
    public static final String AD_HOC_CYCLE_ID = "-1";
    public static final String AD_HOC_CYCLE_NAME = "Ad Hoc";
    public static final String ZAPI_ACCESS_KEY = "zapiAccessKey";
    public static final String VERSION_MAPPING_SHEET_NAME = "version-mapping";
    public static final String MAPPING_VERSION_FILE_NAME ="migration-version-mapping-project-";
    public static final String MAPPING_CYCLE_FILE_NAME ="migration-cycle-mapping-project-";
    public static final String MAPPING_FOLDER_FILE_NAME ="migration-folder-mapping-project-";
    public static final String MAPPING_EXECUTION_FILE_NAME ="migration-execution-mapping-project-";
    public static final String MAPPING_TEST_STEP_FILE_NAME ="migration-test-step-mapping-project-";
    public static final String MAPPING_EXECUTION_ATTACHMENT_FILE_NAME ="migration-execution-attachment-mapping-project-";
    public static final String MAPPING_STEP_RESULT_ATTACHMENT_FILE_NAME ="migration-step-result-attachment-mapping-project-";
    public static final String MAPPING_TEST_STEP_ATTACHMENT_FILE_NAME = "migration-test-step-attachment-mapping-project-";
    public static final String CYCLE_MAPPING_SHEET_NAME = "cycle-mapping";
    public static final String FOLDER_MAPPING_SHEET_NAME = "folder-mapping";
    public static final String EXECUTION_MAPPING_SHEET_NAME = "execution-mapping";
    public static final String TEST_STEP_MAPPING_SHEET_NAME = "test-step-mapping";
    public static final String EXECUTION_ATTACHMENT_MAPPING_SHEET_NAME = "execution-attachment-mapping";
    public static final String STEP_RESULT_ATTACHMENT_MAPPING_SHEET_NAME = "step-result-attachment-mapping";
    public static final String TEST_STEP_ATTACHMENT_MAPPING_SHEET_NAME = "test-step-attachment-mapping";
    public static final String ASSIGNEE_TYPE = "assignee";

    /******* SERVER API endpoint URL *********/
    public static final String SERVER_GET_CYCLES_URL = "cycle?projectId=%s&versionId=%s";
    public static final String SERVER_GET_FOLDERS_URL = "cycle/%s/folders?projectId=%s&versionId=%s&offset=%s&limit=%s";
    public static final String ZAPI_RESOURCE_GET_EXECUTIONS = "execution?projectId=%s&versionId=%s&cycleId=%s&offset=%s&limit=%s";
    public static final String ZAPI_RESOURCE_GET_FOLDER_EXECUTIONS = "execution?projectId=%s&versionId=%s&cycleId=%s&folderId=%s&offset=%s&limit=%s";
    public static final String ZAPI_RESOURCE_GET_ATTACHMENT = "attachment/attachmentsByEntity?entityId=%s&entityType=%s";
    public static final String ZAPI_RESOURCE_GET_ATTACHMENT_FILE_FOR_EXECUTION = "attachment/%s/file";
    public static final String ZAPI_RESOURCE_FETCH_TEST_STEP_RESULT_BY_EXECUTION_ID = "stepResult?executionId=%s&expand=executionStatus";
    public static final String ZAPI_RESOURCE_GET_TEST_STEP = "teststep/%s";

    /******* JIRA SERVER API endpoint URL *********/
    public static final String JIRA_RESOURCE_SEARCH_ISSUE = "search?";
    public static final String JIRA_RESOURCE_SEARCH_ISSUE_JQL = "jql=project=%s+and+issuetype=Test&startAt=%s";
    public static final String JIRA_RESOURCE_SEARCH_ISSUE_KEY_JQL = "jql=issue=%s";
    public static final String JIRA_RESOURCE_ISSUE = "issue/%s";
    public static final String JIRA_RESOURCE_VERSION = "project/%s/version";

    public static final String CYCLE_LEVEL_EXECUTION = "Cycle Level Execution";
    public static final String FOLDER_LEVEL_EXECUTION = "Folder Level Execution";

    public enum ENTITY_TYPE { EXECUTION, TESTSTEP, TESTSTEPRESULT }

    public static final String EXECUTION_ENTITY = "execution";
    public static final String STEP_RESULT_ENTITY = "stepresult";
    public static final String TEST_STEP_ENTITY = "teststep";

}
