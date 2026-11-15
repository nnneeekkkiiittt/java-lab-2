package service;

import model.Row;
import model.Schema;

import java.util.ArrayList;
import java.util.List;

/** Показывает таблицы базы: название, названия колонок и первые строки. */
public final class TablePreview {

    private TablePreview() {
    }

    /** Печатает каждую таблицу с новой строки: заголовок, колонки и не более {@code limit} первых строк.
     * Строки берутся напрямую из таблицы без полного перебора, поэтому вызов быстр и для больших таблиц.
     *
     * @param database база, таблицы которой нужно показать.
     * @param limit максимальное число показываемых строк каждой таблицы.
     */
    public static void print(Database database, int limit) {
        if (database.getTables().isEmpty()) {
            System.out.println("В базе пока нет таблиц. Создайте таблицу командой CREATE TABLE.");
            return;
        }

        System.out.println("Таблицы в базе данных:");
        for (Table table : database.getTables()) {
            Schema schema = table.getSchema();
            System.out.println();
            System.out.println("Таблица " + schema.getTableName() + " (строк: " + table.size() + ")");

            List<String> columns = new ArrayList<>();
            schema.getColumns().forEach(column -> columns.add(column.getName()));

            List<Object[]> rows = new ArrayList<>();
            for (Row row : table.getRows()) {
                if (rows.size() >= limit) {
                    break;
                }
                rows.add(row.toArray());
            }

            TablePrinter.print(Query.ofRows(columns, rows));
            if (table.size() > rows.size()) {
                System.out.println("... показаны первые " + rows.size() + " из " + table.size());
            } else if (rows.isEmpty()) {
                System.out.println("(таблица пуста)");
            }
        }
    }
}
