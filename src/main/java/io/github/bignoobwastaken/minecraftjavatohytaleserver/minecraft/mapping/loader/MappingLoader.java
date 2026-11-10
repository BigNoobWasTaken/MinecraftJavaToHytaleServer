package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.mapping.loader;

import com.google.gson.reflect.TypeToken;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.config.JsonConfig;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.util.GsonUtil;

import java.lang.reflect.Type;
import java.util.Map;

public class MappingLoader {
    public static Map<String, Object> loadMapping(JsonConfig jsonConfig) {
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> mappings = GsonUtil.GSON.fromJson(jsonConfig.get(), type);

        if (mappings == null) {
            throw new RuntimeException("Failed to parse mapping file: " + jsonConfig.getPath());
        }

        return mappings;
    }
}