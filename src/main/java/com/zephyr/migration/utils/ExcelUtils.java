package com.zephyr.migration.utils;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

/** This Utility use for Printing Data in Excel
 * Created by Himanshu.
 */
public class ExcelUtils {

    private static final Logger log = LoggerFactory.getLogger(ExcelUtils.class);

    /**
     * Utility method to create excel and populate calculated value
     * @param fileName  excelFileName
     * @param sheetName
     * @param recordList  List of Records in this record first record should be header and after that Values
     * @throws Exception
     */
    public void writeToExcelFileMethod(String migrationFilePath, String fileName, String sheetName,List<List<String>> recordList) throws Exception {
        int rowNum = 0;
        HSSFSheet firstSheet;
        HSSFWorkbook workbook;
        FileOutputStream fos;
        workbook = new HSSFWorkbook();
        HSSFCellStyle hsfStyle = workbook.createCellStyle();
        hsfStyle.setBorderBottom(BorderStyle.THICK);
        hsfStyle.setFillBackgroundColor((short)245);
        firstSheet = workbook.createSheet(sheetName);
        Row headerRow = firstSheet.createRow(rowNum);
        headerRow.setHeightInPoints(40);
        File excelFile = FileUtils.createFile(migrationFilePath, fileName+".xls");
        fos=new FileOutputStream(excelFile);
        try {

            for (List<String> record : recordList) {
                Row row = firstSheet.createRow(rowNum);
                for (int k = 0; k < record.size(); k++) {
                    Cell cell = row.createCell(k);
                    cell.setCellValue(record.get(k));
                }
                rowNum++;
            }
            workbook.write(fos);

            /*response.setContentType("application/vnd.ms-excel");
            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename="+fileName+".xls");
            response.setHeader(headerKey, headerValue);
            response.setContentLength((int)excelFile.length());
            InputStream inputStream = new BufferedInputStream(new FileInputStream(excelFile));
            FileCopyUtils.copy(inputStream, response.getOutputStream());*/

        } catch (Exception e) {
            log.error("Error occurred while writing to the excel file", e.fillInStackTrace());
        } finally {
            fos.close();
            workbook.close();
        }

    }

    public void writeCycleDataToExcelFile(String migrationFilePath, String fileName, List<List<String>> recordList) throws Exception {
        int rowNum = 0;
        //FileInputStream fis=new FileInputStream(new File(migrationFilePath+"/"+ fileName +".xls"));
        HSSFSheet secondSheet;
        HSSFWorkbook workbook;
        FileOutputStream fos;
        workbook = new HSSFWorkbook();
        HSSFCellStyle hsfStyle = workbook.createCellStyle();
        hsfStyle.setBorderBottom(BorderStyle.THICK);
        hsfStyle.setFillBackgroundColor((short)245);
        secondSheet = workbook.createSheet(ApplicationConstants.CYCLE_MAPPING_SHEET_NAME);
        Row headerRow = secondSheet.createRow(rowNum);
        headerRow.setHeightInPoints(40);
        fos=new FileOutputStream(migrationFilePath+"/"+ fileName +".xls");

        try {

            for (List<String> record : recordList) {
                Row row = secondSheet.createRow(rowNum);
                for (int k = 0; k < record.size(); k++) {
                    Cell cell = row.createCell(k);
                    cell.setCellValue(record.get(k));
                }
                rowNum++;
            }

            workbook.write(fos);

        } catch (Exception e) {
            log.error("Error occurred while writing to the excel file", e.fillInStackTrace());
        } finally {
            fos.close();
            workbook.close();
        }

    }

