package parser.statement;

import model.Condition;
import service.Database;
import service.Query;

public final class DeleteStatement implements Statement {
    private final String table;
    private final Condition where;   // null означает все строки

    public DeleteStatement(String table, Condition where) {
        this.table = table;
        this.where = where;
    }

    @Override
    public Query execute(Database db) {
        return Query.ofAffected(db.delete(table, where));
    }
}
