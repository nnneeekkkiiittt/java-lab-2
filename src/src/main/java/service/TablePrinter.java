package service;

import java.util.List;

public final class TablePrinter {

    private TablePrinter() {
    }

    public static void print(Query result) {
        List<String> columns = result.getColumns();
        List<Object[]> rows = result.getRows();
        int columnCount = columns.size();
        int[] widths = new int[columnCount];

        for (int i = 0; i < columnCount; i++) {
            widths[i] = Math.max(1, text(columns.get(i)).length());
        }
        for (Object[] row : rows) {
            for (int i = 0; i < columnCount; i++) {
                Object value = row != null && i < row.length ? row[i] : null;
                widths[i] = Math.max(widths[i], text(value).length());
            }
        }

        printRow(columns.toArray(), widths);
        printSeparator(widths);
        for (Object[] row : rows) {
            printRow(row, widths);
        }
    }

    private static void printRow(Object[] cells, int[] widths) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < widths.length; i++) {
            if (i > 0) {
                line.append(" | ");
            }
            Object value = cells != null && i < cells.length ? cells[i] : null;
            line.append(String.format("%-" + widths[i] + "s", text(value)));
        }
        System.out.println(line);
    }

    private static void printSeparator(int[] widths) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < widths.length; i++) {
            if (i > 0) {
                line.append("-+-");
            }
            for (int j = 0; j < widths[i]; j++) {
                line.append('-');
            }
        }
        System.out.println(line);
    }

    private static String text(Object value) {
        return value == null ? "null" : String.valueOf(value);
    }
}
