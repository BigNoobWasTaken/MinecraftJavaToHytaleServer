package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.play;

import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.manager.gui.GuiManager;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundContainerClosePacket;

public class ContainerCloseHandler implements PacketHandler<ServerboundContainerClosePacket> {
    @Override
    public void handle(ServerboundContainerClosePacket packet, MinecraftClientConnection connection) {
        GuiManager.INSTANCE.onCloseGui(connection);
    }
}
