package com.nure.restaurant.dataWorkers;

import javafx.scene.control.Alert;
import javafx.stage.StageStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelWorker {
    public static void generateReport(List<List<String>> data, String sheetName) {

        Workbook workbook = new XSSFWorkbook();

        String filePath = "Report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss")) + ".xlsx";

        Sheet sheet = workbook.createSheet(sheetName);

        int rowNum = 0;
        for (List<String> rowData : data) {
            Row row = sheet.createRow(rowNum++);
            int colNum = 0;
            for (String cellData : rowData) {
                Cell cell = row.createCell(colNum++);
                if (!cellData.equals("") && cellData.charAt(0) == '+') {
                    cell.setCellValue(cellData);
                } else {
                    try {
                        double numericValue = Double.parseDouble(cellData);
                        cell.setCellValue(numericValue);
                    } catch (NumberFormatException e) {
                        cell.setCellValue(cellData);
                    }
                }
            }
        }
        int columnCount = data.get(0).size() - 1;
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
            System.out.println("Report successfully saved");
            showAlert();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void showAlert(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Report successfully saved!");
        alert.initStyle(StageStyle.TRANSPARENT);
        alert.show();
    }
}

