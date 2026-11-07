package model;

public final class ForeignKey {
    private final String refTable;
    private final String refColumn;

    public ForeignKey(String refTable, String refColumn) {
        this.refTable = refTable.toLowerCase();
        this.refColumn = refColumn.toLowerCase();
    }

    public String getRefTable()  { return refTable; }
    public String getRefColumn() { return refColumn; }
}
