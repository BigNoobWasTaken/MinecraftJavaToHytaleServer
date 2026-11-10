package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.play;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionChainData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.interaction.ForcedLocalClientChain;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.manager.use.UseManager;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundUseItemOnPacket;

public class UseItemOnHandler implements PacketHandler<ServerboundUseItemOnPacket> {
    @Override
    public void handle(ServerboundUseItemOnPacket packet, MinecraftClientConnection connection) {
        if (packet.getHand() == Hand.OFF_HAND) return;
        PlayerRef playerRefWrapper = connection.getPlayerRef();
        Ref<EntityStore> entityRef = playerRefWrapper.getReference();
        assert entityRef != null;
        Store<EntityStore> store = entityRef.getStore();
        EntityStore entityStore = store.getExternalData();
        World world = entityStore.getWorld();
        BlockPosition blockPosition = new BlockPosition(packet.getPosition().getX(), packet.getPosition().getY(), packet.getPosition().getZ());
        world.execute(() -> {
            ChunkStore chunkStore = world.getChunkStore();
            Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReferenceAtBlock(blockPosition.x, blockPosition.y, blockPosition.z);
            assert sectionRef != null;
            BlockSection blockSection = sectionRef.getStore().getComponent(sectionRef, BlockSection.getComponentType());
            assert blockSection != null;
            int blockId = blockSection.get(blockPosition.x, blockPosition.y, blockPosition.z);

            BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
            String interactionId = getInteractionFromBlockType(blockType);
            if (interactionId != null) {
                RootInteraction interaction = RootInteraction.getAssetMap().getAsset(interactionId);

                InteractionManager interactionManager = store.getComponent(entityRef, InteractionModule.get().getInteractionManagerComponent());
                assert interactionManager != null;
                InteractionContext context = InteractionContext.forInteraction(interactionManager, entityRef, InteractionType.Use, store);
                context.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK, blockPosition);
                context.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK_RAW, blockPosition);
                ForcedLocalClientChain chain = initChain(interaction, context, blockPosition);
                interactionManager.queueExecuteChain(chain);
            } else {
                boolean harvest = UseManager.INSTANCE.harvestBlock(connection, packet.getPosition(), world, store, entityRef);
                if (harvest) return;
                UseManager.INSTANCE.placeBlock(connection, packet.getPosition(), packet.getFace(), world, store, entityRef);
            }
        });
    }

    private static String getInteractionFromBlockType(BlockType blockType) {
        InteractionType[] types = new InteractionType[]{InteractionType.Use, InteractionType.Pickup, InteractionType.Pick};

        for (InteractionType type : types) {
            String interactionId = blockType.getInteractions().get(type);
            if (interactionId != null) return interactionId;
        }

        return null;
    }

    private static ForcedLocalClientChain initChain(RootInteraction root, InteractionContext context, BlockPosition blockPosition) {
        InteractionChainData data = new InteractionChainData(-1, UUIDUtil.EMPTY_UUID, null, null, blockPosition, Integer.MIN_VALUE, null);
        return new ForcedLocalClientChain(InteractionType.Use, context, data, root, null, false);
    }
}
