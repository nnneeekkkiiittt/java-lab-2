package service;

import exception.DuplicateKeyException;
import exception.ForeignKeyException;
import exception.SchemaException;
import model.Column;
import model.Condition;
import model.DataType;
import model.ForeignKey;
import model.Row;
import model.Schema;

import java.util.*;
import java.util.function.Predicate;

public class DatabaseImpl implements Database {
    private final Map<String, Table> tables = new LinkedHashMap<>();

    // ---------------- каталог ----------------

    @Override
    public void createTable(Schema schema) {
        String name = schema.getTableName();
        if (tables.containsKey(name)) {
            throw new SchemaException("Table already exists: " + name);
        }
        for (Column c : schema.getColumns()) {
            ForeignKey fk = c.getForeignKey();
            if (fk != null && !tables.containsKey(fk.getRefTable())) {
                throw new SchemaException("Unknown referenced table: " + fk.getRefTable());
            }
        }
        tables.put(name, new Table(schema));
        // TODO: repository.save(...)
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
        // TODO: repository.delete(targetName)
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

        // Сначала готовим и проверяем ВСЕ записи, потом вставляем: пакет вставляется целиком или никак.
        List<Object[]> prepared = new ArrayList<>();
        Set<Object> batchPks = new HashSet<>();

        for (List<Object> given : valueRows) {
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
        // TODO: repository.save(table), один раз на запрос
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
        for (Row row : table.select(filter(schema, where))) {
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

        // Упрощение: менять PK таблицы, на которую кто-то ссылается, нельзя.
        if (byPosition.containsKey(schema.getPkPosition())
                && hasReferencingTables(schema.getTableName())) {
            throw new ForeignKeyException("Cannot change primary key of referenced table "
                    + schema.getTableName());
        }

        int updated = table.update(filter(schema, where), byPosition);
        // TODO: repository.save(table)
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

        // TODO: repository.save(table)
        return table.delete(filter);
    }

    // ---------------- внутреннее ----------------

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

    /** Позиции колонок из запроса. null, пусто или "*" означает все колонки по порядку. */
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

    private Predicate<Row> filter(Schema schema, Condition where) {
        if (where == null) return row -> true;
        position(schema, where.getColumn());   // чтобы не упасть на get(-1) внутри bind
        return where.bind(schema);
    }

    /** Для каждого внешнего ключа строки: значение (если не null) должно существовать в родителе. */
    private void checkForeignKeys(Schema schema, Object[] values) {
        for (int i = 0; i < schema.size(); i++) {
            ForeignKey fk = schema.getColumns().get(i).getForeignKey();
            if (fk == null || values[i] == null) continue;
            if (table(fk.getRefTable()).findByPk(values[i]) == null) {
                throw new ForeignKeyException("No row in " + fk.getRefTable() + " with key " + values[i]);
            }
        }
    }

    /** Есть ли в других таблицах строки, ссылающиеся на этот ключ родителя. */
    private boolean isReferenced(String parentName, Object pk) {
        for (Table other : tables.values()) {
            Schema s = other.getSchema();
            for (int i = 0; i < s.size(); i++) {
                ForeignKey fk = s.getColumns().get(i).getForeignKey();
                if (fk == null || !fk.getRefTable().equals(parentName)) continue;
                final int pos = i;
                if (!other.select(row -> pk.equals(row.get(pos))).isEmpty()) return true;
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