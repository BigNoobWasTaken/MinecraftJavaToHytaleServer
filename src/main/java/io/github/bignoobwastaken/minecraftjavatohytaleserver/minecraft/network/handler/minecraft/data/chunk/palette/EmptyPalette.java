package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.chunk.palette;

import io.netty.buffer.ByteBuf;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;

public class EmptyPalette extends DataPalette {
    private final int blockId;

    public EmptyPalette(int blockId) {
        super(0);
        this.blockId = blockId;
    }

    @Override
    public int getPaletteIndex(int blockId) {
        return 0;
    }

    @Override
    public int getPaletteBlockId(int index) {
        return blockId;
    }

    @Override
    protected void innerSerialize(ByteBuf buffer) {
        MinecraftTypes.writeVarInt(buffer, this.blockId);
    }
}
