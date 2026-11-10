package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.world;

import com.hypixel.hytale.protocol.packets.world.PaletteType;
import com.hypixel.hytale.protocol.packets.world.SetChunk;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.api.ClientConnection;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.mapping.MappingRegistry;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.data.HyPalette;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.api.chunk.ChunkCoordIntPair;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.chunk.Chunk;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.chunk.ChunkSection;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.chunk.palette.DataPalette;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.chunk.palette.IndirectPalette;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class SetChunkHandler implements PacketHandler<SetChunk> {
    @Override
    public void handle(SetChunk packet, MinecraftClientConnection connection) {
        byte[] chunkData = packet.data;

        ClientConnection<ChunkSection, Chunk>.ChunkBuffer chunkBuffer = connection.getChunkBuffer();

        if (chunkData == null) {
            for (int dy = 0; dy < 2; dy++) {
                for (int dz = 0; dz < 2; dz++) {
                    for (int dx = 0; dx < 2; dx++) {
                        int newX = packet.x * 2 + dx;
                        int newY = packet.y * 2 + dy;
                        int newZ = packet.z * 2 + dz;

                        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(newX, newZ);

                        chunkBuffer.addChunkSection(chunkCoords, newY, ChunkSection.EMPTY_AIR);
                    }
                }
            }
            return;
        }

        ByteBuf buf1 = Unpooled.wrappedBuffer(chunkData);


        PaletteType chunkSectionPaletteType = PaletteType.fromValue(buf1.readByte());

        HyPalette hyPalette = HyPalette.create(chunkSectionPaletteType);
        assert hyPalette != null;
        hyPalette.deserialize(buf1);

        buf1.release();

        int bitsPerEntry = this.getBitsPerEntry(hyPalette.getPaletteSize());

        for (int dy = 0; dy < 2; dy++) {
            for (int dz = 0; dz < 2; dz++) {
                for (int dx = 0; dx < 2; dx++) {
                    int newX = packet.x * 2 + dx;
                    int newY = packet.y * 2 + dy;
                    int newZ = packet.z * 2 + dz;

                    ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(newX, newZ);

                    if (chunkSectionPaletteType.equals(PaletteType.Empty)) {
                        chunkBuffer.addChunkSection(chunkCoords, newY, ChunkSection.EMPTY_AIR);
                        continue;
                    }

                    DataPalette mcPalette = new IndirectPalette(bitsPerEntry);

                    for (int localY = 0; localY < 16; localY++) {
                        for (int localZ = 0; localZ < 16; localZ++) {
                            for (int localX = 0; localX < 16; localX++) {

                                int globalX = dx * 16 + localX;
                                int globalY = dy * 16 + localY;
                                int globalZ = dz * 16 + localZ;

                                int sourceIndex = (globalY * 1024) + (globalZ * 32) + globalX;
                                int localIndex = (localY * 256) + (localZ * 16) + localX;

                                int hytaleBlockId = hyPalette.getBlock(sourceIndex);
                                int minecraftBlockId = MappingRegistry.INSTANCE.getBlockMapper().getMapping(hytaleBlockId);

                                mcPalette.set(localIndex, minecraftBlockId);
                            }
                        }
                    }

                    ChunkSection section = new ChunkSection(mcPalette.getNonAirBlocks(), mcPalette);
                    chunkBuffer.addChunkSection(chunkCoords, newY, section);
                }
            }
        }
    }

    //public static void testGPU(KernelContext ctx) {}

    public int getBitsPerEntry(int size) {
        int bitsPerEntry = 0x1f - Integer.numberOfLeadingZeros(size);
        return Math.max(4, bitsPerEntry);
    }
}
