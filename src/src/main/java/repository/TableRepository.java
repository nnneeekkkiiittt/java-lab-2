package repository;

public interface TableRepository {
    List<Table> loadAll();
    void save(Table table);
    void drop(Table table);
}
