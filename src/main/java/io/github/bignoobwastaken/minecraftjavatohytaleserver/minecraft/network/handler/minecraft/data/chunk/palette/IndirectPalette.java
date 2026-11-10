package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.chunk.palette;

import io.netty.buffer.ByteBuf;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;

import java.util.*;

public class IndirectPalette extends DataPalette {
    private final List<Integer> internalToExternal = new LinkedList<>();
    private final Map<Integer, Integer> externalToInternal = new HashMap<>();

    public IndirectPalette(int bitsPerEntry) {
        super(bitsPerEntry);
    }

    @Override
    public int getPaletteIndex(int blockId) {
        Integer index = externalToInternal.get(blockId);
        if (index == null) {
            index = internalToExternal.size();
            internalToExternal.add(blockId);
            externalToInternal.put(blockId, index);
        }

        return index;
    }

    @Override
    public int getPaletteBlockId(int index) {
        return internalToExternal.get(index);
    }

    @Override
    protected void innerSerialize(ByteBuf buffer) {
        MinecraftTypes.writeVarInt(buffer, internalToExternal.size());
        internalToExternal.forEach(external -> MinecraftTypes.writeVarInt(buffer, external));
    }
}
