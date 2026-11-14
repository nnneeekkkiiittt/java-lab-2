package service;

import exception.DuplicateKeyException;
import exception.SchemaException;
import model.DataType;
import model.Index;
import model.Operator;
import model.Row;
import model.Schema;

import java.util.*;
import java.util.function.Predicate;

public class Table {
    private final Schema schema;
    private final Map<Long, Row> rows = new LinkedHashMap<>();   // rowId -> строка
    private final Map<Object, Long> pkIndex = new HashMap<>();   // значение PK -> rowId
    private final Map<String, Index> indexes = new LinkedHashMap<>();   // имя колонки -> индекс
    private long nextRowId = 1;

    public Table(Schema schema) {
        this.schema = schema;
    }

    public Table(Schema schema, Collection<Row> initial) {
        this(schema);
        for (Row row : initial) {
            put(row);
        }
    }

    /**
     * Загрузка с диска: строки и сохранённые индексы. Индекс, который не совпадает с данными
     * (или относится к несуществующей колонке), перестраивается из строк.
     */
    public Table(Schema schema, Collection<Row> initial, Collection<Index> savedIndexes) {
        this(schema, initial);   // индексы ещё не подключены, строки добавляются без них
        for (Index saved : savedIndexes) {
            if (!schema.hasColumn(saved.getColumn())) continue;
            if (isConsistent(saved)) {
                indexes.put(saved.getColumn(), saved);
            } else {
                createIndex(saved.getName(), saved.getColumn());
            }
        }
    }

    public Schema getSchema() { return schema; }
    public int size()         { return rows.size(); }

    public Collection<Row> getRows() {
        return Collections.unmodifiableCollection(rows.values());
    }

    // ---------------- индексы ----------------

    public boolean hasIndex(String column) {
        return indexes.containsKey(column.toLowerCase());
    }

    public Index getIndex(String column) {
        return indexes.get(column.toLowerCase());
    }

    public Collection<Index> getIndexes() {
        return Collections.unmodifiableCollection(indexes.values());
    }

    /** Создаёт индекс и заполняет его существующими строками. */
    public void createIndex(String name, String column) {
        int pos = schema.indexOf(column);
        if (pos == -1) throw new SchemaException("Unknown column: " + column);

        Index index = new Index(name, schema.getColumns().get(pos).getName());
        int pkPos = schema.getPkPosition();
        for (Row row : rows.values()) {
            index.add(row.get(pos), row.get(pkPos));
        }
        indexes.put(index.getColumn(), index);
    }

    /** Строки по индексу. Литерал уже приведён к типу колонки. Порядок: по возрастанию значения. */
    public List<Row> selectByIndex(Index index, Operator op, Object literal) {
        List<Row> result = new ArrayList<>();
        for (Object pk : index.find(op, literal)) {
            Row row = findByPk(pk);
            if (row != null) result.add(row);
        }
        return result;
    }

    /** Число записей индекса должно равняться числу строк с непустым значением в колонке. */
    private boolean isConsistent(Index index) {
        int pos = schema.indexOf(index.getColumn());
        int expected = 0;
        for (Row row : rows.values()) {
            if (row.get(pos) != null) expected++;
        }
        return expected == index.entryCount();
    }

    // ---------------- INSERT ----------------

    public void insert(Object[] values) {
        Object[] converted = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            converted[i] = schema.getColumns().get(i).getType().cast(values[i]);
        }

