package parser;

import exception.ParseException;
import model.Column;
import model.Condition;
import model.DataType;
import model.ForeignKey;
import model.Operator;
import model.Schema;
import parser.statement.CreateIndexStatement;
import parser.statement.CreateTableStatement;
import parser.statement.DeleteStatement;
import parser.statement.DropTableStatement;
import parser.statement.InsertStatement;
import parser.statement.SelectStatement;
import parser.statement.Statement;
import parser.statement.UpdateStatement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Разбор SQL-подобного языка методом рекурсивного спуска. Превращает текст в {@link Statement}
 * и ничего не знает о таблицах: проверки по схеме делает слой service.
 *
 * <pre>
 * select  := SELECT ( * | ident {, ident} ) FROM ident [WHERE cond]
 * insert  := INSERT INTO ident [ ( ident {, ident} ) ] VALUES row {, row}
 * row     := ( literal {, literal} )
 * update  := UPDATE ident SET ident = literal {, ident = literal} [WHERE cond]
 * delete  := DELETE FROM ident [WHERE cond]
 * create  := CREATE TABLE ident ( coldef {, coldef} )
 *          | CREATE INDEX ident ON ident ( ident )
 * coldef  := ident type [PRIMARY KEY] [REFERENCES ident ( ident )]
 * drop    := DROP TABLE ident
 * cond    := ident operator literal          operator: = != &lt;&gt; &lt; &gt; &lt;= &gt;=
 * literal := number | 'string' | TRUE | FALSE | NULL
 * </pre>
 * Завершающая точка с запятой необязательна. Ключевые слова не зависят от регистра.
 */
public final class SqlParser {
    private static final Set<String> RESERVED = Set.of(
            "SELECT", "FROM", "WHERE", "INSERT", "INTO", "VALUES", "UPDATE", "SET", "DELETE",
            "CREATE", "DROP", "TABLE", "INDEX", "ON", "PRIMARY", "REFERENCES", "TRUE", "FALSE", "NULL");

    private final List<Token> tokens;
    private int pos;

    private SqlParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /** Разбирает строку и создаёт объект SQL-команды.
     * @param sql исходный текст запроса.
     * @return разобранная команда.
     * @throws exception.ParseException если синтаксис не поддерживается или некорректен.
     */
    public static Statement parse(String sql) {
        return new SqlParser(Tokenizer.tokenize(sql)).statement();
    }

    // ---------------- верхний уровень ----------------

    private Statement statement() {
        if (peek().getType() == TokenType.END || (isSymbol(";") && tokens.get(pos + 1).getType() == TokenType.END)) {
            throw new ParseException("Пустой запрос");
        }

        Statement result;
        if (acceptWord("SELECT")) {
            result = select();
        } else if (acceptWord("INSERT")) {
            result = insert();
        } else if (acceptWord("UPDATE")) {
            result = update();
        } else if (acceptWord("DELETE")) {
            result = delete();
        } else if (acceptWord("CREATE")) {
            result = create();
        } else if (acceptWord("DROP")) {
            result = drop();
        } else {
            throw new ParseException("Неизвестная команда " + describe(peek())
                    + ". Поддерживаются: SELECT, INSERT, UPDATE, DELETE, CREATE, DROP");
        }

        acceptSymbol(";");
        if (peek().getType() != TokenType.END) {
            throw new ParseException("Лишний текст после конца запроса: " + describe(peek())
                    + " на позиции " + peek().getPosition());
        }
        return result;
    }

    // ---------------- команды ----------------

    private Statement select() {
        List<String> columns = null;                 // null означает все колонки
        if (!acceptSymbol("*")) {
            columns = identifierList();
        }
        expectWord("FROM");
        String table = identifier();
        return new SelectStatement(table, columns, optionalWhere());
    }

    private Statement insert() {
        expectWord("INTO");
        String table = identifier();

        List<String> columns = null;
        if (acceptSymbol("(")) {
            columns = identifierList();
            expectSymbol(")");
        }

        expectWord("VALUES");
        List<List<Object>> rows = new ArrayList<>();
        do {
            List<Object> row = valueRow();
            if (columns != null && row.size() != columns.size()) {
                throw new ParseException("Число значений (" + row.size()
                        + ") не совпадает с числом колонок (" + columns.size() + ")");
            }
            rows.add(row);
        } while (acceptSymbol(","));

        return new InsertStatement(table, columns, rows);
    }

    private Statement update() {
        String table = identifier();
        expectWord("SET");

        Map<String, Object> assignments = new LinkedHashMap<>();
        do {
            String column = identifier();
            expectSymbol("=");
            Object value = literal();
            if (assignments.containsKey(column)) {
                throw new ParseException("Колонка " + column + " присваивается дважды");
            }
            assignments.put(column, value);
        } while (acceptSymbol(","));

        return new UpdateStatement(table, assignments, optionalWhere());
    }

    private Statement delete() {
        expectWord("FROM");
        String table = identifier();
        return new DeleteStatement(table, optionalWhere());
    }

    private Statement create() {
        if (acceptWord("TABLE")) {
            return createTable();
        }
        if (acceptWord("INDEX")) {
            return createIndex();
        }
        throw new ParseException("После CREATE ожидалось TABLE или INDEX, найдено " + describe(peek()));
    }

