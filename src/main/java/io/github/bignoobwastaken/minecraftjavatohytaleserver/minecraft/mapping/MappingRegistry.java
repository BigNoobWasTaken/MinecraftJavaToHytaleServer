package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.mapping;

import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.mapping.managers.BlockMapper;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.mapping.managers.EntityMapper;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.mapping.managers.ItemMapper;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.mapping.managers.SoundMapper;
import lombok.Getter;

@Getter
public class MappingRegistry {
    public static final MappingRegistry INSTANCE = new MappingRegistry();

    private final BlockMapper blockMapper;
    private final ItemMapper itemMapper;
    private final EntityMapper entityMapper;
    private final SoundMapper soundMapper;

    {
        this.blockMapper = new BlockMapper();
        this.itemMapper = new ItemMapper();
        this.entityMapper = new EntityMapper();
        this.soundMapper = new SoundMapper();
        this.blockMapper.loadMappings();
        this.itemMapper.loadMappings();
        this.entityMapper.loadMappings();
        this.soundMapper.loadMappings();
    }

    private MappingRegistry() {}
}