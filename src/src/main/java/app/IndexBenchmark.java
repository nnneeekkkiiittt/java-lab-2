package app;

import model.Condition;
import model.DataType;
import model.Operator;
import model.Schema;
import service.Database;
import service.DatabaseImpl;
import service.Query;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Сравнение {@code SELECT} без индекса и с индексом на базе в памяти (без диска).
 * Результат печатается в консоль и сохраняется в Markdown-файл.
 */
public final class IndexBenchmark {
    private static final int[] SIZES = {10_000, 100_000, 1_000_000};
    private static final int QUERIES = 30;
    private static final int WARMUP = 5;
    private static final int CHUNK = 50_000;

    private static long sink;   // чтобы результат запросов не выбросил оптимизатор

    private IndexBenchmark() {
    }

    /**
     * Запускает замеры и записывает отчёт.
     *
     * @param args необязательный путь к файлу отчёта (по умолчанию {@code benchmark-results.md})
     * @throws IOException если отчёт не удалось записать
     */
    public static void main(String[] args) throws IOException {
        Path output = Path.of(args.length > 0 ? args[0] : "benchmark-results.md");
        StringBuilder md = new StringBuilder();

        md.append("# Результаты тестирования производительности индекса\n\n");
        md.append("Сравнение `SELECT` без индекса (полный перебор) и с индексом. База в памяти, ")
                .append("без записи на диск. Время запроса: медиана из ").append(QUERIES)
                .append(" замеров после ").append(WARMUP).append(" прогревочных. ")
                .append("Таблица `heroes`, колонка `gold`: значения равномерно в [0, n/10), ")
                .append("то есть около 10 строк на значение.\n\n");
        md.append("**Окружение**\n\n");
        md.append("- Дата: ").append(LocalDateTime.now().withNano(0)).append('\n');
        md.append("- Java: ").append(System.getProperty("java.version"))
                .append(" (").append(System.getProperty("java.vm.name")).append(")\n");
        md.append("- ОС: ").append(System.getProperty("os.name")).append(' ')
                .append(System.getProperty("os.arch")).append('\n');
        md.append("- Логических процессоров: ").append(Runtime.getRuntime().availableProcessors()).append('\n');
        md.append("- Максимум памяти JVM: ")
                .append(Runtime.getRuntime().maxMemory() / (1024 * 1024)).append(" МБ\n\n");
        md.append("**Результаты**\n\n");

        emit(md, "| Rows | Query | No index, ms | With index, ms | Speedup | Result rows |");
        emit(md, "|---:|---|---:|---:|---:|---:|");

        for (int n : SIZES) {
            Database db = new DatabaseImpl();
            fill(db, n);

            int range = Math.max(1, n / 10);
            Query[] scanResults = new Query[3];
            double[][] scan = measureAll(db, range, scanResults);

            long start = System.nanoTime();
            db.createIndex("idx_heroes_gold", "heroes", "gold");
            double buildMs = (System.nanoTime() - start) / 1e6;

            Query[] indexResults = new Query[3];
            double[][] indexed = measureAll(db, range, indexResults);

            String[] names = {"gold = K", "gold >= max-4", "gold < 1% of range"};
            for (int q = 0; q < 3; q++) {
                // сверка: индекс обязан вернуть то же количество строк, что и перебор
                if (scanResults[q].getRows().size() != indexResults[q].getRows().size()) {
                    throw new IllegalStateException("Mismatch for " + names[q]);
                }
                double a = scan[q][0];
                double b = indexed[q][0];
                emit(md, String.format(Locale.US, "| %,d | `%s` | %.3f | %.3f | x%.0f | %,d |",
                        n, names[q], a, b, a / b, (long) indexed[q][1]));
            }
            emit(md, String.format(Locale.US, "| %,d | index build time | | %.1f | | |", n, buildMs));
        }

        md.append("\nSpeedup = время без индекса / время с индексом. ")
                .append("Последняя строка для каждого размера: разовая стоимость построения индекса.\n");
        Files.writeString(output, md.toString(), StandardCharsets.UTF_8);

        System.out.println("\n(sink=" + sink + ")");
        System.out.println("Saved: " + output.toAbsolutePath());
    }

    private static void emit(StringBuilder md, String line) {
        System.out.println(line);
        md.append(line).append('\n');
    }

    private static void fill(Database db, int n) {
        db.createTable(Schema.table("heroes")
                .pk("id", DataType.INT)
                .column("name", DataType.STRING)
                .column("radiant", DataType.BOOLEAN)
                .column("hp", DataType.INT)
                .column("is_alive", DataType.BOOLEAN)
                .column("gold", DataType.INT)
                .column("kills", DataType.INT)
                .column("assists", DataType.INT)
                .column("deaths", DataType.INT)
                .build());

        Random random = new Random(42);
        int range = Math.max(1, n / 10);
        for (int from = 0; from < n; from += CHUNK) {
            int to = Math.min(n, from + CHUNK);
            List<List<Object>> batch = new ArrayList<>(to - from);
            for (int id = from; id < to; id++) {
                batch.add(Arrays.asList(id, "hero" + id, id % 2 == 0, 500 + random.nextInt(500),
                        true, random.nextInt(range), random.nextInt(30), random.nextInt(30), random.nextInt(30)));
            }
            db.insert("heroes", null, batch);
        }
    }

    private static double[][] measureAll(Database db, int range, Query[] lastResults) {
        double[][] out = new double[3][];
        for (int q = 0; q < 3; q++) {
            Random random = new Random(7);          // одинаковые ключи для обеих серий
            double[] times = new double[QUERIES];
            long rows = 0;
            for (int i = -WARMUP; i < QUERIES; i++) {
                Condition where = condition(q, random.nextInt(range), range);
                long start = System.nanoTime();
                Query result = db.select("heroes", Arrays.asList("id", "name", "gold"), where);
                long elapsed = System.nanoTime() - start;
                sink += result.getRows().size();
                if (i >= 0) {
                    times[i] = elapsed / 1e6;
                    rows = result.getRows().size();
                }
                lastResults[q] = result;
            }
            Arrays.sort(times);
            out[q] = new double[]{times[QUERIES / 2], rows};
        }
        return out;
    }

    private static Condition condition(int kind, int key, int range) {
        return switch (kind) {
            case 0 -> new Condition("gold", Operator.EQUAL, (long) key);
            case 1 -> new Condition("gold", Operator.GREATER_OR_EQUAL, (long) (range - 5));
            default -> new Condition("gold", Operator.LESS_THAN, (long) Math.max(1, range / 100));
        };
    }
}
