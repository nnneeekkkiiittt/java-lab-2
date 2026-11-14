package model;

import java.io.Serializable;
import java.util.Arrays;

public final class Row implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Object[] values;

    public Row(Object[] values) { this.values = values.clone(); }

    public Object get(int index) { return values[index]; }
    public int size()            { return values.length; }

    /** Возвращает новую строку с заменённым значением (Row неизменяем). */
    public Row with(int index, Object value) {
        Object[] copy = values.clone();
        copy[index] = value;
        return new Row(copy);
    }

    public Object[] toArray() { return values.clone(); }

    @Override public boolean equals(Object o) {
        return o instanceof Row && Arrays.equals(values, ((Row) o).values);
    }
    @Override public int hashCode() { return Arrays.hashCode(values); }
    @Override public String toString() { return Arrays.toString(values); }
}
