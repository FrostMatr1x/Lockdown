package com.frost.lockdown;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.neoforged.fml.loading.FMLPaths;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JsonStorageManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Path FILE_PATH = FMLPaths.CONFIGDIR.get().resolve("lockdownban.json");

    public record DataEntry(String nickname, String date, String item, int quantity) {}

    public static synchronized void addEntry(String nickname, String date, String item, int quantity) {
        List<DataEntry> entries = loadEntries();
        entries.add(new DataEntry(nickname, date, item, quantity));
        saveEntries(entries);
    }

    public static synchronized void addEntry(String nickname, String item, int quantity) {
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        addEntry(nickname, currentDate, item, quantity);
    }

    public static List<DataEntry> loadEntries() {
        if (!Files.exists(FILE_PATH)) {
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(FILE_PATH.toFile())) {
            List<DataEntry> entries = GSON.fromJson(reader, new TypeToken<List<DataEntry>>() {}.getType());
            return entries != null ? entries : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static void saveEntries(List<DataEntry> entries) {
        try {
            if (FILE_PATH.getParent() != null) {
                Files.createDirectories(FILE_PATH.getParent());
            }
            try (FileWriter writer = new FileWriter(FILE_PATH.toFile())) {
                GSON.toJson(entries, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
