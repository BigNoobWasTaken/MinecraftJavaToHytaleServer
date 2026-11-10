package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.inventory;

import com.hypixel.hytale.protocol.packets.inventory.SetActiveSlot;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundSetHeldSlotPacket;

public class SetActiveSlotHandler implements PacketHandler<SetActiveSlot> {
    private final static int INVENTORY_HOTBAR_SECTION_ID = -1;
    @Override
    public void handle(SetActiveSlot packet, MinecraftClientConnection connection) {
        if (packet.inventorySectionId != INVENTORY_HOTBAR_SECTION_ID) return;

        ClientboundSetHeldSlotPacket heldSlotPacket = new ClientboundSetHeldSlotPacket(packet.activeSlot);
        connection.getSession().send(heldSlotPacket);
    }
}
