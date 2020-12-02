package com.zephyr.migration.utils;

import com.atlassian.jira.rest.client.api.domain.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.zephyr.migration.dto.CycleDTO;
import com.zephyr.migration.model.ZfjCloudCycleBean;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MigrationMappingFileGenerationUtil {

    private static final Long UNSCHEDULED_VERSION_ID = -1L;
    private static final Logger log = LoggerFactory.getLogger(MigrationMappingFileGenerationUtil.class);

    /*
    * Generate Excel File For Migration Report
    * */
    public void generateVersionMappingReportExcel(String migrationFilePath, String projectId, Iterable<Version> versionsFromZephyrServer, JsonNode versionsFromZephyrCloud) {
        try {
            List<List<String>> responseList = versionDataToPrintInExcel(projectId, versionsFromZephyrServer, versionsFromZephyrCloud);
            ExcelUtils excelUtils = new ExcelUtils();
            excelUtils.writeToExcelFileMethod(migrationFilePath, ApplicationConstants.MAPPING_FILE_NAME+projectId, ApplicationConstants.VERSION_MAPPING_SHEET_NAME, responseList);
        }catch (Exception e){
            log.error("Error occurred while writing to the excel file.", e.fillInStackTrace());
        }
    }

    /**
     * Header Creator
     */
    public static List<String> generateHeader()  throws Exception {
        List<String> header1 = new ArrayList<String>();
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
                if(cloudVersionId.equals(UNSCHEDULED_VERSION_ID)) {
                    versionMappingList.add(projectId);
                    versionMappingList.add("-1");
                    versionMappingList.add(cloudVersionId + "");
                    recordToAdd.add(versionMappingList);
                }
            }
        }
        return recordToAdd;
    }

    /**
     * Data adding in list
     * @return
     * @throws Exception
     */
    public void doEntryOfUnscheduledVersionInExcel(String projectId, String migrationFilePath) {
        String excelFilePath = migrationFilePath+"/"+ApplicationConstants.MAPPING_FILE_NAME+projectId+".xls";
        try {
            FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
            HSSFWorkbook wb=new HSSFWorkbook(inputStream);
            HSSFSheet sheet=wb.getSheet(ApplicationConstants.VERSION_MAPPING_SHEET_NAME);

            Object[][] bookData = { {projectId, "-1", "-1"}};

            int rowCount = sheet.getLastRowNum();

            for (Object[] aBook : bookData) {
                Row row = sheet.createRow(++rowCount);
                int columnCount = 0;

                Cell cell;
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

    public void updateVersionMappingFile(Long projectId, String migrationFilePath, Map<String, Long> serverCloudVersionMapping) {
        String excelFilePath = migrationFilePath+"/"+ApplicationConstants.MAPPING_FILE_NAME+projectId+".xls";
        try {
            if(serverCloudVersionMapping.size() > 0) {
                FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
                HSSFWorkbook wb=new HSSFWorkbook(inputStream);
                HSSFSheet sheet=wb.getSheet(ApplicationConstants.VERSION_MAPPING_SHEET_NAME);

                Object[][] rowDataSet = new Object[serverCloudVersionMapping.size()][3];
                int rowCount = sheet.getLastRowNum();

                populateRowDataSet(rowDataSet,projectId, serverCloudVersionMapping);

                for (Object[] rowData : rowDataSet) {
                    Row row = sheet.createRow(++rowCount);
                    int columnCount = 0;

                    Cell cell;
                    for (Object field : rowData) {
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
            }

        } catch (IOException | EncryptedDocumentException ex) {
           log.error("Error occurred while closing the file.", ex.fillInStackTrace());
        }
    }

    private void populateRowDataSet(Object[][] rowDataSet, Long projectId, Map<String, Long> serverCloudVersionMapping) {
        AtomicInteger row = new AtomicInteger();
        serverCloudVersionMapping.forEach((key, value) -> {
            int column = 0;
            rowDataSet[row.get()][column] = projectId + "";
            ++column;
            rowDataSet[row.get()][column] = key + ""; //server version Id
            ++column;
            rowDataSet[row.get()][column] = value + ""; //cloud version Id
            row.incrementAndGet();
        });
    }

    public void generateCycleMappingReportExcel(Map<CycleDTO, ZfjCloudCycleBean> zephyrServerCloudCycleMappingMap, String projectId, String migrationFilePath) {
        try {
            List<List<String>> responseList = cycleDataToPrintInExcel(zephyrServerCloudCycleMappingMap, projectId);
            ExcelUtils excelUtils = new ExcelUtils();
            excelUtils.writeCycleDataToExcelFile(migrationFilePath, ApplicationConstants.MAPPING_FILE_NAME + projectId, responseList);
        }catch (Exception e){
            log.error("Error occurred while writing to the excel file.", e.fillInStackTrace());
        }
    }

    public List<List<String>> cycleDataToPrintInExcel(Map<CycleDTO, ZfjCloudCycleBean> zephyrServerCloudCycleMappingMap, String projectId) throws Exception {
        List<List<String>> recordToAdd = new ArrayList<>();
        recordToAdd.add(generateCycleHeader());
        List cycleMappingList;
        for (Map.Entry<CycleDTO,ZfjCloudCycleBean> entry : zephyrServerCloudCycleMappingMap.entrySet()) {
            cycleMappingList = new ArrayList<>();
            cycleMappingList.add(projectId);
            cycleMappingList.add(entry.getValue().getVersionId().toString());
            cycleMappingList.add(entry.getKey().getId().toString());
            cycleMappingList.add(entry.getValue().getId());
            recordToAdd.add(cycleMappingList);
        }
        return recordToAdd;
    }

    public static List<String> generateCycleHeader()  throws Exception {
        List<String> header1 = new ArrayList<String>();
        header1.add("Project Id");
        header1.add("Cloud Version Id");
        header1.add("server-cycle-id");
        header1.add("cloud-cycle-id");
        return header1;
    }
}