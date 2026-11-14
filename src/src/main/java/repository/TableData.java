package repository;

import model.Row;
import model.Schema;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public final class TableData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Schema schema;
    private final ArrayList<Row> rows;

    public TableData(Schema schema, Collection<Row> rows) {
        this.schema = schema;
        this.rows = new ArrayList<>(rows);   // копия: сериализуется только ArrayList
    }

    public Schema getSchema()        { return schema; }
    public Collection<Row> getRows() { return rows; }
}
