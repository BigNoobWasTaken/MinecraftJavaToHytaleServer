package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.chunk;

import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.api.chunk.ChunkCoordIntPair;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.api.chunk.ChunkImpl;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;

import java.util.Arrays;

@Getter
public class Chunk extends ChunkImpl<ChunkSection> {
    private static final int SECTION_COUNT = 24;
    private static final int MAX_HEIGHT = 319;
    private static final int MIN_HEIGHT = -64;
    private static final int START_EMPTY_SECTIONS = 4;

    public Chunk(ChunkCoordIntPair chunkCoords) {
        super(chunkCoords, new ChunkSection[SECTION_COUNT]);

        Arrays.fill(this.getChunkSections(), ChunkSection.EMPTY_AIR);
    }

    public byte[] getChunkSectionData() {
        ByteBuf byteBuf = Unpooled.buffer();

        for (ChunkSection section : this.getChunkSections()) {
            section.serialize(byteBuf);
        }

        byte[] result = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(result);
        byteBuf.release();

        return result;
    }
}
