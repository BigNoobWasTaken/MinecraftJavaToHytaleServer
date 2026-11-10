package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.manager.blockbreak;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.mapping.MappingRegistry;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.InteractionContext;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.InteractionExtractorResponse;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.InteractionManager;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.protocol.data.game.entity.attribute.Attribute;
import org.geysermc.mcprotocollib.protocol.data.game.entity.attribute.AttributeType;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerAction;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockChangeEntry;
import org.geysermc.mcprotocollib.protocol.data.game.level.event.BreakBlockEventData;
import org.geysermc.mcprotocollib.protocol.data.game.level.event.LevelEventType;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundUpdateAttributesPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundBlockChangedAckPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundBlockUpdatePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundLevelEventPacket;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class BlockBreakManager {
    public static BlockBreakManager INSTANCE = new BlockBreakManager();


    private static final long DEFAULT_DAMAGE_INTERVAL_MS = 350;
    private static final long COOLDOWN_MS = 200;

    private BlockBreakManager() {}

    public void handle(MinecraftClientConnection connection, Vector3i target, PlayerAction status, int sequence) {
        switch (status) {
            case START_DIGGING -> handleStartDigging(connection, target, sequence);
            case CANCEL_DIGGING -> cancelBreaking(connection);
        }
    }

    private void handleStartDigging(MinecraftClientConnection connection, Vector3i targetBlock, int sequence) {
        BlockBreakTracker existing = connection.getBlockBreakTracker();

        if (existing != null) {
            existing.cancel();
        }

        BlockBreakTracker tracker = new BlockBreakTracker(targetBlock, sequence);
        connection.setBlockBreakTracker(tracker);

        PlayerRef playerRefWrapper = connection.getPlayerRef();
        Ref<EntityStore> entityRef = playerRefWrapper.getReference();
        assert entityRef != null;
        Store<EntityStore> store = entityRef.getStore();
        EntityStore entityStore = store.getExternalData();
        World world = entityStore.getWorld();

        world.execute(() -> {
            long delay = 0;
            long currentTime = System.currentTimeMillis();
            if (currentTime - connection.getLastBreakTime() < COOLDOWN_MS) {
                delay = COOLDOWN_MS;
            }

            long cooldown = getItemInHandBreakCooldown(store, entityRef);
            if (cooldown == 0) return;

            tracker.damageTask = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
                world.execute(() -> {
                    applyBlockDamage(connection, tracker, store, entityRef, world);
                });
            }, delay, cooldown, TimeUnit.MILLISECONDS);
        });
    }

    private long getItemInHandBreakCooldown(Store<EntityStore> store, Ref<EntityStore> entityRef) {
        ItemStack hand = InventoryComponent.getItemInHand(store, entityRef);
        if (hand == null || hand.isEmpty()) return DEFAULT_DAMAGE_INTERVAL_MS;

        String interactionId = hand.getItem().getInteractions().get(InteractionType.Primary);
        if (interactionId == null) return DEFAULT_DAMAGE_INTERVAL_MS;

        RootInteraction interaction = RootInteraction.getAssetMap().getAsset(interactionId);
        if (interaction == null || interaction.getCooldown() == null) return DEFAULT_DAMAGE_INTERVAL_MS;

        return (long) interaction.getCooldown().cooldown;
    }

    private void applyBlockDamage(MinecraftClientConnection connection, BlockBreakTracker tracker,
                                  Store<EntityStore> store, Ref<EntityStore> entityRef, World world) {
        if (!entityRef.isValid()) {
            cancelBreaking(connection);
            return;
        }

        org.joml.Vector3i targetBlock = new org.joml.Vector3i(
                tracker.position.getX(),
                tracker.position.getY(),
                tracker.position.getZ()
        );

        try {
            LivingEntity playerEntity = store.getComponent(entityRef, Player.getComponentType());
            if (playerEntity == null) {
                cancelBreaking(connection);
                return;
            }

            ChunkStore chunkStore = world.getChunkStore();
            Store<ChunkStore> chunkStoreStore = chunkStore.getStore();

            int x = targetBlock.x;
            int y = targetBlock.y;
            int z = targetBlock.z;
            Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReferenceAtBlock(x, y, z);

            if (sectionRef == null || !sectionRef.isValid()) {
                cancelBreaking(connection);
                return;
            }

            BlockSection blockSection = sectionRef.getStore().getComponent(sectionRef, BlockSection.getComponentType());
            if (blockSection == null) {
                cancelBreaking(connection);
                return;
            }

            BlockChangeEntry blockChangeEntry = new BlockChangeEntry(tracker.position, 0);
            ClientboundBlockUpdatePacket blockUpdatePacket = new ClientboundBlockUpdatePacket(blockChangeEntry);

            int blockId = blockSection.get(x, y, z);
            BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
            if (blockType == null || blockType.getId().equals("Empty")) {
                connection.getSession().send(blockUpdatePacket);
                cancelBreaking(connection);
                return;
            }

            ItemStack hand = InventoryComponent.getItemInHand(store, entityRef);

            Item item = hand != null ? hand.getItem() : null;
            String itemInHandId = (hand != null && !hand.isEmpty()) ? hand.getItemId() : null;

            Map<String, String> interactionVars = item != null ? item.getInteractionVars() : new HashMap<>();

            String interactionId = item != null ? item.getInteractions().get(InteractionType.Primary) : "*Empty_Interactions_Primary";
            if (interactionId == null) return;

            RootInteraction rootInteraction = RootInteraction.getAssetMap().getAsset(interactionId);
            if (rootInteraction == null) return;

            Interaction mainInteraction = InteractionManager.getInteractionFromRoot(rootInteraction);

            InteractionExtractorResponse interactionResponse = InteractionManager.INSTANCE.extract(new InteractionContext(connection, interactionVars), mainInteraction);
            interactionResponse.interactionPath().forEach(i -> InteractionManager.INSTANCE.playInteraction(connection, i, InteractionType.Primary));

            connection.setLastBreakTime(System.currentTimeMillis());
            boolean destroyed = BlockHarvestUtils.performBlockDamage(
                    entityRef,
                    null,
                    targetBlock,
                    hand,
                    null,
                    itemInHandId,
                    false,
                    1.0F,
                    0,
                    false,
                    sectionRef,
                    store,
                    chunkStoreStore
            );

            if (destroyed) {
                ClientboundBlockChangedAckPacket ackPacket = new ClientboundBlockChangedAckPacket(tracker.sequence);
                int blockStateId = MappingRegistry.INSTANCE.getBlockMapper().getMapping(BlockType.getAssetMap().getIndex(blockType.getId()));
                ClientboundLevelEventPacket levelEventPacket = new ClientboundLevelEventPacket(LevelEventType.from(2001), tracker.position, new BreakBlockEventData(blockStateId));

                connection.getSession().send(ackPacket);
                connection.getSession().send(blockUpdatePacket);
                connection.getSession().send(levelEventPacket);

                tracker.cancel();
                connection.setBlockBreakTracker(null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            cancelBreaking(connection);
        }
    }

    public void changePlayerMiningSpeed(MinecraftClientConnection connection) {
        List<Attribute> properties = new ArrayList<>();
        properties.add(new Attribute(AttributeType.Builtin.BLOCK_BREAK_SPEED, 0.0));

        ClientboundUpdateAttributesPacket packet = new ClientboundUpdateAttributesPacket(connection.getNetworkId(), properties);
        connection.getSession().send(packet);
    }

    private void cancelBreaking(MinecraftClientConnection connection) {
        if (connection.getBlockBreakTracker() != null) {
            connection.getBlockBreakTracker().cancel();
            connection.setBlockBreakTracker(null);
        }
    }
}
