package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.inventory;

import com.hypixel.hytale.protocol.InventorySection;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.protocol.packets.inventory.UpdatePlayerInventory;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.item.ItemStackUtil;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.manager.inventory.InventoryCursor;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.UseCooldown;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;

public class UpdatePlayerInventoryHandler implements PacketHandler<UpdatePlayerInventory> {
    @Override
    public void handle(UpdatePlayerInventory packet, MinecraftClientConnection connection) {
        if (packet.hotbar != null) {
            connection.setCachedHotbar(packet.hotbar);
        }
        if (packet.storage != null) {
            connection.setCachedStorage(packet.storage);
        }
        if (packet.armor != null) {
            connection.setCachedArmor(packet.armor);
        }

        InventorySection hotbar = connection.getCachedHotbar();
        InventorySection storage = connection.getCachedStorage();
        InventorySection armor = connection.getCachedArmor();

        ItemStack[] inventoryItems = new ItemStack[46];
        int index = 0;
        // crafting slots
        for (int i = 0; i < 5; i++) {
            inventoryItems[index++] = ItemStackUtil.EMPTY;
        }

        // armor
        for (int i = 0; i < 4; i++) {
            inventoryItems[index++] = getItem(armor, i);
        }

        // storage
        for (int i = 0; i < 27; i++) {
            inventoryItems[index++] = getItem(storage, i);
        }

        // hotbar
        for (int i = 0; i < 9; i++) {
            ItemStack item = getItem(hotbar, i);
            item.getDataComponentsPatch().put(DataComponentTypes.USE_COOLDOWN, new UseCooldown(1, Key.key("slot:" + i)));
            inventoryItems[index++] = item;
        }

        // offhand
        inventoryItems[index++] = (ItemStackUtil.EMPTY);

        InventoryCursor cursor = connection.getCursor();
        ItemStack cursorItem = cursor.heldItem != null ? ItemStackUtil.fromHytale(cursor.heldItem.toPacket()) : ItemStackUtil.EMPTY;
        int cursorSlotId = cursor.itemSlot;
        if (cursor.heldItem != null) inventoryItems[cursorSlotId] = ItemStackUtil.EMPTY;

        ClientboundContainerSetContentPacket inventoryPacket = new ClientboundContainerSetContentPacket(0, 0, inventoryItems, cursorItem);
        connection.getSession().send(inventoryPacket);
    }

    private static ItemStack getItem(InventorySection section, int slot) {
        if (section == null || section.items == null) return ItemStackUtil.EMPTY;
        ItemWithAllMetadata hytaleItem = section.items.get(slot);
        return hytaleItem != null ? ItemStackUtil.fromHytale(hytaleItem) : ItemStackUtil.EMPTY;
    }
}
