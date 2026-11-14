package repository;

import model.Index;
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
    private final ArrayList<Index> indexes;

    public TableData(Schema schema, Collection<Row> rows, Collection<Index> indexes) {
        this.schema = schema;
        this.rows = new ArrayList<>(rows);   // копия: сериализуется только ArrayList
        this.indexes = new ArrayList<>(indexes); // аналогично
    }

    public Schema getSchema()        { return schema; }
    public Collection<Row> getRows() { return rows; }

    public ArrayList<Index> getIndexes() {
        return indexes;
    }
}
