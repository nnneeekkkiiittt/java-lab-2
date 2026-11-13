package com.example.dbms.service;

import java.util.List;

/**
 * Результат выполнения SQL-запроса: имена колонок и строки данных.
 */
public class QueryResult {

    private List<String> columns;
    private List<List<Object>> rows;

    /**
     * Создаёт результат запроса.
     *
     * @param columns имена колонок
     * @param rows    строки; каждая строка — список значений в порядке колонок
     */
    public QueryResult(List<String> columns, List<List<Object>> rows) {
        this.columns = columns;
        this.rows = rows;
    }

    /**
     * Возвращает имена колонок результата.
     *
     * @return список имён колонок
     */
    public List<String> getColumns() {
        return columns;
    }

    /**
     * Возвращает строки результата.
     *
     * @return список строк
     */
    public List<List<Object>> getRows() {
        return rows;
    }
}
