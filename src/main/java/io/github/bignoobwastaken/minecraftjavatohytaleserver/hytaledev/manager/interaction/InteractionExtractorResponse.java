package io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction;

import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;

import java.util.List;

public record InteractionExtractorResponse(Interaction interaction, List<String> interactionPath) {
}
