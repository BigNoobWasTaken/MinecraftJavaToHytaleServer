package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player;

import org.geysermc.mcprotocollib.auth.GameProfile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MinecraftConnectionMap {
    public static final MinecraftConnectionMap INSTANCE = new MinecraftConnectionMap();
    private final Map<UUID, GameProfile> uuidToGameProfile = new HashMap<>();
    private MinecraftConnectionMap() {}

    public GameProfile getProfile(UUID uuid) {
        return uuidToGameProfile.get(uuid);
    }

    public GameProfile setProfile(UUID uuid, GameProfile profile) {
        return uuidToGameProfile.put(uuid, profile);
    }

    public GameProfile removeProfile(UUID uuid) {
        return uuidToGameProfile.remove(uuid);
    }
}
