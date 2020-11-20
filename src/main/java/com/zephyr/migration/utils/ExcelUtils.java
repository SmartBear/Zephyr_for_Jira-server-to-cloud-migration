package com.zephyr.migration.utils;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileCopyUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/** This Utility use for Printing Data in Excel
 * Created by Himanshu.
 */
public class ExcelUtils {

    /**
     * Utility method to create excel and populate calculated value
     * @param fileName  excelFileName
     * @param sheetName
     * @param recordList  List of Records in this record first record should be header and after that Values
     * @throws Exception
     */
    public  void writeToExcelFileMethod(String migrationFilePath, String fileName, String sheetName,List<List<String>> recordList) throws Exception {
        int rownum = 0;
        int noOfRecords = (recordList.size()-3);
        String errorMessage = "Sorry. The file you are looking for does not exist";
        HSSFSheet firstSheet;
        HSSFWorkbook workbook;
        FileOutputStream fos = null;
        workbook = new HSSFWorkbook();
        HSSFCellStyle hsfstyle=workbook.createCellStyle();
        hsfstyle.setBorderBottom(BorderStyle.THICK);
        hsfstyle.setFillBackgroundColor((short)245);
        firstSheet = workbook.createSheet(sheetName);
        Row headerRow = firstSheet.createRow(rownum);
        headerRow.setHeightInPoints(40);
        File excelFile = FileUtils.createFile(migrationFilePath, fileName+".xls");
        //File excelFile = File.createTempFile(fileName,".xls");
        fos=new FileOutputStream(excelFile);
        try {

            for (int j = 0; j < recordList.size(); j++) {
                Row row = firstSheet.createRow(rownum);
                List<String> l2= recordList.get(j);

                for(int k=0; k<l2.size(); k++)
                {
                    Cell cell = row.createCell(k);
                    cell.setCellValue(l2.get(k));
                }
                rownum++;
            }
            workbook.write(fos);

            /*response.setContentType("application/vnd.ms-excel");
            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename="+fileName+".xls");
            response.setHeader(headerKey, headerValue);
            response.setContentLength((int)excelFile.length());
            InputStream inputStream = new BufferedInputStream(new FileInputStream(excelFile));
            FileCopyUtils.copy(inputStream, response.getOutputStream());*/
            excelFile.delete();
            fos.close();
            workbook.close();

            return;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

    }

    //create header for excel
    public static ArrayList<String> excelHeader(String[] header){
        ArrayList<String> headings=new ArrayList<>();
        for(String head:header){
            headings.add(head);
        }
        return  headings;
    }

}
