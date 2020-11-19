package com.zephyr.migration.utils;

import com.zephyr.migration.exception.NDataException;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public static void readFile(String nDataDir, String filename) throws IOException {
        //obtaining input bytes from a file
        FileInputStream fis=new FileInputStream(new File(nDataDir+"/"+filename));
        //creating workbook instance that refers to .xls file
        HSSFWorkbook wb=new HSSFWorkbook(fis);
        //creating a Sheet object to retrieve the object
        HSSFSheet sheet=wb.getSheetAt(0);
        //evaluating cell type
        FormulaEvaluator formulaEvaluator=wb.getCreationHelper().createFormulaEvaluator();
        for(Row row: sheet)     //iteration over row using for each loop
        {
            for(Cell cell: row)    //iteration over cell using for each loop
            {
                switch(formulaEvaluator.evaluateInCell(cell).getCellType())
                {
                    case Cell.CELL_TYPE_NUMERIC:   //field that represents numeric cell type
        //getting the value of the cell as a number
                        System.out.print(cell.getNumericCellValue()+ "\t\t");
                        break;
                    case Cell.CELL_TYPE_STRING:    //field that represents string cell type
        //getting the value of the cell as a string
                        System.out.print(cell.getStringCellValue()+ "\t\t");
                        break;
                }
            }
            System.out.println();
        }
    }
}