        Object pk = converted[schema.getPkPosition()];
        if (pkIndex.containsKey(pk)) {
            throw new DuplicateKeyException("Duplicate primary key: " + pk);
        }
        put(new Row(converted));
    }

    // ---------------- SELECT ----------------

    /** Возвращает строки, подходящие под фильтр, в порядке вставки. */
    public List<Row> select(Predicate<Row> filter) {
        List<Row> result = new ArrayList<>();
        for (Row row : rows.values()) {
            if (filter.test(row)) {
                result.add(row);
            }
        }
        return result;
    }

    /** Поиск по первичному ключу за O(1). Литерал из запроса сначала приведи к типу PK. */
    public Row findByPk(Object pk) {
        Long id = pkIndex.get(pk);
        return id == null ? null : rows.get(id);
    }

    // ---------------- UPDATE ----------------

    /**
     * Обновляет строки, подходящие под фильтр.
     * @param assignments позиция колонки -> новое значение
     * @return число обновлённых строк
     */
    public int update(Predicate<Row> filter, Map<Integer, Object> assignments) {
        // 1. приводим новые значения к типам колонок
        Map<Integer, Object> values = new HashMap<>();
        for (Map.Entry<Integer, Object> e : assignments.entrySet()) {
            DataType type = schema.getColumns().get(e.getKey()).getType();
            values.put(e.getKey(), type.cast(e.getValue()));
        }

        // 2. сначала считаем новые строки и проверяем PK, ничего не меняя
        int pkPos = schema.getPkPosition();
        List<Long> ids = matchingIds(filter);
        Map<Long, Row> updated = new LinkedHashMap<>();
        Set<Object> newPks = new HashSet<>();

        for (Long id : ids) {
            Row old = rows.get(id);
            Row fresh = old;
            for (Map.Entry<Integer, Object> e : values.entrySet()) {
                fresh = fresh.with(e.getKey(), e.getValue());
            }

            if (values.containsKey(pkPos)) {
                Object oldPk = old.get(pkPos);
                Object newPk = fresh.get(pkPos);
                if (!Objects.equals(oldPk, newPk)
                        && (pkIndex.containsKey(newPk) || !newPks.add(newPk))) {
                    throw new DuplicateKeyException("Duplicate primary key: " + newPk);
                }
            }
            updated.put(id, fresh);
        }

        // 3. ошибок нет, применяем
        for (Map.Entry<Long, Row> e : updated.entrySet()) {
            Row old = rows.get(e.getKey());
            Row fresh = e.getValue();
            if (values.containsKey(pkPos)) {
                pkIndex.remove(old.get(pkPos));
                pkIndex.put(fresh.get(pkPos), e.getKey());
            }
            reindex(old, fresh);
            rows.put(e.getKey(), fresh);
        }
        return updated.size();
    }

    // ---------------- DELETE ----------------

    /** Удаляет строки, подходящие под фильтр. Возвращает их число. */
    public int delete(Predicate<Row> filter) {
        List<Long> ids = matchingIds(filter);
        for (Long id : ids) {
            Row removed = rows.remove(id);
            Object pk = removed.get(schema.getPkPosition());
            pkIndex.remove(pk);
            for (Index index : indexes.values()) {
                index.remove(removed.get(schema.indexOf(index.getColumn())), pk);
            }
        }
        return ids.size();
    }

    // ---------------- внутреннее ----------------

    private void put(Row row) {
        long id = nextRowId++;
        Object pk = row.get(schema.getPkPosition());
        rows.put(id, row);
        pkIndex.put(pk, id);
        for (Index index : indexes.values()) {
            index.add(row.get(schema.indexOf(index.getColumn())), pk);
        }
    }

    /** Переносит запись всех индексов со старой версии строки на новую. */
    private void reindex(Row old, Row fresh) {
        int pkPos = schema.getPkPosition();
        Object oldPk = old.get(pkPos);
        Object newPk = fresh.get(pkPos);
        for (Index index : indexes.values()) {
            int pos = schema.indexOf(index.getColumn());
            Object oldValue = old.get(pos);
            Object newValue = fresh.get(pos);
            // ключ в индексе меняется, если изменилось значение колонки или первичный ключ строки
            if (!Objects.equals(oldValue, newValue) || !Objects.equals(oldPk, newPk)) {
                index.remove(oldValue, oldPk);
                index.add(newValue, newPk);
            }
        }
    }

    private List<Long> matchingIds(Predicate<Row> filter) {
        List<Long> ids = new ArrayList<>();
        for (Map.Entry<Long, Row> e : rows.entrySet()) {
            if (filter.test(e.getValue())) {
                ids.add(e.getKey());
            }
        }
        return ids;
    }
}
