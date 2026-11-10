package io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.api.chunk;

import io.netty.buffer.ByteBuf;

public interface ChunkSectionImpl {
    void serialize(ByteBuf buffer);
}
