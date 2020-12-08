package com.zephyr.migration.utils;

import com.atlassian.jira.rest.client.api.domain.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.zephyr.migration.dto.CycleDTO;
import com.zephyr.migration.dto.FolderDTO;
import com.zephyr.migration.model.ZfjCloudCycleBean;
import com.zephyr.migration.model.ZfjCloudFolderBean;
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
            excelUtils.writeToExcelFileMethod(migrationFilePath, ApplicationConstants.MAPPING_VERSION_FILE_NAME+projectId, ApplicationConstants.VERSION_MAPPING_SHEET_NAME, responseList);
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
        String excelFilePath = migrationFilePath+"/"+ApplicationConstants.MAPPING_VERSION_FILE_NAME+projectId+".xls";
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
        String excelFilePath = migrationFilePath+"/"+ApplicationConstants.MAPPING_VERSION_FILE_NAME+projectId+".xls";
        try {
            if(serverCloudVersionMapping.size() > 0) {
                FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
                HSSFWorkbook wb=new HSSFWorkbook(inputStream);
                HSSFSheet sheet=wb.getSheet(ApplicationConstants.VERSION_MAPPING_SHEET_NAME);

                Object[][] rowDataSet = new Object[serverCloudVersionMapping.size()][3];
                int rowCount = sheet.getLastRowNum();

                populateVersionRowDataSet(rowDataSet,projectId, serverCloudVersionMapping);

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

    public void updateCycleMappingFile(Long projectId, String projectName, String migrationFilePath, Map<CycleDTO, ZfjCloudCycleBean> serverCloudCycleMapping) {
        String excelFilePath = migrationFilePath+"/"+ApplicationConstants.MAPPING_CYCLE_FILE_NAME+projectId+".xls";
        try {
            if(serverCloudCycleMapping.size() > 0) {
                FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
                HSSFWorkbook wb=new HSSFWorkbook(inputStream);
                HSSFSheet sheet=wb.getSheet(ApplicationConstants.CYCLE_MAPPING_SHEET_NAME);

                Object[][] rowDataSet = new Object[serverCloudCycleMapping.size()][6];
                int rowCount = sheet.getLastRowNum();

                populateCycleRowDataSet(rowDataSet, projectId, projectName, serverCloudCycleMapping);

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

    public void updateFolderMappingFile(Long projectId, String migrationFilePath, Map<FolderDTO, ZfjCloudFolderBean> serverCloudFolderMapping) {
        String excelFilePath = migrationFilePath+"/"+ApplicationConstants.MAPPING_FOLDER_FILE_NAME+projectId+".xls";
        try {
            if(serverCloudFolderMapping.size() > 0) {
                FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
                HSSFWorkbook wb=new HSSFWorkbook(inputStream);
                HSSFSheet sheet=wb.getSheet(ApplicationConstants.FOLDER_MAPPING_SHEET_NAME);

                Object[][] rowDataSet = new Object[serverCloudFolderMapping.size()][5];
                int rowCount = sheet.getLastRowNum();

                populateFolderRowDataSet(rowDataSet, projectId, serverCloudFolderMapping);

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


    private void populateVersionRowDataSet(Object[][] rowDataSet, Long projectId, Map<String, Long> serverCloudVersionMapping) {
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

    private void populateCycleRowDataSet(Object[][] rowDataSet, Long projectId, String projectName, Map<CycleDTO, ZfjCloudCycleBean> serverCloudVersionMapping) {
        AtomicInteger row = new AtomicInteger();
        serverCloudVersionMapping.forEach((key, value) -> {
            int column = 0;
            rowDataSet[row.get()][column] = projectId + "";
            ++column;
            rowDataSet[row.get()][column] = projectName;
            ++column;
            rowDataSet[row.get()][column] = value.getVersionId() + "";
            ++column;
            rowDataSet[row.get()][column] = key.getId() + ""; //server cycle Id
            ++column;
            rowDataSet[row.get()][column] = value.getId() + ""; //cloud cycle Id
            ++column;
            rowDataSet[row.get()][column] = value.getName() + ""; //cycle name
            row.incrementAndGet();
        });
    }

    private void populateFolderRowDataSet(Object[][] rowDataSet, Long projectId, Map<FolderDTO, ZfjCloudFolderBean> serverCloudFolderMapping) {
        AtomicInteger row = new AtomicInteger();
        serverCloudFolderMapping.forEach((key, value) -> {
            int column = 0;
            rowDataSet[row.get()][column] = projectId + "";
            ++column;
            rowDataSet[row.get()][column] = value.getVersionId() + "";
            ++column;
            rowDataSet[row.get()][column] = value.getCycleId() + "";
            ++column;
            rowDataSet[row.get()][column] = key.getId() + ""; //server version Id
            ++column;
            rowDataSet[row.get()][column] = value.getId() + ""; //cloud version Id
            row.incrementAndGet();
        });
    }

    public void generateCycleMappingReportExcel(Map<CycleDTO, ZfjCloudCycleBean> zephyrServerCloudCycleMappingMap, String projectId, String projectName, String migrationFilePath) {
        try {
            List<List<String>> responseList = cycleDataToPrintInExcel(zephyrServerCloudCycleMappingMap, projectId, projectName);
            ExcelUtils excelUtils = new ExcelUtils();
            excelUtils.writeCycleDataToExcelFile(migrationFilePath, ApplicationConstants.MAPPING_CYCLE_FILE_NAME + projectId, responseList);
        }catch (Exception e){
            log.error("Error occurred while writing to the excel file.", e.fillInStackTrace());
        }
    }

    public void generateFolderMappingReportExcel(Map<FolderDTO, ZfjCloudFolderBean> zephyrServerCloudFolderMappingMap, String projectId, String migrationFilePath) {
        try {
            List<List<String>> responseList = folderDataToPrintInExcel(zephyrServerCloudFolderMappingMap, projectId);
            ExcelUtils excelUtils = new ExcelUtils();
            excelUtils.writeFolderDataToExcelFile(migrationFilePath, ApplicationConstants.MAPPING_FOLDER_FILE_NAME + projectId, responseList);
        }catch (Exception e){
            log.error("Error occurred while writing to the excel file.", e.fillInStackTrace());
        }
    }

    public List<List<String>> cycleDataToPrintInExcel(Map<CycleDTO, ZfjCloudCycleBean> zephyrServerCloudCycleMappingMap, String projectId, String projectName) throws Exception {
        List<List<String>> recordToAdd = new ArrayList<>();
        recordToAdd.add(generateCycleHeader());
        List cycleMappingList;
        for (Map.Entry<CycleDTO,ZfjCloudCycleBean> entry : zephyrServerCloudCycleMappingMap.entrySet()) {
            ZfjCloudCycleBean cloudCycleBean = entry.getValue();
            cycleMappingList = new ArrayList<>();
            cycleMappingList.add(projectId);
            cycleMappingList.add(projectName);
            cycleMappingList.add(cloudCycleBean.getVersionId()+"");
            cycleMappingList.add(entry.getKey().getId());
            cycleMappingList.add(cloudCycleBean.getId());
            cycleMappingList.add(cloudCycleBean.getName());
            recordToAdd.add(cycleMappingList);
        }
        return recordToAdd;
    }

    public List<List<String>> folderDataToPrintInExcel(Map<FolderDTO, ZfjCloudFolderBean> zephyrServerCloudFolderMappingMap, String projectId) throws Exception {
        List<List<String>> recordToAdd = new ArrayList<>();
        recordToAdd.add(generateFolderHeader());
        List folderMappingList;
        for (Map.Entry<FolderDTO, ZfjCloudFolderBean> entry : zephyrServerCloudFolderMappingMap.entrySet()) {
            folderMappingList = new ArrayList<>();
            folderMappingList.add(projectId);
            folderMappingList.add(entry.getValue().getVersionId().toString());
            folderMappingList.add(entry.getValue().getCycleId().toString());
            folderMappingList.add(entry.getKey().getId().toString());
            folderMappingList.add(entry.getValue().getId());
            recordToAdd.add(folderMappingList);
        }
        return recordToAdd;
    }

    public static List<String> generateCycleHeader()  throws Exception {
        List<String> excelHeader = new ArrayList<String>();
        excelHeader.add("Project Id");
        excelHeader.add("Project Name");
        excelHeader.add("Cloud Version Id");
        excelHeader.add("server-cycle-id");
        excelHeader.add("cloud-cycle-id");
        excelHeader.add("Cycle-Name");
        return excelHeader;
    }

    public static List<String> generateFolderHeader()  throws Exception {
        List<String> header1 = new ArrayList<String>();
        header1.add("Project Id");
        header1.add("Cloud Version Id");
        header1.add("cloud-cycle-id");
        header1.add("server-folder-id");
        header1.add("cloud-folder-id");
        return header1;
    }
}