package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Индекс по одной колонке: значение колонки -> набор первичных ключей строк.
 *
 * <p>Ссылки идут через первичный ключ, а не через внутренний номер строки, поэтому индекс
 * можно записать на диск и прочитать обратно без пересчёта. {@code TreeMap} даёт и точный
 * поиск, и диапазоны. Значения {@code null} не индексируются.
 */
@SuppressWarnings({"rawtypes"})
public class Index implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String column;
    private final TreeMap<Comparable, Set<Object>> entries = new TreeMap<>();

    public Index(String name, String column) {
        this.name = name;
        this.column = column;
    }

    public String getColumn() {
        return column;
    }

    public String getName() {
        return name;
    }

    public Map<Comparable, Set<Object>> getEntries() {
        return Collections.unmodifiableMap(entries);
    }

    /** Общее число пар «значение - первичный ключ». Нужно для проверки целостности после загрузки. */
    public int entryCount() {
        int count = 0;
        for (Set<Object> keys : entries.values()) {
            count += keys.size();
        }
        return count;
    }

    public void add(Object value, Object primaryKey) {
        if (value == null) return;
        Comparable key = (Comparable) value;
        Set<Object> primaryKeys = entries.get(key);
        if (primaryKeys == null) {
            primaryKeys = new LinkedHashSet<>();
            entries.put(key, primaryKeys);
        }
        primaryKeys.add(primaryKey);
    }

    public void remove(Object value, Object removedKey) {
        if (value == null) return;
        Comparable key = (Comparable) value;
        Set<Object> primaryKeys = entries.get(key);
        if (primaryKeys == null) return;
        primaryKeys.remove(removedKey);
        if (primaryKeys.isEmpty()) entries.remove(key);
    }

    /**
     * Первичные ключи строк, у которых значение колонки удовлетворяет «значение оператор literal».
     * Литерал должен быть того же типа, что и значения колонки. Оператор {@code !=} не поддерживается.
     */
    public List<Object> find(Operator op, Object literal) {
        Comparable key = (Comparable) literal;
        Collection<Set<Object>> groups = switch (op) {
            case EQUAL -> {
                Set<Object> exact = entries.get(key);
                yield exact == null ? Collections.<Set<Object>>emptyList()
                        : Collections.singletonList(exact);
            }
            case LESS_THAN -> entries.headMap(key, false).values();
            case LESS_OR_EQUAL -> entries.headMap(key, true).values();
            case GREATER_THAN -> entries.tailMap(key, false).values();
            case GREATER_OR_EQUAL -> entries.tailMap(key, true).values();
            default -> throw new UnsupportedOperationException("Index does not support " + op);
        };
        List<Object> result = new ArrayList<>();
        for (Set<Object> group : groups) {
            result.addAll(group);
        }
        return result;
    }
}
