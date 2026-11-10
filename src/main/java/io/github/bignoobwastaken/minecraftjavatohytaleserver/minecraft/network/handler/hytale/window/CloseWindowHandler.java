package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.window;

import com.hypixel.hytale.protocol.packets.window.CloseWindow;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.manager.gui.GuiManager;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;

public class CloseWindowHandler implements PacketHandler<CloseWindow> {
    @Override
    public void handle(CloseWindow packet, MinecraftClientConnection connection) {
        GuiManager.INSTANCE.closeGui(connection, false);
    }
}
