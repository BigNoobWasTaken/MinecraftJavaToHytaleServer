package io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction;

import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;

import java.util.Map;

public record InteractionContext(MinecraftClientConnection connection, Map<String, String> vars) {
}
