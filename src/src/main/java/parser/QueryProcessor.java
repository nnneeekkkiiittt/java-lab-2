package parser;

import service.Database;
import service.Query;

/** Единая точка входа для интерфейса: строка запроса, разбор, выполнение, результат. */
public class QueryProcessor {
    private final Database database;

    public QueryProcessor(Database database) {
        this.database = database;
    }

    public Query execute(String sql) {
        return SqlParser.parse(sql).execute(database);
    }
}
