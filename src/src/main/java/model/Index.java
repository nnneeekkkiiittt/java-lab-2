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

    /** Имя индекса. */
    private final String name;
    /** Имя индексируемой колонки. */
    private final String column;
    /** Отсортированная карта «значение колонки — множество первичных ключей строк». */
    private final TreeMap<Comparable, Set<Object>> entries = new TreeMap<>();

    /** Создаёт пустой индекс для указанной колонки.
     * @param name имя индекса.
     * @param column имя индексируемой колонки.
     */
    public Index(String name, String column) {
        this.name = name;
        this.column = column;
    }

    /** Возвращает имя индексируемой колонки.
     * @return имя колонки.
     */
    public String getColumn() {
        return column;
    }

    /** Возвращает имя индекса.
     * @return имя индекса.
     */
    public String getName() {
        return name;
    }

    /** Возвращает неизменяемое представление индексированных значений и первичных ключей.
     * @return карта «значение колонки — множество первичных ключей».
     */
    public Map<Comparable, Set<Object>> getEntries() {
        return Collections.unmodifiableMap(entries);
    }

    /** Подсчитывает пары «значение — первичный ключ» в индексе.
     * Метод используется для проверки согласованности индекса после загрузки.
     * @return количество проиндексированных записей.
     */
    public int entryCount() {
        int count = 0;
        for (Set<Object> keys : entries.values()) {
            count += keys.size();
        }
        return count;
    }

    /** Добавляет первичный ключ строки в группу заданного значения.
     * Значения {@code null} в индекс не включаются.
     * @param value значение индексируемой колонки.
     * @param primaryKey первичный ключ строки.
     */
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

    /** Удаляет первичный ключ из группы указанного значения.
     * @param value прежнее значение индексируемой колонки.
     * @param removedKey первичный ключ удаляемой или изменяемой строки.
     */
    public void remove(Object value, Object removedKey) {
        if (value == null) return;
        Comparable key = (Comparable) value;
        Set<Object> primaryKeys = entries.get(key);
        if (primaryKeys == null) return;
        primaryKeys.remove(removedKey);
        if (primaryKeys.isEmpty()) entries.remove(key);
    }

    /**
     * Находит первичные ключи строк, значение индексируемой колонки которых удовлетворяет
     * условию сравнения с литералом. Поддерживаются равенство и диапазонные сравнения;
     * оператор {@code !=} индексом не поддерживается.
     *
     * @param op оператор сравнения.
     * @param literal литерал того же типа, что и значения индексируемой колонки.
     * @return список первичных ключей найденных строк.
     * @throws UnsupportedOperationException если оператор не поддерживается индексом.
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
