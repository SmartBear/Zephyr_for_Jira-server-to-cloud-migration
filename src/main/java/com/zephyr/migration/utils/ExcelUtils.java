package com.zephyr.migration.utils;

import com.zephyr.migration.service.impl.VersionServiceImpl;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileCopyUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
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
        int noOfRecords = (recordList.size()-3);
        String errorMessage = "Sorry. The file you are looking for does not exist";
        HSSFSheet firstSheet;
        HSSFWorkbook workbook;
        FileOutputStream fos;
        workbook = new HSSFWorkbook();
        HSSFCellStyle hsfstyle = workbook.createCellStyle();
        hsfstyle.setBorderBottom(BorderStyle.THICK);
        hsfstyle.setFillBackgroundColor((short)245);
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
           //excelFile.delete();

        } catch (Exception e) {
            log.error("Error occurred while writing to the excel file", e.fillInStackTrace());
        } finally {
            fos.close();
            workbook.close();
        }

    }

    //create header for excel
    public static ArrayList<String> excelHeader(String[] header){
        return new ArrayList<>(Arrays.asList(header));
    }

}
