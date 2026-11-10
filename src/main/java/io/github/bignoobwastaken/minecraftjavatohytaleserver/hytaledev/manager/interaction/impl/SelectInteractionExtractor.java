package io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.impl;

import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.SelectInteraction;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.InteractionContext;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.InteractionExtractor;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.InteractionManager;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.util.reflection.FieldAccessor;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.util.reflection.Reflections;

public class SelectInteractionExtractor implements InteractionExtractor<SelectInteraction> {
    private static final FieldAccessor<String> HIT_ENTITY_FIELD = Reflections.getField(SelectInteraction.class, String.class);

    @Override
    public Interaction extract(SelectInteraction interaction, InteractionContext context) {
        String hitEntityRootId = HIT_ENTITY_FIELD.get(interaction);
        if (hitEntityRootId == null) return null;

        RootInteraction hitEntityRoot = RootInteraction.getAssetMap().getAsset(hitEntityRootId);
        if (hitEntityRoot == null) return null;

        return InteractionManager.getInteractionFromRoot(hitEntityRoot);
    }
}
