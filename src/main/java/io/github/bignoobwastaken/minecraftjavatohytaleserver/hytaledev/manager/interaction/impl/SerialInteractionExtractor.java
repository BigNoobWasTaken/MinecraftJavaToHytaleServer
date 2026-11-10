package io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.impl;

import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.SerialInteraction;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.InteractionContext;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.InteractionExtractor;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.util.reflection.FieldAccessor;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.util.reflection.Reflections;

public class SerialInteractionExtractor implements InteractionExtractor<SerialInteraction> {
    private static final FieldAccessor<String[]> INTERACTIONS_FIELD = Reflections.getField(SerialInteraction.class, String[].class);

    @Override
    public Interaction extract(SerialInteraction interaction, InteractionContext context) {
        String[] interactions = INTERACTIONS_FIELD.get(interaction);
        return Interaction.getAssetMap().getAsset(interactions[1]);
    }
}
