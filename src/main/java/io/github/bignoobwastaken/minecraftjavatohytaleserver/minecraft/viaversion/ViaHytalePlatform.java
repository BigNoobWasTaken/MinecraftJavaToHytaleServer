package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.viaversion;

import com.viaversion.viaversion.platform.UserConnectionViaVersionPlatform;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.ModdingConstant;

import java.io.File;
import java.util.logging.Logger;

public class ViaHytalePlatform extends UserConnectionViaVersionPlatform {
    public ViaHytalePlatform() {
        super(new File(ModdingConstant.PATH, "ViaVersion"));
    }

    @Override
    public Logger createLogger(String name) {
        return Logger.getLogger(name);
    }

    @Override
    public String getPlatformName() {
        return "Hytale";
    }

    @Override
    public String getPlatformVersion() {
        return "0.7.6";
    }
}
