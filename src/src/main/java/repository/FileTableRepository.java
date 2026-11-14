package repository;

import exception.DotaDBException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


public class FileTableRepository implements TableRepository {
    private static final String TABLE_EXT = ".tbl";
    private static final String TEMP_EXT = ".tmp";

    private final Path dir;

    public FileTableRepository(Path dir) {
        this.dir = dir;
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new DotaDBException("Cannot create data directory: " + dir, e);
        }
    }

    @Override
    public List<TableData> loadAll() {
        List<TableData> result = new ArrayList<>();
        try (Stream<Path> listing = Files.list(dir)) {
            List<Path> files = listing.sorted().toList();
            for (Path file : files) {
                String name = file.getFileName().toString();
                if (name.endsWith(TEMP_EXT)) {
                    Files.deleteIfExists(file);          // недописанный файл после сбоя
                } else if (name.endsWith(TABLE_EXT)) {
                    result.add(read(file));
                }
            }
        } catch (IOException e) {
            throw new DotaDBException("Cannot read data directory: " + dir, e);
        }
        return result;
    }

    @Override
    public void save(TableData data) {
        String tableName = data.getSchema().getTableName();
        Path target = file(tableName);
        Path temp = dir.resolve(target.getFileName().toString() + TEMP_EXT);
        try {
            try (FileOutputStream fos = new FileOutputStream(temp.toFile());
                 ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(fos))) {
                out.writeObject(data);
                out.flush();
                fos.getFD().sync();                      // данные физически на диске
            }
            try {
                Files.move(temp, target,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new DotaDBException("Cannot save table " + tableName, e);
        }
    }

    @Override
    public void drop(String tableName) {
        try {
            Files.deleteIfExists(file(tableName));
        } catch (IOException e) {
            throw new DotaDBException("Cannot delete table file " + tableName, e);
        }
    }

    private TableData read(Path file) {
        String fileName = file.getFileName().toString();
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(file)))) {
            Object object = in.readObject();
            if (!(object instanceof TableData)) {
                throw new DotaDBException("Not a table file: " + fileName);
            }
            return (TableData) object;
        } catch (IOException | ClassNotFoundException e) {
            // EOFException = обрезанный файл, InvalidClassException = формат классов изменился
            throw new DotaDBException("Cannot read " + fileName + ": " + e, e);
        }
    }

    private Path file(String tableName) {
        return dir.resolve(tableName.toLowerCase() + TABLE_EXT);
    }
}
