package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.window;

import com.hypixel.hytale.protocol.packets.window.OpenWindow;
import com.hypixel.hytale.protocol.packets.window.WindowType;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.window.WindowManager;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;

public class OpenWindowHandler implements PacketHandler<OpenWindow> {
    @Override
    public void handle(OpenWindow packet, MinecraftClientConnection connection) {
        if (packet.windowType.equals(WindowType.Container) && packet.inventory != null)
            WindowManager.get().openContainer(packet.id, connection, packet.inventory);
    }
}
