package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.world;

import com.hypixel.hytale.protocol.packets.world.UnloadChunk;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.api.ClientConnection;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.api.chunk.ChunkCoordIntPair;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundForgetLevelChunkPacket;

public class UnloadChunkHandler implements PacketHandler<UnloadChunk> {
    @Override
    public void handle(UnloadChunk packet, MinecraftClientConnection connection) {
        ClientConnection.ChunkBuffer chunkBuffer = connection.getChunkBuffer();
        for (int dz = 0; dz < 2; dz++) {
            for (int dx = 0; dx < 2; dx++) {
                int newX = packet.chunkX * 2 + dx;
                int newZ = packet.chunkZ * 2 + dz;

                ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(newX, newZ);
                chunkBuffer.removeChunk(chunkCoords);

                ClientboundForgetLevelChunkPacket unloadPacket = new ClientboundForgetLevelChunkPacket(newX, newZ);
                connection.getSession().send(unloadPacket);
            }
        }
    }
}
