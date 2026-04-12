package com.algoblock.core.levels;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LevelLoader {
    private final Gson gson = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create();

    public Level loadFromResource(String resourcePath) {
        InputStream stream = LevelLoader.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IllegalArgumentException("Level resource not found: " + resourcePath);
        }
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, Level.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Level> loadRange(int from, int to) {
        List<Level> levels = new ArrayList<>();
        for (int i = from; i <= to; i++) {
            levels.add(loadFromResource("/levels/level-" + i + ".json"));
        }
        return levels;
    }
}