    public void writeExecutionDataToExcelFile(String migrationFilePath, String fileName, List<List<String>> recordList) throws Exception {
        int rowNum = 0;
        XSSFSheet xssfSheet;
        XSSFWorkbook workbook;
        FileOutputStream fos;
        workbook = new XSSFWorkbook();
        XSSFCellStyle xssfCellStyle = workbook.createCellStyle();
        xssfCellStyle.setBorderBottom(BorderStyle.THICK);
        xssfCellStyle.setFillBackgroundColor((short)245);
        xssfSheet = workbook.createSheet(ApplicationConstants.EXECUTION_MAPPING_SHEET_NAME);
        Row headerRow = xssfSheet.createRow(rowNum);
        headerRow.setHeightInPoints(40);
        fos=new FileOutputStream(migrationFilePath+"/"+ fileName + ApplicationConstants.XLSX);
        try {

            for (List<String> record : recordList) {
                Row row = xssfSheet.createRow(rowNum);
                for (int k = 0; k < record.size(); k++) {
                    Cell cell = row.createCell(k);
                    cell.setCellValue(record.get(k));
                }
                rowNum++;
            }

            workbook.write(fos);

        } catch (Exception e) {
            log.error("Error occurred while writing to the excel file", e.fillInStackTrace());
        } finally {
            //fis.close();
            fos.close();
            workbook.close();
        }

    }

    public void writeTestStepDataToExcelFile(String migrationFilePath, String fileName, List<List<String>> recordList) throws Exception {
        int rowNum = 0;
        HSSFSheet secondSheet;
        XSSFSheet xssfSheet;
       // HSSFWorkbook workbook;
        XSSFWorkbook xssfWorkbook;
        FileOutputStream fos;
        //workbook = new HSSFWorkbook();
        xssfWorkbook = new XSSFWorkbook();

        XSSFCellStyle xssfCellStyle = xssfWorkbook.createCellStyle();
        xssfCellStyle.setBorderBottom(BorderStyle.THICK);
        xssfCellStyle.setFillBackgroundColor((short)245);

        xssfSheet = xssfWorkbook.createSheet(ApplicationConstants.TEST_STEP_MAPPING_SHEET_NAME);

        Row headerRow = xssfSheet.createRow(rowNum);
        headerRow.setHeightInPoints(40);
        //fos=new FileOutputStream(migrationFilePath+"/"+ fileName +".xls");
        fos=new FileOutputStream(migrationFilePath+"/"+ fileName + ApplicationConstants.XLSX);
        try {

            for (List<String> record : recordList) {
                Row row = xssfSheet.createRow(rowNum);
                for (int k = 0; k < record.size(); k++) {
                    Cell cell = row.createCell(k);
                    cell.setCellValue(record.get(k));
                }
                rowNum++;
            }
            xssfWorkbook.write(fos);

        } catch (Exception e) {
            log.error("Error occurred while writing to the excel file", e.fillInStackTrace());
        } finally {
            //fis.close();
            fos.close();
            xssfWorkbook.close();
        }

    }

    public void writeExecutionAttachmentDataToExcelFile(String migrationFilePath, String fileName, List<List<String>> recordList) throws Exception {
        int rowNum = 0;
        HSSFSheet secondSheet;
        HSSFWorkbook workbook;
        FileOutputStream fos;
        workbook = new HSSFWorkbook();
        HSSFCellStyle hsfStyle = workbook.createCellStyle();
        hsfStyle.setBorderBottom(BorderStyle.THICK);
        hsfStyle.setFillBackgroundColor((short)245);
        secondSheet = workbook.createSheet(ApplicationConstants.EXECUTION_ATTACHMENT_MAPPING_SHEET_NAME);
        Row headerRow = secondSheet.createRow(rowNum);
        headerRow.setHeightInPoints(40);
        fos=new FileOutputStream(migrationFilePath+"/"+ fileName +".xls");
        try {

            for (List<String> record : recordList) {
                Row row = secondSheet.createRow(rowNum);
                for (int k = 0; k < record.size(); k++) {
                    Cell cell = row.createCell(k);
                    cell.setCellValue(record.get(k));
                }
                rowNum++;
            }

            workbook.write(fos);

        } catch (Exception e) {
            log.error("Error occurred while writing to the excel file", e.fillInStackTrace());
        } finally {
            //fis.close();
            fos.close();
            workbook.close();
        }

    }

