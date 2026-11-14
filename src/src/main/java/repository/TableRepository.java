package repository;

import java.util.List;

/** Хранилище таблиц на диске. Работает с {@link TableData}, о слое service ничего не знает. */
public interface TableRepository {
    List<TableData> loadAll();
    void save(TableData data);
    void drop(String tableName);
}
