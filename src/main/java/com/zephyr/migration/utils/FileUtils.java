package com.zephyr.migration.utils;

import com.zephyr.migration.exception.NDataException;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Himanshu Singhal on 18-11-2020.
 */
public class FileUtils {

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
            if ("Server Version Id".equalsIgnoreCase(cell.getStringCellValue())) {
                column_index_1 = cell.getColumnIndex();
                break;
            }
        }
        List<String> cloudVersionList = new ArrayList<>();
        for (Row r : sheet) {
            if (r.getRowNum()==0) continue;//hearders
            Cell c_1 = r.getCell(column_index_1);
            if (c_1 != null && c_1.getCellType() != Cell.CELL_TYPE_BLANK) {
                cloudVersionList.add(c_1.getStringCellValue());
            }
        }
        return cloudVersionList;
    }
}