    public void writeStepResultAttachmentDataToExcelFile(String migrationFilePath, String fileName, List<List<String>> recordList) throws Exception {
        int rowNum = 0;
        HSSFSheet secondSheet;
        HSSFWorkbook workbook;
        FileOutputStream fos;
        workbook = new HSSFWorkbook();
        HSSFCellStyle hsfStyle = workbook.createCellStyle();
        hsfStyle.setBorderBottom(BorderStyle.THICK);
        hsfStyle.setFillBackgroundColor((short)245);
        secondSheet = workbook.createSheet(ApplicationConstants.STEP_RESULT_ATTACHMENT_MAPPING_SHEET_NAME);
        Row headerRow = secondSheet.createRow(rowNum);
        headerRow.setHeightInPoints(40);
        fos=new FileOutputStream(migrationFilePath+"/"+ fileName +".xls");
        try {

            for (List<String> record : recordList) {
                Row row = secondSheet.createRow(rowNum);
                for (int k = 0; k < record.size(); k++) {
                    Cell cell = row.createCell(k);
                    cell.setCellValue(record.get(k));
                }
                rowNum++;
            }

            workbook.write(fos);

        } catch (Exception e) {
            log.error("Error occurred while writing to the excel file", e.fillInStackTrace());
        } finally {
            //fis.close();
            fos.close();
            workbook.close();
        }

    }

    public void writeFolderDataToExcelFile(String migrationFilePath, String fileName, List<List<String>> recordList) throws Exception {
        int rowNum = 0;
        HSSFSheet secondSheet;
        HSSFWorkbook workbook;
        FileOutputStream fos;
        workbook = new HSSFWorkbook();
        HSSFCellStyle hsfStyle = workbook.createCellStyle();
        hsfStyle.setBorderBottom(BorderStyle.THICK);
        hsfStyle.setFillBackgroundColor((short)245);
        secondSheet = workbook.createSheet(ApplicationConstants.FOLDER_MAPPING_SHEET_NAME);
        Row headerRow = secondSheet.createRow(rowNum);
        headerRow.setHeightInPoints(40);
        fos=new FileOutputStream(migrationFilePath+"/"+ fileName +".xls");
        try {

            for (List<String> record : recordList) {
                Row row = secondSheet.createRow(rowNum);
                for (int k = 0; k < record.size(); k++) {
                    Cell cell = row.createCell(k);
                    cell.setCellValue(record.get(k));
                }
                rowNum++;
            }

            workbook.write(fos);

        } catch (Exception e) {
            log.error("Error occurred while writing to the excel file", e.fillInStackTrace());
        } finally {
            //fis.close();
            fos.close();
            workbook.close();
        }

    }

    public void writeTestStepAttachmentDataToExcelFile(String migrationFilePath, String fileName, List<List<String>> dataList) throws IOException {
        int rowNum = 0;
        HSSFSheet secondSheet;
        HSSFWorkbook workbook;
        FileOutputStream fos;
        workbook = new HSSFWorkbook();
        HSSFCellStyle hsfStyle = workbook.createCellStyle();
        hsfStyle.setBorderBottom(BorderStyle.THICK);
        hsfStyle.setFillBackgroundColor((short)245);
        secondSheet = workbook.createSheet(ApplicationConstants.TEST_STEP_ATTACHMENT_MAPPING_SHEET_NAME);
        Row headerRow = secondSheet.createRow(rowNum);
        headerRow.setHeightInPoints(40);
        fos=new FileOutputStream(migrationFilePath+"/"+ fileName +".xls");
        try {
            for (List<String> record : dataList) {
                Row row = secondSheet.createRow(rowNum);
                for (int k = 0; k < record.size(); k++) {
                    Cell cell = row.createCell(k);
                    cell.setCellValue(record.get(k));
                }
                rowNum++;
            }
            workbook.write(fos);
        } catch (Exception e) {
            log.error("Error occurred while writing to the excel file", e.fillInStackTrace());
        } finally {
            //fis.close();
            fos.close();
            workbook.close();
        }
    }

