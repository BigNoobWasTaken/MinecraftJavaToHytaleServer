package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.manager.use;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockRotation;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFace;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.asset.type.gameplay.WorldConfig;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.modules.interaction.BlockPlaceUtils;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.util.VectorUtil;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.protocol.data.game.entity.object.Direction;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Animation;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundAnimatePacket;
import org.joml.Vector3d;

public class UseManager {
    public static final UseManager INSTANCE = new UseManager();

    private UseManager() {}

    public boolean harvestBlock(MinecraftClientConnection connection, Vector3i targetBlock, World world, Store<EntityStore> store, Ref<EntityStore> entityRef) {
        ChunkStore chunkStore = world.getChunkStore();
        Store<ChunkStore> chunkStoreStore = chunkStore.getStore();
        int x = targetBlock.getX();
        int y = targetBlock.getY();
        int z = targetBlock.getZ();
        Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReferenceAtBlock(x, y, z);
        if (sectionRef != null && sectionRef.isValid()) {
            BlockSection blockSection = sectionRef.getStore().getComponent(sectionRef, BlockSection.getComponentType());
            if (blockSection == null) return false;

            int blockId = blockSection.get(x, y, z);
            BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
            if (blockType == null) return false;

            GameplayConfig gameplayConfig = world.getGameplayConfig();
            WorldConfig worldConfig = gameplayConfig.getWorldConfig();

            if (!worldConfig.isBlockGatheringAllowed()) return false;
            if (!BlockHarvestUtils.shouldPickupByInteraction(blockType)) return false;

            ClientboundAnimatePacket swingArmPacket = new ClientboundAnimatePacket(connection.getNetworkId(), Animation.CRITICAL_HIT);
            connection.getSession().send(swingArmPacket);

            org.joml.Vector3i jomlTargetBlock = new org.joml.Vector3i(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
            int filler = blockSection.getFiller(x, y, z);
            BlockHarvestUtils.performPickupByInteraction(entityRef, jomlTargetBlock, blockType, filler, sectionRef, blockSection, store, chunkStoreStore);
        }

        return true;
    }

    @SuppressWarnings("removal")
    public void placeBlock(MinecraftClientConnection connection, Vector3i targetBlock, Direction face, World world, Store<EntityStore> store, Ref<EntityStore> entityRef) {
        ChunkStore chunkStore = world.getChunkStore();
        Store<ChunkStore> chunkStoreStore = chunkStore.getStore();

        Vector3i placementPosition = getPlacementPosition(targetBlock, face);

        Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReferenceAtBlock(placementPosition.getX(), placementPosition.getY(), placementPosition.getZ());

        if (sectionRef == null || !sectionRef.isValid()) {
            return;
        }

        Player playerComponent = store.getComponent(entityRef, Player.getComponentType());
        TransformComponent transformComponent = store.getComponent(entityRef, TransformComponent.getComponentType());

        if (playerComponent == null) {
            return;
        }

        com.hypixel.hytale.server.core.inventory.Inventory inventory = playerComponent.getInventory();
        if (inventory == null) {
            return;
        }

        byte heldSlot = inventory.getActiveHotbarSlot();
        ItemContainer heldItemContainer = inventory.getHotbar();
        if (heldItemContainer == null) {
            return;
        }

        ItemStack heldItemStack = heldItemContainer.getItemStack(heldSlot);
        if (heldItemStack == null) {
            return;
        }

        if (transformComponent != null) {
            Vector3d playerPosition = transformComponent.getPosition();
            int playerBlockX = (int) Math.floor(playerPosition.x);
            int playerBlockY = (int) Math.floor(playerPosition.y);
            int playerBlockZ = (int) Math.floor(playerPosition.z);

            if ((playerBlockX == placementPosition.getX() &&
                    playerBlockZ == placementPosition.getZ() &&
                    (playerBlockY == placementPosition.getY() ||
                            playerBlockY + 1 == placementPosition.getY()))) {
                return;
            }
        }

        if (transformComponent != null && playerComponent.getGameMode() != GameMode.Creative) {
            Vector3d playerPosition = transformComponent.getPosition();
            Vector3d blockCenter = new Vector3d(
                    (double) placementPosition.getX() + 0.5,
                    (double) placementPosition.getY() + 0.5,
                    (double) placementPosition.getZ() + 0.5
            );

            if (VectorUtil.distanceSquaredTo(playerPosition, blockCenter) > 36.0) {
                return;
            }
        }

        String blockTypeKey = heldItemStack.getBlockKey();
        if (blockTypeKey == null) return;

        BlockType blockType = BlockType.getAssetMap().getAsset(blockTypeKey);
        if (blockType == null) return;

        if (placementPosition.getY() < 0 || placementPosition.getY() >= 320) {
            return;
        }

        BlockFace blockFace = BlockFace.values()[Math.min(face.ordinal(), BlockFace.values().length - 1)];

        BlockRotation blockRotation = new BlockRotation(
                Rotation.None,
                Rotation.None,
                Rotation.None
        );

        BlockPlaceUtils.placeBlock(
                entityRef,
                heldItemStack,
                blockTypeKey,
                heldItemContainer,
                new org.joml.Vector3i(blockFace.getDirection()),
                new org.joml.Vector3i(
                        placementPosition.getX(),
                        placementPosition.getY(),
                        placementPosition.getZ()
                ),
                blockRotation,
                heldSlot,
                true,
                sectionRef,
                chunkStoreStore,
                store,
                false,
                false,
                false
        );
    }

    private static Vector3i getPlacementPosition(Vector3i clickedBlock, Direction face) {
        int x = clickedBlock.getX();
        int y = clickedBlock.getY();
        int z = clickedBlock.getZ();

        switch (face) {
            case DOWN -> y--;
            case UP -> y++;
            case NORTH -> z--;
            case SOUTH -> z++;
            case WEST -> x--;
            case EAST -> x++;
        }

        return Vector3i.from(x, y, z);
    }
}