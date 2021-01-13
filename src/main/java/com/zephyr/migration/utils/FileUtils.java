package com.zephyr.migration.utils;

import com.zephyr.migration.dto.ExecutionDTO;
import com.zephyr.migration.exception.NDataException;
import com.zephyr.migration.model.SearchRequest;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Himanshu Singhal on 18-11-2020.
 */
@Component
public class FileUtils {

    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    private static final String SERVER_VERSION_ID_COLUMN_NAME = "Server Version Id";
    private static final String CLOUD_VERSION_ID_COLUMN_NAME = "Cloud Version Id";
    private static final String SERVER_CYCLE_ID_COLUMN_NAME = "server-cycle-id";
    private static final String CLOUD_CYCLE_ID_COLUMN_NAME = "cloud-cycle-id";
    private static final String SERVER_FOLDER_ID_COLUMN_NAME = "server-folder-id";
    private static final String CLOUD_FOLDER_ID_COLUMN_NAME = "cloud-folder-id";
    private static final String PROJECT_ID_COLUMN_NAME = "Project Id";
    private static final String CYCLE_NAME_COLUMN_NAME = "Cycle-Name";
    private static final String ISSUE_ID_COLUMN_NAME = "Issue Id";

    public static File createFile(String nDataDir, String filename) {
        //  nDataDir = doPreProcessing(nDataDir);
        try {
            Files.createDirectories(Paths.get(nDataDir));
            // filename = filename.replaceAll("[^a-zA-Z0-9.-]", "_");
            Path path = Paths.get(nDataDir, filename);

            if(Files.exists(path)){
                path = Paths.get(nDataDir, addTimeStampToFilename(filename));
            }
            File file = path.toFile();
            file.createNewFile();
            return file;
        } catch (IOException e) {
            throw  new NDataException(e.getMessage(),e.getCause());
        }
    }

    public static String addTimeStampToFilename(String filename){
        if (filename.contains(".")) {
            filename = filename.substring(0, filename.lastIndexOf(".")) + "_" + System.currentTimeMillis()
                    + filename.substring(filename.lastIndexOf("."), filename.length());
        } else {
            filename = filename + "_" + System.currentTimeMillis();
        }
        return filename;
    }

    public static List<String> readFile(String nDataDir, String filename) throws IOException {
        //obtaining input bytes from a file
        FileInputStream fis=new FileInputStream(new File(nDataDir+"/"+filename));
        //creating workbook instance that refers to .xls file
        HSSFWorkbook wb=new HSSFWorkbook(fis);
        //creating a Sheet object to retrieve the object
        HSSFSheet sheet=wb.getSheet(ApplicationConstants.VERSION_MAPPING_SHEET_NAME);

        int column_index_1 = 0;
        Row row = sheet.getRow(0);
        for (Cell cell : row) {
            // Column header names.
            if (SERVER_VERSION_ID_COLUMN_NAME.equalsIgnoreCase(cell.getStringCellValue())) {
                column_index_1 = cell.getColumnIndex();
                break;
            }
        }
        List<String> cloudVersionList = new ArrayList<>();
        for (Row r : sheet) {
            if (r.getRowNum()==0) continue;//headers
            Cell c_1 = r.getCell(column_index_1);
            if (c_1 != null && c_1.getCellType() != Cell.CELL_TYPE_BLANK) {
                cloudVersionList.add(c_1.getStringCellValue());
            }
        }
        return cloudVersionList;
    }

    public static Boolean readCycleMappingFile(String nDataDir, String filename, String cloudVersionId, String serverCycleId) throws IOException {

        //obtaining input bytes from a file
        try (FileInputStream fis=new FileInputStream(nDataDir+"/"+filename)) {
            HSSFWorkbook wb=new HSSFWorkbook(fis);
            //creating a Sheet object to retrieve the object
            HSSFSheet sheet=wb.getSheet(ApplicationConstants.CYCLE_MAPPING_SHEET_NAME);

            int column_index_1 = 0;
            int column_index_2 = 0;
            Row row = sheet.getRow(0);
            for (Cell cell : row) {
                // Column header names.
                if (CLOUD_VERSION_ID_COLUMN_NAME.equalsIgnoreCase(cell.getStringCellValue())) {
                    column_index_1 = cell.getColumnIndex();
                }else if (SERVER_CYCLE_ID_COLUMN_NAME.equalsIgnoreCase(cell.getStringCellValue())) {
                    column_index_2 = cell.getColumnIndex();
                }
            }
            for (Row r : sheet) {
                if (r.getRowNum()==0) continue;//headers
                Cell c_1 = r.getCell(column_index_1);
                Cell c_2 = r.getCell(column_index_2);
                if (c_1 != null && c_1.getCellType() != Cell.CELL_TYPE_BLANK && c_2 != null && c_2.getCellType() != Cell.CELL_TYPE_BLANK) {
                    if (cloudVersionId.equalsIgnoreCase(c_1.getStringCellValue()) && serverCycleId.equalsIgnoreCase(c_2.getStringCellValue()))
                        return true;
                }
            }
            wb.close();
        } catch (Exception ex) {
            log.error("Error occurred while closing the file stream"+ ex.fillInStackTrace());
        }

        return false;
    }

