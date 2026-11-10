package io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction;


import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interaction.PlayInteractionFor;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ChainingInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ChargingInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.UseBlockInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.*;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.DamageEntityInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.api.ClientConnection;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.impl.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import testhytalecrossplayproject.minecraftjavatohytale.hytaledev.manager.interaction.impl.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InteractionManager {
    public static final InteractionManager INSTANCE = new InteractionManager();

    private static final int MAX_EXTRACTOR_ITERATIONS = 50;
    private static final int INTERACTION_RADIUS = 96;

    private final Map<Class<? extends Interaction>, InteractionExtractor<? extends Interaction>> interactionExtractors;

    {
        interactionExtractors = new HashMap<>();
        this.addInteractionExtractor(ChainingInteraction.class, new ChainingInteractionExtractor());
        this.addInteractionExtractor(ChargingInteraction.class, new ChargingInteractionExtractor());
        this.addInteractionExtractor(ParallelInteraction.class, new ParallelInteractionExtractor());
        this.addInteractionExtractor(ReplaceInteraction.class, new ReplaceInteractionExtractor());
        this.addInteractionExtractor(SelectInteraction.class, new SelectInteractionExtractor());
        this.addInteractionExtractor(SimpleInteraction.class, new SimpleInteractionExtractor());
        this.addInteractionExtractor(UseBlockInteraction.class, new UseBlockInteractionExtractor());
        this.addInteractionExtractor(ConditionInteraction.class, new ConditionInteractionExtractor());
        this.addInteractionExtractor(SerialInteraction.class, new SerialInteractionExtractor());
    }

    private InteractionManager() {}

    private <T extends Interaction> void addInteractionExtractor(Class<T> intercationClass, InteractionExtractor<T> interactionExtractor) {
        interactionExtractors.put(intercationClass, interactionExtractor);
    }

    public void playInteraction(ClientConnection connection, String id, InteractionType type) {
        int interactionId = Interaction.getAssetMap().getIndex(id);
        playInteraction(connection, interactionId, type);
    }

    public void playInteraction(ClientConnection connection, int id, InteractionType type) {
        Ref<EntityStore> entityRef = connection.getPlayerRef().getReference();
        assert entityRef != null;
        Store<EntityStore> store = entityRef.getStore();

        EntityStore entityStore = store.getExternalData();
        var world = entityStore.getWorld();

        world.execute(() -> {
            TransformComponent transform = store.getComponent(entityRef, TransformComponent.getComponentType());
            NetworkId networkIdComp = store.getComponent(entityRef, NetworkId.getComponentType());

            if (transform == null || networkIdComp == null) return;

            PlayInteractionFor animPacket = new PlayInteractionFor(
                    networkIdComp.getId(),
                    connection.getClientChainId().getAndIncrement(),
                    null,
                    0,
                    id,
                    null,
                    type,
                    false
            );

            SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource =
                    store.getResource(EntityModule.get().getPlayerSpatialResourceType());

            ObjectList<Ref<EntityStore>> nearbyPlayers = new ObjectArrayList<>();
            playerSpatialResource.getSpatialStructure().collect(transform.getPosition(), INTERACTION_RADIUS, nearbyPlayers);

            for (Ref<EntityStore> targetRef : nearbyPlayers) {
                if (targetRef.equals(entityRef)) continue;

                PlayerRef targetPlayerComp = store.getComponent(targetRef, PlayerRef.getComponentType());
                if (targetPlayerComp == null) return;

                targetPlayerComp.getPacketHandler().writeNoCache(animPacket);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <T extends Interaction> Interaction extractInteraction(T interaction, InteractionContext context) {
        InteractionExtractor<T> extractor = (InteractionExtractor<T>) interactionExtractors.get(interaction.getClass());
        if (extractor == null)
            return null;
        return extractor.extract(interaction, context);
    }

    public InteractionExtractorResponse extract(InteractionContext context, Interaction interaction) {
        List<String> interactionPath = new ArrayList<>();
        interactionPath.add(interaction.getId());

        for (int i = 0; !(interaction instanceof DamageEntityInteraction) && i < MAX_EXTRACTOR_ITERATIONS; i++) {
            interaction = extractInteraction(interaction, context);
            if (interaction == null) break;

            interactionPath.add(interaction.getId());
        }

        return new InteractionExtractorResponse(interaction, interactionPath);
    }

    public static Interaction getInteractionFromRoot(RootInteraction root) {
        if (root == null) return null;

        String[] interactionIds = root.getInteractionIds();
        if (interactionIds == null || interactionIds.length == 0) return null;

        return Interaction.getAssetMap().getAsset(interactionIds[0]);
    }
}
