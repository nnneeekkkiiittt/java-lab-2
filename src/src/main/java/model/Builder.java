package model;

import java.util.ArrayList;
import java.util.List;

public class Builder {
    private final String name;
    private final List<Column> cols = new ArrayList<>();

    public Builder(String name) { this.name = name; }

    public Builder pk(String col, DataType t) {
        cols.add(new Column(col, t, true, null));
        return this;
    }

    public Builder column(String col, DataType t) {
        cols.add(new Column(col, t, false, null));
        return this;
    }

    public Builder fk(String col, DataType t, String refTable, String refCol) {
        cols.add(new Column(col, t, false, new ForeignKey(refTable, refCol)));
        return this;
    }

    public Schema build() { return new Schema(name, cols); }
}
