package model;

import java.util.ArrayList;
import java.util.List;

/** Постепенно формирует схему таблицы, добавляя первичный ключ, обычные колонки и внешние ключи. */
public class Builder {
    private final String name;
    private final List<Column> cols = new ArrayList<>();

    /** Создаёт построитель схемы.
     * @param name имя таблицы.
     */
    public Builder(String name) { this.name = name; }

    /** Добавляет первичный ключ.
     * @param col имя колонки.
     * @param t тип значений.
     * @return этот построитель.
     */
    public Builder pk(String col, DataType t) {
        cols.add(new Column(col, t, true, null));
        return this;
    }

    /** Добавляет обычную колонку.
     * @param col имя колонки.
     * @param t тип значений.
     * @return этот построитель.
     */
    public Builder column(String col, DataType t) {
        cols.add(new Column(col, t, false, null));
        return this;
    }

    /** Добавляет колонку внешнего ключа.
     * @param col имя новой колонки.
     * @param t тип значений.
     * @param refTable родительская таблица.
     * @param refCol целевая колонка.
     * @return этот построитель.
     */
    public Builder fk(String col, DataType t, String refTable, String refCol) {
        cols.add(new Column(col, t, false, new ForeignKey(refTable, refCol)));
        return this;
    }

    /** Завершает построение схемы.
     * @return созданная схема.
     */
    public Schema build() { return new Schema(name, cols); }
}