    public void writeStepResultsMigrationDataToExcelFile(String migrationFilePath, String fileName, List<List<String>> responseList) throws IOException {
        int rowNum = 0;
        XSSFSheet xssfSheet;
        XSSFWorkbook xssfWorkbook;
        FileOutputStream fos;
        xssfWorkbook = new XSSFWorkbook();

        XSSFCellStyle xssfCellStyle = xssfWorkbook.createCellStyle();
        xssfCellStyle.setBorderBottom(BorderStyle.THICK);
        xssfCellStyle.setFillBackgroundColor((short)245);

        xssfSheet = xssfWorkbook.createSheet(ApplicationConstants.STEP_RESULTS_MAPPING_SHEET_NAME);

        Row headerRow = xssfSheet.createRow(rowNum);
        headerRow.setHeightInPoints(40);
        fos=new FileOutputStream(migrationFilePath+"/"+ fileName + ApplicationConstants.XLSX);
        try {

            for (List<String> record : responseList) {
                Row row = xssfSheet.createRow(rowNum);
                for (int k = 0; k < record.size(); k++) {
                    Cell cell = row.createCell(k);
                    cell.setCellValue(record.get(k));
                }
                rowNum++;
            }
            xssfWorkbook.write(fos);

        } catch (Exception e) {
            log.error("Error occurred while writing to the excel file", e.fillInStackTrace());
        } finally {
            //fis.close();
            fos.close();
            xssfWorkbook.close();
        }
    }

    public void writeExecutionLevelDefectMigrationDataToExcelFile(String migrationFilePath, String fileName, List<List<String>> responseList) throws IOException {
            int rowNum = 0;
            XSSFSheet xssfSheet;
            XSSFWorkbook xssfWorkbook;
            FileOutputStream fos;
            xssfWorkbook = new XSSFWorkbook();

            XSSFCellStyle xssfCellStyle = xssfWorkbook.createCellStyle();
            xssfCellStyle.setBorderBottom(BorderStyle.THICK);
            xssfCellStyle.setFillBackgroundColor((short)245);

            xssfSheet = xssfWorkbook.createSheet(ApplicationConstants.EXECUTION_LEVEL_DEFECT_SHEET_NAME);

            Row headerRow = xssfSheet.createRow(rowNum);
            headerRow.setHeightInPoints(40);
            fos=new FileOutputStream(migrationFilePath+"/"+ fileName + ApplicationConstants.XLSX);
            try {

                for (List<String> record : responseList) {
                    Row row = xssfSheet.createRow(rowNum);
                    for (int k = 0; k < record.size(); k++) {
                        Cell cell = row.createCell(k);
                        cell.setCellValue(record.get(k));
                    }
                    rowNum++;
                }
                xssfWorkbook.write(fos);

            } catch (Exception e) {
                log.error("Error occurred while writing to the excel file", e.fillInStackTrace());
            } finally {
                fos.close();
                xssfWorkbook.close();
            }
    }

    public void writeStepResultsDefectMigrationDataToExcelFile(String migrationFilePath, String fileName, List<List<String>> responseList) throws IOException {
        int rowNum = 0;
        XSSFSheet xssfSheet;
        XSSFWorkbook xssfWorkbook;
        FileOutputStream fos;
        xssfWorkbook = new XSSFWorkbook();

        XSSFCellStyle xssfCellStyle = xssfWorkbook.createCellStyle();
        xssfCellStyle.setBorderBottom(BorderStyle.THICK);
        xssfCellStyle.setFillBackgroundColor((short)245);

        xssfSheet = xssfWorkbook.createSheet(ApplicationConstants.STEP_RESULTS_DEFECT_SHEET_NAME);

        Row headerRow = xssfSheet.createRow(rowNum);
        headerRow.setHeightInPoints(40);
        fos=new FileOutputStream(migrationFilePath+"/"+ fileName + ApplicationConstants.XLSX);
        try {

            for (List<String> record : responseList) {
                Row row = xssfSheet.createRow(rowNum);
                for (int k = 0; k < record.size(); k++) {
                    Cell cell = row.createCell(k);
                    cell.setCellValue(record.get(k));
                }
                rowNum++;
            }
            xssfWorkbook.write(fos);

        } catch (Exception e) {
            log.error("Error occurred while writing to the excel file", e.fillInStackTrace());
        } finally {
            fos.close();
            xssfWorkbook.close();
        }
    }
}
