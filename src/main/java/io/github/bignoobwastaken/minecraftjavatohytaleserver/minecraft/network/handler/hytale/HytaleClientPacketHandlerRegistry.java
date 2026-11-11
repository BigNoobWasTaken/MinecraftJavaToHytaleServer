package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale;

import com.hypixel.hytale.protocol.CachedPacket;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.packets.connection.Ping;
import com.hypixel.hytale.protocol.packets.entities.EntityUpdates;
import com.hypixel.hytale.protocol.packets.interaction.PlayInteractionFor;
import com.hypixel.hytale.protocol.packets.interface_.*;
import com.hypixel.hytale.protocol.packets.inventory.SetActiveSlot;
import com.hypixel.hytale.protocol.packets.inventory.UpdatePlayerInventory;
import com.hypixel.hytale.protocol.packets.player.ClientTeleport;
import com.hypixel.hytale.protocol.packets.player.JoinWorld;
import com.hypixel.hytale.protocol.packets.player.SetClientId;
import com.hypixel.hytale.protocol.packets.player.SetGameMode;
import com.hypixel.hytale.protocol.packets.window.CloseWindow;
import com.hypixel.hytale.protocol.packets.window.OpenWindow;
import com.hypixel.hytale.protocol.packets.window.UpdateWindow;
import com.hypixel.hytale.protocol.packets.world.*;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.HandlerRegistry;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.connection.PingHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.interaction.PlayInteractionForHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.interface_.*;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.world.*;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.inventory.SetActiveSlotHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.inventory.UpdatePlayerInventoryHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.player.ClientTeleportHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.player.SetClientIdHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.entity.EntityUpdatesHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.player.SetGameModeHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.window.CloseWindowHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.window.OpenWindowHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.window.UpdateWindowHandler;

public final class HytaleClientPacketHandlerRegistry extends HandlerRegistry<ToClientPacket> {
    {
        // cached packet
        this.addHandler(CachedPacket.class, new CachedPacketHandler());

        // connection
        this.addHandler(Ping.class, new PingHandler());

        // entity
        this.addHandler(EntityUpdates.class, new EntityUpdatesHandler());

        // interaction
        this.addHandler(PlayInteractionFor.class, new PlayInteractionForHandler());

        // interface
        this.addHandler(AddToServerPlayerList.class, new AddToServerPlayerListHandler());
        this.addHandler(RemoveFromServerPlayerList.class, new RemoveFromServerPlayerListHandler());
        this.addHandler(UpdateServerPlayerList.class, new UpdateServerPlayerListHandler());
        this.addHandler(UpdateServerPlayerListPing.class, new UpdateServerPlayerListPingHandler());
        this.addHandler(ServerInfo.class, new ServerInfoHandler());

        // inventory
        this.addHandler(SetActiveSlot.class, new SetActiveSlotHandler());
        this.addHandler(UpdatePlayerInventory.class, new UpdatePlayerInventoryHandler());

        // player
        this.addHandler(ClientTeleport.class, new ClientTeleportHandler());
        this.addHandler(SetClientId.class, new SetClientIdHandler());
        this.addHandler(SetGameMode.class, new SetGameModeHandler());

        // window
        this.addHandler(CloseWindow.class, new CloseWindowHandler());
        this.addHandler(OpenWindow.class, new OpenWindowHandler());
        this.addHandler(UpdateWindow.class, new UpdateWindowHandler());

        // world
        this.addHandler(JoinWorld.class, new JoinWorldHandler());
        this.addHandler(SetChunk.class, new SetChunkHandler());
        this.addHandler(UnloadChunk.class, new UnloadChunkHandler());
        this.addHandler(ServerSetBlock.class, new ServerSetBlockHandler());
        this.addHandler(ServerSetBlocks.class, new ServerSetBlocksHandler());
        this.addHandler(UpdateBlockDamage.class, new UpdateBlockDamageHandler());
        this.addHandler(UpdateTime.class, new UpdateTimeHandler());
    }

    public static final HytaleClientPacketHandlerRegistry INSTANCE = new HytaleClientPacketHandlerRegistry();
}
