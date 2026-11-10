package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.window;

import com.hypixel.hytale.protocol.packets.window.UpdateWindow;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.manager.window.WindowManager;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;

public class UpdateWindowHandler implements PacketHandler<UpdateWindow> {
    @Override
    public void handle(UpdateWindow packet, MinecraftClientConnection connection) {
        if (packet.inventory != null)
            WindowManager.get().updateContainer(connection, packet.inventory);
    }
}
