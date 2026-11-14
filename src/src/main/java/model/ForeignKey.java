package model;

import java.io.Serializable;

public final class ForeignKey implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String refTable;
    private final String refColumn;

    public ForeignKey(String refTable, String refColumn) {
        this.refTable = refTable.toLowerCase();
        this.refColumn = refColumn.toLowerCase();
    }

    public String getRefTable()  { return refTable; }
    public String getRefColumn() { return refColumn; }
}