    public static Boolean readFolderMappingFile(String nDataDir, String filename, String cloudCycleId, String serverFolderId)  {
        try (FileInputStream fis=new FileInputStream(nDataDir+"/"+filename)) {
            //creating workbook instance that refers to .xls file
            HSSFWorkbook wb=new HSSFWorkbook(fis);
            //creating a Sheet object to retrieve the object
            HSSFSheet sheet=wb.getSheet(ApplicationConstants.FOLDER_MAPPING_SHEET_NAME);

            int column_index_1 = 0;
            int column_index_2 = 0;
            Row row = sheet.getRow(0);
            for (Cell cell : row) {
                // Column header names.
                if (CLOUD_CYCLE_ID_COLUMN_NAME.equalsIgnoreCase(cell.getStringCellValue())) {
                    column_index_1 = cell.getColumnIndex();
                }else if (SERVER_FOLDER_ID_COLUMN_NAME.equalsIgnoreCase(cell.getStringCellValue())) {
                    column_index_2 = cell.getColumnIndex();
                }
            }
            for (Row r : sheet) {
                if (r.getRowNum()==0) continue;//headers
                Cell c_1 = r.getCell(column_index_1);
                Cell c_2 = r.getCell(column_index_2);
                if (c_1 != null && c_1.getCellType() != Cell.CELL_TYPE_BLANK && c_2 != null && c_2.getCellType() != Cell.CELL_TYPE_BLANK) {
                    if (cloudCycleId.equalsIgnoreCase(c_1.getStringCellValue()) && serverFolderId.equalsIgnoreCase(c_2.getStringCellValue()))
                        return true;
                }
            }

        } catch(IOException e) {
            log.error("Error occurred while closing the file stream"+ e.fillInStackTrace());
        }
        return false;
    }

