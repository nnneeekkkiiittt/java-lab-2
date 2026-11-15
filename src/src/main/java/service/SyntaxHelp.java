package service;

/** Справка по синтаксису запросов, которые понимает консоль. */
public final class SyntaxHelp {

    private static final String TEXT = """
            Поддерживаемые команды (ключевые слова в любом регистре, ';' в конце необязательна):

              SELECT * | колонка[, колонка...] FROM таблица [WHERE колонка оператор значение]
              INSERT INTO таблица [(колонка, ...)] VALUES (значение, ...)[, (значение, ...)]
              UPDATE таблица SET колонка = значение[, колонка = значение...] [WHERE колонка оператор значение]
              DELETE FROM таблица [WHERE колонка оператор значение]
              CREATE TABLE таблица (колонка ТИП [PRIMARY KEY] [REFERENCES таблица(колонка)], ...)
              DROP TABLE таблица
              CREATE INDEX имя ON таблица (колонка)

            Операторы сравнения: =  !=  <>  <  >  <=  >=   (в WHERE допускается одно условие)
            Типы колонок: INT, DOUBLE, STRING, DATE, BOOLEAN
            Значения: 42, 3.14, 'текст', TRUE, FALSE, NULL; дата записывается строкой '2026-11-15'

            Примеры:
              SELECT name, gold FROM heroes WHERE gold > 2000
              INSERT INTO heroes (id, name) VALUES (6, 'Lion')
              UPDATE heroes SET gold = 3000 WHERE id = 2
              DELETE FROM items WHERE id = 4
              CREATE TABLE cars (id INT PRIMARY KEY, brand STRING, price DOUBLE)
              CREATE INDEX idx_heroes_gold ON heroes (gold)

            Команды консоли: help - эта справка, tables - показать таблицы, exit - выход""";

    private SyntaxHelp() {
    }

    /** Возвращает текст справки.
     * @return многострочный текст с синтаксисом, операторами, типами и примерами.
     */
    public static String text() {
        return TEXT;
    }

    /** Печатает справку в стандартный вывод. */
    public static void print() {
        System.out.println(TEXT);
    }
}
