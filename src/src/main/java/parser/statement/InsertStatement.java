package parser.statement;

import service.Database;
import service.Query;

import java.util.List;

/** Представляет разобранную команду INSERT INTO с одной или несколькими записями. */
public final class InsertStatement implements Statement {
    private final String table;
    private final List<String> columns;        // null означает все колонки по порядку
    private final List<List<Object>> rows;     // одна или несколько записей

    /** Создаёт команду вставки одной или нескольких записей.
     * @param table имя целевой таблицы.
     * @param columns список колонок или {@code null}, если значения заданы для всех колонок.
     * @param rows значения вставляемых записей.
     */
    public InsertStatement(String table, List<String> columns, List<List<Object>> rows) {
        this.table = table;
        this.columns = columns;
        this.rows = rows;
    }

    @Override
    public Query execute(Database db) {
        return Query.ofAffected(db.insert(table, columns, rows));
    }
}
