package model;

import java.io.Serializable;
import java.util.Arrays;

/** Неизменяемое представление записи таблицы; значения расположены в порядке колонок схемы. */
public final class Row implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Значения строки в порядке колонок соответствующей схемы. */
    private final Object[] values;

    /** Создаёт строку, копируя массив значений.
     * @param values значения в порядке колонок схемы.
     */
    public Row(Object[] values) { this.values = values.clone(); }

    /** Возвращает значение по позиции.
     * @param index позиция колонки.
     * @return значение колонки.
     */
    public Object get(int index) { return values[index]; }
    /** Возвращает количество значений, хранящихся в строке.
     * @return количество значений в строке.
     */
    public int size()            { return values.length; }

    /** Создаёт новую строку с заменённым значением, не изменяя текущий объект.
     * @param index позиция изменяемой колонки.
     * @param value новое значение.
     * @return новая строка с изменённым значением.
     */
    public Row with(int index, Object value) {
        Object[] copy = values.clone();
        copy[index] = value;
        return new Row(copy);
    }

    /** Возвращает копию массива значений строки.
     * @return новый массив со значениями в порядке колонок схемы.
     */
    public Object[] toArray() { return values.clone(); }

    @Override public boolean equals(Object o) {
        return o instanceof Row && Arrays.equals(values, ((Row) o).values);
    }
    @Override public int hashCode() { return Arrays.hashCode(values); }
    @Override public String toString() { return Arrays.toString(values); }
}
