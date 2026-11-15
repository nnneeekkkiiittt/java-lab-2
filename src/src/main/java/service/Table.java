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

/** Хранит строки одной таблицы, индекс первичного ключа и вторичные индексы; поддерживает синхронизацию индексов при изменении данных. */
public class Table {
    private final Schema schema;
    private final Map<Long, Row> rows = new LinkedHashMap<>();   // rowId -> строка
    private final Map<Object, Long> pkIndex = new HashMap<>();   // значение PK -> rowId
    private final Map<String, Index> indexes = new LinkedHashMap<>();   // имя колонки -> индекс
    private long nextRowId = 1;

    /** Создаёт пустую таблицу по заданной схеме.
     * @param schema схема создаваемой таблицы.
     */
    public Table(Schema schema) {
        this.schema = schema;
    }

    /** Создаёт таблицу и загружает в неё начальные строки.
     * @param schema схема таблицы.
     * @param initial начальные строки в порядке колонок схемы.
     */
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
    /** Восстанавливает таблицу из схемы, строк и сохранённых индексов.
     * Несогласованные индексы пересоздаются на основе строк.
     * @param schema схема таблицы.
     * @param initial восстановленные строки таблицы.
     * @param savedIndexes индексы, загруженные из постоянного хранилища.
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

    /** Возвращает схему таблицы.
     * @return схема таблицы.
     */
    public Schema getSchema() { return schema; }

    /** Возвращает число строк в таблице.
     * @return количество строк.
     */
    public int size()         { return rows.size(); }

    /** Возвращает строки таблицы в порядке вставки.
     * @return неизменяемое представление коллекции строк.
     */
    public Collection<Row> getRows() {
        return Collections.unmodifiableCollection(rows.values());
    }

    // ---------------- индексы ----------------

    /** Проверяет, создан ли индекс для указанной колонки.
     * @param column имя колонки.
     * @return {@code true}, если для колонки существует индекс.
     */
    public boolean hasIndex(String column) {
        return indexes.containsKey(column.toLowerCase());
    }

    /** Возвращает индекс указанной колонки.
     * @param column имя колонки.
     * @return индекс или {@code null}, если индекс не создан.
     */
    public Index getIndex(String column) {
        return indexes.get(column.toLowerCase());
    }

    /** Возвращает все вторичные индексы таблицы.
     * @return неизменяемое представление коллекции индексов.
     */
    public Collection<Index> getIndexes() {
        return Collections.unmodifiableCollection(indexes.values());
    }

    /** Создаёт индекс для колонки и заполняет его существующими строками.
     * @param name имя нового индекса.
     * @param column имя индексируемой колонки.
     * @throws SchemaException если колонка с указанным именем отсутствует.
     */
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

    /** Выбирает строки через индекс в порядке возрастания значения индексируемой колонки.
     * Перед вызовом литерал должен быть приведён к типу колонки.
     * @param index индекс, используемый для поиска.
     * @param op оператор сравнения.
     * @param literal значение, с которым сравнивается колонка.
     * @return строки, удовлетворяющие условию.
     */
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

    /** Добавляет новую строку и обновляет первичный и вторичные индексы.
     * @param values значения колонок в порядке, заданном схемой.
     * @throws DuplicateKeyException если первичный ключ уже существует.
     * @throws exception.TypeMismatchException если значение не соответствует типу колонки.
     */
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

    /** Возвращает строки, подходящие под предикат, в порядке вставки.
     * @param filter условие отбора строк.
     * @return список строк, удовлетворяющих предикату.
     */
    public List<Row> select(Predicate<Row> filter) {
        List<Row> result = new ArrayList<>();
        for (Row row : rows.values()) {
            if (filter.test(row)) {
                result.add(row);
            }
        }
        return result;
    }

    /** Находит строку по первичному ключу за ожидаемое время {@code O(1)}.
     * Значение ключа перед вызовом должно быть приведено к типу первичной колонки.
     * @param pk значение первичного ключа.
     * @return найденная строка или {@code null}, если ключ отсутствует.
     */
    public Row findByPk(Object pk) {
        Long id = pkIndex.get(pk);
        return id == null ? null : rows.get(id);
    }

    // ---------------- UPDATE ----------------

    /**
     * Обновляет строки, подходящие под фильтр, и синхронизирует индексы.
     * @param filter условие отбора обновляемых строк.
     * @param assignments отображение позиций колонок на новые значения.
     * @return число обновлённых строк.
     * @throws DuplicateKeyException если обновление приводит к повторению первичного ключа.
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

    /** Удаляет строки, подходящие под предикат, и удаляет их записи из индексов.
     * @param filter условие отбора удаляемых строк.
     * @return количество удалённых строк.
     */
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
