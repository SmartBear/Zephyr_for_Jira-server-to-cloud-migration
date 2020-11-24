package com.zephyr.migration.utils;

import com.atlassian.jira.rest.client.api.domain.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.zephyr.migration.service.VersionService;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Component
public class MigrationMappingFileGenerationUtil {

    /*
    * Generate Excel File For Migration Report
    * */
    public void generateVersionMappingReportExcel(String migrationFilePath, String projectId, Iterable<Version> versionsFromZephyrServer, JsonNode versionsFromZephyrCloud)  throws Exception{
        List<List<String>> sampleList = versionDataToPrintInExcel(projectId, versionsFromZephyrServer, versionsFromZephyrCloud);
        try {
            ExcelUtils excelUtils = new ExcelUtils();
            excelUtils.writeToExcelFileMethod(migrationFilePath, ApplicationConstants.VERSION_MAPPING_FILE_NAME+projectId, ApplicationConstants.VERSION_MAPPING_SHEET_NAME, sampleList);
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
        //List serverVersionIdList = new ArrayList<>();
        Map<Long, Version> serverVersionMap = new HashMap<>();
        if(Objects.nonNull(versionsFromZephyrServer)) {
            versionsFromZephyrServer.forEach(version -> {
                //serverVersionIdList.add(version.getId());
                serverVersionMap.put(version.getId(), version);
            });
        }


        List versionMappingList;
        int count = -1;
        for (JsonNode jn : versionsFromZephyrCloud) {
            versionMappingList = new ArrayList<>();
            Long cloudVersionId = Long.parseLong(jn.findValue("versionId").toString());
            if(serverVersionMap.containsKey(cloudVersionId)) {
                versionMappingList.add(projectId);
                versionMappingList.add(serverVersionMap.get(cloudVersionId).getId() + "");
                versionMappingList.add(cloudVersionId + "");
                recordToAdd.add(versionMappingList);
            }else {
                versionMappingList.add(projectId);
                versionMappingList.add("");
                versionMappingList.add(cloudVersionId + "");
                recordToAdd.add(versionMappingList);
            }

               /*versionMappingList.add(projectId);
                versionMappingList.add(serverVersionIdList.get(++count).toString());
                versionMappingList.add(jn.findValue("versionId").toString());
                recordToAdd.add(versionMappingList);*/
            }
        return recordToAdd;
    }

    /**
     * Data adding in list
     * @return
     * @throws Exception
     */
    public void doEntryOfUnscheduledVersionInExcel(String projectId, String migrationFilePath) throws Exception {
        String excelFilePath = migrationFilePath+"/"+ApplicationConstants.VERSION_MAPPING_FILE_NAME+projectId+".xls";

        try {
            FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
            HSSFWorkbook wb=new HSSFWorkbook(inputStream);

            HSSFSheet sheet=wb.getSheet(ApplicationConstants.VERSION_MAPPING_SHEET_NAME);

            Object[][] bookData = {
                    {projectId, "", "-1"},
            };

            int rowCount = sheet.getLastRowNum();

            for (Object[] aBook : bookData) {
                Row row = sheet.createRow(++rowCount);

                int columnCount = 0;

                Cell cell = row.createCell(columnCount);
                //cell.setCellValue(rowCount);

                for (Object field : aBook) {
                    cell = row.createCell(columnCount);
                    if (field instanceof String) {
                        cell.setCellValue((String) field);
                    } else if (field instanceof Integer) {
                        cell.setCellValue((Integer) field);
                    }
                    ++columnCount;
                }

            }

            inputStream.close();

            FileOutputStream outputStream = new FileOutputStream(excelFilePath);
            wb.write(outputStream);
            wb.close();
            outputStream.close();

        } catch (IOException | EncryptedDocumentException ex) {
            ex.printStackTrace();
        }
    }

}