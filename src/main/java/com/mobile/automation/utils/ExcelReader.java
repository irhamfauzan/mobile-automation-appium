package com.mobile.automation.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads test data from .xlsx files.
 * First row is treated as the header row; subsequent rows become data maps.
 */
public class ExcelReader {

    private ExcelReader() {}

    /**
     * Returns all rows from the given sheet as a list of header->value maps.
     */
    public static List<Map<String, String>> readSheet(String filePath, String sheetName) {
        List<Map<String, String>> data = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                LogUtil.error("Sheet '" + sheetName + "' not found in: " + filePath);
                return data;
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return data;

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValue(cell));
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> rowData = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    rowData.put(headers.get(j), getCellValue(cell));
                }
                data.add(rowData);
            }

            LogUtil.info("Read " + data.size() + " rows from sheet: " + sheetName);

        } catch (IOException e) {
            LogUtil.error("Failed to read Excel file: " + e.getMessage());
        }

        return data;
    }

    /**
     * Returns test data as a 2D Object array for TestNG @DataProvider.
     * Columns: all values in header order.
     */
    public static Object[][] getTestData(String filePath, String sheetName) {
        List<Map<String, String>> rows = readSheet(filePath, sheetName);
        if (rows.isEmpty()) return new Object[0][0];

        List<String> headers = new ArrayList<>(rows.get(0).keySet());
        Object[][] data = new Object[rows.size()][headers.size()];

        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            for (int j = 0; j < headers.size(); j++) {
                data[i][j] = row.get(headers.get(j));
            }
        }
        return data;
    }

    /**
     * Returns test data as array of Maps for named-column access.
     */
    public static Object[][] getTestDataAsMaps(String filePath, String sheetName) {
        List<Map<String, String>> rows = readSheet(filePath, sheetName);
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            data[i][0] = rows.get(i);
        }
        return data;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getDateCellValue().toString()
                    : String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }
}
