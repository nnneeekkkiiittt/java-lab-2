package parser.statement;

import service.Database;
import service.Query;

/** Разобранный запрос. Паттерн Command: знает, какой метод {@link Database} вызвать. */
public interface Statement {
    /** Выполняет разобранную команду над указанной базой данных.
     * @param db база данных, над которой выполняется команда.
     * @return результат выполнения команды.
     */
    Query execute(Database db);
}