    private Statement createTable() {
        String tableName = identifier();
        expectSymbol("(");

        List<Column> columns = new ArrayList<>();
        Set<String> names = new HashSet<>();
        int primaryKeys = 0;
        do {
            String name = identifier();
            if (!names.add(name)) {
                throw new ParseException("Колонка " + name + " объявлена дважды");
            }
            DataType type = type();

            boolean primaryKey = false;
            ForeignKey foreignKey = null;
            while (true) {
                if (acceptWord("PRIMARY")) {
                    expectWord("KEY");
                    primaryKey = true;
                    primaryKeys++;
                } else if (acceptWord("REFERENCES")) {
                    String refTable = identifier();
                    expectSymbol("(");
                    String refColumn = identifier();
                    expectSymbol(")");
                    foreignKey = new ForeignKey(refTable, refColumn);
                } else {
                    break;
                }
            }
            columns.add(new Column(name, type, primaryKey, foreignKey));
        } while (acceptSymbol(","));
        expectSymbol(")");

        if (primaryKeys != 1) {
            throw new ParseException(primaryKeys == 0
                    ? "Нужна ровно одна колонка PRIMARY KEY, ни одной не объявлено"
                    : "Поддерживается только один PRIMARY KEY, объявлено: " + primaryKeys);
        }
        return new CreateTableStatement(new Schema(tableName, columns));
    }

    private Statement createIndex() {
        String indexName = identifier();
        expectWord("ON");
        String table = identifier();
        expectSymbol("(");
        String column = identifier();
        expectSymbol(")");
        return new CreateIndexStatement(indexName, table, column);
    }

    private Statement drop() {
        expectWord("TABLE");
        return new DropTableStatement(identifier());
    }

    // ---------------- части запросов ----------------

    private Condition optionalWhere() {
        if (!acceptWord("WHERE")) {
            return null;
        }
        String column = identifier();

        Token operatorToken = peek();
        Operator operator = operatorToken.getType() == TokenType.SYMBOL
                ? Operator.ToOperatorFromSql(operatorToken.getText()) : null;
        if (operator == null) {
            throw new ParseException("Ожидался оператор сравнения (= != <> < > <= >=), найдено "
                    + describe(operatorToken) + " на позиции " + operatorToken.getPosition());
        }
        pos++;

        Object value = literal();
        if (isWord("AND") || isWord("OR")) {
            throw new ParseException("В WHERE поддерживается только одно условие, "
                    + peek().getText().toUpperCase() + " не поддерживается");
        }
        return new Condition(column, operator, value);
    }

    private List<Object> valueRow() {
        expectSymbol("(");
        List<Object> values = new ArrayList<>();
        do {
            values.add(literal());
        } while (acceptSymbol(","));
        expectSymbol(")");
        return values;
    }

    private DataType type() {
        Token token = peek();
        if (token.getType() != TokenType.WORD) {
            throw new ParseException("Ожидался тип колонки, найдено " + describe(token)
                    + " на позиции " + token.getPosition());
        }
        pos++;
        return DataType.ToDataTypeFromSql(token.getText());
    }

    private Object literal() {
        Token token = peek();
        switch (token.getType()) {
            case NUMBER:
                pos++;
                return token.getValue();
            case STRING:
                pos++;
                return token.getText();
            case WORD:
                if (token.getText().equalsIgnoreCase("TRUE")) { pos++; return Boolean.TRUE; }
                if (token.getText().equalsIgnoreCase("FALSE")) { pos++; return Boolean.FALSE; }
                if (token.getText().equalsIgnoreCase("NULL")) { pos++; return null; }
                break;
            default:
                break;
        }
        throw new ParseException("Ожидалось значение (число, 'строка', TRUE, FALSE или NULL), найдено "
                + describe(token) + " на позиции " + token.getPosition());
    }

    // ---------------- работа с токенами ----------------

    private Token peek() {
        return tokens.get(pos);
    }

    private boolean isWord(String word) {
        Token t = peek();
        return t.getType() == TokenType.WORD && t.getText().equalsIgnoreCase(word);
    }

    private boolean acceptWord(String word) {
        if (isWord(word)) {
            pos++;
            return true;
        }
        return false;
    }

    private void expectWord(String word) {
        if (!acceptWord(word)) {
            throw new ParseException("Ожидалось " + word + ", найдено " + describe(peek())
                    + " на позиции " + peek().getPosition());
        }
    }

    private boolean isSymbol(String symbol) {
        Token t = peek();
        return t.getType() == TokenType.SYMBOL && t.getText().equals(symbol);
    }

    private boolean acceptSymbol(String symbol) {
        if (isSymbol(symbol)) {
            pos++;
            return true;
        }
        return false;
    }

    private void expectSymbol(String symbol) {
        if (!acceptSymbol(symbol)) {
            throw new ParseException("Ожидалось '" + symbol + "', найдено " + describe(peek())
                    + " на позиции " + peek().getPosition());
        }
    }

    /** Имя таблицы, колонки или индекса, приводится к нижнему регистру. Ключевые слова не годятся. */
    private String identifier() {
        Token t = peek();
        if (t.getType() != TokenType.WORD || RESERVED.contains(t.getText().toUpperCase())) {
            throw new ParseException("Ожидалось имя, найдено " + describe(t) + " на позиции " + t.getPosition());
        }
        pos++;
        return t.getText().toLowerCase();
    }

    private List<String> identifierList() {
        List<String> names = new ArrayList<>();
        do {
            names.add(identifier());
        } while (acceptSymbol(","));
        return names;
    }

    private static String describe(Token t) {
        return t.getType() == TokenType.END ? "конец запроса" : "'" + t.getText() + "'";
    }
}
