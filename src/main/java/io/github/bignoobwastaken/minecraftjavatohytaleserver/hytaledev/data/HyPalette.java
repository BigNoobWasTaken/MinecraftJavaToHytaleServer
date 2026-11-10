package io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.data;

import com.hypixel.hytale.protocol.packets.world.PaletteType;
import io.netty.buffer.ByteBuf;

public interface HyPalette {
    static HyPalette create(PaletteType type) {
        switch (type) {
            case Empty: return new HyEmptyPalette();
            case HalfByte: return new HyHalfBytePalette();
            case Byte: return new HyBytePalette();
            case Short:
                System.out.println("Short palette received");
                break;
        }
        return null;
    }

    int getPaletteSize();
    void deserialize(ByteBuf buffer);
    int getBlock(int index);
}
