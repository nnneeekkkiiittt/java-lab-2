package app;

import model.DataType;
import model.Schema;
import service.Database;

import java.util.Arrays;
import java.util.List;

/** Начальные данные предметной области (Dota 2). Выполняется только если база пуста. */
public final class DotaSeed {

    private DotaSeed() {
    }

    public static void fill(Database db) {
        Schema heroes = Schema.table("heroes")
                .pk("id", DataType.INT)
                .column("name", DataType.STRING)
                .column("radiant", DataType.BOOLEAN)
                .column("hp", DataType.INT)
                .column("is_alive", DataType.BOOLEAN)
                .column("gold", DataType.INT)
                .column("kills", DataType.INT)
                .column("assists", DataType.INT)
                .column("deaths", DataType.INT)
                .build();

        Schema items = Schema.table("items")
                .pk("id", DataType.INT)
                .column("name", DataType.STRING)
                .fk("owner_id", DataType.INT, "heroes", "id")
                .column("cost", DataType.INT)
                .column("description", DataType.STRING)
                .build();

        db.createTable(heroes);
        db.createTable(items);

        db.insert("heroes", null, List.of(
                Arrays.asList(1L, "Invoker",       true,  560L, true,  3200L, 5L, 7L, 1L),
                Arrays.asList(2L, "Pudge",         false, 780L, true,  2100L, 8L, 2L, 3L),
                Arrays.asList(3L, "Crystal Maiden", true, 480L, false, 1500L, 1L, 12L, 6L),
                Arrays.asList(4L, "Axe",           false, 820L, true,  2800L, 6L, 4L, 2L),
                Arrays.asList(5L, "Lina",          true,  500L, true,  3500L, 9L, 5L, 2L)));

        db.insert("items", null, List.of(
                Arrays.asList(1L, "Blink Dagger", 1L, 2250L, "Teleports a short distance"),
                Arrays.asList(2L, "Black King Bar", 4L, 3975L, "Grants spell immunity"),
                Arrays.asList(3L, "Force Staff", 3L, 2200L, "Pushes a unit forward"),
                Arrays.asList(4L, "Town Portal Scroll", null, 50L, "Not owned by any hero")));

        db.createIndex("idx_items_owner", "items", "owner_id");
    }
}
