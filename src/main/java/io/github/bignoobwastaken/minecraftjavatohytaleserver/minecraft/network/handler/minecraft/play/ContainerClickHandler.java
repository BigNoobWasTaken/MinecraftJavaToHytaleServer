package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.play;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.manager.gui.GuiManager;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.manager.inventory.InventoryManager;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundContainerClickPacket;

public class ContainerClickHandler implements PacketHandler<ServerboundContainerClickPacket> {
    @Override
    public void handle(ServerboundContainerClickPacket packet, MinecraftClientConnection connection) {
        PlayerRef playerRef = connection.getPlayerRef();
        if (playerRef == null) return;

        Ref<EntityStore> entityRef = playerRef.getReference();
        assert entityRef != null;
        Store<EntityStore> store = entityRef.getStore();
        store.getExternalData().getWorld().execute(() -> {
            LivingEntity entity = store.getComponent(entityRef, Player.getComponentType());
            if (entity == null) return;

            if (packet.getContainerId() != 0) {
                GuiManager.INSTANCE.clickGui(connection, store, entityRef, packet.getSlot(), packet.getAction(), packet.getParam());
            } else {
                InventoryManager.INSTANCE.handleClick(connection, store, entityRef, (short) packet.getSlot(), packet.getParam(), packet.getAction());
            }
        });
    }
}
