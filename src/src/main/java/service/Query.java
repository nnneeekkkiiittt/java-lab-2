package service;

import java.util.List;

public class Query{
    private final List<String> columns;   // null, если это не SELECT
    private final List<Object[]> rows;
    private final int affected;

    private Query(List<String> columns, List<Object[]> rows, int affected) {
        this.columns = columns;
        this.rows = rows;
        this.affected = affected;
    }

    public static Query ofRows(List<String> columns, List<Object[]> rows) {
        return new Query(columns, rows, rows.size());
    }

    public static Query ofAffected(int affected) {
        return new Query(null, null, affected);
    }

    public boolean hasRows()          { return columns != null; }
    public List<String> getColumns()  { return columns; }
    public List<Object[]> getRows()   { return rows; }
    public int getAffected()          { return affected; }
}
