package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Хранит неизменяемое описание таблицы и отображение имён колонок в их позиции. */
public final class Schema implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Имя таблицы в нижнем регистре. */
    private final String tableName;
    /** Неизменяемый список колонок в порядке хранения значений строки. */
    private final List<Column> columns;
    /** Отображение нормализованного имени колонки на её позицию. */
    private final Map<String, Integer> positions = new HashMap<>();
    /** Позиция колонки первичного ключа в списке колонок. */
    private final int pkPosition;

    /** Создаёт схему таблицы.
     * @param tableName имя таблицы.
     * @param columns список колонок в порядке хранения.
     */
    public Schema(String tableName, List<Column> columns) {
        this.tableName = tableName.toLowerCase();
        this.columns = List.copyOf(columns);

        int pk = -1;
        for (int i = 0; i < this.columns.size(); i++) {
            Column c = this.columns.get(i);
            positions.put(c.getName(), i);
            if (c.isPrimaryKey()) pk = i;
        }
        this.pkPosition = pk;
    }

    /** Возвращает имя таблицы в нижнем регистре.
     * @return имя таблицы.
     */
    public String getTableName()    { return tableName; }
    /** Возвращает список колонок схемы в порядке хранения значений строки.
     * @return неизменяемый список колонок.
     */
    public List<Column> getColumns() { return columns; }
    /** Возвращает количество колонок таблицы.
     * @return число колонок.
     */
    public int size()               { return columns.size(); }
    /** Возвращает позицию первичного ключа в списке колонок.
     * @return индекс первичного ключа, отсчитываемый с нуля.
     */
    public int getPkPosition()      { return pkPosition; }
    /** Возвращает описание колонки первичного ключа.
     * @return колонка первичного ключа.
     */
    public Column getPrimaryKey()   { return columns.get(pkPosition); }

    /** Проверяет наличие колонки.
     * @param name имя колонки.
     * @return {@code true}, если колонка существует.
     */
    public boolean hasColumn(String name) {
        return positions.containsKey(name.toLowerCase());
    }

    /** Находит позицию колонки с указанным именем.
     * @param name имя колонки без учёта регистра.
     * @return позиция колонки, отсчитываемая с нуля, или {@code -1}, если колонка отсутствует.
     */
    public int indexOf(String name) {
        Integer i = positions.get(name.toLowerCase());
        return i == null ? -1 : i;
    }

    /** Возвращает описание колонки по имени.
     * @param name имя колонки.
     * @return найденная колонка.
     */
    public Column column(String name) {
        return columns.get(indexOf(name));
    }

    /** Начинает построение схемы.
     * @param name имя таблицы.
     * @return построитель схемы.
     */
    public static Builder table(String name) { return new Builder(name); }
}
