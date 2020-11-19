package com.zephyr.migration.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.zephyr.migration.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class MigrationMappingFileGenerationUtil {

    public String accessKey = "ZTI1YjE1YjctNzBiYi0zNzdkLTg5OGEtYmI4ZDdiYjg0ODU2IDVjZGQyNTRmYWVlMzA4MGRjMmY2MmFjNCBVU0VSX0RFRkFVTFRfTkFNRQ";
    public String zephyrBaseUrl = "https://himanshuconnect.ngrok.io";

    @Autowired
    VersionService versionService;

    /*
    * Generate Excel File For Migration Report
    * */
    public void generateVersionMappingReportExcel(String migrationFilePath, String projectId, HttpServletResponse response)  throws Exception{
        List<List<String>> sampleList = versionDataToPrintInExcel(projectId);
        try {
            ExcelUtils excelUtils = new ExcelUtils();
            excelUtils.writeToExcelFileMethod(migrationFilePath, ApplicationConstants.VERSION_MAPPING_FILE_NAME+projectId, "version-mapping", sampleList, response);
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
        header1.add("Version");
        header1.add("Server Version Id");
        header1.add("Cloud Version Id");
        return header1;
    }

    /**
     * Data adding in list
     * @return
     * @throws Exception
     */
    public List<List<String>> versionDataToPrintInExcel(String projectId) throws Exception {
        List<List<String>> recordToAdd = new ArrayList<>();
        recordToAdd.add(generateHeader());
        JsonNode response =  versionService.getVersionsFromZephyrCloud(projectId, zephyrBaseUrl, accessKey);
        if (response == null) {
            //do something
        }else {
            List versionMappingList = null;
            for (JsonNode jn : response) {
                versionMappingList = new ArrayList<>();
                versionMappingList.add(projectId);
                versionMappingList.add(jn.findValue("name").textValue());
                versionMappingList.add("");
                versionMappingList.add(jn.findValue("versionId").toString());
                recordToAdd.add(versionMappingList);
            }
        }
        return recordToAdd;
    }

}