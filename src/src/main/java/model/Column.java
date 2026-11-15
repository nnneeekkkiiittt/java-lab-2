package model;

import java.io.Serializable;

/** Описывает имя и тип колонки, признак первичного ключа и необязательное ограничение внешнего ключа. */
public final class Column implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Имя колонки в нижнем регистре. */
    private final String name;
    /** Тип значений, допустимых в колонке. */
    private final DataType type;
    /** Признак использования колонки в качестве первичного ключа. */
    private final boolean isPrimaryKey;
    /** Ограничение внешнего ключа или {@code null}, если оно не задано. */
    private final ForeignKey foreignKey;

    /** Создаёт описание колонки.
     * @param name имя колонки.
     * @param type тип данных.
     * @param isPrimaryKey является ли колонка первичным ключом.
     * @param foreignKey описание внешнего ключа или {@code null}.
     */
    public Column(String name, DataType type, boolean isPrimaryKey, ForeignKey foreignKey) {
        this.name = name.toLowerCase();
        this.type = type;
        this.isPrimaryKey = isPrimaryKey;
        this.foreignKey = foreignKey;
    }

    /** Возвращает нормализованное имя колонки.
     * @return имя колонки в нижнем регистре.
     */
    public String getName()          { return name; }
    /** Возвращает тип данных колонки.
     * @return тип данных колонки.
     */
    public DataType getType()        { return type; }
    /** Проверяет, является ли колонка первичным ключом.
     * @return {@code true}, если колонка — первичный ключ.
     */
    public boolean isPrimaryKey()    { return isPrimaryKey; }
    /** Возвращает ограничение внешнего ключа.
     * @return внешний ключ или {@code null}, если ограничение не задано.
     */
    public ForeignKey getForeignKey(){ return foreignKey; }
    /** Проверяет, допускает ли колонка значение {@code NULL}.
     * @return {@code true}, если колонка допускает {@code NULL}.
     */
    public boolean isNullable()      { return !isPrimaryKey(); }
}
