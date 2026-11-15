package parser.statement;

import service.Database;
import service.Query;

import java.util.List;

public final class InsertStatement implements Statement {
    private final String table;
    private final List<String> columns;        // null означает все колонки по порядку
    private final List<List<Object>> rows;     // одна или несколько записей

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
