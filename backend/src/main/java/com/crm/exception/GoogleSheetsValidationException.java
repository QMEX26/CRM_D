package com.crm.exception;

import lombok.Getter;

@Getter
public class GoogleSheetsValidationException extends RuntimeException {

    private final String sheetName;
    private final Integer rowNumber;
    private final String columnName;
    private final String reason;

    public GoogleSheetsValidationException(String sheetName, Integer rowNumber, String columnName, String reason) {
        super(formatMessage(sheetName, rowNumber, columnName, reason));
        this.sheetName = sheetName;
        this.rowNumber = rowNumber;
        this.columnName = columnName;
        this.reason = reason;
    }

    private static String formatMessage(String sheetName, Integer rowNumber, String columnName, String reason) {
        StringBuilder sb = new StringBuilder("PUSH FAILED\n\n");
        if (sheetName != null) {
            sb.append("Sheet: ").append(sheetName).append("\n");
        }
        if (rowNumber != null && rowNumber > 0) {
            sb.append("Row: ").append(rowNumber).append("\n");
        }
        if (columnName != null && !columnName.isBlank()) {
            sb.append("Column: ").append(columnName).append("\n");
        }
        sb.append("\nReason:\n").append(reason != null ? reason : "Unknown validation failure").append("\n\n");
        sb.append("Database changes:\nROLLED BACK");
        return sb.toString();
    }
}
