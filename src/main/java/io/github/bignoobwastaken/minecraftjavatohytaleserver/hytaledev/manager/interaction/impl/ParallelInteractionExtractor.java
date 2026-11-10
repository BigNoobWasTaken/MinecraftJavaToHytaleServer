package io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.impl;

import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.ParallelInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.SelectInteraction;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.InteractionContext;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.InteractionExtractor;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.InteractionManager;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.util.reflection.FieldAccessor;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.util.reflection.Reflections;

public class ParallelInteractionExtractor implements InteractionExtractor<ParallelInteraction> {
    private static final FieldAccessor<String[]> INTERACTIONS_FIELD = Reflections.getField(ParallelInteraction.class, String[].class);

    @Override
    public Interaction extract(ParallelInteraction interaction, InteractionContext context) {
        String[] parallelInteractions = INTERACTIONS_FIELD.get(interaction);
        if (parallelInteractions == null) return null;

        for (String parallelId : parallelInteractions) {
            RootInteraction parallelRoot = RootInteraction.getAssetMap().getAsset(parallelId);
            if (parallelRoot == null) continue;

            Interaction parallelInner = InteractionManager.getInteractionFromRoot(parallelRoot);
            if (parallelInner instanceof SelectInteraction) return parallelInner;
        }

        return null;
    }
}
