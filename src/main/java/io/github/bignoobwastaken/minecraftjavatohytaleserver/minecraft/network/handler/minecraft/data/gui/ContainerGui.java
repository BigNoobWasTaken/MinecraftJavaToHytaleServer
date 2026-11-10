package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.gui;

import com.hypixel.hytale.protocol.packets.window.CloseWindow;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerActionType;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerType;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@Getter
public class ContainerGui {
    private final int id;
    private final String title;
    private final int size;
    private final Map<Integer, ItemStack> items;
    private final Set<Integer> invalidSlots = new HashSet<>();

    public boolean onClick(MinecraftClientConnection connection, int slot, ContainerActionType mode) {
        return invalidSlots.contains(slot);
    }

    public void onClose(MinecraftClientConnection connection) {
        CloseWindow packet = new CloseWindow(id);
        connection.getHytaleChannel().sendPacket(packet);
    }

    public ContainerType getType() {
        return this.getTypeBySize(this.size);
    }

    public int getGuiSlotCount() {
        return (this.getType().ordinal() + 1) * 9;
    }

    private ContainerType getTypeBySize(int size) {
        int rows = (int) Math.ceil((double) size / 9);
        if (rows > 6) rows = 6;
        return ContainerType.from(rows-1);
    }

    public void clearItems() {
        items.keySet().removeIf(invalidSlots::contains);
    }
}
