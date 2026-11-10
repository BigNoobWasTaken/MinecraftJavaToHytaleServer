package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.chunk;

import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.api.chunk.ChunkSectionImpl;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.chunk.palette.DataPalette;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.chunk.palette.EmptyPalette;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

@Getter
public class ChunkSection implements ChunkSectionImpl {
    public static ChunkSection EMPTY_AIR = new ChunkSection((short) 0, new EmptyPalette(0));

    private final short nonAirBlocks;
    private final DataPalette blockPalette;
    private final DataPalette biomePalette;

    public ChunkSection(short nonAirBlocks, DataPalette blockPalette) {
        this.nonAirBlocks = nonAirBlocks;
        this.blockPalette = blockPalette;
        this.biomePalette = new EmptyPalette(1);
    }

    @Override
    public void serialize(ByteBuf buffer) {
        buffer.writeShort(nonAirBlocks);
        blockPalette.serialize(buffer);
        biomePalette.serialize(buffer);
    }
}
