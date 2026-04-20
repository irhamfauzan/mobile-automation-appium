package com.mobile.automation.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Reads test data from JSON files using Jackson.
 * Supports flat maps, arrays of maps, and nested structures.
 */
public class JsonReader {

    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonReader() {}

    /**
     * Reads a JSON array of objects and returns as a list of maps.
     */
    public static List<Map<String, Object>> readJsonArray(String filePath) {
        try {
            return mapper.readValue(new File(filePath),
                    new TypeReference<List<Map<String, Object>>>() {});
        } catch (IOException e) {
            LogUtil.error("Failed to read JSON array from: " + filePath + " | " + e.getMessage());
            throw new RuntimeException("JSON read failed: " + filePath, e);
        }
    }

    /**
     * Reads a JSON object and returns as a flat map.
     */
    public static Map<String, Object> readJsonObject(String filePath) {
        try {
            return mapper.readValue(new File(filePath),
                    new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            LogUtil.error("Failed to read JSON object from: " + filePath + " | " + e.getMessage());
            throw new RuntimeException("JSON read failed: " + filePath, e);
        }
    }

    /**
     * Reads a specific key from a JSON file.
     */
    public static List<Map<String, Object>> readSection(String filePath, String key) {
        try {
            JsonNode root = mapper.readTree(new File(filePath));
            JsonNode section = root.get(key);
            if (section == null) {
                LogUtil.warn("Key '" + key + "' not found in: " + filePath);
                return List.of();
            }
            return mapper.convertValue(section, new TypeReference<List<Map<String, Object>>>() {});
        } catch (IOException e) {
            LogUtil.error("Failed to read JSON section '" + key + "' from: " + filePath);
            throw new RuntimeException("JSON read failed", e);
        }
    }

    /**
     * Returns test data as 2D Object array for TestNG @DataProvider.
     */
    public static Object[][] getTestData(String filePath) {
        List<Map<String, Object>> rows = readJsonArray(filePath);
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            data[i][0] = rows.get(i);
        }
        LogUtil.info("Loaded " + rows.size() + " test cases from: " + filePath);
        return data;
    }

    /**
     * Returns test data for a specific section key.
     */
    public static Object[][] getTestData(String filePath, String key) {
        List<Map<String, Object>> rows = readSection(filePath, key);
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            data[i][0] = rows.get(i);
        }
        return data;
    }

    /**
     * Deserializes JSON file directly into a typed POJO.
     */
    public static <T> T readAs(String filePath, Class<T> type) {
        try {
            return mapper.readValue(new File(filePath), type);
        } catch (IOException e) {
            LogUtil.error("Failed to deserialize JSON to " + type.getSimpleName() + ": " + e.getMessage());
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }
}
