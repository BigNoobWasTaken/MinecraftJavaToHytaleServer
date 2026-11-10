package io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.data;

import io.netty.buffer.ByteBuf;

public class HyEmptyPalette implements HyPalette{
    @Override
    public int getPaletteSize() {
        return 0;
    }

    @Override
    public void deserialize(ByteBuf buffer) {

    }

    @Override
    public int getBlock(int index) {
        return 0;
    }
}
