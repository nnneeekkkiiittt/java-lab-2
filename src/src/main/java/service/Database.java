package service;

import model.Schema;

import java.util.List;
import java.util.Map;

public interface Database {
    void createTable(Schema schema);
    void dropTable(String table);
    int insert(String table, List<String> columns, List<List<Object>> rows);
    QueryResult select(String table, List<String> columns, Condition where);
    int update(String table, Map<String, Object> assignments, Condition where);
    int delete(String table, Condition where);
    void createIndex(String name, String table, String column);
}
