package repository;

import service.Table;

import java.util.List;

public interface TableRepository {
    List<Table> loadAll();
    void save(Table table);
    void drop(Table table);
}
