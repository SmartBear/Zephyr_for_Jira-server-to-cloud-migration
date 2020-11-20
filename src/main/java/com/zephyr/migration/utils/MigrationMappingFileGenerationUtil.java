package com.zephyr.migration.utils;

import com.atlassian.jira.rest.client.api.domain.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.zephyr.migration.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class MigrationMappingFileGenerationUtil {


    @Autowired
    VersionService versionService;

    /*
    * Generate Excel File For Migration Report
    * */
    public void generateVersionMappingReportExcel(String migrationFilePath, String projectId, Iterable<Version> versionsFromZephyrServer, JsonNode versionsFromZephyrCloud)  throws Exception{
        List<List<String>> sampleList = versionDataToPrintInExcel(projectId, versionsFromZephyrServer, versionsFromZephyrCloud);
        try {
            ExcelUtils excelUtils = new ExcelUtils();
            excelUtils.writeToExcelFileMethod(migrationFilePath, ApplicationConstants.VERSION_MAPPING_FILE_NAME+projectId, "version-mapping", sampleList);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Header Creator
     */
    public static List<String> generateHeader()  throws Exception {
        List<String> header1 = new ArrayList<String>();;
        header1.add("Project Id");
        header1.add("Server Version Id");
        header1.add("Cloud Version Id");
        return header1;
    }

    /**
     * Data adding in list
     * @return
     * @throws Exception
     */
    public List<List<String>> versionDataToPrintInExcel(String projectId, Iterable<Version> versionsFromZephyrServer, JsonNode versionsFromZephyrCloud) throws Exception {
        List<List<String>> recordToAdd = new ArrayList<>();
        recordToAdd.add(generateHeader());
        List serverVersionIdList = new ArrayList<>();
        if(Objects.nonNull(versionsFromZephyrServer)) {
            versionsFromZephyrServer.forEach(version -> {
                serverVersionIdList.add(version.getId());
            });
        }

        if (versionsFromZephyrCloud == null) {
            //do something
        }else {
            List versionMappingList = null;
            int count = -1;
            for (JsonNode jn : versionsFromZephyrCloud) {
                versionMappingList = new ArrayList<>();
                versionMappingList.add(projectId);
                versionMappingList.add(serverVersionIdList.get(++count).toString());
                versionMappingList.add(jn.findValue("versionId").toString());
                recordToAdd.add(versionMappingList);
            }
        }
        return recordToAdd;
    }

}