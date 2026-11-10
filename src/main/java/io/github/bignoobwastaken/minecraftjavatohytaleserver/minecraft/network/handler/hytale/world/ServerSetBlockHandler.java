package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.world;

import com.hypixel.hytale.protocol.packets.world.ServerSetBlock;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.mapping.MappingRegistry;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockChangeEntry;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundBlockUpdatePacket;

public class ServerSetBlockHandler implements PacketHandler<ServerSetBlock> {
    @Override
    public void handle(ServerSetBlock packet, MinecraftClientConnection connection) {
        Vector3i blockPos = Vector3i.from(packet.x, packet.y, packet.z);
        int minecraftBlockId = MappingRegistry.INSTANCE.getBlockMapper().getMapping(packet.blockId);

        BlockChangeEntry blockChangeEntry = new BlockChangeEntry(blockPos, minecraftBlockId);

        ClientboundBlockUpdatePacket blockUpdate = new ClientboundBlockUpdatePacket(blockChangeEntry);
        connection.getSession().send(blockUpdate);
    }
}
