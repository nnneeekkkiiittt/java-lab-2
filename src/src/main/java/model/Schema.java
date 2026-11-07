package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Schema {
    private final String tableName;
    private final List<Column> columns;
    private final Map<String, Integer> positions = new HashMap<>();
    private final int pkPosition;

    public Schema(String tableName, List<Column> columns) {
        this.tableName = tableName.toLowerCase();
        this.columns = List.copyOf(columns);

        int pk = -1;
        for (int i = 0; i < this.columns.size(); i++) {
            Column c = this.columns.get(i);
            positions.put(c.getName(), i);
            if (c.isPrimaryKey()) pk = i;
        }
        this.pkPosition = pk;
    }

    public String getTableName()    { return tableName; }
    public List<Column> getColumns() { return columns; }
    public int size()               { return columns.size(); }
    public int getPkPosition()      { return pkPosition; }
    public Column getPrimaryKey()   { return columns.get(pkPosition); }

    public boolean hasColumn(String name) {
        return positions.containsKey(name.toLowerCase());
    }

    /** Позиция колонки или -1, если такой колонки нет. */
    public int indexOf(String name) {
        Integer i = positions.get(name.toLowerCase());
        return i == null ? -1 : i;
    }

    public Column column(String name) {
        return columns.get(indexOf(name));
    }

    public static Builder table(String name) { return new Builder(name); }
}
