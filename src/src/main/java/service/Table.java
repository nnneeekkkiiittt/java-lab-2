package service;

import exception.DuplicateKeyException;
import model.DataType;
import model.Row;
import model.Schema;

import java.util.*;
import java.util.function.Predicate;

public class Table {
    private final Schema schema;
    private final Map<Long, Row> rows = new LinkedHashMap<>();   // rowId -> строка
    private final Map<Object, Long> pkIndex = new HashMap<>();   // значение PK -> rowId
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

    public Schema getSchema() { return schema; }
    public int size()         { return rows.size(); }

    public Collection<Row> getRows() {
        return Collections.unmodifiableCollection(rows.values());
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
            pkIndex.remove(removed.get(schema.getPkPosition()));
        }
        return ids.size();
    }

    // ---------------- внутреннее ----------------

    private void put(Row row) {
        long id = nextRowId++;
        rows.put(id, row);
        pkIndex.put(row.get(schema.getPkPosition()), id);
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
