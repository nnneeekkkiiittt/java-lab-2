package model;

import java.io.Serializable;

/** Содержит имя родительской таблицы и имя колонки, на которую ссылается внешний ключ. */
public final class ForeignKey implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Имя родительской таблицы, на первичный ключ которой ссылается колонка. */
    private final String refTable;
    /** Имя целевой колонки родительской таблицы. */
    private final String refColumn;

    /** Создаёт ссылку на колонку другой таблицы.
     * @param refTable имя родительской таблицы.
     * @param refColumn имя целевой колонки.
     */
    public ForeignKey(String refTable, String refColumn) {
        this.refTable = refTable.toLowerCase();
        this.refColumn = refColumn.toLowerCase();
    }

    /** Возвращает имя таблицы, на первичный ключ которой ссылается внешний ключ.
     * @return имя родительской таблицы.
     */
    public String getRefTable()  { return refTable; }
    /** Возвращает имя целевой колонки во внешней таблице.
     * @return имя целевой колонки.
     */
    public String getRefColumn() { return refColumn; }
}
