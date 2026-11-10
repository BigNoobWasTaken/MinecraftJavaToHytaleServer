package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.viaversion;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;

public class ViaHytalePlatformLoader implements ViaPlatformLoader {
    @Override
    public void load() {
        Via.getManager().getProviders().use(VersionProvider.class, connection -> ProtocolVersion.v1_21_11);
    }

    @Override
    public void unload() {

    }
}
