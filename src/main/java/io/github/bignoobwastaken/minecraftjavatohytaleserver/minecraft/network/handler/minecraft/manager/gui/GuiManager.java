package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.manager.gui;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.gui.ContainerGui;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.item.ItemStackUtil;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.manager.inventory.InventoryManager;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerAction;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerActionType;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerClosePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundOpenScreenPacket;

import java.util.Collections;
import java.util.Map;

public class GuiManager {
    public static final GuiManager INSTANCE = new GuiManager();
    private GuiManager() {}

    public void sendGuiPackets(MinecraftClientConnection connection, ContainerGui gui) {
        ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(gui.getId(), gui.getType(), Component.text(gui.getTitle()));
        ClientboundContainerSetContentPacket contentPacket = new ClientboundContainerSetContentPacket(gui.getId(), 0, itemsToList(gui.getItems()), ItemStackUtil.EMPTY);

        connection.getSession().send(packet);
        connection.getSession().send(contentPacket);
    }

    public void onCloseGui(MinecraftClientConnection connection) {
        onCloseGui(connection, true);
    }

    public void onCloseGui(MinecraftClientConnection connection, boolean triggerOnClose) {
        ContainerGui gui = connection.getOpenedGui();
        if (gui == null) return;
        if (triggerOnClose) gui.onClose(connection);
        connection.setOpenedGui(null);
    }

    public void closeGui(MinecraftClientConnection connection) {
        closeGui(connection, true);
    }

    public void closeGui(MinecraftClientConnection connection, boolean triggerOnClose) {
        onCloseGui(connection, triggerOnClose);
        ClientboundContainerClosePacket packet = new ClientboundContainerClosePacket(0);
        connection.getSession().send(packet);
    }

    public void clickGui(MinecraftClientConnection connection, ComponentAccessor<EntityStore> store, Ref<EntityStore> ref, int slot, ContainerActionType mode, ContainerAction button) {
        ContainerGui gui = connection.getOpenedGui();
        if (gui == null) {
            closeGui(connection);
            InventoryManager.INSTANCE.resyncInventory(connection, store, ref);
            return;
        }

        boolean cancel = gui.onClick(connection, slot, mode);

        if (cancel) {
            InventoryManager.INSTANCE.resyncInventory(connection, store, ref);
            sendGuiPackets(connection, gui);
            return;
        }

        InventoryManager.INSTANCE.handleContainerClick(connection, store, ref, (short) slot, button, mode, gui.getId(), gui.getGuiSlotCount());
    }

    public ItemStack[] itemsToList(Map<Integer, ItemStack> items) {
        int max = Collections.max(items.keySet());

        ItemStack[] list = new ItemStack[max];
        items.forEach((id, item) -> list[id] = item);

        return list;
    }
}
