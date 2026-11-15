package service;

import exception.DuplicateKeyException;
import exception.ForeignKeyException;
import exception.SchemaException;
import exception.TypeMismatchException;
import model.Column;
import model.Condition;
import model.DataType;
import model.ForeignKey;
import model.Index;
import model.Operator;
import model.Row;
import model.Schema;
import repository.NoOpTableRepository;
import repository.TableData;
import repository.TableRepository;

import java.util.*;
import java.util.function.Predicate;

/** Реализует операции СУБД, поддерживает ограничения целостности, индексы и сохранение состояния через репозиторий. */
public class DatabaseImpl implements Database {
    private final Map<String, Table> tables = new LinkedHashMap<>();
    private final TableRepository repository;

    /** Создаёт СУБД без персистентного хранилища. */
    public DatabaseImpl() {
        this(new NoOpTableRepository());
    }

    /** Создаёт СУБД и восстанавливает данные из репозитория.
     * @param repository хранилище снимков таблиц.
     */
    public DatabaseImpl(TableRepository repository) {
        this.repository = repository;
        for (TableData data : repository.loadAll()) {
            tables.put(data.getSchema().getTableName(),
                    new Table(data.getSchema(), data.getRows(), data.getIndexes()));
        }
    }

    // ---------------- каталог ----------------

    @Override
    public void createTable(Schema schema) {
        String name = schema.getTableName();
        if (tables.containsKey(name)) {
            throw new SchemaException("Table already exists: " + name);
        }
        for (Column c : schema.getColumns()) {
            ForeignKey fk = c.getForeignKey();
            if (fk == null) continue;
            Table parent = tables.get(fk.getRefTable());
            if (parent == null) {
                throw new SchemaException("Unknown referenced table: " + fk.getRefTable());
            }
            // внешний ключ может ссылаться только на первичный ключ родителя, и типы должны совпадать
            Column parentKey = parent.getSchema().getPrimaryKey();
            if (!parentKey.getName().equals(fk.getRefColumn()) || parentKey.getType() != c.getType()) {
                throw new SchemaException("Column " + c.getName() + " must reference the primary key "
                        + fk.getRefTable() + "." + parentKey.getName() + " (" + parentKey.getType() + ")");
            }
        }
        Table table = new Table(schema);
        tables.put(name, table);
        persist(table);
    }

    @Override
    public void dropTable(String name) {
        Table target = table(name);
        String targetName = target.getSchema().getTableName();

        for (Table other : tables.values()) {
            if (other == target) continue;
            for (Column c : other.getSchema().getColumns()) {
                ForeignKey fk = c.getForeignKey();
                if (fk != null && fk.getRefTable().equals(targetName)) {
                    throw new ForeignKeyException("Table " + targetName + " is referenced by "
                            + other.getSchema().getTableName() + "." + c.getName());
                }
            }
        }
        tables.remove(targetName);
        repository.drop(targetName);
    }

    @Override
    public void createIndex(String indexName, String tableName, String column) {
        Table table = table(tableName);
        position(table.getSchema(), column);   // колонка существует

        String name = indexName.toLowerCase();
        for (Table t : tables.values()) {
            for (Index i : t.getIndexes()) {
                if (i.getName().equals(name)) {
                    throw new SchemaException("Index already exists: " + indexName);
                }
            }
        }
        if (table.hasIndex(column)) {
            throw new SchemaException("Column " + column + " is already indexed");
        }
        table.createIndex(name, column);
        persist(table);
    }

    @Override
    public Collection<Table> getTables() {
        return Collections.unmodifiableCollection(tables.values());
    }

    // ---------------- INSERT ----------------

    @Override
    public int insert(String tableName, List<String> columns, List<List<Object>> valueRows) {
        Table table = table(tableName);
        Schema schema = table.getSchema();
        int[] targets = positions(schema, columns);
        int pkPos = schema.getPkPosition();

        List<Object[]> prepared = new ArrayList<>();
        Set<Object> batchPks = new HashSet<>();

        for (List<Object> given : valueRows) {
            if (given.size() != targets.length) {
                throw new SchemaException("Expected " + targets.length + " values but got " + given.size());
            }
            Object[] values = new Object[schema.size()];   // непереданные колонки остаются null
            for (int i = 0; i < targets.length; i++) {
                int pos = targets[i];
                DataType type = schema.getColumns().get(pos).getType();
                values[pos] = type.cast(given.get(i));
            }

            checkForeignKeys(schema, values);

            Object pk = values[pkPos];
            if (table.findByPk(pk) != null || !batchPks.add(pk)) {
                throw new DuplicateKeyException("Duplicate primary key: " + pk);
            }
            prepared.add(values);
        }

        for (Object[] values : prepared) {
            table.insert(values);
        }
        persist(table);   // один раз на запрос
        return prepared.size();
    }

    // ---------------- SELECT ----------------

    @Override
    public Query select(String tableName, List<String> columns, Condition where) {
        Table table = table(tableName);
        Schema schema = table.getSchema();
        int[] positions = positions(schema, columns);

        List<String> names = new ArrayList<>();
        for (int pos : positions) {
            names.add(schema.getColumns().get(pos).getName());
        }

        List<Object[]> result = new ArrayList<>();
        for (Row row : findRows(table, where)) {
            Object[] projected = new Object[positions.length];
            for (int i = 0; i < positions.length; i++) {
                projected[i] = row.get(positions[i]);
            }
            result.add(projected);
        }
        return Query.ofRows(names, result);
    }

