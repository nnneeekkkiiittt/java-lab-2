package repository;

import java.util.List;

/** Репозиторий-пустышка: ничего не хранит. Для тестов и бенчмарков без диска. */
public class NoOpTableRepository implements TableRepository {
    /** Создаёт репозиторий, который не выполняет операции с постоянным хранилищем. */
    public NoOpTableRepository() {
    }

    @Override public List<TableData> loadAll()   { return List.of(); }
    @Override public void save(TableData data)   { }
    @Override public void drop(String tableName) { }
}
