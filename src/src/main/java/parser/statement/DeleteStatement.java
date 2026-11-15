package parser.statement;

import model.Condition;
import service.Database;
import service.Query;

/** Представляет разобранную команду DELETE FROM. */
public final class DeleteStatement implements Statement {
    private final String table;
    private final Condition where;   // null означает все строки

    /** Создаёт команду удаления строк таблицы.
     * @param table имя таблицы.
     * @param where условие удаления или {@code null} для удаления всех строк.
     */
    public DeleteStatement(String table, Condition where) {
        this.table = table;
        this.where = where;
    }

    @Override
    public Query execute(Database db) {
        return Query.ofAffected(db.delete(table, where));
    }
}
