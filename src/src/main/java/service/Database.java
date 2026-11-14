package service;

import model.Condition;
import model.Schema;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Database {
    void createTable(Schema schema);
    void dropTable(String table);
    int insert(String table, List<String> columns, List<List<Object>> rows);
    Query select(String table, List<String> columns, Condition where);
    int update(String table, Map<String, Object> assignments, Condition where);
    int delete(String table, Condition where);
    void createIndex(String indexName, String table, String column);
    Collection<Table> getTables();
}
