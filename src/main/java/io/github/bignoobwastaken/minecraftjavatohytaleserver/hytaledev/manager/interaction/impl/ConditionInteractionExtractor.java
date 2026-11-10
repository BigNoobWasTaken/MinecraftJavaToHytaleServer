package io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.impl;

import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.ConditionInteraction;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.InteractionContext;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.InteractionExtractor;

public class ConditionInteractionExtractor implements InteractionExtractor<ConditionInteraction> {
    @Override
    public Interaction extract(ConditionInteraction interaction, InteractionContext context) {
        String failedId = SimpleInteractionExtractor.NEXT_FIELD.get(interaction);
        return Interaction.getAssetMap().getAsset(failedId);
    }
}
