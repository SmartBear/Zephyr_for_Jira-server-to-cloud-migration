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

    /******* General constants *********/
    public static final String XLS = ".xls";
    public static final String CLOUD_UNSCHEDULED_VERSION_ID = "-1";
    public static final String AD_HOC_CYCLE_ID = "-1";
    public static final String AD_HOC_CYCLE_NAME = "Ad Hoc";
    public static final String ZAPI_ACCESS_KEY = "zapiAccessKey";
    public static final String VERSION_MAPPING_SHEET_NAME = "version-mapping";
    public static final String MAPPING_VERSION_FILE_NAME ="migration-version-mapping-project-";
    public static final String MAPPING_CYCLE_FILE_NAME ="migration-cycle-mapping-project-";
    public static final String MAPPING_FOLDER_FILE_NAME ="migration-folder-mapping-project-";
    public static final String MAPPING_EXECUTION_FILE_NAME ="migration-execution-mapping-project-";
    public static final String CYCLE_MAPPING_SHEET_NAME = "cycle-mapping";
    public static final String FOLDER_MAPPING_SHEET_NAME = "folder-mapping";
    public static final String EXECUTION_MAPPING_SHEET_NAME = "execution-mapping";

    /******* SERVER API endpoint URL *********/
    public static final String SERVER_GET_CYCLES_URL = "cycle?projectId=%s&versionId=%s";
    public static final String SERVER_GET_FOLDERS_URL = "cycle/%s/folders?projectId=%s&versionId=%s&offset=%s&limit=%s";
    public static final String ZAPI_RESOURCE_GET_EXECUTIONS = "execution?projectId=%s&versionId=%s&cycleId=%s&offset=%s&limit=%s";
    public static final String ZAPI_RESOURCE_GET_FOLDER_EXECUTIONS = "execution?projectId=%s&versionId=%s&cycleId=%s&folderId=%s&offset=%s&limit=%s";
}
