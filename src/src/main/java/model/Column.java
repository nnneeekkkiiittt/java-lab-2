package model;

public final class Column {
    private final String name;
    private final DataType type;
    private final boolean isPrimaryKey;
    private final ForeignKey foreignKey; // nullable

    public Column(String name, DataType type, boolean isPrimaryKey, ForeignKey foreignKey) {
        this.name = name.toLowerCase();
        this.type = type;
        this.isPrimaryKey = isPrimaryKey;
        this.foreignKey = foreignKey;
    }

    public String getName()          { return name; }
    public DataType getType()        { return type; }
    public boolean isPrimaryKey()    { return isPrimaryKey; }
    public ForeignKey getForeignKey(){ return foreignKey; }
    public boolean isNullable()      { return !isPrimaryKey(); }
}
