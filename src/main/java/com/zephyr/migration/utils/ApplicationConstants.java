package com.zephyr.migration.utils;

import org.springframework.stereotype.Component;

/**
 * Created by Himanshu
 */
@Component
public class ApplicationConstants {

    public static final String VERSION_MAPPING_FILE_NAME ="migration-version-mapping-project-";
    public static final String CLOUD_FETCH_VERSION_URL ="/public/rest/api/1.0/migration/fetch/versions";
    public static final String CLOUD_CREATE_UNSCHEDULED_VERSION_URL ="/public/rest/api/1.0/migration/create/unscheduled/version";
    public static final String ZAPI_ACCESS_KEY = "zapiAccessKey";


}
