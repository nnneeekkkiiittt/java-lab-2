package service;

import java.util.List;

public class Query{
    private final List<String> columns;   // null, если это не SELECT
    private final List<Object[]> rows;
    private final int affected;
    private final String message;         // текст для CREATE/DROP, иначе null

    private Query(List<String> columns, List<Object[]> rows, int affected, String message) {
        this.columns = columns;
        this.rows = rows;
        this.affected = affected;
        this.message = message;
    }

    public static Query ofRows(List<String> columns, List<Object[]> rows) {
        return new Query(columns, rows, rows.size(), null);
    }

    public static Query ofAffected(int affected) {
        return new Query(null, null, affected, null);
    }

    public static Query ofMessage(String message) {
        return new Query(null, null, 0, message);
    }

    public boolean hasRows()          { return columns != null; }
    public List<String> getColumns()  { return columns; }
    public List<Object[]> getRows()   { return rows; }
    public int getAffected()          { return affected; }
    public String getMessage()        { return message; }
}
