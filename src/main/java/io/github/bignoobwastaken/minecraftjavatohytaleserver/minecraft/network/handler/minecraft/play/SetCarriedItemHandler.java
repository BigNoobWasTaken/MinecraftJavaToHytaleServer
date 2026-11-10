package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.play;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundSetCarriedItemPacket;

public class SetCarriedItemHandler implements PacketHandler<ServerboundSetCarriedItemPacket> {
    @Override
    public void handle(ServerboundSetCarriedItemPacket packet, MinecraftClientConnection connection) {
        PlayerRef playerRef = connection.getPlayerRef();
        if (playerRef == null) return;

        Ref<EntityStore> entityRef = playerRef.getReference();
        assert entityRef != null;
        Store<EntityStore> store = entityRef.getStore();
        store.getExternalData().getWorld().execute(() -> {
            LivingEntity entity = store.getComponent(entityRef, Player.getComponentType());
            if (entity == null) return;

            InventoryComponent.Hotbar hotbar = store.getComponent(entityRef, InventoryComponent.Hotbar.getComponentType());
            if (hotbar == null) return;

            hotbar.setActiveSlot((byte) packet.getSlot(), entityRef, store);
        });
    }
}
