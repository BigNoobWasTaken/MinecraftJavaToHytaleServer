package io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.api.chunk;

import lombok.Getter;

@Getter
public abstract class ChunkImpl<S extends ChunkSectionImpl> {
    private static final int SECTION_COUNT = 24;
    private static final int MAX_HEIGHT = 319;
    private static final int MIN_HEIGHT = -64;
    private static final int START_EMPTY_SECTIONS = 4;

    private final ChunkCoordIntPair chunkCoords;
    private final S[] chunkSections;

    protected ChunkImpl(ChunkCoordIntPair chunkCoords, S[] chunkSections) {
        this.chunkCoords = chunkCoords;
        this.chunkSections = chunkSections;
    }

    public final void addChunkSection(S section, int y) {
        chunkSections[y+START_EMPTY_SECTIONS] = section;
    }

    public static ChunkCoordIntPair getChunkCoords(double x, double z) {
        return new ChunkCoordIntPair((int) Math.floor(x) >> 4, (int) Math.floor(z) >> 4);
    }
}
