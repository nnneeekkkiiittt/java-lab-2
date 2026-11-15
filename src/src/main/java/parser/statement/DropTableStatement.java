package parser.statement;

import service.Database;
import service.Query;

public final class DropTableStatement implements Statement {
    private final String table;

    public DropTableStatement(String table) {
        this.table = table;
    }

    @Override
    public Query execute(Database db) {
        db.dropTable(table);
        return Query.ofMessage("Таблица " + table + " удалена");
    }
}
