package parser;

import service.Database;
import service.Query;

/** Единая точка входа для интерфейса: строка запроса, разбор, выполнение, результат. */
public class QueryProcessor {
    private final Database database;

    /** Создаёт обработчик запросов.
     * @param database база данных для исполнения команд.
     */
    public QueryProcessor(Database database) {
        this.database = database;
    }

    /** Разбирает и выполняет одну SQL-команду.
     * @param sql текст запроса.
     * @return результат выполнения.
     */
    public Query execute(String sql) {
        return SqlParser.parse(sql).execute(database);
    }
}
