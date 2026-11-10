package io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.window;

import com.hypixel.hytale.protocol.InventorySection;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.gui.ContainerGui;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.item.ItemStackUtil;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.manager.gui.GuiManager;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class WindowManager {
    private static final WindowManager INSTANCE = new WindowManager();
    public static WindowManager get() { return INSTANCE; }

    public void openContainer(int id, MinecraftClientConnection connection, InventorySection inventory) {
        Map<Integer, ItemStack> items = new HashMap<>();
        assert inventory.items != null;
        inventory.items.forEach((itemId, itemMetadata) -> items.put(itemId, ItemStackUtil.fromHytale(itemMetadata)));
        ContainerGui gui = new ContainerGui(id, "Chest", inventory.capacity, items);
        connection.setOpenedGui(gui);
        GuiManager.INSTANCE.sendGuiPackets(connection, gui);
    }

    public void updateContainer(MinecraftClientConnection connection, InventorySection inventory) {
        ContainerGui gui = connection.getOpenedGui();
        gui.clearItems();
        assert inventory.items != null;
        inventory.items.forEach((slot, hytaleItem) -> gui.getItems().put(slot, ItemStackUtil.fromHytale(hytaleItem)));
        GuiManager.INSTANCE.sendGuiPackets(connection, gui);
    }
}
