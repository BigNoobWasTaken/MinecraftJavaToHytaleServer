package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.play;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.inventory.DropItemStack;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.manager.blockbreak.BlockBreakManager;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerAction;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundPlayerActionPacket;

public class PlayerActionHandler implements PacketHandler<ServerboundPlayerActionPacket> {
    @Override
    public void handle(ServerboundPlayerActionPacket packet, MinecraftClientConnection connection) {
        if (packet.getAction().name().endsWith("DIGGING"))
            BlockBreakManager.INSTANCE.handle(connection, packet.getPosition(), packet.getAction(), packet.getSequence());
        else if (packet.getAction().name().startsWith("DROP"))
            handleItemDrop(packet.getAction(), connection);
    }

    private static void handleItemDrop(PlayerAction type, MinecraftClientConnection connection) {
        Ref<EntityStore> ref = connection.getPlayerRef().getReference();
        assert ref != null;
        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();

        world.execute(() -> {
            ItemStack hand = InventoryComponent.getItemInHand(store, ref);
            if (hand == null || hand.isEmpty()) return;

            InventoryComponent.Hotbar hotbarComponent = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
            if (hotbarComponent == null) return;

            int quantity = type.equals(PlayerAction.DROP_ITEM) ? 1 : hand.getQuantity();
            int slotId = hotbarComponent.getActiveSlot();

            DropItemStack packet = new DropItemStack(InventoryComponent.HOTBAR_SECTION_ID, slotId, quantity);
            connection.getHytaleChannel().sendPacket(packet);
        });
    }
}
