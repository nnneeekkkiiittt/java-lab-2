package repository;

import java.util.List;

/** Хранилище таблиц на диске. Работает с {@link TableData}, о слое service ничего не знает. */
public interface TableRepository {
    /** Загружает все сохранённые таблицы из постоянного хранилища.
     * @return список восстановленных снимков таблиц.
     */
    List<TableData> loadAll();

    /** Сохраняет снимок таблицы в постоянном хранилище.
     * @param data схема, строки и индексы сохраняемой таблицы.
     */
    void save(TableData data);

    /** Удаляет сохранённые данные таблицы из хранилища.
     * @param tableName имя удаляемой таблицы.
     */
    void drop(String tableName);
}
