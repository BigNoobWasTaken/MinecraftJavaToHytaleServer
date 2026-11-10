package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.world;

import com.hypixel.hytale.protocol.packets.world.ServerSetBlocks;
import com.hypixel.hytale.protocol.packets.world.SetBlockCmd;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.mapping.MappingRegistry;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockChangeEntry;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSectionBlocksUpdatePacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerSetBlocksHandler implements PacketHandler<ServerSetBlocks> {
    private static final int HYTALE_CHUNK_SIZE = 32;
    private static final int MC_SECTION_SIZE = 16;

    @Override
    public void handle(ServerSetBlocks packet, MinecraftClientConnection connection) {
        Map<Vector3i, List<BlockChangeEntry>> sectionBlocks = new HashMap<>();

        for (SetBlockCmd blockCmd : packet.cmds) {
            int localX = blockCmd.index % HYTALE_CHUNK_SIZE;
            int localZ = (blockCmd.index / HYTALE_CHUNK_SIZE) % HYTALE_CHUNK_SIZE;
            int localY = blockCmd.index / (HYTALE_CHUNK_SIZE * HYTALE_CHUNK_SIZE);

            int worldX = packet.x * HYTALE_CHUNK_SIZE + localX;
            int worldY = packet.y * HYTALE_CHUNK_SIZE + localY;
            int worldZ = packet.z * HYTALE_CHUNK_SIZE + localZ;

            int sectionX = Math.floorDiv(worldX, MC_SECTION_SIZE);
            int sectionY = Math.floorDiv(worldY, MC_SECTION_SIZE);
            int sectionZ = Math.floorDiv(worldZ, MC_SECTION_SIZE);

            int blockLocalX = Math.floorMod(worldX, MC_SECTION_SIZE);
            int blockLocalY = Math.floorMod(worldY, MC_SECTION_SIZE);
            int blockLocalZ = Math.floorMod(worldZ, MC_SECTION_SIZE);

//            long sectionPos = UpdateSectionBlocksPacket.encodeSectionPosition(sectionX, sectionY, sectionZ);

            int blockId = MappingRegistry.INSTANCE.getBlockMapper().getMapping(blockCmd.blockId);
//            long encodedBlock = UpdateSectionBlocksPacket.encodeBlockChange(
//                    blockId, blockLocalX, blockLocalY, blockLocalZ
//            );

            Vector3i sectionPos = Vector3i.from(sectionX, sectionY, sectionZ);

            BlockChangeEntry blockChangeEntry = new BlockChangeEntry(Vector3i.from(blockLocalX, blockLocalY, blockLocalZ), blockId);

            sectionBlocks.computeIfAbsent(sectionPos, k -> new ArrayList<>()).add(blockChangeEntry);
        }

        for (Map.Entry<Vector3i, List<BlockChangeEntry>> entry : sectionBlocks.entrySet()) {
            Vector3i sectionPos = entry.getKey();
            List<BlockChangeEntry> blocksList = entry.getValue();

            BlockChangeEntry[] blockChangeEntries = blocksList.toArray(new BlockChangeEntry[0]);
            //BlockChangeEntry blockChangeEntry = new BlockChangeEntry(Vector3i.from(blockLocalX, blockLocalY, blockLocalZ), blockId);
            ClientboundSectionBlocksUpdatePacket sectionPacket = new ClientboundSectionBlocksUpdatePacket(sectionPos.getX(), sectionPos.getY(), sectionPos.getZ(), blockChangeEntries);
            connection.getSession().send(sectionPacket);
        }
    }
}