    // ---------------- UPDATE ----------------

    @Override
    public int update(String tableName, Map<String, Object> assignments, Condition where) {
        Table table = table(tableName);
        Schema schema = table.getSchema();

        Map<Integer, Object> byPosition = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : assignments.entrySet()) {
            int pos = position(schema, e.getKey());
            Column column = schema.getColumns().get(pos);
            Object value = column.getType().cast(e.getValue());
            byPosition.put(pos, value);

            ForeignKey fk = column.getForeignKey();
            if (fk != null && value != null && table(fk.getRefTable()).findByPk(value) == null) {
                throw new ForeignKeyException("No row in " + fk.getRefTable() + " with key " + value);
            }
        }

        if (byPosition.containsKey(schema.getPkPosition())
                && hasReferencingTables(schema.getTableName())) {
            throw new ForeignKeyException("Cannot change primary key of referenced table "
                    + schema.getTableName());
        }

        int updated = table.update(filter(schema, where), byPosition);
        if (updated > 0) persist(table);
        return updated;
    }

    // ---------------- DELETE ----------------

    @Override
    public int delete(String tableName, Condition where) {
        Table table = table(tableName);
        Schema schema = table.getSchema();
        Predicate<Row> filter = filter(schema, where);

        for (Row row : table.select(filter)) {
            Object pk = row.get(schema.getPkPosition());
            if (isReferenced(schema.getTableName(), pk)) {
                throw new ForeignKeyException("Row with key " + pk + " is referenced from another table");
            }
        }

        int deleted = table.delete(filter);
        if (deleted > 0) persist(table);
        return deleted;
    }

    // ---------------- внутреннее ----------------

    private void persist(Table table) {
        repository.save(new TableData(table.getSchema(), table.getRows(), table.getIndexes()));
    }

    private Table table(String name) {
        Table t = tables.get(name.toLowerCase());
        if (t == null) throw new SchemaException("Unknown table: " + name);
        return t;
    }

    private int position(Schema schema, String column) {
        int pos = schema.indexOf(column);
        if (pos == -1) throw new SchemaException("Unknown column: " + column);
        return pos;
    }

    private int[] positions(Schema schema, List<String> columns) {
        boolean all = columns == null || columns.isEmpty()
                || (columns.size() == 1 && columns.getFirst().equals("*"));
        if (all) {
            int[] result = new int[schema.size()];
            for (int i = 0; i < result.length; i++) result[i] = i;
            return result;
        }
        int[] result = new int[columns.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = position(schema, columns.get(i));
        }
        return result;
    }

    private List<Row> findRows(Table table, Condition where) {
        Schema schema = table.getSchema();
        if (where == null) return table.select(row -> true);

        int pos = position(schema, where.getColumn());
        Column column = schema.getColumns().get(pos);
        Operator op = where.getOperator();
        Object literal = where.getValue();

        if (literal != null && op != Operator.NOT_EQUAL) {
            Object key = tryCast(column.getType(), literal);
            if (key != null) {
                if (pos == schema.getPkPosition() && op == Operator.EQUAL) {
                    Row row = table.findByPk(key);
                    return row == null ? List.of() : List.of(row);
                }
                Index index = table.getIndex(column.getName());
                if (index != null) {
                    return table.selectByIndex(index, op, key);
                }
            }
        }
        return table.select(where.bind(schema));   // полный перебор
    }

    private Object tryCast(DataType type, Object literal) {
        try {
            return type.cast(literal);
        } catch (TypeMismatchException e) {
            return null;
        }
    }

    private Predicate<Row> filter(Schema schema, Condition where) {
        if (where == null) return row -> true;
        position(schema, where.getColumn());   // чтобы не упасть на get(-1) внутри bind
        return where.bind(schema);
    }

    private void checkForeignKeys(Schema schema, Object[] values) {
        for (int i = 0; i < schema.size(); i++) {
            ForeignKey fk = schema.getColumns().get(i).getForeignKey();
            if (fk == null || values[i] == null) continue;
            if (table(fk.getRefTable()).findByPk(values[i]) == null) {
                throw new ForeignKeyException("No row in " + fk.getRefTable() + " with key " + values[i]);
            }
        }
    }

    private boolean isReferenced(String parentName, Object pk) {
        DataType parentKeyType = table(parentName).getSchema().getPrimaryKey().getType();
        for (Table other : tables.values()) {
            Schema s = other.getSchema();
            for (int i = 0; i < s.size(); i++) {
                Column column = s.getColumns().get(i);
                ForeignKey fk = column.getForeignKey();
                if (fk == null || !fk.getRefTable().equals(parentName)) continue;

                Index index = other.getIndex(column.getName());
                if (index != null && column.getType() == parentKeyType) {
                    if (!index.find(Operator.EQUAL, pk).isEmpty()) return true;   // по индексу
                } else {
                    final int pos = i;
                    if (!other.select(row -> pk.equals(row.get(pos))).isEmpty()) return true;
                }
            }
        }
        return false;
    }

    private boolean hasReferencingTables(String parentName) {
        for (Table other : tables.values()) {
            for (Column c : other.getSchema().getColumns()) {
                ForeignKey fk = c.getForeignKey();
                if (fk != null && fk.getRefTable().equals(parentName)) return true;
            }
        }
        return false;
    }
}