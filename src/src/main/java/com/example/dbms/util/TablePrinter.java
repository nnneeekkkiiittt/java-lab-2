package com.example.dbms.util;

import com.example.dbms.service.QueryResult;

import java.util.List;

/**
 * Утилита табличного вывода результата SQL-запроса в консоль.
 */
public final class TablePrinter {

    private TablePrinter() {
    }

    /**
     * Печатает {@link QueryResult} в виде выровненной таблицы.
     * Ширина колонки равна максимальной длине значения (включая заголовок), но не меньше 1.
     * {@code null} выводится как строка {@code "null"}.
     *
     * @param result результат запроса с колонками и строками
     */
    public static void print(QueryResult result) {
        List<String> columns = result.getColumns();
        List<List<Object>> rows = result.getRows();
        int columnCount = columns.size();
        int[] widths = new int[columnCount];

        for (int i = 0; i < columnCount; i++) {
            widths[i] = Math.max(1, cellToString(columns.get(i)).length());
        }
        for (List<Object> row : rows) {
            for (int i = 0; i < columnCount; i++) {
                Object value = i < row.size() ? row.get(i) : null;
                widths[i] = Math.max(widths[i], cellToString(value).length());
            }
        }

        printAlignedRow(columns, widths);
        printSeparator(widths);
        for (List<Object> row : rows) {
            printAlignedRow(row, widths);
        }
    }

    private static void printAlignedRow(List<?> cells, int[] widths) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < widths.length; i++) {
            if (i > 0) {
                line.append(" | ");
            }
            Object value = i < cells.size() ? cells.get(i) : null;
            line.append(String.format("%-" + widths[i] + "s", cellToString(value)));
        }
        System.out.println(line);
    }

    private static void printSeparator(int[] widths) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < widths.length; i++) {
            if (i > 0) {
                line.append("-+-");
            }
            line.append(repeat('-', widths[i]));
        }
        System.out.println(line);
    }

    private static String cellToString(Object value) {
        return value == null ? "null" : String.valueOf(value);
    }

    private static String repeat(char ch, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(ch);
        }
        return builder.toString();
    }
}
