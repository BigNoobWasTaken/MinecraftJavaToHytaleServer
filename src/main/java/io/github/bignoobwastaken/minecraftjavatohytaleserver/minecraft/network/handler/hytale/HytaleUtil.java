package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale;

import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.server.core.universe.Universe;

import java.util.Objects;
import java.util.UUID;

public class HytaleUtil {

    public static String getDimensionName(UUID uuid) {
        return Objects.requireNonNull(Universe.get().getWorld(uuid)).getName().toLowerCase();
    }

    public static Direction getHytaleDirection(float mcYaw, float mcPitch) {
        float hytaleYawDegrees = 180.0f - mcYaw;

        while (hytaleYawDegrees > 180.0f) hytaleYawDegrees -= 360.0f;
        while (hytaleYawDegrees <= -180.0f) hytaleYawDegrees += 360.0f;

        float hytalePitchDegrees = -mcPitch;

        float hytaleYaw = (float) Math.toRadians(hytaleYawDegrees);
        float hytalePitch = (float) Math.toRadians(hytalePitchDegrees);

        return new Direction(hytaleYaw, hytalePitch, 0.0f);
    }

    public record MinecraftYawPitch(float mcYaw, float mcPitch) {
    }

    public static MinecraftYawPitch getMinecraftYawPitch(Direction direction) {
        return getMinecraftYawPitch(direction.yaw, direction.pitch);
    }

    public static MinecraftYawPitch getMinecraftYawPitch(float yaw, float pitch) {
        float mcYaw = getMinecraftYaw(yaw);
        float mcPitch = getMinecraftPitch(pitch);
        return new  MinecraftYawPitch(mcYaw, mcPitch);
    }

    public static float getMinecraftYaw(float yaw) {
        float hytaleYawDegrees = (float) Math.toDegrees(yaw);
        float mcYaw = 180.0f - hytaleYawDegrees;
        while (mcYaw > 180.0f) mcYaw -= 360.0f;
        while (mcYaw <= -180.0f) mcYaw += 360.0f;
        return mcYaw;
    }

    public static float getMinecraftPitch(float pitch) {
        float hytalePitchDegrees = (float) Math.toDegrees(pitch);
        return -hytalePitchDegrees;
    }
}
