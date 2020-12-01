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

    /******* General constants *********/
    public static final String XLS = ".xls";
    public static final String CLOUD_UNSCHEDULED_VERSION_ID = "-1";
    public static final String ZAPI_ACCESS_KEY = "zapiAccessKey";
    public static final String VERSION_MAPPING_SHEET_NAME = "version-mapping";
    public static final String VERSION_MAPPING_FILE_NAME ="migration-version-mapping-project-";

    /******* SERVER API endpoint URL *********/
    public static final String SERVER_GET_CYCLES_URL = "/rest/zapi/latest/cycle?projectId=%s&versionId=%s";
}
