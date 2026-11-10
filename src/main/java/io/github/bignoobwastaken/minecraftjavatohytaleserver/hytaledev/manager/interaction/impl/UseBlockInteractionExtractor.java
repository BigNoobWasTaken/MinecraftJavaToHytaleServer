package io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.impl;

import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.UseBlockInteraction;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.InteractionContext;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.InteractionExtractor;

public class UseBlockInteractionExtractor implements InteractionExtractor<UseBlockInteraction> {
    @Override
    public Interaction extract(UseBlockInteraction interaction, InteractionContext context) {
        String failedId = SimpleInteractionExtractor.FAILED_FIELD.get(interaction);
        return Interaction.getAssetMap().getAsset(failedId);
    }
}
