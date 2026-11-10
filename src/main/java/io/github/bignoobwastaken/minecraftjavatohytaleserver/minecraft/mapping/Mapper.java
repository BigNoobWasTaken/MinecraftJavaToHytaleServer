package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.mapping;

import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.config.JsonConfig;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.mapping.loader.MappingLoader;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.util.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Mapper<K> {
    private final static String UNMAPPED_VALUE_ID = "Unmapped_Value";

    private final Map<K, Integer> mappings = new HashMap<>();
    private int unmappedValue;
    private final String fileName;
    private final JsonConfig jsonConfig;

    private final Set<String> neededStringIds = new HashSet<>();

    public Mapper(String fileName) {
        this.fileName = fileName;
        this.jsonConfig = new JsonConfig(fileName);
    }

    public void loadMappings() {
        try {
            Map<String, Object> rawMappings = MappingLoader.loadMapping(jsonConfig);
            mappings.clear();
            neededStringIds.clear();

            for (Map.Entry<String, Object> entry : rawMappings.entrySet()) {
                Object mappingValue = entry.getValue();

                if (mappingValue instanceof String mappingValueString) {
                    neededStringIds.add(mappingValueString);
                }
            }

            if (!neededStringIds.isEmpty()) {
                Logger.INFO.log("Pre-loading " + neededStringIds.size() + " Minecraft mappings...");
                preloadMappingValueIds(neededStringIds);
            }

            for (Map.Entry<String, Object> entry : rawMappings.entrySet()) {
                String mappingKey = entry.getKey();
                Object mappingValue = entry.getValue();

                int mappingValueId;
                if (mappingValue instanceof Number mappingNumber) {
                    mappingValueId = mappingNumber.intValue();
                } else if (mappingValue instanceof String stringId) {
                    mappingValueId = getMappingValueId(stringId);
                    if (mappingValueId == -1) {
                        Logger.WARN.log("Invalid value id for " + stringId);
                        continue;
                    }
                } else {
                    Logger.WARN.log("Unknown value type for " + mappingKey + ": " + mappingValue.getClass().getName());
                    continue;
                }

                if (mappingKey.equalsIgnoreCase(UNMAPPED_VALUE_ID)) {
                    this.unmappedValue = mappingValueId;
                    continue;
                }

                K mappingKeyId = getMappingKeyId(mappingKey);
                if (mappingKeyId == null) {
                    Logger.WARN.log("Invalid key id for " + mappingKey);
                    continue;
                }

                mappings.put(mappingKeyId, mappingValueId);
            }

            Logger.INFO.log("Loaded " + mappings.size() + " block mappings");

            cleanup();

        } catch (Exception e) {
            Logger.ERROR.log("Failed to load block mappings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getMapping(K key) {
        return mappings.getOrDefault(key, unmappedValue);
    }

    protected abstract void preloadMappingValueIds(Set<String> stringIds);
    protected abstract int getMappingValueId(String key);
    protected abstract K getMappingKeyId(String key);
    protected abstract void cleanup();
}