    public static void readExecutionMappingFile(String nDataDir, String filename, String cloudCycleId, String cloudFolderId, List<ExecutionDTO> executionList, String executionLevel)  {
        try (FileInputStream fis=new FileInputStream(nDataDir+"/"+filename)) {
            //creating workbook instance that refers to .xls file
            HSSFWorkbook wb=new HSSFWorkbook(fis);
            //creating a Sheet object to retrieve the object
            HSSFSheet sheet=wb.getSheet(ApplicationConstants.EXECUTION_MAPPING_SHEET_NAME);

            int column_index_1 = 0;
            int column_index_2 = 0;
            Row row = sheet.getRow(0);
            if (ApplicationConstants.CYCLE_LEVEL_EXECUTION.equalsIgnoreCase(executionLevel)) {
                for (Cell cell : row) {
                    // Column header names.
                    if (CLOUD_CYCLE_ID_COLUMN_NAME.equalsIgnoreCase(cell.getStringCellValue())) {
                        column_index_1 = cell.getColumnIndex();
                    }else if (ISSUE_ID_COLUMN_NAME.equalsIgnoreCase(cell.getStringCellValue())) {
                        column_index_2 = cell.getColumnIndex();
                    }
                }
                for (Row r : sheet) {
                    if (executionList.isEmpty()) {
                        break;
                    }
                    if (r.getRowNum()==0) continue;//headers
                    Cell c_1 = r.getCell(column_index_1);
                    Cell c_2 = r.getCell(column_index_2);
                    if (c_1 != null && c_1.getCellType() != Cell.CELL_TYPE_BLANK && c_2 != null && c_2.getCellType() != Cell.CELL_TYPE_BLANK) {
                        if (cloudCycleId.equalsIgnoreCase(c_1.getStringCellValue())) {
                            for (ExecutionDTO execution : executionList) {
                                String issueId = execution.getIssueId().toString();
                                if (issueId.equalsIgnoreCase(c_2.getStringCellValue())) {
                                    executionList.remove(execution);
                                    break;
                                }
                            }
                        }
                    }
                }
            }else if (ApplicationConstants.FOLDER_LEVEL_EXECUTION.equalsIgnoreCase(executionLevel)) {
                for (Cell cell : row) {
                    // Column header names.
                    if (CLOUD_FOLDER_ID_COLUMN_NAME.equalsIgnoreCase(cell.getStringCellValue())) {
                        column_index_1 = cell.getColumnIndex();
                    }else if (ISSUE_ID_COLUMN_NAME.equalsIgnoreCase(cell.getStringCellValue())) {
                        column_index_2 = cell.getColumnIndex();
                    }
                }
                for (Row r : sheet) {
                    if (executionList.isEmpty()) {
                        break;
                    }
                    if (r.getRowNum()==0) continue;//headers
                    Cell c_1 = r.getCell(column_index_1);
                    Cell c_2 = r.getCell(column_index_2);
                    if (c_1 != null && c_1.getCellType() != Cell.CELL_TYPE_BLANK && c_2 != null && c_2.getCellType() != Cell.CELL_TYPE_BLANK) {
                        if (cloudFolderId.equalsIgnoreCase(c_1.getStringCellValue())) {
                            for (ExecutionDTO execution : executionList) {
                                String issueId = execution.getIssueId().toString();
                                if (issueId.equalsIgnoreCase(c_2.getStringCellValue())) {
                                    executionList.remove(execution);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch(IOException e) {
            log.error("Error occurred while closing the file stream"+ e.fillInStackTrace());
        }
    }

    public static Map<String, String> readVersionMappingFile(String directory, String filename) throws IOException {
        //obtaining input bytes from a file
        HSSFWorkbook wb;
        try (FileInputStream fis = new FileInputStream(directory + "/" + filename)) {
            //creating workbook instance that refers to .xls file
            wb = new HSSFWorkbook(fis);
        }
        //creating a Sheet object to retrieve the object
        HSSFSheet sheet=wb.getSheet(ApplicationConstants.VERSION_MAPPING_SHEET_NAME);

        int serverIdColIndex = 0, cloudIdColIndex=0;
        Row row = sheet.getRow(0);
        for (Cell cell : row) {
            // Column header names.
            if (SERVER_VERSION_ID_COLUMN_NAME.equalsIgnoreCase(cell.getStringCellValue())) {
                serverIdColIndex = cell.getColumnIndex();
            }else if(CLOUD_VERSION_ID_COLUMN_NAME.equalsIgnoreCase(cell.getStringCellValue())) {
                cloudIdColIndex = cell.getColumnIndex();
            }
        }
        Map<String,String> serverCloudIdsMapping = new HashMap<>();
        for (Row r : sheet) {
            if (r.getRowNum()==0) continue;//headers

            Cell serverIdCellVal = r.getCell(serverIdColIndex);
            Cell cloudIdCellVal = r.getCell(cloudIdColIndex);
            if (Objects.nonNull(cloudIdCellVal) && Objects.nonNull(serverIdCellVal)) {
                serverCloudIdsMapping.put(serverIdCellVal.getStringCellValue(), cloudIdCellVal.getStringCellValue());
            }
        }
        return serverCloudIdsMapping;
    }

    public static Map<String, SearchRequest> readCycleMappingFile(String directory, String filename) throws IOException {
        //obtaining input bytes from a file
        HSSFSheet sheet;
        try (FileInputStream fis = new FileInputStream(directory + "/" + filename)) {
            //creating workbook instance that refers to .xls file
            try (HSSFWorkbook wb = new HSSFWorkbook(fis)) {
                //creating a Sheet object to retrieve the object
                sheet = wb.getSheet(ApplicationConstants.CYCLE_MAPPING_SHEET_NAME);
            }
        }

        int serverIdColIndex = 0, cloudIdColIndex=0, serverVersionColIndex =0, projectIdIndex =0, cloudVersionIdColIndex=0,
            cycleNameIndex=0;
        Row row = sheet.getRow(0);
        for (Cell cell : row) {
            // Column header names.
            if (SERVER_CYCLE_ID_COLUMN_NAME.equalsIgnoreCase(cell.getStringCellValue())) {
                serverIdColIndex = cell.getColumnIndex();
            }else if(CLOUD_CYCLE_ID_COLUMN_NAME.equalsIgnoreCase(cell.getStringCellValue())) {
                cloudIdColIndex = cell.getColumnIndex();
            }else if(SERVER_VERSION_ID_COLUMN_NAME.equalsIgnoreCase(cell.getStringCellValue())) {
                serverVersionColIndex = cell.getColumnIndex();
            }else if(CLOUD_VERSION_ID_COLUMN_NAME.equalsIgnoreCase(cell.getStringCellValue())) {
                cloudVersionIdColIndex = cell.getColumnIndex();
            }else if(PROJECT_ID_COLUMN_NAME.equalsIgnoreCase(cell.getStringCellValue())) {
                projectIdIndex = cell.getColumnIndex();
            }else if(CYCLE_NAME_COLUMN_NAME.equalsIgnoreCase(cell.getStringCellValue())) {
                cycleNameIndex = cell.getColumnIndex();
            }
        }
        Map<String, SearchRequest> serverCloudIdsMapping = new HashMap<>();
        for (Row r : sheet) {
            if (r.getRowNum()==0) continue;//headers

            Cell serverIdCellVal = r.getCell(serverIdColIndex);
            Cell cloudIdCellVal = r.getCell(cloudIdColIndex);
            Cell serverVersionCellVal = r.getCell(serverVersionColIndex);
            Cell projectIdCellVal = r.getCell(projectIdIndex);
            Cell cloudVersionIdCellVal = r.getCell(cloudVersionIdColIndex);
            Cell cycleNameCellVal = r.getCell(cycleNameIndex);

            if (Objects.nonNull(cloudIdCellVal) && Objects.nonNull(serverIdCellVal)) {
                SearchRequest searchRequest = new SearchRequest();
                searchRequest.setProjectId(projectIdCellVal.getStringCellValue());
                searchRequest.setVersionId(serverVersionCellVal.getStringCellValue());
                searchRequest.setServerCycleId(serverIdCellVal.getStringCellValue());
                searchRequest.setCloudCycleId(cloudIdCellVal.getStringCellValue());
                searchRequest.setCloudVersionId(cloudVersionIdCellVal.getStringCellValue());
                searchRequest.setCycleName(cycleNameCellVal.getStringCellValue());
                serverCloudIdsMapping.put(serverIdCellVal.getStringCellValue(), searchRequest);
            }
        }
        return serverCloudIdsMapping;
    }

    public static Map<String,String> getServerCloudFolderMapping(String migrationFilePath, String fileName, String cloudCycleId, String serverCycleId) {
        try (FileInputStream fis=new FileInputStream(migrationFilePath+"/"+fileName)) {
            //creating workbook instance that refers to .xls file
            HSSFWorkbook wb=new HSSFWorkbook(fis);
            //creating a Sheet object to retrieve the object
            HSSFSheet sheet=wb.getSheet(ApplicationConstants.FOLDER_MAPPING_SHEET_NAME);

            int cloudCycleIdIdx = 0, serverFolderIdIdx = 0 , cloudFolderIdIdx = 0;

            Row row = sheet.getRow(0);
            for (Cell cell : row) {
                // Column header names.
                if (CLOUD_CYCLE_ID_COLUMN_NAME.equalsIgnoreCase(cell.getStringCellValue())) {
                    cloudCycleIdIdx = cell.getColumnIndex();
                }else if (SERVER_FOLDER_ID_COLUMN_NAME.equalsIgnoreCase(cell.getStringCellValue())) {
                    serverFolderIdIdx = cell.getColumnIndex();
                }else if(CLOUD_FOLDER_ID_COLUMN_NAME.equalsIgnoreCase(cell.getStringCellValue())) {
                    cloudFolderIdIdx = cell.getColumnIndex();
                }
            }

            Map<String,String> serverCloudFolderMapping = new HashMap<>();
            String folderId, cloudFolderId;
            for (Row r : sheet) {
                if (r.getRowNum()==0) continue;//headers
                Cell c_1 = r.getCell(cloudCycleIdIdx);
                Cell c_2 = r.getCell(serverFolderIdIdx);
                Cell c_3 = r.getCell(cloudFolderIdIdx);

                if (c_1 != null && c_1.getCellType() != Cell.CELL_TYPE_BLANK && c_2 != null && c_2.getCellType() != Cell.CELL_TYPE_BLANK) {
                    if (cloudCycleId.equalsIgnoreCase(c_1.getStringCellValue())) {
                        folderId = c_2.getStringCellValue();
                        cloudFolderId = c_3.getStringCellValue();
                        serverCloudFolderMapping.put(folderId, cloudFolderId);
                    }
                }
            }
            return serverCloudFolderMapping;
        } catch(IOException e) {
            log.error("Error occurred while closing the file stream"+ e.fillInStackTrace());
            return null;
        }
    }
}
