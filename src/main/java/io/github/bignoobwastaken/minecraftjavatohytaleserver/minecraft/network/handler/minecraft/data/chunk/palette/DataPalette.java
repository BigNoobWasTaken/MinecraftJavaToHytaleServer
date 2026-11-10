package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.chunk.palette;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

public abstract class DataPalette {
    private final int bitsPerEntry;
    private final int[] blocksData = new int[4096];
    @Getter
    private short nonAirBlocks = 0;

    public DataPalette(int bitsPerEntry) {
        this.bitsPerEntry = bitsPerEntry;
    }

    protected abstract int getPaletteIndex(int blockId);
    protected abstract int getPaletteBlockId(int index);
    protected abstract void innerSerialize(ByteBuf buffer);

    public void set(int index, int blockId) {
        if (blockId != 0) nonAirBlocks++;
        blocksData[index] = bitsPerEntry == 15 ? blockId : getPaletteIndex(blockId);
    }

    public void serialize(ByteBuf buffer) {
        buffer.writeByte(bitsPerEntry);
        innerSerialize(buffer);

        if (bitsPerEntry == 0) {
            return;
        }

        int entriesPerLong = 64 / bitsPerEntry;
        int numLongs = (blocksData.length + entriesPerLong - 1) / entriesPerLong;

        long[] dataArray = new long[numLongs];

        for (int i = 0; i < blocksData.length; i++) {
            int longIndex = i / entriesPerLong;
            int bitIndex = (i % entriesPerLong) * bitsPerEntry;

            dataArray[longIndex] |= ((long) blocksData[i]) << bitIndex;
        }

        for (long value : dataArray) {
            buffer.writeLong(value);
        }
    }

}
