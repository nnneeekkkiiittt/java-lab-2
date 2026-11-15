package repository;

import model.Index;
import model.Row;
import model.Schema;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/** Сериализуемый снимок таблицы, содержащий схему, строки и вторичные индексы. */
public final class TableData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Схема таблицы на момент сохранения. */
    private final Schema schema;
    /** Копия набора строк таблицы. */
    private final ArrayList<Row> rows;
    /** Копия вторичных индексов таблицы. */
    private final ArrayList<Index> indexes;

    /** Создаёт сериализуемый снимок таблицы.
     * @param schema схема таблицы.
     * @param rows строки таблицы.
     * @param indexes вторичные индексы таблицы.
     */
    public TableData(Schema schema, Collection<Row> rows, Collection<Index> indexes) {
        this.schema = schema;
        this.rows = new ArrayList<>(rows);   // копия: сериализуется только ArrayList
        this.indexes = new ArrayList<>(indexes); // аналогично
    }

    /** Возвращает схему сохранённой таблицы.
     * @return схема таблицы.
     */
    public Schema getSchema()        { return schema; }

    /** Возвращает строки сохранённой таблицы.
     * @return коллекция строк.
     */
    public Collection<Row> getRows() { return rows; }

    /** Возвращает вторичные индексы сохранённой таблицы.
     * @return список индексов.
     */
    public ArrayList<Index> getIndexes() {
        return indexes;
    }
}
